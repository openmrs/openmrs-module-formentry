package org.openmrs.module.formentry.test;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmrs.module.formentry.PublishInfoPath;

public class PublishInfoPathTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	@Test
	public void shouldMatchQuotedConceptNameHl7Messages() {
		final String CONCEPT_ID = "1107";
		final String CONCEPT_TEXT = "NONE";
		final String HL7_TO_BE_UPDATED = "\"" + CONCEPT_ID + "^" + CONCEPT_TEXT + "^99DCT\"";

    	Matcher m = PublishInfoPath.hl7ConceptNamePattern.matcher(HL7_TO_BE_UPDATED);
    	
    	assertTrue(m.find());
    	assertEquals(CONCEPT_ID, m.group(2));
    	assertEquals(CONCEPT_TEXT, m.group(3));
	}
	
	@Test
	public void shouldMatchHl7MessagesWithSpacesInName() {
		final String HL7_TO_BE_UPDATED = "\"2124^2RHZ / 4RH^99DCT\"";

    	Matcher m = PublishInfoPath.hl7ConceptNamePattern.matcher(HL7_TO_BE_UPDATED);
    	
    	assertTrue(m.find());
	}
	
	/**
	 * The XSL update should match HL7 formatted messages with 
	 * concept values.
	 * 
	 */
	@Test
	public void shouldMatchWithinRadioButtonSelectionWhenUpdatingXsl() {
		final String HL7_TO_BE_UPDATED = "<div><input class=\"xdBehavior_Boolean\" title=\"\" type=\"radio\" name=\"{generate-id(obs/actual_treatment/current_antitb_holder/tb_schema/value)}\" xd:xctname=\"OptionButton\" xd:CtrlId=\"CTRL1895\" tabIndex=\"0\" xd:binding=\"obs/actual_treatment/current_antitb_holder/tb_schema/value\" xd:boundProp=\"xd:value\" xd:onValue=\"2124^2RHZ / 4RH^99DCT\">";
		final String EXPECTED_MATCH_CONCEPT_ID = "2124";

    	Matcher m = PublishInfoPath.hl7ConceptNamePattern.matcher(HL7_TO_BE_UPDATED);
    	
    	assertTrue(m.find());
    	
    	assertEquals(EXPECTED_MATCH_CONCEPT_ID, m.group(2));
	}

	/**
	 * During rebuilding of the XSN, a regex pattern is used to 
	 * identify HL7 messages with concepts. A concept-name component
	 * is appended to the messagem, using the default locale.  
	 * 
	 * It should not match against HL7 messages which already have
	 * a concept-name.
	 */
	@Test
	public void shouldIgnoreExistingConceptNamesWhenUpdatingXsl() {
		final String HL7_WITH_CONCEPT_NAME = "\"2124^2RHZ / 4RH^99DCT^2292^2RHZ / 4RH^99DCT\"";

    	Matcher m = PublishInfoPath.hl7ConceptNamePattern.matcher(HL7_WITH_CONCEPT_NAME);
    	
    	assertFalse(m.find());
	}
	
}
