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
package org.openmrs.module.formentry;

import java.util.Collection;
import java.util.List;
import java.util.SortedMap;

import org.openmrs.Form;
import org.openmrs.annotation.Authorized;
import org.openmrs.module.formentry.db.FormEntryDAO;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service methods for the Form Entry module
 */
@Transactional
public interface FormEntryService {

	public void setFormEntryDAO(FormEntryDAO dao);

	/**
	 * Get the list of key value pairs showing important formentry information
	 * 
	 * @return map of system variables
	 */
	@Transactional(readOnly=true)
	public SortedMap<String, String> getSystemVariables();
	
	/***************************************************************************
	 * FormEntryQueue Service Methods
	 **************************************************************************/

	/**
	 * Creates a file in FormEntryConstants.FORMENTRY_GP_QUEUE_DIR for the data in 
	 * this queue item
	 * 
	 * @param formEntryQueue object containing form data to save in the processing queue
	 */
	@Authorized(value = {FormEntryConstants.PRIV_ADD_FORMENTRY_QUEUE, FormEntryConstants.PRIV_FORM_ENTRY}, requireAll=true)
	public void createFormEntryQueue(FormEntryQueue formEntryQueue);
	
	/**
	 * Delete the given formentry queue item
	 * 
	 * @param formEntryQueue to delete
	 */
	@Authorized({FormEntryConstants.PRIV_DELETE_FORMENTRY_QUEUE})
	public void deleteFormEntryQueue(FormEntryQueue formEntryQueue);
	
