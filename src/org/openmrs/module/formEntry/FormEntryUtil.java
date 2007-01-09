package org.openmrs.module.formEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryUtil {

	private static Log log = LogFactory.getLog(FormEntryUtil.class);

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
			cmdBuffer.append("/usr/bin/cabextract -d ").append(
					tempDir.getAbsolutePath()).append(" ").append(xsnFilePath);
			execCmd(cmdBuffer.toString(), tempDir);
		} else {
			cmdBuffer.append("expand -F:* \"").append(xsnFilePath).append(
					"\" \"").append(tempDir.getAbsolutePath()).append("\"");
			execCmd(cmdBuffer.toString(), null);
		}

		return tempDir;
	}

	/**
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank XSN template
	 * to play with in Infopath.  Should be used similar to 
	 * <code>org.openmrs.module.formEntry.FormEntryUtil.expandXsn(java.lang.String)</code>
	 * Generates an expanded 'starter XSN'. This starter is essentially a blank
	 * XSN template to play with in Infopath. Should be used similar to
	 * <code>org.openmrs.formentry.FormEntryUtil.expandXsn(java.lang.String)</code>
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
		
		// temp directory to hold the new xsn contents
		File tempDir = FormEntryUtil.createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");

		// iterate over and copy each file in the given folder
		for (File f : xsnFolder.listFiles()) {
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
		*/
		
		// get the location of the starter documents
		URL url = c.getResource(xsnFolderPath);
		if (url == null) {
			String err = "Could not open starter xsn folder directory: " + xsnFolderPath;
			log.error(err);
			throw new FileNotFoundException(err);
		}
		
		// directory to store the starter xsn in 
		File tempDir = FormEntryUtil.createTempDirectory("XSN");
		if (tempDir == null)
			throw new IOException("Failed to create temporary directory");
		
		return OpenmrsUtil.url2file(url);
		
	}

	/**
	 * Gets the current xsn file for a form. If the xsn is not found, the
	 * starter xsn is returned instead
	 * 
	 * @param form
	 * @return form's xsn file or starter xsn if none
	 * @throws IOException
	 */
	public static FileInputStream getCurrentXSN(Form form) throws IOException {
		// Find the form file data
		String formDir = Context.getAdministrationService().getGlobalProperty("formEntry.infopath_output_dir");
		String formFilePath = formDir + (formDir.endsWith(File.separator) ? "" : File.separator)
		    + FormEntryUtil.getFormUri(form);

		log.debug("Attempting to open xsn from: " + formFilePath);

		// The expanded the xsn
		File tmpXSN = null;

		if (new File(formFilePath).exists())
			tmpXSN = FormEntryUtil.expandXsn(formFilePath);
		else {
			// use starter xsn as the
			log.debug("Using starter xsn");
			tmpXSN = FormEntryUtil.getExpandedStarterXSN();
		}

		return compileXSN(form, tmpXSN);
	}

	/**
	 * Returns a .xsn file compiled from the starter data set
	 * 
	 * @param form
	 * @return .xsn file
	 * @throws IOException
	 */
	public static FileInputStream getStarterXSN(Form form) throws IOException {
		File tmpXSN = FormEntryUtil.getExpandedStarterXSN();
		return compileXSN(form, tmpXSN);
	}

	/**
	 * Modifies schema, template.xml, and sample data, defaults, urls in
	 * <code>tmpXSN</code>
	 * 
	 * @param form
	 * @param tmpXSN
	 *            directory containing xsn files.
	 * @return
	 * @throws IOException
	 */
	private static FileInputStream compileXSN(Form form, File tmpXSN)
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
		File schemaFile = findFile(tmpXSN, schemaFilename);
		if (schemaFile == null)
			throw new IOException("Schema: '" + schemaFilename
					+ "' cannot be null");
		FileWriter schemaOutput = new FileWriter(schemaFile, false);
		schemaOutput.write(schema);
		schemaOutput.close();

		// replace template.xml with the generated xml
		File templateFile = findFile(tmpXSN, templateFilename);
		if (templateFile == null)
			throw new IOException("Template: '" + templateFilename
					+ "' cannot be null");
		FileWriter templateOutput = new FileWriter(templateFile, false);
		templateOutput.write(template);
		templateOutput.close();

		// replace defautls.xml with the xml template, including default scripts
		File defaultsFile = findFile(tmpXSN, defaultsFilename);
		if (defaultsFile == null)
			throw new IOException("Defaults: '" + defaultsFilename
					+ "' cannot be null");
		FileWriter defaultsOutput = new FileWriter(defaultsFile, false);
		defaultsOutput.write(templateWithDefaultScripts);
		defaultsOutput.close();

		// replace sampleData.xml with the generated xml
		File sampleDataFile = findFile(tmpXSN, sampleDataFilename);
		if (sampleDataFile == null)
			throw new IOException("Template: '" + sampleDataFilename
					+ "' cannot be null");
		FileWriter sampleDataOutput = new FileWriter(sampleDataFile, false);
		sampleDataOutput.write(template);
		sampleDataOutput.close();

		FormEntryUtil.makeCab(tmpXSN, tmpXSN.getAbsolutePath(), "new.xsn");

		File xsn = findFile(tmpXSN, "new.xsn");
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
		if (OpenmrsConstants.OPERATING_SYSTEM_LINUX
				.equalsIgnoreCase(OpenmrsConstants.OPERATING_SYSTEM)) {

			cmdBuffer.append("/usr/local/bin/lcab -rn ").append(tempDir)
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
		String out = "";
		try {
			String line;

			// Needed to add support for working directory because of a linux
			// file system permission issue.
			// Could not create lcab.tmp file in default working directory
			// (jmiranda).
			Process p = (wd != null) ? Runtime.getRuntime().exec(cmd, null, wd)
					: Runtime.getRuntime().exec(cmd);

			Reader reader = new InputStreamReader(p.getInputStream());
			BufferedReader input = new BufferedReader(reader);
			while ((line = input.readLine()) != null) {
				out += line + "\n";
			}
			input.close();
			reader.close();
		} catch (Exception e) {
			log.error("Error while executing command: '" + cmd + "'", e);
		}
		log.debug("execCmd output: " + out);
		return out;
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
		String serverURL = Context.getAdministrationService().getGlobalProperty("formEntry.infopath_server_url", "formEntry.infopath_server_url cannot be empty");
		String baseUrl = serverURL + FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH;
		return baseUrl + getFormUri(form);
	}

	public static String getFormSchemaNamespace(Form form) {
		String serverURL = Context.getAdministrationService().getGlobalProperty("formEntry.infopath_server_url", "formEntry.infopath_server_url cannot be empty");
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
	 * Get the given filename out of the XSN storage location
	 * @param filename
	 * @return File pointing to the <code>filename</code> file inside of 
	 * 		this server's XSN repository
	 */
	public static File getXSNFile(String filename) {
		String url = Context.getAdministrationService().getGlobalProperty("formEntry.infopath_output_dir");
		if (!url.endsWith(File.separator))
			url += File.separator;
		url = url + filename;
		log.debug("url = " + url);
		return new File(url);
	}
}
