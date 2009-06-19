package org.openmrs.module.formentry.advice;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * This class deletes the formentry xsn row when the form is deleted
 */
public class BeforeDeleteFormAdvice implements MethodBeforeAdvice {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see org.springframework.aop.MethodBeforeAdvice#before(java.lang.reflect.Method, java.lang.Object[], java.lang.Object)
	 */
	public void before(Method m, Object[] args, Object target) throws Throwable {
		
		if (log.isDebugEnabled())
			log.debug("Calling form advice for method: " + m.getName());
		
		if (m.getName().equals("deleteForm") || m.getName().equals("purgeForm")) {
			
			// TODO: Should be concerned about transaction rollback if the delete fails?
			
			FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
			formEntryService.deleteFormEntryXsn((Form)args[0]);
		}
		
	}
	
}