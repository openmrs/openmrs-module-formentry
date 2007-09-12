package org.openmrs.module.formentry.advice;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.FormEntryXsn;
import org.openmrs.module.formentry.PublishInfoPath;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class DuplicateFormAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {

	private static final long serialVersionUID = 38539204394323L;

	private Log log = LogFactory.getLog(this.getClass());
	
	public boolean matches(Method method, Class targetClass) {
		return method.getName().equals("duplicateForm");
	}

	@Override
	public Advice getAdvice() {
		return new DuplicateFormAdvice();
	}
	
	private class DuplicateFormAdvice implements MethodInterceptor {
		
		public Object invoke(MethodInvocation invocation) throws Throwable {
			
			FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
			
			if (log.isDebugEnabled())
				log.debug("Method: " + invocation.getMethod().getName());
			
			Form oldForm = (Form)invocation.getArguments()[0];
			
			FormEntryXsn oldXsn = formEntryService.getFormEntryXsn(oldForm);
			
			Object[] streamAndDir = FormEntryUtil.getCurrentXSN(oldForm, false);
			InputStream oldFormStream = (InputStream) streamAndDir[0];
			File tempDir = (File) streamAndDir[1];
			if (log.isDebugEnabled())
				log.debug("oldXSN: " + oldXsn);
			
			Form newForm = null;
			try {
				newForm = (Form)invocation.proceed();
				
				// only try to copy the file if there is an old xsn
				if (oldXsn != null && oldFormStream != null) {
					// rebuild the XSN -- this is really just used to change the form "id"
					// attribute in the xsd file
					PublishInfoPath.publishXSN(oldFormStream, newForm);
					log.debug("Done duplicating xsn");
					try {
						oldFormStream.close();
					} 
					catch (IOException ioe) {
						// pass
					}
				}
			}
			finally {
				try {
					OpenmrsUtil.deleteDirectory(tempDir);
				} 
				catch (IOException ioe) {
					// pass
				}
			}
			
			return newForm;
		}
	}
	
}