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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.SerializableComplexObsHandler;
import org.openmrs.test.Verifies;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class FormSchemaBuilderTest {
	
	private ObsService os;
	
	private AdministrationService as;
	
	@Before
	public void setup() {
		os = Mockito.mock(ObsService.class);
		as = Mockito.mock(AdministrationService.class);
		
		PowerMockito.mockStatic(Context.class);
		Mockito.when(Context.getAdministrationService()).thenReturn(as);
		Mockito.when(Context.getObsService()).thenReturn(os);
	}
	
	/**
	 * @see {@link FormSchemaBuilder#getSchema()}
	 */
	@SuppressWarnings("unchecked")
	@Test
	@Verifies(value = "should include schema segments from serializable complex obs handlers", method = "getSchema()")
	public void getSchema_shouldIncludeSchemaSegmentsFromSerializableComplexObsHandlers() throws Exception {
		final String schema1 = "<some schema segment />";
		final String schema2 = "<another schema segment />";
		Map<String, ComplexObsHandler> handlers = new HashMap<String, ComplexObsHandler>();
		handlers.put("TestHandler1", new ObsHandler(schema1));
		handlers.put("TestHandler2", new ObsHandler(schema2));
		Mockito.when(os.getHandlers()).thenReturn(handlers);
		
		Form form = new Form();
		form.setFormFields(Collections.EMPTY_SET);
		String schema = new FormSchemaBuilder(form).getSchema();
		Assert.assertTrue(schema.indexOf(schema1) > 0);
		Assert.assertTrue(schema.indexOf(schema2) > 0);
	}
	
	/**
	 * This is a test complex obs handler
	 */
	private class ObsHandler implements SerializableComplexObsHandler {
		
		private String schema;
		
		public ObsHandler(String schema) {
			this.schema = schema;
		}
		
		public String getSchema(String format) {
			return this.schema;
		}
		
		public Obs saveObs(Obs obs) throws APIException {
			return null;
		}
		
		public Obs getObs(Obs obs, String view) {
			return null;
		}
		
		public boolean purgeComplexData(Obs obs) {
			return false;
		}
		
		public String serializeFormData(String data) {
			return null;
		}
	}
}
