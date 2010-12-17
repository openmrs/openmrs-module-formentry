package org.openmrs.module.formentry.web.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.migration.MigrateFormEntryQueueThread;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Controller for the migration assistant
 */
public class MigrateFormEntryArchiveFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
		
		// not really used
		
        return "";
    }
    
	/**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException errors) throws Exception {
	    
    	String action = ServletRequestUtils.getStringParameter(request, "action", "");
    	
    	MessageSourceAccessor msa = getMessageSourceAccessor();
    	
    	if (action.equals(msa.getMessage("formentry.startQueueArchiveMigration"))) {
    		
    		if (!MigrateFormEntryQueueThread.isActive()) {
    			// Spawn a thread to do the queue/archive migration from db to filesystem
    			// A thread is needed instead of a direct call because the formentry 
    			// service isn't set up until after the webmoduleutil.startup is complete
    			MigrateFormEntryQueueThread.setActive(true);
    			MigrateFormEntryQueueThread migrateQueueThread = new MigrateFormEntryQueueThread(Context.getUserContext());
    			migrateQueueThread.setName("Migrate Form Entry Queue Thread");
    			migrateQueueThread.start();
    		}

    	}
    	else if (action.equals(msa.getMessage("formentry.stopQueueArchiveMigration"))) {
    		MigrateFormEntryQueueThread.setActive(false);
    	}
    	
	    return showForm(request, response, errors);
    }


	/**
	 * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest)
	 */
	protected Map referenceData(HttpServletRequest request) throws Exception {
		
		// figure out where they want to store the archives
		File archiveDir = FormEntryUtil.getFormEntryArchiveDir(null);
		Boolean migrationNeeded = false;
		
		//only fill the objects if the user has authenticated properly
		if (Context.isAuthenticated()) {
			FormEntryService fs = (FormEntryService)Context.getService(FormEntryService.class);
			migrationNeeded = fs.migrateFormEntryArchiveNeeded();
		}
    	
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("active", MigrateFormEntryQueueThread.isActive());
		map.put("archiveDir", archiveDir.getAbsolutePath());
		map.put("migrationNeeded", migrationNeeded);
		
        return map;
	}
	
}