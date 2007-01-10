package org.openmrs.module.formentry.advice;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Form;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.module.formentry.PublishInfoPath;
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
				
			Method method = invocation.getMethod();
			log.debug("Method: " + method.getName());
			
			Form oldForm = (Form)invocation.getArguments()[0];
			File oldXSN = FormEntryUtil.getXSNFile(oldForm.getFormId() + ".xsn");
			InputStream oldFormStream = FormEntryUtil.getCurrentXSN(oldForm);
			log.debug("oldXSN: " + oldXSN.getAbsolutePath());
			
			Form newForm = (Form)invocation.proceed();
			
			// only try to copy the file if there is an old xsn
			if (oldXSN.exists()) {
				// rebuild the XSN -- this is really just used to change the form "id"
				// attribute in the xsd file
				PublishInfoPath.publishXSN(oldFormStream, newForm);
				log.debug("Done duplcating xsn");
			}
			
			return newForm;
		}
	}
	
}