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
package org.openmrs.module.formentry.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryArchive;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryError;
import org.openmrs.module.formentry.FormEntryException;
import org.openmrs.module.formentry.FormEntryQueue;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.FormEntryXsnMetadata;
import org.openmrs.module.formentry.FormSchemaBuilder;
import org.openmrs.module.formentry.db.FormEntryDAO;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

/**
 * Default implementation of the FormEntryService
 * 
 * @see org.openmrs.module.formentry.FormEntryService
 */
public class FormEntryServiceImpl implements FormEntryService {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private FormEntryDAO dao;
	
	public FormEntryServiceImpl() {
	}
	
	private FormEntryDAO getFormEntryDAO() {
		return dao;
	}
	
	public void setFormEntryDAO(FormEntryDAO dao) {
		this.dao = dao;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getSystemVariables()
	 */
	public SortedMap<String, String> getSystemVariables() {
		if (!Context.hasPrivilege(OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS))
			throw new APIAuthenticationException("Privilege required: " + OpenmrsConstants.PRIV_VIEW_ADMIN_FUNCTIONS);
		
		TreeMap<String, String> systemVariables = new TreeMap<String, String>();
		systemVariables.put("FORMENTRY_INFOPATH_PUBLISH_PATH", String
		        .valueOf(FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH));
		systemVariables.put("FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH", String
		        .valueOf(FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH));
		systemVariables.put("FORMENTRY_INFOPATH_SUBMIT_PATH", String
		        .valueOf(FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_PATH));
		systemVariables.put("FORMENTRY_GP_QUEUE_DIR", FormEntryUtil.getFormEntryQueueDir().getAbsolutePath());
		systemVariables.put("FORMENTRY_GP_QUEUE_ARCHIVE_DIR", FormEntryUtil.getFormEntryArchiveDir(null).getAbsolutePath());
		
		// the other formentry system variables (the editable ones) are located in global properties
		
		return systemVariables;
	}
	
	/***************************************************************************
	 * FormEntryQueue Service Methods
	 **************************************************************************/
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryQueue(org.openmrs.module.formentry.FormEntryQueue)
	 */
	public void createFormEntryQueue(FormEntryQueue formEntryQueue) throws FormEntryException {
		User creator = Context.getAuthenticatedUser();
		if (formEntryQueue.getDateCreated() == null)
			formEntryQueue.setDateCreated(new Date());
		
		File queueDir = FormEntryUtil.getFormEntryQueueDir();
		
		File outFile = FormEntryUtil.getOutFile(queueDir, formEntryQueue.getDateCreated(), creator);
		
		// write the queue's data to the file
		FileWriter writer = null;
		try {
			writer = new FileWriter(outFile);
			
			writer.write(formEntryQueue.getFormData());
		}
		catch (IOException io) {
			throw new FormEntryException("Unable to save formentry queue", io);
		}
		finally {
			try {
				writer.close();
			}
			catch (Exception e) {
				log.debug("Error creating queu item", e);
			}
		}
		
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryQueues()
	 */
	public Collection<FormEntryQueue> getFormEntryQueues() {
		List<FormEntryQueue> queues = new Vector<FormEntryQueue>();
		
		File queueDir = FormEntryUtil.getFormEntryQueueDir();
		
		if (queueDir.exists() == false) {
			log.warn("Unable to open queue directory: " + queueDir);
			return queues;
		}
		
		// loop over all files in queue dir and create lazy queue items
		for (File file : queueDir.listFiles()) {
			FormEntryQueue queueItem = new FormEntryQueue();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			queues.add(queueItem);
		}
		
		return queues;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryQueue(org.openmrs.module.formentry.FormEntryQueue)
	 */
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue) {
		if (formEntryQueue == null || formEntryQueue.getFileSystemUrl() == null)
			throw new FormEntryException("Unable to load formEntryQueue with empty file system url");
		
		File file = new File(formEntryQueue.getFileSystemUrl());
		
		if (file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getNextFormEntryQueue()
	 */
	public FormEntryQueue getNextFormEntryQueue() {
		File queueDir = FormEntryUtil.getFormEntryQueueDir();
		
		// return the first queue item
		for (File file : queueDir.listFiles()) {
			FormEntryQueue queueItem = new FormEntryQueue();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			return queueItem;
		}
		
		return null;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryQueueSize()
	 */
	public Integer getFormEntryQueueSize() {
		File queueDir = FormEntryUtil.getFormEntryQueueDir();
		
		return queueDir.list().length;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryArchive(org.openmrs.module.formentry.FormEntryArchive)
	 */
	public void createFormEntryArchive(FormEntryArchive formEntryArchive) {
		User creator = Context.getAuthenticatedUser();
		
		File queueDir = FormEntryUtil.getFormEntryArchiveDir(formEntryArchive.getDateCreated());
		
		File outFile = FormEntryUtil.getOutFile(queueDir, formEntryArchive.getDateCreated(), creator);
		
		// write the queue's data to the file
		try {
			FormEntryUtil.stringToFile(formEntryArchive.getFormData(), outFile);
		}
		catch (IOException io) {
			throw new FormEntryException("Unable to save formentry archive", io);
		}
		
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryArchives()
	 */
	public Collection<FormEntryArchive> getFormEntryArchives() {
		List<FormEntryArchive> archives = new Vector<FormEntryArchive>();
		
		File archiveDir = FormEntryUtil.getFormEntryArchiveDir(null);
		
		if (archiveDir.exists() == false) {
			log.warn("Unable to open archive directory: " + archiveDir);
			return archives;
		}
		
		// loop over all files in archive dir and create lazy archive items
		for (File file : archiveDir.listFiles()) {
			FormEntryArchive queueItem = new FormEntryArchive();
			queueItem.setFileSystemUrl(file.getAbsolutePath());
			queueItem.setDateCreated(new Date(file.lastModified()));
			archives.add(queueItem);
		}
		
		return archives;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryArchive(org.openmrs.module.formentry.FormEntryArchive)
	 */
	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive) {
		if (formEntryArchive == null || formEntryArchive.getFileSystemUrl() == null)
			throw new FormEntryException("Unable to load formEntryArchive with empty file system url");
		
		File file = new File(formEntryArchive.getFileSystemUrl());
		
		if (file.exists()) {
			file.delete();
		}
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryArchiveSize()
	 */
	public Integer getFormEntryArchiveSize() {
		File archiveDir = FormEntryUtil.getFormEntryArchiveDir(null);
		
		return archiveDir.list().length;
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void createFormEntryError(FormEntryError formEntryError) {
		formEntryError.setCreator(Context.getAuthenticatedUser());
		formEntryError.setDateCreated(new Date());
		if (formEntryError.getFormData().length() > 16777214) { // size of mediumtext - 1
			formEntryError.setFormData(formEntryError.getFormData().substring(0, 16777214));
		}
		getFormEntryDAO().createFormEntryError(formEntryError);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryError(java.lang.Integer)
	 */
	public FormEntryError getFormEntryError(Integer formEntryErrorId) {
		return getFormEntryDAO().getFormEntryError(formEntryErrorId);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryErrors()
	 */
	public Collection<FormEntryError> getFormEntryErrors() {
		return getFormEntryDAO().getFormEntryErrors();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#updateFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void updateFormEntryError(FormEntryError formEntryError) {
		getFormEntryDAO().updateFormEntryError(formEntryError);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryError(org.openmrs.module.formentry.FormEntryError)
	 */
	public void deleteFormEntryError(FormEntryError formEntryError) {
		getFormEntryDAO().deleteFormEntryError(formEntryError);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryErrorSize()
	 */
	public Integer getFormEntryErrorSize() {
		return getFormEntryDAO().getFormEntryErrorSize();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getSchema(org.openmrs.Form)
	 */
	public String getSchema(Form form) {
		return new FormSchemaBuilder(form).getSchema();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#garbageCollect()
	 */
	public void garbageCollect() {
		getFormEntryDAO().garbageCollect();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#migrateQueueAndArchiveToFilesystem()
	 */
	public void migrateQueueAndArchiveToFilesystem() {
		getFormEntryDAO().migrateQueueAndArchiveToFilesystem();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#archiveFormEntryXsn(org.openmrs.module.formentry.FormEntryXsn)
	 */
	public void archiveFormEntryXsn(FormEntryXsn formEntryXsn) {
		formEntryXsn.setArchived(true);
		formEntryXsn.setArchivedBy(Context.getAuthenticatedUser());
		formEntryXsn.setDateArchived(new Date());
		
		getFormEntryDAO().updateFormEntryXsn(formEntryXsn);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryXsn(org.openmrs.Form)
	 */
	public void deleteFormEntryXsn(Form form) {
		getFormEntryDAO().deleteFormEntryXsn(form.getFormId());
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryXsn(org.openmrs.module.formentry.FormEntryXsn)
	 */
	public void createFormEntryXsn(FormEntryXsn formEntryXsn) {
		formEntryXsn.setCreator(Context.getAuthenticatedUser());
		formEntryXsn.setDateCreated(new Date());
		
		// archive all old matching xsns if they exist
		for (FormEntryXsn oldXsn : getFormEntryDAO().getAllFormEntryXsnsForForm(formEntryXsn.getForm(), false)) {
			archiveFormEntryXsn(oldXsn);
		}
		
		getFormEntryDAO().updateFormEntryXsn(formEntryXsn);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryXsn(org.openmrs.Form)
	 */
	public FormEntryXsn getFormEntryXsn(Form form) {
		return getFormEntryXsn(form.getFormId());
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryXsn(java.lang.Integer)
	 */
	public FormEntryXsn getFormEntryXsn(Integer formId) {
		if (formId == null)
			return null;
		
		return getFormEntryDAO().getFormEntryXsn(formId);
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#migrateXsnsToDatabase()
	 */
	public void migrateXsnsToDatabase() {
		getFormEntryDAO().migrateXsnsToDatabase();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#migrateFormEntryArchiveNeeded()
	 */
	public Boolean migrateFormEntryArchiveNeeded() {
		return getFormEntryDAO().migrateFormEntryArchiveNeeded();
	}
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormsWithXsn(boolean)
	 */
	public List<Form> getFormsWithXsn(boolean publishedOnly) {
		return getFormEntryDAO().getFormsWithXsns(publishedOnly);
	}

    /**
     * @see org.openmrs.module.formentry.FormEntryService#getAllFormEntryXsnMetadata() 
     */
    public List<FormEntryXsnMetadata> getAllFormEntryXsnMetadata() {
        return dao.getAllFormEntryXsnMetadata();
    }

    /**
     * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryXsn(FormEntryXsn)
     */
    public void deleteFormEntryXsn(FormEntryXsn xsn) {
        dao.deleteFormEntryXsn(xsn);
    }

    /**
     * @see org.openmrs.module.formentry.FormEntryService#getFormEntryXsnById(Integer)
     */
    public FormEntryXsn getFormEntryXsnById(Integer xsnId) {
        return dao.getFormEntryXsnById(xsnId);
    }

	/**
	 * @see org.openmrs.module.formentry.FormEntryService#migrateFormEntryXsnToFilesystem(FormEntryXsn)
	 */
	public void migrateFormEntryXsnToFilesystem(FormEntryXsn xsn) throws APIException {
		if (xsn == null)
			return;

		// get the directory
		String dir = Context.getAdministrationService().getGlobalProperty(
				FormEntryConstants.FORMENTRY_GP_XSN_ARCHIVE_DIR, null);

		if (dir != null) {
			File xsnDir = OpenmrsUtil
					.getDirectoryInApplicationDataDirectory(dir);
			if (xsnDir.exists() && xsnDir.isDirectory()) {

				// directory exists, create the file
				File xsnFile = new File(xsnDir, generateXsnFileName(xsn));
				FileOutputStream fos = null;

				try {

					// write the file
					xsnFile.createNewFile();
					fos = new FileOutputStream(xsnFile);
					fos.write(xsn.getXsnData());
					fos.flush();

					// delete the XSN
					FormEntryService service = Context
							.getService(FormEntryService.class);
					service.deleteFormEntryXsn(xsn);

				} catch (IOException e) {
					throw new APIException(
							"could not write XSN to file system", e);
				} finally {
					try {
                        if (fos != null)
    						fos.close();
					} catch (IOException e) {
						// pass
					}
				}
			}
		}

	}

	/**
	 * generate a unique filename name for a Form Entry XSN
	 * 
	 * @param xsn
	 *            the XSN used to create the filename
	 * @return the filename
	 */
	private String generateXsnFileName(FormEntryXsn xsn) {
		if (xsn == null)
			return null;

        String filename = xsn.getForm().getUuid() + "-v"
                + xsn.getForm().getVersion() + "-"
                + xsn.getFormEntryXsnId() + ".xsn";

		return filename;
	}
	
}
