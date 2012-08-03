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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.Form;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Contains tests for methods in {@link FormEntryUtil}
 */
public class FormEntryUtilTest extends BaseModuleContextSensitiveTest {
	
	private static final String EXTRA_FORM_AND_RESOURCES = "extraFormsAndResources.xml";
	
	private static String defaultXslt;
	
	FormService service;
	
	@BeforeClass
	public static void setDefaultXslt() throws IOException {
		defaultXslt = FormEntryUtil.getDefaultXslt();
	}
	
	@Before
	public void setup() throws IOException {
		service = Context.getFormService();
	}
	
	/**
	 * @see {@link FormEntryUtil#encodeUTF8String(String)}
	 */
	@Test
	@Verifies(value = "should encode Utf8 string", method = "encodeUTF8String(String)")
	public void encodeUTF8String_shouldEncodeUtf8String() throws Exception {
		Assert.assertEquals("HEIGHT - \\u00d0 - (CM)^", FormEntryUtil.encodeUTF8String("HEIGHT - Ð - (CM)^"));
	}
	
	/**
	 * @see {@link FormEntryUtil#getDefaultXslt()}
	 */
	@Test
	@Verifies(value = "should return the default xslt", method = "getDefaultXslt()")
	public void getDefaultXslt_shouldReturnTheDefaultXslt() throws Exception {
		Assert.assertTrue(StringUtils.isNotBlank(FormEntryUtil.getDefaultXslt()));
	}
	
	/**
	 * @see {@link FormEntryUtil#getFormXslt(Form)}
	 */
	@Test
	@Verifies(value = "should return the xslt for the form", method = "getFormXslt(Form)")
	public void getFormXslt_shouldReturnTheXsltForTheForm() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		Assert.assertEquals("xslt text", FormEntryUtil.getFormXslt(service.getForm(1)));
	}
	
	/**
	 * @see {@link FormEntryUtil#getFormXslt(Form)}
	 */
	@Test
	@Verifies(value = "should return the default xslt if the form has no custom one", method = "getFormXslt(Form)")
	public void getFormXslt_shouldReturnTheDefaultXsltIfTheFormHasNoCustomOne() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		Assert.assertEquals(defaultXslt, FormEntryUtil.getFormXslt(service.getForm(10)));
	}
	
	/**
	 * @see {@link FormEntryUtil#getFormTemplate(Form)}
	 */
	@Test
	@Verifies(value = "should return the template for the form", method = "getFormTemplate(Form)")
	public void getFormTemplate_shouldReturnTheTemplateForTheForm() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		Assert.assertEquals("template text", FormEntryUtil.getFormTemplate(service.getForm(1)));
	}
	
	/**
	 * @see {@link FormEntryUtil#getFormTemplate(Form)}
	 */
	@Test
	@Verifies(value = "should return null if the form has no template resource", method = "getFormTemplate(Form)")
	public void getFormTemplate_shouldReturnNullIfTheFormHasNoTemplateResource() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		Assert.assertNull(FormEntryUtil.getFormTemplate(service.getForm(10)));
	}
	
	/**
	 * @see {@link FormEntryUtil#saveXsltorTemplateFormResource(Form,String,String, CustomDatatypeHandler)}
	 */
	@Test
	@Verifies(value = "should not add an xslt that is the same as the default", method = "saveXsltorTemplateFormResource(Form,String,String)")
	public void saveXsltorTemplateFormResource_shouldNotAddAnXsltThatIsTheSameAsTheDefault() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		final Form form = service.getForm(1);
		final String originalXlst = FormEntryUtil.getFormXslt(form);
		//sanity check
		Assert.assertNotSame(defaultXslt, originalXlst);
		
		FormEntryUtil.saveXsltorTemplateFormResource(form, defaultXslt,
		    FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME_SUFFIX, null);
		//shouldn't have changed
		Assert.assertNotSame(defaultXslt, FormEntryUtil.getFormXslt(form));
	}
	
	/**
	 * @see {@link FormEntryUtil#saveXsltorTemplateFormResource(Form,String,String, CustomDatatypeHandler)}
	 */
	@Test
	@Verifies(value = "should save the form resource to the database", method = "saveXsltorTemplateFormResource(Form,String,String)")
	public void saveXsltorTemplateFormResource_shouldSaveTheFormResourceToTheDatabase() throws Exception {
		executeDataSet(EXTRA_FORM_AND_RESOURCES);
		final Form form = service.getForm(10);
		//sanity check to ensure the form has no existing custom xslt resource
		Assert.assertSame(defaultXslt, FormEntryUtil.getFormXslt(form));
		String newXslt = "New test xslt";
		FormEntryUtil.saveXsltorTemplateFormResource(form, newXslt,
		    FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME_SUFFIX, null);
		//shouldn't have changed
		Assert.assertSame(newXslt, FormEntryUtil.getFormXslt(form));
	}
}
