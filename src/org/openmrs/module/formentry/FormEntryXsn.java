package org.openmrs.module.formentry;

import java.util.Date;

import org.openmrs.Form;
import org.openmrs.User;

/**
 * This class holds metadat about the formentry xsn
 */
public class FormEntryXsn {
	//private Log log = LogFactory.getLog(this.getClass());

	private Integer formEntryXsnId;
	private byte[] xsnData;
	private Form form;
	private Boolean archived = false;
	private User creator;
	private Date dateCreated;
	private User archivedBy;
	private Date dateArchived;
	

	/**
	 * Default constructor
	 */
	public FormEntryXsn() {
	}
	
	/**
	 * @return Returns the creator.
	 */
	public User getCreator() {
		return creator;
	}

	/**
	 * @param creator The creator to set.
	 */
	public void setCreator(User creator) {
		this.creator = creator;
	}

	/**
	 * @return Returns the dateCreated.
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated The dateCreated to set.
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
     * @return the archived
     */
    public Boolean getArchived() {
    	return archived;
    }

	/**
     * @param archived the archived to set
     */
    public void setArchived(Boolean archived) {
    	this.archived = archived;
    }

	/**
     * @return the formEntryXsnId
     */
    public Integer getFormEntryXsnId() {
    	return formEntryXsnId;
    }

	/**
     * @param formEntryXsnId the formEntryXsnId to set
     */
    public void setFormEntryXsnId(Integer formEntryXsnId) {
    	this.formEntryXsnId = formEntryXsnId;
    }

	/**
     * @return the archivedBy
     */
    public User getArchivedBy() {
    	return archivedBy;
    }

	/**
     * @param archivedBy the archivedBy to set
     */
    public void setArchivedBy(User archivedBy) {
    	this.archivedBy = archivedBy;
    }

	/**
     * @return the dateArchived
     */
    public Date getDateArchived() {
    	return dateArchived;
    }

	/**
     * @param dateArchived the dateArchived to set
     */
    public void setDateArchived(Date dateArchived) {
    	this.dateArchived = dateArchived;
    }

	/**
     * @return the form
     */
    public Form getForm() {
    	return form;
    }

	/**
     * @param form the form to set
     */
    public void setForm(Form form) {
    	this.form = form;
    }

	/**
     * @return the xsnData
     */
    public byte[] getXsnData() {
    	return xsnData;
    }

	/**
     * @param xsnData the xsnData to set
     */
    public void setXsnData(byte[] xsnData) {
    	this.xsnData = xsnData;
    }
    
}
