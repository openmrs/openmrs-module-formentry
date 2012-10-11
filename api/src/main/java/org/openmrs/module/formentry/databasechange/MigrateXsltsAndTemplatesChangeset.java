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
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler;

/**
 * Creates form resources from existing xslts in the form table.
 */
public class MigrateXsltsAndTemplatesChangeset implements CustomTaskChange {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * @see liquibase.change.custom.CustomTaskChange#execute(liquibase.database.Database)
	 */
	public void execute(Database database) throws CustomChangeException {
		final JdbcConnection connection = (JdbcConnection) database.getConnection();
		migrateResources(connection, true);//move xslts
		migrateResources(connection, false);//move templates
	}
	
	private void migrateResources(JdbcConnection connection, boolean isXslt) throws CustomChangeException {
		Statement selectStmt = null;
		PreparedStatement insertResourcesStmt = null;
		PreparedStatement insertClobsStmt = null;
		Boolean originalAutoCommit = null;
		ResultSet rs = null;
		String resourceName = (isXslt) ? FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME
		        : FormEntryConstants.FORMENTRY_TEMPLATE_FORM_RESOURCE_NAME;
		String columnName = (isXslt) ? "xslt" : "template";
		
		try {
			originalAutoCommit = connection.getAutoCommit();
			selectStmt = connection.createStatement();
			boolean hasResults = selectStmt.execute("SELECT form_id, " + columnName + " FROM form WHERE " + columnName
			        + " IS NOT NULL AND " + columnName + " != ''");
			if (hasResults) {
				rs = selectStmt.getResultSet();
				insertClobsStmt = connection.prepareStatement("INSERT INTO clob_datatype_storage (value, uuid) VALUES(?,?)");
				insertResourcesStmt = connection
				        .prepareStatement("INSERT INTO form_resource (form_id, name, value_reference, datatype, preferred_handler, uuid) VALUES (?,'"
				                + resourceName
				                + "',?,'"
				                + LongFreeTextDatatype.class.getName()
				                + "','"
				                + LongFreeTextFileUploadHandler.class.getName() + "',?)");
				
				String defaultXslt = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("default.xslt"));
				//intentionally didn't check for NULL so the exception halts the changeset
				defaultXslt = defaultXslt.trim();
				
				while (rs.next()) {
					String resourceValue = rs.getString(columnName);
					//if the form has an xslt and it differs from the default one
					if (StringUtils.isNotBlank(resourceValue) && (!isXslt || !resourceValue.trim().equals(defaultXslt))) {
						//set the clob storage values
						String clobUuid = UUID.randomUUID().toString();
						insertClobsStmt.setString(1, resourceValue.trim());
						insertClobsStmt.setString(2, clobUuid);
						insertClobsStmt.addBatch();
						
						//set the resource column values
						insertResourcesStmt.setInt(1, rs.getInt("form_id"));
						insertResourcesStmt.setString(2, clobUuid);
						insertResourcesStmt.setString(3, UUID.randomUUID().toString());
						insertResourcesStmt.addBatch();
					}
				}
				
				boolean successfullyAddedClobs = false;
				int[] clobInsertCounts = insertClobsStmt.executeBatch();
				if (clobInsertCounts != null) {
					for (int i = 0; i < clobInsertCounts.length; i++) {
						if (clobInsertCounts[i] > -1) {
							successfullyAddedClobs = true;
							log.debug("Successfully inserted resource clobs: insert count=" + clobInsertCounts[i]);
						} else if (clobInsertCounts[i] == Statement.SUCCESS_NO_INFO) {
							successfullyAddedClobs = true;
							log.debug("Successfully inserted resource clobs; No Success info");
						} else if (clobInsertCounts[i] == Statement.EXECUTE_FAILED) {
							log.warn("Failed to insert resource clobs");
						}
					}
				}
				
				if (successfullyAddedClobs) {
					int[] resourceInsertCounts = insertResourcesStmt.executeBatch();
					if (resourceInsertCounts != null) {
						boolean commit = false;
						for (int i = 0; i < resourceInsertCounts.length; i++) {
							if (resourceInsertCounts[i] > -1) {
								commit = true;
								log.debug("Successfully inserted " + columnName + " resources: insert count="
								        + resourceInsertCounts[i]);
							} else if (resourceInsertCounts[i] == Statement.SUCCESS_NO_INFO) {
								commit = true;
								log.debug("Successfully inserted " + columnName + " resources; No Success info");
							} else if (resourceInsertCounts[i] == Statement.EXECUTE_FAILED) {
								log.warn("Failed to insert " + columnName + " resources");
							}
						}
						
						if (commit) {
							log.debug("Committing " + columnName + " resource inserts...");
							connection.commit();
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("Error generated while processsing generation of " + columnName + " form resources", e);
			
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
			closeStatementQuietly(insertClobsStmt);
			closeStatementQuietly(insertResourcesStmt);
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
	public String getConfirmationMessage() {
		return "Finished generating xslt and template form resources";
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setUp()
	 */
	public void setUp() throws SetupException {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#setFileOpener(liquibase.resource.ResourceAccessor)
	 */
	public void setFileOpener(ResourceAccessor resourceAccessor) {
	}
	
	/**
	 * @see liquibase.change.custom.CustomChange#validate(liquibase.database.Database)
	 */
	public ValidationErrors validate(Database database) {
		return null;
	}
	
}
