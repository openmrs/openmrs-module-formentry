package org.openmrs.module.formentry.test;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.formentry.FormEntryUtil;


public class FormEntryUtilTest {
	
	@Test
	public void shouldEncodeUtf8() throws Exception {
		Assert.assertEquals("HEIGHT - \\u00d0 - (CM)^", FormEntryUtil.encodeUTF8String("HEIGHT - Ð - (CM)^"));
	}
}
