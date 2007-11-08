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
package org.openmrs.module.formentry.test;

import org.openmrs.BaseModuleContextSensitiveTest;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryError;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.util.OpenmrsClassLoader;

/**
 * Test the FormEntryError object going into and out of the database
 */
public class TestFormEntryError extends BaseModuleContextSensitiveTest {

	/**
	 * Test that an error queue item can get into the database
	 * 
	 * @throws Exception
	 */
	public void testCreatingErrorQueueItem() throws Exception {
		
		authenticate();
		
		Thread.currentThread().setContextClassLoader(OpenmrsClassLoader.getInstance());

		System.out.println("FormEntryService.class.getClassLoader(): " + FormEntryService.class.getClassLoader());
		//System.out.println("OpenmrsClassLoader.get(FormEntryService.class).getClassLoader(): " + OpenmrsClassLoader.getInstance().loadClass("org.openmrs.module.FormEntryService.FormEntryService").getClassLoader());
		
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		// create and save an error object
		FormEntryError error = new FormEntryError();
		error.setError("Some error");
		error.setErrorDetails("Some error details");
		error.setFormData("Some form data");
		
		formEntryService.createFormEntryError(error);
		
		FormEntryError fetchedError = formEntryService.getFormEntryError(1);
		
		assertNotNull(fetchedError);
		
	}
	
}