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

import java.util.Date;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Form;
import org.openmrs.User;

/**
 * This class holds metadata about the formentry xsn
 */
public class FormEntryXsn extends BaseOpenmrsObject {
	// private Log log = LogFactory.getLog(this.getClass());

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
	 * @param creator
	 *            The creator to set.
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
	 * @param dateCreated
	 *            The dateCreated to set.
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
	 * @param archived
	 *            the archived to set
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
	 * @param formEntryXsnId
	 *            the formEntryXsnId to set
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
	 * @param archivedBy
	 *            the archivedBy to set
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
	 * @param dateArchived
	 *            the dateArchived to set
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
	 * @param form
	 *            the form to set
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
	 * @param xsnData
	 *            the xsnData to set
	 */
	public void setXsnData(byte[] xsnData) {
		this.xsnData = xsnData;
	}

	@Override
	public Integer getId() {
		return getFormEntryXsnId();
	}

	@Override
	public void setId(Integer id) {
		setFormEntryXsnId(id);
	}

}
