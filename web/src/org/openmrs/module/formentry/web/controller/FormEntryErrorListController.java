package org.openmrs.module.formentry.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryError;
import org.openmrs.module.formentry.FormEntryService;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class FormEntryErrorListController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		//default empty Object
		List<FormEntryError> errorList = new Vector<FormEntryError>();
		
		// not used
		
        return errorList;
    }

	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		//default empty Objects
		Integer errorSize = 0;
		
		//only fill the objects if the user has authenticated properly
		if (Context.isAuthenticated()) {
			FormEntryService fs = (FormEntryService)Context.getService(FormEntryService.class);
			errorSize = fs.getFormEntryErrorSize();
		}
    	
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("errorSize", errorSize);
		
        return map;
	}
	
}