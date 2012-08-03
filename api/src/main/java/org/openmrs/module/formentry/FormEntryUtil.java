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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Form;
import org.openmrs.FormResource;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeHandler;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.datatype.LongFreeTextDatatype;
import org.openmrs.util.FormConstants;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.web.attribute.handler.LongFreeTextFileUploadHandler;

/**
 *
 */
public class FormEntryUtil {
	
	private static Log log = LogFactory.getLog(FormEntryUtil.class);
	
	/**
	 * Cached directory where queue items are stored
	 * 
	 * @see #getFormEntryQueueDir()
	 */
	private static File formEntryQueueDir = null;
	
	/**
	 * Cached directory where gp says archive items are stored
	 * 
	 * @see #getFormEntryArchiveDir()
	 */
	private static String formEntryArchiveFileName = null;
	
	/**
	 * A FilenameFilter for xsl files.
	 */
	private static FilenameFilter xslFilenameFilter = null;
	
	/**
	 * Regex pattern for the end of the form in an XSL page (</body>)
	 */
	public static Pattern endOfXSLPattern = Pattern.compile("(</body>)");
	
	private static String defaultXslt;
	
	/**
	 * Expand the xsn defined by <code>xsnFileContents</code> into a temp dir The file returned by
	 * this method should be deleted after use.
	 * 
	 * @param xsnFileContents byte array of xsn file content data
	 * @return Directory in temp dir containing xsn contents
	 * @throws IOException
	 */
	public static File expandXsnContents(byte[] xsnFileContents) throws IOException {
		// copy the xsn contents to a temporary directory
		File tempXsnFromDatabaseDir = createTempDirectory("XSN-db-file");
		if (tempXsnFromDatabaseDir == null)
			throw new IOException("Failed to create temporary content directory");
		
		// copy the xsn contents to a new file
		File tmpXsnFromDatabaseFile = new File(tempXsnFromDatabaseDir, "tempContent.xsn");
		OutputStream out = new FileOutputStream(tmpXsnFromDatabaseFile);
		out.write(xsnFileContents);
		out.flush();
		out.close();
		
		String xsnFilePath = tmpXsnFromDatabaseFile.getAbsolutePath();
		
		File expandedContentsDir = null;
		try {
			expandedContentsDir = expandXsn(xsnFilePath);
		}
		finally {
			try {
				OpenmrsUtil.deleteDirectory(tempXsnFromDatabaseDir);
			}
			finally {
				// pass
			}
		}
		
		return expandedContentsDir;
	}
	
	/**
	 * Expand the xsn at <code>xsnFilePath</code> into a temp dir
	 * 
	 * @param xsnFilePath
	 * @return Directory in temp dir containing xsn contents
	 * @throws IOException
	 */
	public static File expandXsn(String xsnFilePath) throws IOException {
		File xsnFile = new File(xsnFilePath);
		if (!xsnFile.exists())
			return null;
		
		File tempDir = createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");
		
		StringBuffer cmdBuffer = new StringBuffer();
		
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
			
			// retrieve the cabextract path from the runtime properties
			String cabextLocation = FormEntryConstants.FORMENTRY_CABEXTRACT_LOCATION;
			if (cabextLocation == null)
				cabextLocation = "/usr/local/bin/cabextract";
			
			File cabextractExecutable = new File(cabextLocation);
			if (!cabextractExecutable.exists()) {
				log.warn("cabextract not found at "
				        + cabextLocation
				        + ", using cabextract from search path. SERIOUS: This may be a security violation! Please set the formentry.cabextract_location runtime property to the proper path");
				cabextLocation = "cabextract"; // ABK: hope to find it on the
				                               // path
			}
			
			cmdBuffer.append(cabextLocation + " -d ").append(tempDir.getAbsolutePath()).append(" ").append(xsnFilePath);
			execCmd(cmdBuffer.toString(), tempDir);
		} else {
			cmdBuffer.append("expand -F:* \"").append(xsnFilePath).append("\" \"").append(tempDir.getAbsolutePath())
			        .append("\"");
			execCmd(cmdBuffer.toString(), null);
		}
		
