package org.openmrs.module.formentry;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.RelationshipType;
import org.openmrs.Tribe;
import org.openmrs.User;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.module.formentry.db.FormEntryDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service methods for the Form Entry module
 */
@Transactional
public interface FormEntryService {

	public void setFormEntryDAO(FormEntryDAO dao);

	/**
	 * @see org.openmrs.api.PatientService.createPatient(org.openmrs.Patient)
	 */
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public void createPatient(Patient patient) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatient(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Patient getPatient(Integer patientId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.updatePatient(org.openmrs.Patient)
	 */
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public void updatePatient(Patient patient) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByIdentifier(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Set<Patient> getPatientsByIdentifier(String identifier,
			boolean includeVoided) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Collection<Patient> getPatientsByName(String name) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientsByName(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Collection<Patient> getPatientsByName(String name, boolean includeVoided)
			throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierTypes()
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public List<PatientIdentifierType> getPatientIdentifierTypes()
			throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getPatientIdentifierType(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public PatientIdentifierType getPatientIdentifierType(
			Integer patientIdentifierTypeId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getTribe(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Tribe getTribe(Integer tribeId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getTribes()
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public List<Tribe> getTribes() throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.findTribes(java.lang.String)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public List<Tribe> findTribes(String s) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getLocations()
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public List<Location> getLocations() throws APIException;

	/**
	 * @see org.openmrs.api.EncounterService.findLocations()
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public List<Location> findLocations(String txt) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.getLocation(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Location getLocation(Integer locationId) throws APIException;

	/**
	 * @see org.openmrs.api.PatientService.findPatients(java.lang.String,boolean)
	 */
	@Transactional(readOnly=true)
	@Authorized({FormEntryConstants.PRIV_FORM_ENTRY})
	public Collection<Patient> findPatients(String query, boolean includeVoided);

	/**
	 * @see org.openmrs.api.FormService.getForm(java.lang.Integer)
	 */
	@Transactional(readOnly=true)
	public Form getForm(Integer formId);

	@Transactional(readOnly=true)
	public Collection<Form> getForms(boolean onlyPublished, boolean includeRetired);

	/**
	 * @see org.openmrs.api.UserService.getUserByUsername(String)
	 */
	@Transactional(readOnly=true)
	public User getUserByUsername(String username);
	
	/**
	 * @see org.openmrs.api.UserService.findUsers(String, List<String>, boolean)
	 */
	@Transactional(readOnly=true)
	public Collection<User> findUsers(String searchValue, List<String> roles,
			boolean includeVoided);

	/**
	 * @see org.openmrs.api.UserService.getAllUsers(List<String>, boolean)
	 */
	@Transactional(readOnly=true)
	public Collection<User> getAllUsers(List<String> strRoles,
			boolean includeVoided);

	/**
	 * Get the list of key value pairs showing important formentry information
	 * 
	 * @return map of system variables
	 */
	@Transactional(readOnly=true)
	public SortedMap<String, String> getSystemVariables();
	
	@Transactional(readOnly=true)
	public RelationshipType getRelationshipType(Integer id);

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
}