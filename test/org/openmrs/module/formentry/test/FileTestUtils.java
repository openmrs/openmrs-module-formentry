package org.openmrs.module.formentry.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

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
}
