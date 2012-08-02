/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.formentry.databasechange;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.formentry.FormEntryConstants;

/**
 * Creates form resources from existing xslts in the form table.
 */
public class MigrateXsltsChangeset implements CustomTaskChange {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	@Override
	public void execute(Database database) throws CustomChangeException {
		final JdbcConnection connection = (JdbcConnection) database.getConnection();
		Statement selectStmt = null;
		PreparedStatement pStmt = null;
		Boolean originalAutoCommit = null;
		ResultSet rs = null;
		
		try {
			originalAutoCommit = connection.getAutoCommit();
			selectStmt = connection.createStatement();
			boolean hasResults = selectStmt
			        .execute("SELECT form_id, name, xslt FROM form WHERE xslt IS NOT NULL AND xslt != ''");
			if (hasResults) {
				rs = selectStmt.getResultSet();
				pStmt = connection
				        .prepareStatement("INSERT INTO form_resource (form_id, name, value_reference, uuid) VALUES (?,?,?,?)");
				
				String defaultXslt = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("default.xslt"));
				//intentionally didn't check for NULL so the exception halts the changeset
				defaultXslt = defaultXslt.trim();
				
				while (rs.next()) {
					String xslt = rs.getString("xslt");
					//if the form has an xslt and it differs from the default one
					if (StringUtils.isNotBlank(xslt) && !xslt.trim().equals(defaultXslt)) {
						pStmt.setInt(1, rs.getInt("form_id"));
						pStmt.setString(2, FormEntryConstants.MODULE_ID + "." + rs.getString("name")
						        + FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME_SUFFIX);
						pStmt.setString(3, xslt);
						pStmt.setString(4, UUID.randomUUID().toString());
						
						pStmt.addBatch();
					}
				}
				
				int[] insertCounts = pStmt.executeBatch();
				if (insertCounts != null) {
					boolean commit = false;
					for (int i = 0; i < insertCounts.length; i++) {
						if (insertCounts[i] > -1) {
							commit = true;
							log.debug("Successfully executed: insert count=" + insertCounts[i]);
						} else if (insertCounts[i] == Statement.SUCCESS_NO_INFO) {
							commit = true;
							log.debug("Successfully executed; No Success info");
						} else if (insertCounts[i] == Statement.EXECUTE_FAILED) {
							log.warn("Failed to execute batch statement");
						}
					}
					
					if (commit) {
						log.debug("Committing inserts...");
						connection.commit();
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("Error generated while processsing generation of xslt form resources", e);
			
			try {
				if (connection != null) {
					connection.rollback();
				}
			}
			catch (Exception ex) {
				log.error("Failed to rollback", ex);
			}
			
			throw new CustomChangeException(e);
		}
		finally {
			if (rs != null) {
				try {
					rs.close();
				}
				catch (SQLException e) {
					log.warn("Failed to close the resultset object");
				}
			}
			if (connection != null && originalAutoCommit != null) {
				try {
					connection.setAutoCommit(originalAutoCommit);
				}
				catch (DatabaseException e) {
					log.error("Failed to reset auto commit", e);
				}
			}
			
			closeStatementQuietly(selectStmt);
			closeStatementQuietly(pStmt);
		}
	}
	
	/**
	 * Closes the statement quietly.
	 * 
	 * @param statement
	 */
	private void closeStatementQuietly(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			}
			catch (SQLException e) {
				log.error("Failed to close statement", e);
			}
		}
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#getConfirmationMessage()
	 */
	@Override
	public String getConfirmationMessage() {
		return "Finished generating xslt form resources";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	@Override
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.resource.ResourceAccessor)
	 */
	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
	
}
