package org.openmrs.module.formentry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Form;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryUtil {

	private static Log log = LogFactory.getLog(FormEntryUtil.class);
	
	/**
	 * Cached directory where queue items are stored
	 * @see #getFormEntryQueueDir()
	 */
	private static File formEntryQueueDir = null;
	
	/**
	 * Cached directory where gp says archive items are stored
	 * @see #getFormEntryArchiveDir()
	 */
	private static String formEntryArchiveFileName = null;
	

	/**
	 * Expand the xsn defined by <code>xsnFileContents</code> into a temp dir
	 * 
	 * The file returned by this method should be deleted after use.
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

		if (OpenmrsConstants.OPERATING_SYSTEM_LINUX
				.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM)) {
			
			// retrieve the cabextract path from the global properties
			String cabextLocation = Context.getAdministrationService().getGlobalProperty(
					FormEntryConstants.FORMENTRY_GP_CABEXTRACT_LOCATION, 
					"/usr/bin/cabextract");
			
			cmdBuffer.append(cabextLocation + " -d ").append(
					tempDir.getAbsolutePath()).append(" ").append(xsnFilePath);
			execCmd(cmdBuffer.toString(), tempDir);
		} 
		else if (OpenmrsConstants.OPERATING_SYSTEM_FREEBSD.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM)) {
			
			// retrieve the cabextract path from the global properties
			String cabextLocation = Context.getAdministrationService().getGlobalProperty(
					FormEntryConstants.FORMENTRY_GP_CABEXTRACT_LOCATION, 
					"/usr/local/bin/cabextract");
			
			cmdBuffer.append(cabextLocation + " -d ").append(tempDir.getAbsolutePath()).append(" ").append(xsnFilePath);
			execCmd(cmdBuffer.toString(), tempDir);
		}
		else {
			cmdBuffer.append("expand -F:* \"").append(xsnFilePath).append(
					"\" \"").append(tempDir.getAbsolutePath()).append("\"");
			execCmd(cmdBuffer.toString(), null);
		}

		return tempDir;
	}
		

	/**
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank XSN template
	 * to play with in Infopath.  Should be used similar to 
	 * <code>org.openmrs.module.formentry.FormEntryUtil.expandXsnContents(java.lang.String)</code>
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank
	 * XSN template to play with in Infopath. Should be used similar to
	 * <code>org.openmrs.formentry.FormEntryUtil.expandXsnContents(java.lang.String)</code>
	 * 
	 * @return File directory holding blank xsn contents
	 * @throws IOException
	 */
	public static File getExpandedStarterXSN() throws IOException {

		String xsnFolderPath = FormEntryConstants.FORMENTRY_STARTER_XSN_FOLDER_PATH;
		log.debug("Getting starter XSN contents: " + xsnFolderPath);

		Class c = FormEntryUtil.class;
		/*
		URL url = c.getResource(xsnFolderPath);
		File xsnFolder = null;
		try {
			log.error("url.getFile: " + url.getFile());
			log.error("xsnFolderPath - : " + xsnFolderPath);
			xsnFolder = OpenmrsUtil.url2file(url);
		}
		catch (Exception e) {
			String err = "Unable to open starter xsn: " + xsnFolderPath + " : " + url;
			log.error(err, e);
			throw new IOException(err);
		}
		
		if (xsnFolder == null || !xsnFolder.exists()) {
			String err = "Could not open starter xsn folder directory: " + xsnFolderPath;
			if (xsnFolder != null) err += ". Absolute path: " + xsnFolder.getAbsolutePath();
			log.error(err);
			throw new FileNotFoundException(err);
		}
		*/

		// get the location of the starter documents
		URL url = c.getResource(xsnFolderPath);
		if (url == null) {
			String err = "Could not open starter xsn folder directory: " + xsnFolderPath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		
		// temp directory to hold the new xsn contents
		File tempDir = FormEntryUtil.createTempDirectory("XSN-starter");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");

		// iterate over and copy each file in the given folder
		File starterDir = OpenmrsUtil.url2file(url);
		for (File f : starterDir.listFiles()) {
			File newFile = new File(tempDir, f.getName());
			FileChannel in = null, out = null;
			try {
				in = new FileInputStream(f).getChannel();
				out = new FileOutputStream(newFile).getChannel();
				in.transferTo(0, in.size(), out);
			} finally {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
			}
		}
		
		return tempDir;
	}
	
	/**
	 * Gets the current xsn file for a form. If the xsn is not found, the
	 * starter xsn is returned instead
	 * 
	 * The second array value in the returned object is a pointer to a temporary
	 * directory containing the expanded xsn contents.  This folder should be 
	 * deleted after use
	 * 
	 * @param form
	 * @param defaultToStarter true/false whether or not the starter xsn is returned when no current xsn is found
	 * @return objects array: [0]: InputStream to form's xsn file or starter xsn if none, [1]: folder containing temporary expanded xsn files
	 * @throws IOException
	 */
	public static Object[] getCurrentXSN(Form form, boolean defaultToStarter) throws IOException {
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		// Find the form's xsn file data
		FormEntryXsn xsn = formEntryService.getFormEntryXsn(form);
		
		// The expanded the xsn
		File tempDir = null;

		if (xsn != null) {
			log.debug("Expanding xsn contents");
			tempDir = FormEntryUtil.expandXsnContents(xsn.getXsnData());
		}
		else if (defaultToStarter == true) {
			// use starter xsn as the
			log.debug("Using starter xsn");
			tempDir = FormEntryUtil.getExpandedStarterXSN();
		}
		else
			return new Object[] {null, tempDir};
		
		return new Object[] {compileXSN(form, tempDir), tempDir};
	}

	/**
	 * Returns a .xsn file compiled from the starter data set
	 * 
	 * @param form
	 * @return .xsn file
	 * @throws IOException
	 */
//	public static FileInputStream getStarterXSN(Form form) throws IOException {
//		File tmpXSN = FormEntryUtil.getExpandedStarterXSN();
//		return compileXSN(form, tmpXSN);
//	}

	/**
	 * Modifies schema, template.xml, and sample data, defaults, urls in
	 * <code>tmpXSN</code>
	 * 
	 * @param form
	 * @param tempDir
	 *            directory containing xsn files.
	 * @return
	 * @throws IOException
	 */
	private static FileInputStream compileXSN(Form form, File tempDir)
			throws IOException {
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
			throw new IOException("Schema: '" + schemaFilename
					+ "' cannot be null. Compiling xsn for form " + form);
		FileWriter schemaOutput = new FileWriter(schemaFile, false);
		schemaOutput.write(schema);
		schemaOutput.close();

		// replace template.xml with the generated xml
		File templateFile = findFile(tempDir, templateFilename);
		if (templateFile == null)
			throw new IOException("Template: '" + templateFilename
					+ "' cannot be null. Compiling xsn for form " + form);
		FileWriter templateOutput = new FileWriter(templateFile, false);
		templateOutput.write(template);
		templateOutput.close();

		// replace defautls.xml with the xml template, including default scripts
		File defaultsFile = findFile(tempDir, defaultsFilename);
		if (defaultsFile == null)
			throw new IOException("Defaults: '" + defaultsFilename
					+ "' cannot be null. Compiling xsn for form " + form);
		FileWriter defaultsOutput = new FileWriter(defaultsFile, false);
		defaultsOutput.write(templateWithDefaultScripts);
		defaultsOutput.close();

		// replace sampleData.xml with the generated xml
		File sampleDataFile = findFile(tempDir, sampleDataFilename);
		if (sampleDataFile == null)
			throw new IOException("Template: '" + sampleDataFilename
					+ "' cannot be null. Compiling xsn for form " + form);
		FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
		sampleDataOutput.write(template);
		sampleDataOutput.close();

		FormEntryUtil.makeCab(tempDir, tempDir.getAbsolutePath(), "new.xsn");

		File xsn = findFile(tempDir, "new.xsn");
		if (xsn == null)
			throw new IOException("MakeCab has failed because the generated 'new.xsn' file in the temp directory '" + tempDir
					+ "' cannot be null. Compiling xsn for form " + form);
		
		FileInputStream xsnInputStream = new FileInputStream(xsn);
		return xsnInputStream;
	}

	/**
	 * Make an xsn (aka CAB file) with the contents of <code>tempDir</code>
	 * 
	 * @param tempDir
	 */
	public static void makeCab(File tempDir, String outputDirName,
			String outputFilename) {
		// """calls MakeCAB to make a CAB file from DDF in tempdir directory"""

		StringBuffer cmdBuffer = new StringBuffer();

		// Special case : Linux operating sytem uses lcab utility
		if (OpenmrsConstants.OPERATING_SYSTEM_LINUX.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM) || 
				(OpenmrsConstants.OPERATING_SYSTEM_FREEBSD.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM))) {
			
			String lcabLocation = Context.getAdministrationService().getGlobalProperty(
								FormEntryConstants.FORMENTRY_GP_LCAB_LOCATION, 
								"/usr/local/bin/lcab");
			
			cmdBuffer.append(lcabLocation + " -rn ").append(tempDir)
					.append(" ").append(outputDirName).append("/").append(
							outputFilename);

			// Execute command with working directory
			execCmd(cmdBuffer.toString(), tempDir);
		}
		// Otherwise, assume windows
		else {
			// create ddf
			FormEntryUtil.createDdf(tempDir, outputDirName, outputFilename);

			// Create makecab command
			cmdBuffer.append("makecab /F \"").append(tempDir.getAbsolutePath())
					.append("\\publish.ddf\"");

			// Execute command without working directory
			execCmd(cmdBuffer.toString(), null);

		}

	}

	/**
	 * 
	 * @param cmd
	 *            command to execute
	 * @param wd
	 *            working directory
	 * @return
	 */
	private static String execCmd(String cmd, File wd) {
		log.debug("executing command: " + cmd);
		StringBuffer out = new StringBuffer();
		try {
			// Needed to add support for working directory because of a linux
			// file system permission issue.
			// Could not create lcab.tmp file in default working directory
			// (jmiranda).
			Process p = (wd != null) ? Runtime.getRuntime().exec(cmd, null, wd)
					: Runtime.getRuntime().exec(cmd);
			
			out.append("Normal cmd output:\n");
			Reader reader = new InputStreamReader(p.getInputStream());
			BufferedReader input = new BufferedReader(reader);
			int readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char)readChar);
				}
			input.close();
			reader.close();
			
			out.append("ErrorStream cmd output:\n");
			reader = new InputStreamReader(p.getErrorStream());
			input = new BufferedReader(reader);
			readChar = 0;
			while ((readChar = input.read()) != -1) {
				out.append((char)readChar);
				}
			input.close();
			reader.close();
			
			log.debug("Process exit value: " + p.exitValue());
			
		} catch (Exception e) {
			log.error("Error while executing command: '" + cmd + "'", e);
		}	
		log.debug("execCmd output: \n" + out.toString());
		return out.toString();
	}

	/**
	 * Create a temporary directory with the given prefix and a random suffix
	 * 
	 * @param prefix
	 * @return New temp directory pointer
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix) throws IOException {
		String dirname = System.getProperty("java.io.tmpdir");
		if (dirname == null)
			throw new IOException("Cannot determine system temporary directory");

		File directory = new File(dirname);
		if (!directory.exists())
			throw new IOException("System temporary directory "
					+ directory.getName() + " does not exist.");
		if (!directory.isDirectory())
			throw new IOException("System temporary directory "
					+ directory.getName() + " is not really a directory.");

		File tempDir;
		do {
			String filename = prefix + System.currentTimeMillis();
			tempDir = new File(directory, filename);
		} while (tempDir.exists());

		if (!tempDir.mkdirs())
			throw new IOException("Could not create temporary directory '"
					+ tempDir.getAbsolutePath() + "'");
		if (log.isDebugEnabled())
			log.debug("Successfully created temporary directory: "
					+ tempDir.getAbsolutePath());

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
	public static void createDdf(File xsnDir, String outputDir,
			String outputFileName) {
		String ddf = ";*** MakeCAB Directive file for "
				+ outputFileName
				+ "\n"
				+ ".OPTION EXPLICIT			; generate errors\n"
				+ ".Set CabinetNameTemplate="
				+ outputFileName
				+ "\n"
				+ ".set DiskDirectoryTemplate=CDROM	; all cabinets go in a single directory\n"
				+ ".Set CompressionType=MSZIP		; all files are compressed in cabinet files\n"
				+ ".Set UniqueFiles=\"OFF\"\n" + ".Set Cabinet=on\n"
				+ ".Set DiskDirectory1=\""
				+ outputDir.replace("/", File.separator) // allow for either
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
		} catch (IOException e) {
			log.error("Could not create DDF file to generate XSN archive", e);
		}
	}

	public static String getFormUriWithoutExtension(Form form) {
		return String.valueOf(form.getFormId());
	}

	public static String getFormUriExtension(Form form) {
		return ".xsn";
	}

	public static String getFormUri(Form form) {
		return getFormUriWithoutExtension(form) + getFormUriExtension(form);
	}

	public static String getFormAbsoluteUrl(Form form) {
		// int endOfDomain = requestURL.indexOf('/', 8);
		// String baseUrl = requestURL.substring(0, (endOfDomain > 8 ?
		// endOfDomain : requestURL.length()));
		String serverURL = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL, FormEntryConstants.FORMENTRY_GP_SERVER_URL + " cannot be empty");
		String baseUrl = serverURL + FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + getFormUri(form);
	}

	public static String getFormSchemaNamespace(Form form) {
		String serverURL = Context.getAdministrationService().getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL, FormEntryConstants.FORMENTRY_GP_SERVER_URL + " cannot be empty");
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
		if (form.getBuild() == null || form.getBuild() < 1
				|| form.getBuild() > 9999)
			form.setBuild(1);
		version += "." + form.getBuild();
		return version;
	}
	
	public static String conceptToString(Concept concept, Locale locale) {
		return concept.getConceptId() + "^" + concept.getName(locale).getName()
				+ "^" + FormEntryConstants.HL7_LOCAL_CONCEPT;
	}

	public static String drugToString(Drug drug) {
		return drug.getDrugId() + "^" + drug.getName() + "^"
				+ FormEntryConstants.HL7_LOCAL_DRUG;
	}
	
	// max length of HL7 message control ID is 20
	private static final int FORM_UID_LENGTH = 20;

	public static String generateFormUid() {
		StringBuffer sb = new StringBuffer(FORM_UID_LENGTH);
		for (int i = 0; i < FORM_UID_LENGTH; i++) {
			int ch = (int) (Math.random() * 62);
			if (ch < 10) // 0-9
				sb.append(ch);
			else if (ch < 36) // a-z
				sb.append((char) (ch - 10 + 'a'));
			else
				sb.append((char) (ch - 36 + 'A'));
		}
		return sb.toString();
	}

	/**
     * Gets the directory where the user specified their queues were being stored
     * 
     * @return directory in which to store queued items
     */
    public static File getFormEntryQueueDir() {
    	
    	if (formEntryQueueDir == null) {
    		AdministrationService as = Context.getAdministrationService();
    		String folderName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_DIR, FormEntryConstants.FORMENTRY_GP_QUEUE_DIR_DEFAULT);
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
	    	formEntryArchiveFileName = as.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR, FormEntryConstants.FORMENTRY_GP_QUEUE_ARCHIVE_DIR_DEFAULT);
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
     * Replaces %Y in the string with the four digit year.
     * Replaces %M with the two digit month
     * Replaces %D with the two digit day
     * Replaces %w with week of the year
     * Replaces %W with week of the month
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
     * Gets an out File object.  If date is not provided, the current 
     * timestamp is used.
     * 
     * If user is not provided, the user id is not put into the filename.
     * 
     * Assumes dir is already created
     * 
     * @param dir directory to make the random filename in
     * @param date optional Date object used for the name
     * @param user optional User creating this file object 
     * @return file new file that is able to be written to
     */
    public static File getOutFile(File dir, Date date, User user) {
    	
    	File outFile;
		do {
	    	// format to print date in filenmae
	    	DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HHmm-ssSSS");
	    	
	    	// use current date if none provided
	    	if (date == null)
	    		date = new Date();
	    	
	    	StringBuilder filename = new StringBuilder();
	    	
	    	// the start of the filename is the time so we can do some sorting
			filename.append(dateFormat.format(date));
			
			// insert the user id if they provided it
			if (user != null) {
				filename.append("-");
				filename.append(user.getUserId());
				filename.append("-");
			}
			
			// the end of the filename is a randome number between 0 and 10000
			filename.append((int)(Math.random() * 10000));
			filename.append(".xml");
			
			outFile = new File(dir, filename.toString());
			
			// set to null to avoid very minimal possiblity of an infinite loop
			date = null;
			
		} while (outFile.exists());
		
		return outFile;
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
     * Creates a zip file in <code>xsnDir</code> containing the <code>filesToZip</code>
     * The name of the dir is 
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
    	
	    	ZipOutputStream zos			= new ZipOutputStream(xsnDirOutStream);
			ZipEntry zipEntry			= null;
			
			for (File file : filesToZip) {
		        
				try {
					// string xsn data
					String fileData = OpenmrsUtil.getFileAsString(file);
					
					byte [] uncompressedBytes = fileData.getBytes();
			        
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
    
}
