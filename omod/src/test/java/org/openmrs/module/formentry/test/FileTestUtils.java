package org.openmrs.module.formentry.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openmrs.module.formentry.PublishInfoPath;

public class FileTestUtils {


	/**
	 * Compares all the files within two directories.
	 * 
	 * Directories are expected to have the same filenames.
	 * 
	 * Files with the same name are expected to have
	 * matching data.
	 * 
	 * @param expected expected contents
	 * @param actual actual contents
	 */
	public static void assertContentEquals(File expected, File actual) {
		if (expected.isDirectory()) {
			assertTrue(actual.exists());
			assertTrue(actual.isDirectory());	
			
			List<String> expectedFilenames = Arrays.asList(expected.list());
			List<String> actualFilenames = Arrays.asList(actual.list());
			
			for (String expectedFilename : expectedFilenames) {
				assertTrue("missing expected filename \"" + expectedFilename + "\" in directory \"" + actual.getPath() + "\"",
						actualFilenames.contains(expectedFilename));
				assertContentEquals(new File(expected, expectedFilename), new File(actual, expectedFilename));
			}
		}
	}
	
	public static void filteredCopy(File from, File to, FileFilter filtered) throws IOException {

		if (filtered.accept(from)) {
			if (from.isDirectory()) {
				if (!to.exists()) {
					to.mkdir();
				}
				for (String child : from.list()) {
					filteredCopy(new File(from, child), new File(to, child), filtered);
				}
			} else {
				FileUtils.copyFile(from, to);
			}
		}
	}
	
	   @Test
	    public void shouldRemovePreciseNameFromHl7() throws Exception {
	        String input = "</xsl:attribute>\n" + 
	            "<xsl:if test=\"obs/history_of_present_illness/patient_already_seen_by_traditional_practitioner/value=&quot;1065^YES^99DCT^1102^YES^99DCT&quot;\">\n" +
	            "<xsl:attribute name=\"CHECKED\">CHECKED</xsl:attribute>";
	        String correctOutput = "</xsl:attribute>\n" + 
	            "<xsl:if test=\"obs/history_of_present_illness/patient_already_seen_by_traditional_practitioner/value=&quot;1065^YES^99DCT&quot;\">\n" +
	            "<xsl:attribute name=\"CHECKED\">CHECKED</xsl:attribute>";
	        StringWriter sw = new StringWriter();
	        PublishInfoPath.removeConceptNamesInXslHelper(new BufferedReader(new StringReader(input)), new PrintWriter(sw));
	        System.out.println("We got: \n" + sw.toString());
	        assertEquals(correctOutput, sw.toString().trim());
	    }
}
