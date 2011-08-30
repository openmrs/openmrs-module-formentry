package org.openmrs.module.formentry;

import java.util.Date;

public class FormEntryXsnMetadata {

	private Integer formEntryXsnId;
	private Integer formId;
	private Boolean archived = false;
	private Date dateCreated;
	private Date dateArchived;

	public FormEntryXsnMetadata() {
		// pass
	}
	
	public FormEntryXsnMetadata(FormEntryXsn xsn) {
		this.setFormEntryXsnId(xsn.getFormEntryXsnId());
		this.setFormId(xsn.getForm().getFormId());
		this.setArchived(xsn.getArchived());
		this.setDateCreated(xsn.getDateCreated());
		this.setDateArchived(xsn.getDateArchived());
	}
	
	public Integer getFormEntryXsnId() {
		return formEntryXsnId;
	}

	public void setFormEntryXsnId(Integer formEntryXsnId) {
		this.formEntryXsnId = formEntryXsnId;
	}

	public Integer getFormId() {
		return formId;
	}

	public void setFormId(Integer formId) {
		this.formId = formId;
	}

	public Boolean getArchived() {
		return archived;
	}

	public void setArchived(Boolean archived) {
		this.archived = archived;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateArchived() {
		return dateArchived;
	}

	public void setDateArchived(Date dateArchived) {
		this.dateArchived = dateArchived;
	}

}
