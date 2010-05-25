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
package org.openmrs.module.formentry.db;

import java.util.Collection;
import java.util.List;

import org.openmrs.Form;
import org.openmrs.api.db.DAOException;
import org.openmrs.module.formentry.FormEntryError;
import org.openmrs.module.formentry.FormEntryXsn;

public interface FormEntryDAO {

	/****************************************************************
	 * FormEntryQueue Methods
	 ****************************************************************/
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryError(FormEntryError)
	 */
	public void createFormEntryError(FormEntryError formEntryError) throws DAOException;
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryError(Integer)
	 */
	public FormEntryError getFormEntryError(Integer formEntryErrorId) throws DAOException;
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryErrors()
	 */
	public Collection<FormEntryError> getFormEntryErrors() throws DAOException;
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#updateFormEntryError(FormEntryError)
	 */
	public void updateFormEntryError(FormEntryError formEntryError) throws DAOException;
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryError(FormEntryError)
	 */
	public void deleteFormEntryError(FormEntryError formEntryError) throws DAOException;

	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryErrorSize()
	 */
	public Integer getFormEntryErrorSize() throws DAOException;
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#garbageCollect()
	 */
	public void garbageCollect();
	
	/**
	 * @see org.openmrs.module.formentry.FormEntryService#migrateQueueAndArchiveToFilesystem()
	 */
	public void migrateQueueAndArchiveToFilesystem();

	/**
	 * Creates or updates the given FormEntryXSN
	 * @see org.openmrs.module.formentry.FormEntryService#createFormEntryXsn(org.openmrs.module.formentry.FormEntryXsn)
	 * @see org.openmrs.module.formentry.FormEntryService#archiveFormEntryXsn(org.openmrs.module.formentry.FormEntryXsn)
	 */
	public void updateFormEntryXsn(FormEntryXsn formEntryXsn);

	/**
	 * @see org.openmrs.module.formentry.FormEntryService#getFormEntryXsn(java.lang.Integer)
	 */
	public FormEntryXsn getFormEntryXsn(Integer formId);
	
	public List<FormEntryXsn> getAllFormEntryXsnsForForm(Form form, boolean includeArchived);

	/**
     * @see org.openmrs.module.formentry.FormEntryService#migrateXsnsToDatabase()
     */
    public void migrateXsnsToDatabase();
	
    /**
     * @see org.openmrs.module.formentry.FormEntryService#migrateFormEntryArchiveNeeded()
     */
    public Boolean migrateFormEntryArchiveNeeded();

    /**
	 * @see org.openmrs.module.formentry.FormEntryService#deleteFormEntryXsn(org.openmrs.Form)
	 */
	public void deleteFormEntryXsn(Integer formId);
}
