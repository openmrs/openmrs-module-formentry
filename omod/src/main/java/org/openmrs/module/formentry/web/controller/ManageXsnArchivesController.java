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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.FormEntryXsnMetadata;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;

/**
 * controller for manageXsnArchives.htm requests
 */
@Controller
@RequestMapping("module/formentry/manageXsnArchives.htm")
public class ManageXsnArchivesController {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * generate the model and direct GET requests to the JSP
     * 
     * @param modelMap the model for the JSP
     * @return path to the JSP file
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(method=RequestMethod.GET)
    public String getPage(ModelMap modelMap){
        modelMap.put("xsnmap", this.getFormEntryXsnMap());
		modelMap.put(
				"location",
				Context.getAdministrationService().getGlobalProperty(
						FormEntryConstants.FORMENTRY_GP_XSN_ARCHIVE_DIR,
						"unknown"));
        return "module/formentry/manageXsnArchives";
    }

    /**
     * accept POST requests to migrate specific XSNs to the filesystem
     * 
     * @param xsnIds list of XSN identifiers to be moved
     * @param modelMap the model for the resulting JSP
     * @return path to the JSP file
     */
    @SuppressWarnings("unchecked")
	@RequestMapping(method=RequestMethod.POST)
    public String migrateXSNs(WebRequest request, @RequestParam(value="xsnIds", required=false) List<Integer> xsnIds, ModelMap modelMap){
    	FormEntryService service = Context.getService(FormEntryService.class);
    	int succeeded = 0;
    	List<Integer> failed = new ArrayList<Integer>();
    	
    	if (xsnIds == null)
    		xsnIds = new ArrayList<Integer>();
    	
    	// migrate XSNs one by one
        for (Integer xsnId: xsnIds) {
        	try {
	            FormEntryXsn xsn = service.getFormEntryXsnById(xsnId);
	            service.migrateFormEntryXsnToFilesystem(xsn);
	            log.info("migrated Form Entry XSN #" + xsnId + " to the filesystem.");
	            succeeded++;
        	} catch (APIException e) {
        		log.error("could not migrate Form Entry XSN #" + xsnId + " to the filesystem.", e);
        		failed.add(xsnId);
        	}
        }
        
        // send success / error messages back
		String msg = null;
		if (succeeded > 0) {
			if (succeeded == 1)
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"formentry.xsnarchives.success.single",
						WebRequest.SCOPE_SESSION);
			else {
				msg = Context.getMessageSourceService().getMessage(
						"formentry.xsnarchives.success", new Object[] { succeeded },
						Context.getLocale());
				request.setAttribute(WebConstants.OPENMRS_MSG_ATTR, msg,
						WebRequest.SCOPE_SESSION);
			}
		}
		if (failed.size() > 0) {
			if (failed.size() == 1)
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"formentry.xsnarchives.failure.single",
						WebRequest.SCOPE_SESSION);
			else {
				msg = Context.getMessageSourceService().getMessage(
						"formentry.xsnarchives.failure",
						new Object[] { failed.size() }, Context.getLocale());
				request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, msg,
						WebRequest.SCOPE_SESSION);
			}
		}
		if (succeeded == 0 && failed.size() == 0)
			request.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "formentry.xsnarchives.nothingdone",
					WebRequest.SCOPE_SESSION);
        
        modelMap.put("xsnmap", this.getFormEntryXsnMap());
		modelMap.put(
				"location",
				Context.getAdministrationService().getGlobalProperty(
						FormEntryConstants.FORMENTRY_GP_XSN_ARCHIVE_DIR,
						"unknown"));
        modelMap.put("failedArchives", failed);
        return "module/formentry/manageXsnArchives";
    }

    /**
     * builds a Map of Forms to ordered Lists of XSN Metadata for the model
     *  
     * @return a map of forms to ordered lists of XSN metadata
     */
    private Map<Form, Collection<FormEntryXsnMetadata>> getFormEntryXsnMap(){
        FormEntryService service = Context.getService(FormEntryService.class);
        List<FormEntryXsnMetadata> xsns = service.getAllFormEntryXsnMetadata();

        Map<Form, Collection<FormEntryXsnMetadata>> xsnmap = new HashMap<Form, Collection<FormEntryXsnMetadata>>();
        Map<Integer, Form> formmap = new HashMap<Integer, Form>();
        
        for (FormEntryXsnMetadata xsn: xsns) {
        	
            Integer formId = xsn.getFormId();
            if (!formmap.containsKey(formId))
            	formmap.put(formId, Context.getFormService().getForm(formId));
            Form form = formmap.get(formId);
            
            if (!xsnmap.containsKey(form))
                xsnmap.put(form, new ArrayList<FormEntryXsnMetadata>());
            xsnmap.get(form).add(xsn);
        }
        return xsnmap;
    }

}