		return tempDir;
	}
	
	/**
	 * Finds a folder path within resources and returns it as a File
	 * 
	 * @should throw a FileNotFoundException if the folderPath does not exist
	 * @should return both directories and files
	 * @param resourcePath path to the resource
	 * @return file handle for the resource
	 * @throws IOException
	 */
	public static File getResourceFile(String resourcePath) throws IOException {
		
		log.debug("Getting URL directory: " + resourcePath);
		
		Class<FormEntryUtil> c = FormEntryUtil.class;
		
		// get the location of the starter documents
		URL url = c.getResource(resourcePath);
		if (url == null) {
			String err = "Could not open resource folder directory: " + resourcePath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		
		return OpenmrsUtil.url2file(url);
	}
	
	/**
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank XSN template to play
	 * with in Infopath. Should be used similar to
	 * <code>org.openmrs.module.formentry.FormEntryUtil.expandXsnContents(java.lang.String)</code>
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank XSN template to play
	 * with in Infopath. Should be used similar to
	 * <code>org.openmrs.formentry.FormEntryUtil.expandXsnContents(java.lang.String)</code>
	 * 
	 * @return File directory holding blank xsn contents
	 * @throws IOException
	 */
	public static File getExpandedStarterXSN() throws IOException {
		
		// temp directory to hold the new xsn contents
		File tempDir = FormEntryUtil.createTempDirectory("XSN-starter");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");
		
		// iterate over and copy each file in the given folder
		File starterDir = getResourceFile(FormEntryConstants.FORMENTRY_STARTER_XSN_FOLDER_PATH);
		for (File f : starterDir.listFiles()) {
			File newFile = new File(tempDir, f.getName());
			FileChannel in = null, out = null;
			try {
				in = new FileInputStream(f).getChannel();
				out = new FileOutputStream(newFile).getChannel();
				in.transferTo(0, in.size(), out);
			}
			finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
		}
		
		return tempDir;
	}
	
	/**
	 * Gets the current xsn file for a form. If the xsn is not found, the starter xsn is returned
	 * instead The second array value in the returned object is a pointer to a temporary directory
	 * containing the expanded xsn contents. This folder should be deleted after use
	 * 
	 * @param form
	 * @param defaultToStarter true/false whether or not the starter xsn is returned when no current
	 *            xsn is found
	 * @return objects array: [0]: InputStream to form's xsn file or starter xsn if none, [1]:
	 *         folder containing temporary expanded xsn files
	 * @throws IOException
	 */
	public static Object[] getCurrentXSN(Form form, boolean defaultToStarter) throws IOException {
		FormEntryService formEntryService = (FormEntryService) Context.getService(FormEntryService.class);
		
		// Find the form's xsn file data
		FormEntryXsn xsn = formEntryService.getFormEntryXsn(form);
		
		// The expanded the xsn
		File tempDir = null;
		
		if (xsn != null) {
			log.debug("Expanding xsn contents");
			tempDir = FormEntryUtil.expandXsnContents(xsn.getXsnData());
		} else if (defaultToStarter == true) {
			// use starter xsn as the
			log.debug("Using starter xsn");
			tempDir = FormEntryUtil.getExpandedStarterXSN();
		} else
			return new Object[] { null, tempDir };
		
		return new Object[] { compileXSN(form, tempDir), tempDir };
	}
	
	/**
	 * Modifies schema, template.xml, and sample data, defaults, urls in <code>tmpXSN</code>
	 * 
	 * @param form
	 * @param tempDir directory containing xsn files.
	 * @return
	 * @throws IOException
	 */
	private static FileInputStream compileXSN(Form form, File tempDir) throws IOException {
		// Get Constants
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME;
		String templateFilename = FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME;
		String sampleDataFilename = FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME;
		String defaultsFilename = FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME;
		String url = getFormAbsoluteUrl(form);
		
		// Generate the schema and template.xml
		FormXmlTemplateBuilder fxtb = new FormXmlTemplateBuilder(form, url);
		String template = fxtb.getXmlTemplate(false);
		String templateWithDefaultScripts = fxtb.getXmlTemplate(true);
		String schema = new FormSchemaBuilder(form).getSchema();
		
		// Generate and overwrite the schema
		File schemaFile = findFile(tempDir, schemaFilename);
		if (schemaFile == null)
			throw new IOException("Schema: '" + schemaFilename + "' cannot be null. Compiling xsn for form " + form);
		FileWriter schemaOutput = new FileWriter(schemaFile, false);
		schemaOutput.write(schema);
		schemaOutput.close();
		
		// replace template.xml with the generated xml
		File templateFile = findFile(tempDir, templateFilename);
		if (templateFile == null)
			throw new IOException("Template: '" + templateFilename + "' cannot be null. Compiling xsn for form " + form);
		FileWriter templateOutput = new FileWriter(templateFile, false);
		templateOutput.write(template);
		templateOutput.close();
		
		// replace defautls.xml with the xml template, including default scripts
		File defaultsFile = findFile(tempDir, defaultsFilename);
		if (defaultsFile == null)
			throw new IOException("Defaults: '" + defaultsFilename + "' cannot be null. Compiling xsn for form " + form);
		FileWriter defaultsOutput = new FileWriter(defaultsFile, false);
		defaultsOutput.write(templateWithDefaultScripts);
		defaultsOutput.close();
		
		// replace sampleData.xml with the generated xml
		File sampleDataFile = findFile(tempDir, sampleDataFilename);
		if (sampleDataFile == null)
			throw new IOException("Template: '" + sampleDataFilename + "' cannot be null. Compiling xsn for form " + form);
		FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
		sampleDataOutput.write(template);
		sampleDataOutput.close();
		
		FormEntryUtil.makeCab(tempDir, tempDir.getAbsolutePath(), "new.xsn");
		
		File xsn = findFile(tempDir, "new.xsn");
		if (xsn == null)
			throw new IOException("MakeCab has failed because the generated 'new.xsn' file in the temp directory '"
			        + tempDir + "' cannot be null. Compiling xsn for form " + form);
		
		FileInputStream xsnInputStream = new FileInputStream(xsn);
		return xsnInputStream;
	}
	
	/**
	 * Make an xsn (aka CAB file) with the contents of <code>tempDir</code>
	 * 
	 * @param tempDir
	 */
	public static void makeCab(File tempDir, String outputDirName, String outputFilename) {
		// """calls MakeCAB to make a CAB file from DDF in tempdir directory"""
		
		StringBuffer cmdBuffer = new StringBuffer();
		
		// Special case : Linux operating sytem uses lcab utility
		if (OpenmrsConstants.UNIX_BASED_OPERATING_SYSTEM) {
			
			String lcabLocation = FormEntryConstants.FORMENTRY_LCAB_LOCATION;
			if (lcabLocation == null)
				lcabLocation = "/usr/local/bin/lcab";
			
			File lcabExecutable = new File(lcabLocation);
			if (!lcabExecutable.exists()) {
				log.warn("lcab not found at "
				        + lcabLocation
				        + ", using lcab from search path. SERIOUS: This may be a security violation! Please set the formentry.lcab_location runtime property to the proper path.");
				lcabLocation = "lcab"; // ABK: not at the hard-coded location,
				                       // so hope to find it on the path
			}
			
			cmdBuffer.append(lcabLocation + " -rn ").append(tempDir).append(" ").append(outputDirName).append("/")
			        .append(outputFilename);
			
			// Execute command with working directory
			execCmd(cmdBuffer.toString(), tempDir);
		}
		// Otherwise, assume windows
		else {
			// create ddf
			FormEntryUtil.createDdf(tempDir, outputDirName, outputFilename);
			
			// Create makecab command
			cmdBuffer.append("makecab /F \"").append(tempDir.getAbsolutePath()).append("\\publish.ddf\"");
			
			// Execute command without working directory
			execCmd(cmdBuffer.toString(), null);
			
		}
		
	}
	
	/**
	 * Convenience method to execute the given command in an environment agnostic manner
	 * 
	 * @param cmd command to execute
	 * @param wd working directory (can be null)
	 * @return command output
	 */
	private static String execCmd(String cmd, File wd) {
		log.debug("executing command: " + cmd);
		StringBuffer out = new StringBuffer();
		try {
			// Needed to add support for working directory because of a linux
			// file system permission issue.
			// Could not create lcab.tmp file in default working directory
			// (jmiranda).
			Process p = (wd != null) ? Runtime.getRuntime().exec(cmd, null, wd) : Runtime.getRuntime().exec(cmd);
			
			// get the stdout
			out.append("Normal cmd output:\n");
			Reader reader = new InputStreamReader(p.getInputStream());
			BufferedReader input = new BufferedReader(reader);
			int readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char) readChar);
			}
			input.close();
			reader.close();
			
			// get the errout
			out.append("ErrorStream cmd output:\n");
			reader = new InputStreamReader(p.getErrorStream());
			input = new BufferedReader(reader);
			readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char) readChar);
			}
			input.close();
			reader.close();
			
			// wait for the thread to finish and get the exit value
			int exitValue = p.waitFor();
			
			if (log.isDebugEnabled())
				log.debug("Process exit value: " + exitValue);
			
		}
		catch (Exception e) {
			log.error("Error while executing command: '" + cmd + "'", e);
		}
		
		if (log.isDebugEnabled())
			log.debug("execCmd output: \n" + out.toString());
		
		return out.toString();
	}
	
	/**
	 * Create a temporary directory with the given prefix and a random suffix
	 * 
	 * @param prefix String to insert before the random generated filename
	 * @return New temp directory pointer
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix) throws IOException {
		String dirname = System.getProperty("java.io.tmpdir");
		if (dirname == null)
			throw new IOException("Cannot determine system temporary directory");
		
		File directory = new File(dirname);
		if (!directory.exists())
			throw new IOException("System temporary directory " + directory.getName() + " does not exist.");
		if (!directory.isDirectory())
			throw new IOException("System temporary directory " + directory.getName() + " is not really a directory.");
		
		File tempDir;
		do {
			String filename = prefix + System.currentTimeMillis();
			tempDir = new File(directory, filename);
		} while (tempDir.exists());
		
		if (!tempDir.mkdirs())
			throw new IOException("Could not create temporary directory '" + tempDir.getAbsolutePath() + "'");
		if (log.isDebugEnabled())
			log.debug("Successfully created temporary directory: " + tempDir.getAbsolutePath());
		
		tempDir.deleteOnExit();
		return tempDir;
	}
	
	/**
	 * Finds the given filename in the given dir
	 * 
	 * @param dir
	 * @param filename
	 * @return File or null if not found
	 */
	public static File findFile(File dir, String filename) {
		File file = null;
		for (File f : dir.listFiles()) {
			if (f.getName().equalsIgnoreCase(filename)) {
				file = f;
				break;
			}
		}
		return file;
	}
	
	/**
	 * Create ddf that the makeCab exe uses to compile the xsn
	 * 
	 * @param xsnDir
	 * @param outputDir
	 * @param outputFileName
	 */
	public static void createDdf(File xsnDir, String outputDir, String outputFileName) {
		String ddf = ";*** MakeCAB Directive file for " + outputFileName + "\n" + ".OPTION EXPLICIT			; generate errors\n"
		        + ".Set CabinetNameTemplate=" + outputFileName + "\n"
		        + ".set DiskDirectoryTemplate=CDROM	; all cabinets go in a single directory\n"
		        + ".Set CompressionType=MSZIP		; all files are compressed in cabinet files\n" + ".Set UniqueFiles=\"OFF\"\n"
		        + ".Set Cabinet=on\n" + ".Set DiskDirectory1=\"" + outputDir.replace("/", File.separator) // allow for either
		        // direction of
		        // slash
		        + "\"\n";
		
		for (File f : xsnDir.listFiles())
			ddf += "\"" + f.getPath() + "\"\n";
		
		log.debug("ddf = " + ddf);
		
		File ddfFile = new File(xsnDir, "publish.ddf");
		try {
			FileWriter out = new FileWriter(ddfFile);
			out.write(ddf);
			out.close();
		}
		catch (IOException e) {
			log.error("Could not create DDF file to generate XSN archive", e);
		}
	}
	
	public static String getFormUriExtension(Form form) {
		return ".xsn";
	}
	
	public static String getFormUri(Form form) {
		return FormUtil.getFormUriWithoutExtension(form) + getFormUriExtension(form);
	}
	
	public static String getFormAbsoluteUrl(Form form) {
		// int endOfDomain = requestURL.indexOf('/', 8);
		// String baseUrl = requestURL.substring(0, (endOfDomain > 8 ?
		// endOfDomain : requestURL.length()));
		String serverURL = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL,
		    FormEntryConstants.FORMENTRY_GP_SERVER_URL + " cannot be empty");
		String baseUrl = serverURL + FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + getFormUri(form);
	}
	
	public static String getFormSchemaNamespace(Form form) {
		String serverURL = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL,
		    FormEntryConstants.FORMENTRY_GP_SERVER_URL + " cannot be empty");
		String baseUrl = serverURL + FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + "schema/" + form.getFormId() + "-" + form.getBuild();
	}
	
	public static String getSolutionVersion(Form form) {
		String version = form.getVersion();
		if (version == null || version.length() < 1 || version.length() > 4)
			version = "1.0.0";
		int numDots, i;
		for (numDots = 0, i = 0; (i = version.indexOf('.', i + 1)) > 0; numDots++)
			;
		if (numDots < 2)
			for (i = numDots; i < 2; i++)
				version += ".0";
		if (form.getBuild() == null || form.getBuild() < 1 || form.getBuild() > 9999)
			form.setBuild(1);
		version += "." + form.getBuild();
		return version;
	}
	
	/**
	 * @deprecated use org.openmrs.util#FormUtil.conceptToString(Concept, Locale)
	 */
	public static String conceptToString(Concept concept, Locale locale) {
		// return org.openmrs.util.FormUtil.conceptToString(concept, locale);
		// TODO undeprecate this or change the name of the method to reflect the tuple it is generating
		return concept.getConceptId() + "^" + encodeUTF8String(concept.getName(locale).getName()) + "^"
		        + FormConstants.HL7_LOCAL_CONCEPT;
	}
	
	/**
	 * @deprecated use org.openmrs.util.FormUtil#drugToString(Drug)
	 */
	public static String drugToString(Drug drug) {
		return org.openmrs.util.FormUtil.drugToString(drug);
	}
	
	// max length of HL7 message control ID is 20
	private static final int FORM_UID_LENGTH = 20;
	
	/**
	 * Generates a uid in a length acceptable to hl7
	 * 
	 * @return relatively unique string
	 */
	public static String generateFormUid() {
		return OpenmrsUtil.generateUid(FORM_UID_LENGTH);
	}
	
	/**
	 * Gets the directory where the user specified their queues were being stored
	 * 
	 * @return directory in which to store queued items
	 */
	public static File getFormEntryQueueDir() {
		
		if (formEntryQueueDir == null) {
			AdministrationService as = Context.getAdministrationService();
			String folderName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_DIR,
			    FormEntryConstants.FORMENTRY_GP_QUEUE_DIR_DEFAULT);
			formEntryQueueDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
			if (log.isDebugEnabled())
				log.debug("Loaded formentry queue directory from global properties: " + formEntryQueueDir.getAbsolutePath());
		}
		
		return formEntryQueueDir;
	}
	
	/**
	 * Gets the directory where the user specified their archives were being stored
	 * 
	 * @param optional Date to specify the folder this should possibly be sorted into
	 * @return directory in which to store archived items
	 */
	public static File getFormEntryArchiveDir(Date d) {
		// cache the global property location so we don't have to hit the db
		// everytime
		if (formEntryArchiveFileName == null) {
			AdministrationService as = Context.getAdministrationService();
			formEntryArchiveFileName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR,
			    FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR_DEFAULT);
		}
		
		// replace %Y %M %D in the folderName with the date
		String folderName = replaceVariables(formEntryArchiveFileName, d);
		
		// get the file object for this potentially new file
		File formEntryArchiveDir = OpenmrsUtil.getDirectoryInApplicationDataDirectory(folderName);
		
		if (log.isDebugEnabled())
			log.debug("Loaded formentry archive directory from global properties: " + formEntryArchiveDir.getAbsolutePath());
		
		return formEntryArchiveDir;
	}
	
	/**
	 * Replaces %Y in the string with the four digit year. Replaces %M with the two digit month
	 * Replaces %D with the two digit day Replaces %w with week of the year Replaces %W with week of
	 * the month
	 * 
	 * @param str String filename containing variables to replace with date strings
	 * @return String with variables replaced
	 */
	public static String replaceVariables(String str, Date d) {
		
		Calendar calendar = Calendar.getInstance();
		if (d != null)
			calendar.setTime(d);
		
		int year = calendar.get(Calendar.YEAR);
		str = str.replace("%Y", Integer.toString(year));
		
		int month = calendar.get(Calendar.MONTH) + 1;
		String monthString = Integer.toString(month);
		if (month < 10)
			monthString = "0" + monthString;
		str = str.replace("%M", monthString);
		
		int day = calendar.get(Calendar.DATE);
		String dayString = Integer.toString(day);
		if (day < 10)
			dayString = "0" + dayString;
		str = str.replace("%D", dayString);
		
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		String weekString = Integer.toString(week);
		if (week < 10)
			weekString = "0" + week;
		str = str.replace("%w", weekString);
		
		int weekmonth = calendar.get(Calendar.WEEK_OF_MONTH);
		String weekmonthString = Integer.toString(weekmonth);
		if (weekmonth < 10)
			weekmonthString = "0" + weekmonthString;
		str = str.replace("%W", weekmonthString);
		
		return str;
	}
	
	/**
	 * @deprecated this method has been moved into the OpenmrsUtil class
	 * @see org.openmrs.util.OpenmrsUtil#getOutFile(File,Date,User)
	 */
	public static File getOutFile(File dir, Date date, User user) {
		return OpenmrsUtil.getOutFile(dir, date, user);
	}
	
	/**
	 * Writes the give fileContentst to the given outFile
	 * 
	 * @param fileContents string to write to the file
	 * @param outFile File to be overwritten with the given file contents
	 * @throws IOException on write exceptions
	 */
	public static void stringToFile(String fileContents, File outFile) throws IOException {
		FileWriter writer = new FileWriter(outFile);
		
		writer.write(fileContents);
		
		writer.close();
	}
	
	/**
	 * Creates a zip file in <code>xsnDir</code> containing the <code>filesToZip</code> The name of
	 * the dir is
	 * 
	 * @param xsnDir location to put the zip file
	 * @param zipName name of the zip file. will be prepended with a timestamp
	 * @param filesToZip list of files to zip into <code>zipName</code>
	 * @throws IOException if the directory can't be written to
	 */
	public static void moveToZipFile(File xsnDir, String zipName, List<File> filesToZip) throws IOException {
		
		if (filesToZip == null || filesToZip.size() < 1)
			return;
		
		// prepend a timestamp to the backup so we don't overwrite anything
		zipName = (new Date()).getTime() + zipName;
		
		if (!zipName.endsWith(".zip"))
			zipName = zipName + ".zip";
		
		File outFile = new File(xsnDir, zipName);
		if (!outFile.exists())
			outFile.createNewFile();
		
		FileOutputStream xsnDirOutStream = null;
		List<File> filesToDelete = new ArrayList<File>();
		
		try {
			xsnDirOutStream = new FileOutputStream(outFile);
			
			ZipOutputStream zos = new ZipOutputStream(xsnDirOutStream);
			ZipEntry zipEntry = null;
			
			for (File file : filesToZip) {
				
				try {
					// string xsn data
					String fileData = OpenmrsUtil.getFileAsString(file);
					
					byte[] uncompressedBytes = fileData.getBytes();
					
					// name this entry
					zipEntry = new ZipEntry(file.getName());
					
					// Add ZIP entry to output stream.
					zos.putNextEntry(zipEntry);
					
					// Transfer bytes from the formData to the ZIP file
					zos.write(uncompressedBytes, 0, uncompressedBytes.length);
					
					zos.closeEntry();
					
					filesToDelete.add(file);
				}
				catch (IOException io) {
					log.error("Unable to zip file: " + file.getAbsolutePath(), io);
				}
			}
			
			zos.close();
		}
		finally {
			if (xsnDirOutStream != null)
				xsnDirOutStream.close();
			
			for (File file : filesToDelete) {
				if (!file.delete())
					file.deleteOnExit();
			}
		}
	}
	
	/**
	 * The rebuilding process is basically just a download and reupload of the xsn. The point of
	 * rebuilding would be to get a new schema into the xsn or to get new concepts/concept answers
	 * into the form
	 * 
	 * @param form Form to rebuild the xsn for
	 */
	public static void rebuildXSN(Form form) throws IOException {
		Object[] streamAndDir = FormEntryUtil.getCurrentXSN(form, true);
		InputStream formStream = (InputStream) streamAndDir[0];
		File tempDir = (File) streamAndDir[1];
		
		if (formStream == null)
			throw new IOException("The formstream for form: " + form + " should not be null (but it is)");
		
		PublishInfoPath.publishXSN(formStream);
		
		try {
			formStream.close();
		}
		catch (IOException ioe) {}
		try {
			OpenmrsUtil.deleteDirectory(tempDir);
		}
		catch (IOException ioe) {}
	}
	
	/**
	 * Converts utf-8 characters into unicode escape characters
	 * 
	 * @param s the string to convert
	 * @return the string with characters converted to unicode escapes with backslashes
	 * @should encode Utf8 string
	 */
	public static String encodeUTF8String(String s) {
		
		try {
			BufferedReader reader = null;
			BufferedWriter writer = null;
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			try {
				reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(s.getBytes("UTF-8"))));
				writer = new BufferedWriter(new OutputStreamWriter(byteOutputStream, "ISO8859_1")); // or ASCII
				int temp;
				while ((temp = reader.read()) != -1) {
					if (temp <= 0x7f)
						writer.write(temp);
					else
						writer.write(toEscape((char) temp));
				}
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
			}
			finally {
				try {
					if (reader != null)
						reader.close();
					if (writer != null)
						writer.close();
				}
				catch (IOException ex) {
					;
				}
			}
			
			return byteOutputStream.toString("ISO8859-1");
		}
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			log.error("Unable to convert to unicode escape characters", e);
			return s;
		}
	}
	
	/**
	 * Helper method for the encodeUTF8String method above.
	 * 
	 * @param c
	 * @return
	 */
	private static String toEscape(char c) { // instead of this method
		                                     // charToHex() can be used
		int n = (int) c;
		String body = Integer.toHexString(n);
		// String body=charToHex(c); //instead of this the above can be used
		String zeros = "000";
		return ("\\u" + zeros.substring(0, 4 - body.length()) + body);
	} // end of method
	
	/**
	 * adds a widget to the end of every XSL file in a form and publishes it
	 * 
	 * @should throw an IOException if the widget does not exist
	 * @should use the starter XSN if no XSN currently exists for the form
	 * @should only increment the build by one digit
	 * @param form the form to be modified
	 * @param widgetName the name of the widget (should exist already in the application context)
	 * @return the modified form
	 * @throws IOException
	 */
	public static Form addWidgetToForm(Form form, String widgetName) throws IOException {
		FormEntryService formEntryService = (FormEntryService) Context.getService(FormEntryService.class);
		
		// get the widget's contents
		File widgetFolder = FormEntryUtil.getResourceFile(FormEntryConstants.FORMENTRY_INFOPATH_WIDGET_PATH);
		if (!OpenmrsUtil.folderContains(widgetFolder, widgetName))
			throw new IOException("cannot find widget folder for '" + widgetName + "'");
		
		widgetFolder = new File(widgetFolder.getAbsolutePath(), widgetName);
		if (!OpenmrsUtil.folderContains(widgetFolder, FormEntryConstants.FORMENTRY_INFOPATH_WIDGET_FILENAME))
			throw new IOException("cannot find widget file for '" + widgetName + "'");
		
		File widgetFile = new File(widgetFolder.getAbsolutePath(), FormEntryConstants.FORMENTRY_INFOPATH_WIDGET_FILENAME);
		String widgetContents = OpenmrsUtil.getFileAsString(widgetFile);
		
		// get the XSN extracted to a temporary location
		FormEntryXsn xsn = formEntryService.getFormEntryXsn(form);
		File tempDir = null;
		if (xsn == null)
			tempDir = FormEntryUtil.getExpandedStarterXSN();
		else
			tempDir = FormEntryUtil.expandXsnContents(xsn.getXsnData());
		
		// iterate over the XSL files
		String[] xslFilenames = tempDir.list(getXslFilenameFilter());
		for (String xslFilename : xslFilenames) {
			File xslFile = new File(tempDir.getAbsolutePath(), xslFilename);
			try {
				FormEntryUtil.addWidgetToXSLFile(widgetContents, xslFile);
			}
			catch (FileNotFoundException e) {
				log.error("update of relationship widget in \"" + xslFilename + "\" failed, because: " + e);
				e.printStackTrace();
			}
			catch (IOException e) {
				log.error("update of relationship widget in \"" + xslFilename + "\" failed, because: " + e);
				e.printStackTrace();
			}
		}
		
		// repackage the XSN
		InputStream newXSN = FormEntryUtil.compileXSN(form, tempDir);
		
		// publish the modified XSN
		form = PublishInfoPath.publishXSN(newXSN, form);
		
		// close the stream
		try {
			newXSN.close();
		}
		catch (IOException ioe) {}
		
		// delete the temp folder
		try {
			OpenmrsUtil.deleteDirectory(tempDir);
		}
		catch (IOException ioe) {}
		
		return form;
	}
	
	/**
	 * adds a widget before the bottom of the <body/> of a XSL file
	 * 
	 * @should throw an IOException if the XSL file is not found
	 * @should inject the widget's content directly before the body tag of each XSL in the form
	 * @param widget the contents of the widget to inject
	 * @param xslFile the XSL file to update
	 * @throws IOException
	 */
	private static void addWidgetToXSLFile(String widget, File xslFile) throws IOException {
		
		BufferedReader xslReader = new BufferedReader(new FileReader(xslFile));
		File tmpXslFile = File.createTempFile("infopath", ".xsltmp", xslFile.getParentFile());
		PrintWriter tmpXslWriter = new PrintWriter(new FileWriter(tmpXslFile));
		
		// drop the widget right before the </body> tag
		String line = xslReader.readLine();
		while (line != null) {
			Matcher m = endOfXSLPattern.matcher(line);
			if (m.find()) {
				tmpXslWriter.println(widget);
			}
			tmpXslWriter.println(line);
			line = xslReader.readLine();
		}
		
		// swap files
		tmpXslWriter.close();
		xslReader.close();
		xslFile.delete();
		if (!tmpXslFile.renameTo(xslFile)) {
			throw new IOException("Unable to rename xsl file from " + tmpXslFile.getAbsolutePath() + " to "
			        + xslFile.getAbsolutePath());
		}
	}
	
	/**
	 * Lazy factory method of xslFilenameFilter. STOLEN from PublishInfoPath
	 * 
	 * @return a cached FilenameFilter for *.xsl files
	 */
	private static FilenameFilter getXslFilenameFilter() {
		if (xslFilenameFilter == null) {
			xslFilenameFilter = new FilenameFilter() {
				
				public boolean accept(File dir, String name) {
					return name.endsWith("xsl");
				}
			};
		}
		return xslFilenameFilter;
	}
	
	/**
	 * Convenience method that returns the form's xslt
	 * 
	 * @param form the {@link Form} object
	 * @return the xslt text
	 * @should return the xslt for the form
	 * @should return the default xslt if the form has no custom one
	 */
	public static String getFormXslt(Form form) {
		String xslt = getXsltOrTemplate(form, true);
		if (StringUtils.isNotBlank(xslt))
			return xslt;
		
		return getDefaultXslt();
	}
	
	/**
	 * Convenience method that returns the form's template
	 * 
	 * @param form the {@link Form} object
	 * @return the template text
	 * @should return null if the form has no template resource
	 */
	public static String getFormTemplate(Form form) {
		return getXsltOrTemplate(form, false);
	}
	
	/**
	 * Adds the specified resource to the specified form and saved it to the database
	 * 
	 * @param form the {@link Form} object
	 * @param resource the resource to save
	 * @param resourceNameSuffix the resource name suffix
	 * @param handler TODO
	 * @should save the form resource to the database
	 * @should not add an xslt that is the same as the default
	 */
	@SuppressWarnings("rawtypes")
	public static void saveXsltorTemplateFormResource(Form form, String resource, String resourceName,
	                                                  CustomDatatypeHandler handler) {
		//If this is an xslt and is the same as the default, ignore it
		if (FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME.equals(resourceName) && StringUtils.isNotBlank(resource)
		        && resource.equals(getDefaultXslt())) {
			return;
		}
		
		try {
			FormResource formResource = new FormResource();
			formResource.setForm(form);
			formResource.setName(resourceName);
			formResource.setDatatypeClassname(LongFreeTextDatatype.class.getName());
			if (handler == null)
				formResource.setPreferredHandlerClassname(LongFreeTextFileUploadHandler.class.getName());
			formResource.setValue(resource);
			Context.getFormService().saveFormResource(formResource);
		}
		catch (Exception e) {
			log.error("Error while saving form resource:", e);
		}
	}
	
	/**
	 * Gets the default xslt
	 * 
	 * @return the xslt text
	 * @should return the default xslt
	 */
	public static String getDefaultXslt() {
		if (defaultXslt == null) {
			try {
				defaultXslt = IOUtils.toString(FormEntryUtil.class.getClassLoader().getResourceAsStream(
				    FormEntryConstants.FORMENTRY_DEFAULT_XSLT_FILENAME));
			}
			catch (IOException e) {
				throw new APIException("Failed to load the default xslt:", e);
			}
		}
		
		return defaultXslt;
	}
	
	/**
	 * Gets a an xslt or template form resource with the specified resource name suffix, the for
	 * 
	 * @param form the form
	 * @param getXslt specifies if we are getting an xslt or template
	 * @return the resource
	 */
	@SuppressWarnings({ "rawtypes" })
	private static String getXsltOrTemplate(Form form, boolean getXslt) {
		String resourceName = (getXslt) ? FormEntryConstants.FORMENTRY_XSLT_FORM_RESOURCE_NAME
		        : FormEntryConstants.FORMENTRY_TEMPLATE_FORM_RESOURCE_NAME;
		
		FormResource resource = Context.getFormService().getFormResource(form, resourceName);
		if (resource != null) {
			CustomDatatype datatype = CustomDatatypeUtil.getDatatype(resource);
			if (datatype != null) {
				return ((LongFreeTextDatatype) datatype).fromReferenceString(resource.getValueReference());
			}
		}
		
		return null;
	}
}