	/**
	 * Find and return all queue items 
	 * 
	 * @return list of queue items
	 */
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE})
	public Collection<FormEntryQueue> getFormEntryQueues();
	
	/**
	 * Find the next queue item to be processed
	 * 
	 * @return next queue item or null if none
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE})
	public FormEntryQueue getNextFormEntryQueue();

	/**
	 * Get the number of queue items waiting to be processed
	 * 
	 * @return integer size of queue item list
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_QUEUE})
	public Integer getFormEntryQueueSize();
	
	/**
	 * Creates a file in FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR for the data in 
	 * this archive item
	 * 
	 * @param formEntryArchive object containing form data to save in the processing archive
	 */
	@Authorized({FormEntryConstants.PRIV_ADD_FORMENTRY_ARCHIVE})
	public void createFormEntryArchive(FormEntryArchive formEntryArchive);

	/**
	 * Get all formentry archive items
	 * 
	 * @return list of formentry archive items
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_ARCHIVE})
	public Collection<FormEntryArchive> getFormEntryArchives();
	
	/**
	 * Delete the given formentry archive from the system
	 * 
	 * @param formEntryArchive to be deleted
	 */
	@Authorized({FormEntryConstants.PRIV_DELETE_FORMENTRY_ARCHIVE})
	public void deleteFormEntryArchive(FormEntryArchive formEntryArchive);
	
	/**
	 * Get the number of formentry archive items
	 * 
	 * @return integer number of archive items
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR})
	public Integer getFormEntryArchiveSize();

	/**
	 * Create and store the given formentry error item
	 * 
	 * @param formEntryError to save to the db
	 */
	@Authorized({FormEntryConstants.PRIV_ADD_FORMENTRY_ERROR})
	public void createFormEntryError(FormEntryError formEntryError);

	/**
	 * Get formentry error item defined by the given id
	 * 
	 * @param formEntryErrorId
	 * @return database stored formentry error item
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR})
	public FormEntryError getFormEntryError(Integer formEntryErrorId);

	/**
	 * Get all formentry error items
	 * 
	 * @return list of formentry items
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR})
	public Collection<FormEntryError> getFormEntryErrors();

	/**
	 * Update database version of the given formentry error item
	 * 
	 * @param formEntryError to update
	 */
	@Authorized({FormEntryConstants.PRIV_EDIT_FORMENTRY_ERROR})
	public void updateFormEntryError(FormEntryError formEntryError);

	/**
	 * Delete the given formentry error item
	 * 
	 * @param formEntryError to delete
	 */
	@Authorized({FormEntryConstants.PRIV_DELETE_FORMENTRY_ERROR})
	public void deleteFormEntryError(FormEntryError formEntryError);

	/**
	 * Get the number of formentry error items
	 * 
	 * @return integer number of formentry errors
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_VIEW_FORMENTRY_ERROR})
	public Integer getFormEntryErrorSize();

	/**
	 * Returns XML Schema for form based on the defined fields
	 * 
	 * @param form
	 * @return XML Schema for form
	 */
	@Transactional(readOnly=true)
	public String getSchema(Form form);
	
	/**
	 * Clean up the jvm memory space and any cached dao items.
	 */
	public void garbageCollect();
	
	/**
	 * Since version 2.6, formentry_queue and formentry_archives are stored in the filesystem.  
	 * Here, we make sure that there aren't any straggling entries in those tables. 
	 * This moves the formentry_queue and formentry_archive tables to the filesystem and deletes these tables
 	 * We've removed the database connectivity to the queue and archive tables in the api.  Must go through the 
	 * standard jdbc connection
	 */
	public void migrateQueueAndArchiveToFilesystem();
	
	/**
	 * Create an xsn entry
	 * 
	 * @param formEntryXsn
	 */
	@Authorized({FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN})
	public void createFormEntryXsn(FormEntryXsn formEntryXsn);
	
	/**
	 * Archive the given formentry xsn
	 * 
	 * @param formEntryXsn
	 */
	@Authorized({FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN})
	public void archiveFormEntryXsn(FormEntryXsn formEntryXsn);
	
	/**
	 * Deletes all xsns (the active one and the archives) that are 
	 * associated with the given form
	 * 
	 * @param form Form object 
	 */
	@Authorized({FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN})
	public void deleteFormEntryXsn(Form form);
	
	/**
	 * Get the formentryxsn for the given form
	 * 
	 * @param form
	 * @return FormEntryXSN (non-archived) associated with the form
	 */
	@Transactional(readOnly=true)
	public FormEntryXsn getFormEntryXsn(Form form);
	
	/**
	 * Get the formentryxsn for the given form
	 * 
	 * @param formId id of the form that owns the xsn to retrieve
	 * @return FormEntryXSN (non-archived) associated with the form
	 */
	@Transactional(readOnly=true)
	public FormEntryXsn getFormEntryXsn(Integer formId);
	
	/**
	 * Since version 2.6, xsns are stored in the database instead of the 
	 * filesystem
	 */
	public void migrateXsnsToDatabase();
	
	/**
	 * Check if the formentry_archive table exists.
	 * 
	 * @return true/false whether migration is 
	 * 		needed (by checking for formentry_archive table)
	 */
	public Boolean migrateFormEntryArchiveNeeded();
	
	/**
	 * Gets forms that have an xsn defined for them already
	 * 
	 * @param publishedOnly if true, only fetches forms marked as published
	 * @return list of forms that have an xsn defined
	 */
	@Transactional(readOnly = true)
	public List<Form> getFormsWithXsn(boolean publishedOnly);
	
	/**
	 * Gets all Form Entry XSNs regardless of archived status, grouped by
	 * associated forms
	 * 
	 * @return a map of forms to lists of XNSs
	 */
	@Transactional(readOnly = true)
	public List<FormEntryXsnMetadata> getAllFormEntryXsnMetadata();

	/**
	 * Gets a Form Entry XSN by its id
	 * 
	 * @param xsnId
	 *            the id of the XSN to get
	 * @return the XSN
	 */
	@Transactional(readOnly = true)
	public FormEntryXsn getFormEntryXsnById(Integer xsnId);

	/**
	 * Deletes a Form Entry XSN object
	 * 
	 * @param xsn
	 *            the Form Entry XSN object to be deleted
	 */
	@Authorized({ OpenmrsConstants.PRIV_MANAGE_FORMS })
	public void deleteFormEntryXsn(FormEntryXsn xsn);

	/**
	 * Migrates a Form Entry XSN object to the filesystem, deleting the
	 * original. Only works on archived XSNs.
	 * 
	 * @param xsn
	 *            the Form Entry XSN object be migrated
	 */
	@Authorized({ OpenmrsConstants.PRIV_MANAGE_FORMS })
	public void migrateFormEntryXsnToFilesystem(FormEntryXsn xsn);
}