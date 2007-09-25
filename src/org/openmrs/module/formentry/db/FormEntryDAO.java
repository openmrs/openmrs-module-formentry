package org.openmrs.module.formentry.db;

import java.util.Collection;

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

	/**
     * @see org.openmrs.module.formentry.FormEntryService#migrateXsnsToDatabase()
     */
    public void migrateXsnsToDatabase();
	
    /**
     * @see org.openmrs.module.formentry.FormEntryService#migrateFormEntryArchiveNeeded()
     */
    public Boolean migrateFormEntryArchiveNeeded();
}
