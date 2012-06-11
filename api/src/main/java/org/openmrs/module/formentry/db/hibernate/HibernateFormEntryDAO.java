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
package org.openmrs.module.formentry.db.hibernate;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.formentry.FormEntryArchive;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryError;
import org.openmrs.module.formentry.FormEntryQueue;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.FormEntryXsnMetadata;
import org.openmrs.module.formentry.db.FormEntryDAO;
import org.openmrs.module.formentry.migration.MigrateFormEntryQueueThread;
import org.openmrs.util.OpenmrsUtil;

public class HibernateFormEntryDAO implements FormEntryDAO {

	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * Hibernate session factory
	 */
	private SessionFactory sessionFactory;
	
	/**
	 * Default public constructor
	 */
	public HibernateFormEntryDAO() { }
	
	/**
	 * Set session factory
	 * 
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) { 
		this.sessionFactory = sessionFactory;
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#createFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void createFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		sessionFactory.getCurrentSession().save(formEntryError);
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormEntryError(java.lang.Integer)
	 */
	public FormEntryError getFormEntryError(Integer formEntryErrorId)
			throws DAOException {
		FormEntryError formEntryError;
		formEntryError = (FormEntryError) sessionFactory.getCurrentSession().get(FormEntryError.class,
				formEntryErrorId);

		return formEntryError;
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormEntryErrors()
	 */
	@SuppressWarnings("unchecked")
	public Collection<FormEntryError> getFormEntryErrors() throws DAOException {
		return sessionFactory.getCurrentSession().createCriteria(FormEntryError.class).addOrder(Order.asc("formEntryErrorId")).list();
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#updateFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void updateFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		if (formEntryError.getFormEntryErrorId() == 0)
			createFormEntryError(formEntryError);
		else {
			sessionFactory.getCurrentSession().saveOrUpdate(formEntryError);
		}

	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#deleteFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void deleteFormEntryError(FormEntryError formEntryError)
			throws DAOException {
		sessionFactory.getCurrentSession().delete(formEntryError);
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormEntryErrorSize()
	 */
	public Integer getFormEntryErrorSize() throws DAOException {
		Long size = (Long) sessionFactory.getCurrentSession().createQuery(
				"select count(*) from FormEntryError").uniqueResult();

		return size.intValue();
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#garbageCollect()
	 */
	public void garbageCollect() {
		Context.clearSession();
	}
	
	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#migrateQueueAndArchiveToFilesystem()
	 */
	public void migrateQueueAndArchiveToFilesystem() {
		// this feels like a lot of business level logic in a dao layer.  refactoring needed?
		
		Connection conn = sessionFactory.getCurrentSession().connection();
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		boolean formEntryQueueExists = true; // assume true in case of an error
		// figure out if the tables exist or not.  If they do not, this has been run before
		// and we can just skip migrating the queue
		try {
			PreparedStatement ps = conn.prepareStatement("select count(*) from formentry_queue");
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				// pass
			}
			formEntryQueueExists = true;
		}
		catch (Exception e) {
			formEntryQueueExists = false;
			log.trace("error while finding formentry_queue size", e);
		}
		
		// migrate the formentry queue
		if (formEntryQueueExists) {
			try {
				String sql = "select form_data from formentry_queue";
			
				PreparedStatement ps = conn.prepareStatement(sql);
				
				ResultSet rs = ps.executeQuery();
				while(rs.next()) {
					String data = rs.getString("form_data");
					FormEntryQueue queue = new FormEntryQueue();
					queue.setFormData(data);
					formEntryService.createFormEntryQueue(queue);
				}
				
				// delete the formentry_queue table
				ps = conn.prepareStatement("drop table formentry_queue");
				ps.executeUpdate();
			}
			catch (Exception e) {
				log.error("Error while moving old formentry_queue items to the filesystem", e);
			}
			
		}
		
		boolean migrationNeeded = true; // assume true in case of an error
		boolean deleteTable = true; // assume we want to delete the table when done
		boolean active = MigrateFormEntryQueueThread.isActive();
		int chunkSize = 100;
		
		PreparedStatement selectFormentry = null;
		PreparedStatement deleteFormentry = null;
		try {
			selectFormentry = conn.prepareStatement("select form_data, date_created from formentry_archive limit ?");
			selectFormentry.setInt(1, chunkSize);
			
			deleteFormentry = conn.prepareStatement("delete from formentry_archive limit ?");
			deleteFormentry.setInt(1, chunkSize);
		}
		catch (SQLException sql) {
			log.warn("Uh oh.  Trouble creating the formentry prepared statements", sql);
		}
		
		List<FormEntryArchive> toDeleteIfError = new ArrayList<FormEntryArchive>(); 
		
		// must do the form entry archive in chunks
		while (migrationNeeded && active) {
			
			// figure out if the tables exist or not.  If they do not, this has been run before
			// and we can just skip migrating the queue
			migrationNeeded = migrateFormEntryArchiveNeeded();
			toDeleteIfError.clear();
			
			// migrate the formentry archive
			if (migrationNeeded) {
				try {
					ResultSet rs = selectFormentry.executeQuery();
					int count = 0;
					while(rs.next()) {
						FormEntryArchive archive = new FormEntryArchive();
						archive.setFormData(rs.getString("form_data"));
						archive.setDateCreated(rs.getTimestamp("date_created"));
						formEntryService.createFormEntryArchive(archive);
						archive = null;
						count = count + 1;
						toDeleteIfError.add(archive);
					}
					
					// force the while loop to stop because we selected zero rows
					if (count == 0)
						migrationNeeded = false;
					
					deleteFormentry.executeUpdate();
					
					conn.commit();
					
					toDeleteIfError.clear();
				}
				catch (Exception e) {
					log.error("Error while moving old formentry_archive items to the filesystem", e);
					
					// delete archive written before the error
					for (FormEntryArchive archive : toDeleteIfError) {
						formEntryService.deleteFormEntryArchive(archive);
					}
				}
			}
			
			active = MigrateFormEntryQueueThread.isActive();
			
		} // end while loop
		
		if (deleteTable && active) {
			try {
				// delete the formentry_archive table
				PreparedStatement ps = conn.prepareStatement("drop table if exists formentry_archive");
				ps.executeUpdate();
			}
			catch (Exception e) {
				log.warn("Unable to drop the formentry_archive table", e);
			}
		}

	}
	
	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#migrateFormEntryArchiveNeeded()
	 */
	public Boolean migrateFormEntryArchiveNeeded() {
		Connection conn = sessionFactory.getCurrentSession().connection();
		PreparedStatement ps = null;
        try {
        	ps = conn.prepareStatement("select count(*) from formentry_archive");
	        ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getInt(1) > 0;
			else
				return false;
        } catch (SQLException e) {
	        // essentially swallow the error
	        log.trace("The table doesn't exist, so migration is not needed", e);
	        return false;
        }
        finally {
        	try {
	            ps.close();
            }
            catch (SQLException e) { }
        }
        
	}
	
    /**
     * @see org.openmrs.module.formentry.db.FormEntryDAO#getAllFormEntryXsnsForForm(org.openmrs.Form, boolean)
     */
    public List<FormEntryXsn> getAllFormEntryXsnsForForm(Form form, boolean includeArchived) {
    	Criteria crit = sessionFactory.getCurrentSession().createCriteria(FormEntryXsn.class);
    	crit.add(Restrictions.eq("form", form));
    	
    	if (includeArchived == false)
    		crit.add(Restrictions.eq("archived", false));
    	
    	return (List<FormEntryXsn>)crit.list();
    }
	
	/**
     * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormEntryXsn(java.lang.Integer)
     */
    public FormEntryXsn getFormEntryXsn(Integer formId) {
    	Query query = sessionFactory.getCurrentSession().createQuery("from FormEntryXsn where form.formId = :formId and archived = 0 order by formEntryXsnId desc");
		query.setParameter("formId", formId);
    	query.setMaxResults(1); // do this just in case there are two xsns marked as nonarchived
    	// the results are sorted with the newest xsns first
    	
    	return (FormEntryXsn)query.uniqueResult();
    }

	/**
     * @see org.openmrs.module.formentry.db.FormEntryDAO#updateFormEntryXsn(org.openmrs.module.formentry.FormEntryXsn)
     */
    public void updateFormEntryXsn(FormEntryXsn formEntryXsn) {
    	sessionFactory.getCurrentSession().saveOrUpdate(formEntryXsn);
    }
    
	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#deleteFormEntryXsn(java.lang.Integer)
	 */
	public void deleteFormEntryXsn(Integer formId) {
		Query query = sessionFactory.getCurrentSession().createQuery("delete from FormEntryXsn where form.formId = :formId");
		query.setParameter("formId", formId);
    	 
    	query.executeUpdate();
    }

	/**
     * @see org.openmrs.module.formentry.db.FormEntryDAO#migrateXsnsToDatabase()
     */
    @SuppressWarnings("deprecation")
    public void migrateXsnsToDatabase() {
    	// this feels like a lot of business level logic in a dao layer.  refactoring needed?
    	
    	FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
    	FormService formService = Context.getFormService();
    	
    	// keep a count of the objects so we can do a flush every so often
		int count = 0;
		
    	String dir = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_INFOPATH_OUTPUT_DIR, null);
    	
    	if (dir != null) {
			File xsnDir = new File(dir);
			
			// list of files to "archive" to a zip file
			List<File> filesToZip = new Vector<File>();
			
			if (xsnDir.exists() && xsnDir.isDirectory()) {
				for (File xsnFile : xsnDir.listFiles()) {
					String filename = xsnFile.getName();
					if (filename.endsWith(".xsn")) {
						try {
							FormEntryXsn xsn = new FormEntryXsn();
							// the first n characters before the first period is the form_id
							String formId = filename.substring(0, filename.indexOf("."));
							Form form = formService.getForm(Integer.valueOf(formId));
							
							if (form == null) {
								log.error("Unable to find form with id: " + formId);
								continue;
							}
							
							xsn.setForm(form);
							// get the xsn file contents as a string and put it on the object
							byte[] xsnContents = OpenmrsUtil.getFileAsBytes(xsnFile);
							xsn.setXsnData(xsnContents);
							formEntryService.createFormEntryXsn(xsn);
							
							// garbage collect (flush to db) every once in a while
							if (++count % 50 == 0)
								formEntryService.garbageCollect();
							
							filesToZip.add(xsnFile);
						}
						catch (Exception e) {
							log.error("Unable to move " + xsnFile.getAbsolutePath() + " to formentry xsn table", e);
						}
					}
				}
				
				try {
					FormEntryUtil.moveToZipFile(xsnDir, "xsns-moved-to-db", filesToZip);
				}
				catch (IOException io) {
					log.error("Unable to zip xsns", io);
				}
			}
			else
				log.error("Xsn directory is not valid: " + xsnDir.getAbsolutePath());
    	}
		
		
		dir = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_INFOPATH_ARCHIVE_DIR, null);
		if (dir != null) {
			File xsnArchiveDir = new File(dir);

			// list of files to "archive" to a zip file
			List<File> filesToZip = new Vector<File>();
			
			if (xsnArchiveDir.exists() && xsnArchiveDir.isDirectory()) {
				for (File xsnArchiveFile : xsnArchiveDir.listFiles()) {
					String filename = xsnArchiveFile.getName();
					if (filename.endsWith(".xsn")) {
						try {
							FormEntryXsn xsn = new FormEntryXsn();
							
							// the first n characters before the first period is the form_id
							String formId = filename.substring(0, filename.indexOf("."));
							Form form = formService.getForm(Integer.valueOf(formId));
							xsn.setForm(form);
							
							byte[] xsnContents = OpenmrsUtil.getFileAsBytes(xsnArchiveFile);
							xsn.setXsnData(xsnContents);
							
							xsn.setCreator(Context.getAuthenticatedUser());
							xsn.setDateCreated(new Date());
							
							formEntryService.archiveFormEntryXsn(xsn);
							
							// garbage collect (flush to db) every once in a while
							if (++count % 50 == 0)
								formEntryService.garbageCollect();
							
							filesToZip.add(xsnArchiveFile);
						}
						catch (Exception e) {
							log.error("Unable to move " + xsnArchiveFile.getAbsolutePath() + " to formentry xsn table (as archived)", e);
						}
					}
				}
				
				try {
					FormEntryUtil.moveToZipFile(xsnArchiveDir, "archived-xsns-moved-to-db", filesToZip);
				}
				catch (IOException io) {
					log.error("Unable to zip archived xsns", io);
				}
			}
			else
				log.error("Xsn archive directory is not valid: " + xsnArchiveDir.getAbsolutePath());
			
		}
    }

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormsWithXsns(boolean)
	 */
	@Override
	public List<Form> getFormsWithXsns(boolean publishedOnly) {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(FormEntryXsn.class);
		crit.setProjection(Projections.property("form"));
		crit.add(Restrictions.eq("archived", false));
		if (publishedOnly) {
			crit.createAlias("form", "form");
			crit.add(Restrictions.eq("form.published", true));
		}
    	
    	return (List<Form>)crit.list();
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getAllFormEntryXsnMetadata()
	 */
	public List<FormEntryXsnMetadata> getAllFormEntryXsnMetadata() {
		Criteria crit = sessionFactory
				.getCurrentSession()
				.createCriteria(FormEntryXsn.class)
				.setProjection(
					Projections.projectionList().add(
						Projections.property("formEntryXsnId")).add(
						Projections.property("form.id")).add(
						Projections.property("archived")).add(
						Projections.property("dateCreated")).add(
						Projections.property("dateArchived")))
				.addOrder(Order.asc("form.id"))
				.addOrder(Order.asc("archived"))
				.addOrder(Order.desc("dateCreated"));

		List<Object[]> xsns = crit.list();
		List<FormEntryXsnMetadata> res = new ArrayList<FormEntryXsnMetadata>();
		
		for (Object[] metadata : xsns) {
			try {
				FormEntryXsnMetadata md = new FormEntryXsnMetadata();
				md.setFormEntryXsnId((Integer) metadata[0]);
				md.setFormId((Integer) metadata[1]);
				md.setArchived((Boolean) metadata[2]);
				md.setDateCreated((Date) metadata[3]);
				md.setDateArchived((Date) metadata[4]);
				res.add(md);
			} catch (NumberFormatException ex) {
				throw new APIException("could not cast " + metadata[0] + " to an Integer.", ex);
			}
		}
		
		return res;
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#deleteFormEntryXsn(FormEntryXsn)
	 */
	public void deleteFormEntryXsn(FormEntryXsn xsn) {
		sessionFactory.getCurrentSession().delete(xsn);
	}

	/**
	 * @see org.openmrs.module.formentry.db.FormEntryDAO#getFormEntryXsnById(Integer)
	 */
	public FormEntryXsn getFormEntryXsnById(Integer xsnId) {
		return (FormEntryXsn) sessionFactory.getCurrentSession().get(
				FormEntryXsn.class, xsnId);
	}

}
