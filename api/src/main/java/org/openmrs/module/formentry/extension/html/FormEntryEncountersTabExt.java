package org.openmrs.module.formentry.extension.html;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.web.FormEntryContext;
import org.openmrs.module.web.extension.FormEntryHandler;

/**
 * Adds links to the Encounters tab in openmrs v1.5
 */
public class FormEntryEncountersTabExt extends FormEntryHandler {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
	
	/**
	 * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getFormEntryUrl()
	 */
	public String getFormEntryUrl() {
		return "moduleServlet/formentry/formDownload?target=formentry";
	}
	
	/**
	 * @see org.openmrs.module.web.extension.FormEntryHandler#getFormsModuleCanEnter(org.openmrs.module.web.FormEntryContext)
	 */
	@Override
	public List<Form> getFormsModuleCanEnter(FormEntryContext formentryContext) {
		Boolean published = true;
		
		if (Context.hasPrivilege(FormEntryConstants.PRIV_VIEW_UNPUBLISHED_FORMS)) {
			published = false; // so both published and unpublished forms are shown
			log.debug("including unpublished forms");
		}
		
		FormEntryService fes = Context.getService(FormEntryService.class);
		return fes.getFormsWithXsn(published);
	}
	
	/**
	 * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getFormListTitle()
	 */
	public String getFormListTitle() {
		return "formentry.patientDashboard.formListTitle";
	}
	
	/**
	 * @see org.openmrs.module.web.extension.FormEntryModuleExtension#getRequiredPrivilege()
	 */
	@Override
	public String getRequiredPrivilege() {
		return FormEntryConstants.PRIV_FORM_ENTRY;
	}
	
}
