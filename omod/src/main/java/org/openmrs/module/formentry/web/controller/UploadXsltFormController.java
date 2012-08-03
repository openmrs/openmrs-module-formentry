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
package org.openmrs.module.formentry.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.web.WebConstants;
import org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler;
import org.openmrs.web.controller.form.FormResourceController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * {@link Controller} for the uploadXslt.jsp page
 */
@Controller
public class UploadXsltFormController {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String UPLOAD_XSLT_FORM = "/module/formentry/uploadXslt";
	
	/**
	 * Processes requests to display the form
	 * 
	 * @param formId the id of the form
	 * @param model model The {@link ModelMap} object
	 */
	@RequestMapping(value = UPLOAD_XSLT_FORM, method = RequestMethod.GET)
	public void showForm(@RequestParam("formId") Integer formId, ModelMap model) {
		model.addAttribute("formId", formId);
	}
	
	/**
	 * Processes requests to save upload an xslt
	 * 
	 * @param formId the id of the form
	 * @param model The {@link ModelMap} object
	 * @param request The {@link HttpServletRequest} object
	 * @return view page or url to send the user
	 */
	@RequestMapping(value = UPLOAD_XSLT_FORM, method = RequestMethod.POST)
	public String handleUploadXslt(@RequestParam("formId") Integer formId, ModelMap model, HttpServletRequest request) {
		
		Context.requirePrivilege("Manage FormEntry Xslt");
		
		Form form = Context.getFormService().getForm(formId);
		if (form == null)
			throw new APIException("Cannot find form with id:" + formId);
		
		FormResource xsltResource = new FormResource();
		xsltResource.setForm(form);
		xsltResource.setName(FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME);
		xsltResource.setDatatypeClassname(LongFreeTextDatatype.class.getName());
		xsltResource.setPreferredHandlerClassname(LongFreeTextFileUploadHandler.class.getName());
		
		try {
			new FormResourceController().handleAddFormResource(xsltResource, new BindException("resourceValue",
			        "resourceValue"), request);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "formentry.xslt.upload.success");
			
			//the redirect from core doesn't work here
			return "redirect:/admin/forms/formResources.form?formId=" + formId;
		}
		catch (Exception e) {
			//Core show an ugly page here, instead send the user back notifying them of the error
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "formentry.xslt.upload.error");
			model.addAttribute("formId", formId);
			
			return UPLOAD_XSLT_FORM;
		}
	}
}
