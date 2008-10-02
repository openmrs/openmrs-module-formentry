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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Form;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.util.FormUtil;
import org.openmrs.util.OpenmrsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.SAXException;

/**
 * Performs the <em>publish</em> process for InfoPath forms. Publishing an
 * InfoPath form requires that multiple URL references and some specific XML
 * attributes are altered within the contents of the XSN file.
 * 
 * @author Ben Wolfe
 * @author Burke Mamlin
 * @version 1.0
 */
public class PublishInfoPath {

	private static Log log = LogFactory.getLog(PublishInfoPath.class);

	/**
	 * A FilenameFilter for xsl files. 
	 */
	private static FilenameFilter xslFilenameFilter = null;
	
	/**
	 * Regex pattern for an unqualified (lacking concept name) concept specification in HL7 format.
	 * 
	 * group(1) should be the single character that starts the HL7 spec
	 * group(2) should be the Hl7 spec
	 * group(3) should be the single character that ends the HL7 spec
	 */
	private static Pattern hl7ConceptNamePattern = Pattern.compile("([\"|&quot;|>])([0-9]+)\\^[^\\^]+\\^99DCT([\"|&quot;|<])");
	
	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param file
	 *            the XSN file to be published
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static Form publishXSN(File file) throws IOException {
		return publishXSN(file, null);
	}
	
	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * If form is null, form is determined from xsn file
	 * 
	 * @param file
	 *            the XSN file to be published
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static Form publishXSN(File file, Form form) throws IOException {

		if (file.exists())
			form = publishXSN(file.getAbsolutePath(), form);
		else
			form = publishXSN(new FileInputStream(file), form);

		return form;
	}

	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param inputStream
	 *            inputStream from which XSN may be read
	 */
	public static Form publishXSN(InputStream inputStream) throws IOException {
		return publishXSN(inputStream, null);
	}
		
	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * If form is null, form is assumed from the xsn
	 * 
	 * @param inputStream
	 *            inputStream from which XSN may be read
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static Form publishXSN(InputStream inputStream, Form form) throws IOException {
		
		File tempDir = FormEntryUtil.createTempDirectory("UPLOADEDXSN");

		log.debug("Temp publish dir: " + tempDir.getAbsolutePath());

		// create file on file system to hold the uploaded file
		File filesystemXSN = File.createTempFile("upload", ".xsn", tempDir);

		// copy the uploaded file over to the temp file system file
		OpenmrsUtil.copyFile(inputStream, new FileOutputStream(filesystemXSN));

		form = publishXSN(filesystemXSN.getAbsolutePath(), form);

		OpenmrsUtil.deleteDirectory(tempDir);

		return form;
	}
	
	
	/**
	 * Public access method for publishing an InfoPath&reg; form (XSN file). The
	 * given file is expanded into its constituents and the various URL and
	 * schema references within those files are updated before the files are
	 * re-constituted into an XSN archive.
	 * 
	 * @param xsnFilePath
	 *            full path to the XSN file
	 * @param form
	 *            the form add this xsn to. If null, form is determined from xsn
	 * @param form
	 *            the OpenMRS form with which the given XSN is to be associated
	 */
	public static Form publishXSN(String xsnFilePath, Form form) throws IOException {
		
		if (log.isDebugEnabled())
			log.debug("publishing xsn at: " + xsnFilePath);

		File tempDir = FormEntryUtil.expandXsn(xsnFilePath);
		if (tempDir == null)
			throw new IOException("Filename not found: '" + xsnFilePath + "'");
		
		if (form == null)
			form = determineForm(tempDir);
		else if (!form.equals(determineForm(tempDir))) {
			modifyFormId(tempDir, form);
		}
			
		String originalFormUri = FormEntryUtil.getFormUri(form);
		form.setBuild(form.getBuild() == null ? 1 : form.getBuild() + 1);

		String outputFilename = FormEntryUtil.getFormUri(form);
		String namespace = FormEntryUtil.getFormSchemaNamespace(form);
		String solutionVersion = FormEntryUtil.getSolutionVersion(form);
		log.debug("solution version: " + solutionVersion);

		AdministrationService adminService = Context.getAdministrationService();
		
		String serverUrl = adminService.getGlobalProperty(FormEntryConstants.FORMENTRY_GP_SERVER_URL);
		String publishUrl = serverUrl + FormEntryConstants.FORMENTRY_INFOPATH_PUBLISH_PATH + outputFilename;
		String taskPaneCaption = adminService.getGlobalProperty("formentry.infopath_taskpane_caption"); // "Welcome!";
		String taskPaneInitialUrl = serverUrl + FormEntryConstants.FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH; // "http://localhost:8080/amrs/taskPane.htm";
		String submitUrl = serverUrl + FormEntryConstants.FORMENTRY_INFOPATH_SUBMIT_PATH; // "http://localhost:8080/amrs/formUpload";
		String schemaFilename = FormEntryConstants.FORMENTRY_DEFAULT_SCHEMA_NAME; // "FormEntry.xsd";
		
		// prepare manifest
		prepareManifest(tempDir, publishUrl, namespace, solutionVersion, taskPaneCaption,
		    taskPaneInitialUrl, submitUrl);

		// set schema
		File schema = FormEntryUtil.findFile(tempDir, schemaFilename);
		if (schema == null)
			throw new IOException("Schema: '" + schemaFilename + "' cannot be null");
		String tag = "xs:schema";
		setNamespace(schema, tag, namespace);

		// Ensure that we have a template with default scripts
		String templateWithDefaults;
		File templateWithDefaultsFile = FormEntryUtil.findFile(tempDir,
		    FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME);
		if (templateWithDefaultsFile == null) {
			// if template containing defaults is missing, create one on the fly
			templateWithDefaults = new FormXmlTemplateBuilder(form, publishUrl)
			    .getXmlTemplate(true);
			try {
				log.debug("Writing new template with defaults to: "
				    + templateWithDefaultsFile.getAbsolutePath());
				FileWriter out = new FileWriter(templateWithDefaultsFile);
				out.write(templateWithDefaults);
				out.close();
			}
			catch (IOException e) {
				log.error("Could not write '" + FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME
				    + "'", e);
			}
		}
		else {
			prepareTemplate(tempDir, FormEntryConstants.FORMENTRY_DEFAULT_DEFAULTS_NAME,
			    solutionVersion, publishUrl, namespace);
			templateWithDefaults = readFile(templateWithDefaultsFile);
		}

		// update InfoPath solutionVersion within all XML template documents
		prepareTemplate(tempDir, FormEntryConstants.FORMENTRY_DEFAULT_TEMPLATE_NAME, solutionVersion,
		    publishUrl, namespace);
		prepareTemplate(tempDir, FormEntryConstants.FORMENTRY_DEFAULT_SAMPLEDATA_NAME,
		    solutionVersion, publishUrl, namespace);

		// update server_url in openmrs-infopath.js
		Map<String, String> vars = new HashMap<String, String>();
		vars.put(FormEntryConstants.FORMENTRY_SERVER_URL_VARIABLE_NAME, "\"" + serverUrl + "\"");
		vars.put(FormEntryConstants.FORMENTRY_TASKPANE_URL_VARIABLE_NAME, FormEntryConstants.FORMENTRY_SERVER_URL_VARIABLE_NAME + " + \"/module/formentry/taskpane\"");
		vars.put(FormEntryConstants.FORMENTRY_SUBMIT_URL_VARIABLE_NAME, FormEntryConstants.FORMENTRY_SERVER_URL_VARIABLE_NAME + " + \"/moduleServlet/formentry/formUpload\"");
		setVariables(tempDir, FormEntryConstants.FORMENTRY_DEFAULT_JSCRIPT_NAME, vars);

		// ABK: apply any needed updates to XSL files
		updateXslFiles(tempDir);
		
		// make cab
		// creates the file in the same temp directory
		FormEntryUtil.makeCab(tempDir, tempDir.getAbsolutePath(), outputFilename);
		
		// create and save the formentry xsn file
		File newXsnFile = FormEntryUtil.findFile(tempDir, outputFilename);
		byte[] xsnContents = OpenmrsUtil.getFileAsBytes(newXsnFile);
		FormEntryXsn xsn = new FormEntryXsn();
		xsn.setForm(form);
		xsn.setXsnData(xsnContents);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		formEntryService.createFormEntryXsn(xsn);
		
		// clean up
		OpenmrsUtil.deleteDirectory(tempDir);
		if (originalFormUri != null && !originalFormUri.equals(outputFilename)) {
			System.gc();
		}

		// update template, solution version, and build number on server
		form.setTemplate(templateWithDefaults);
		Context.getFormService().updateForm(form);
		
		return form;
	}

	// Prepare template file (update solutionVersion and href)
	private static void prepareTemplate(File tempDir, String fileName, String solutionVersion,
	    String publishUrl, String namespace) {
		File file = new File(tempDir, fileName);
		if (file == null) {
			log.warn("Missing file: '" + fileName + "'");
			return;
		}
		if (log.isDebugEnabled())
			log.debug("Preparing template: " + file.getAbsolutePath());
		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(file);
			
			// set namespace
			String tag = "form";
			Element elem = getSingleElement(doc, tag);
			if (elem == null) {
				log.warn("Could not locate " + tag + " element in " + file.getName());
				return;
			}
			elem.setAttribute("xmlns:openmrs", namespace);
			
			Node root = doc.getDocumentElement().getParentNode();
			NodeList children = root.getChildNodes();
			log.debug("Scanning for processing instructions");
			for (int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE
				    && node.getNodeName().equals("mso-infoPathSolution")) {
					ProcessingInstruction pi = (ProcessingInstruction) node;
					String data = pi.getData();
					if (log.isDebugEnabled())
						log.debug("  found: " + data);
					data = data.replaceAll("(\\bsolutionVersion\\s*=\\s*\")[^\"]+\"", "$1"
					    + solutionVersion + "\"");
					data = data.replaceAll("(\\bhref\\s*=\\s*\")[^\"]+\"", "$1" + publishUrl + "\"");
					if (log.isDebugEnabled())
						log.debug("  replacing with: " + data);
					pi.setData(data);
				}
			}

		}
		catch (Exception e) {
			log.error("Trouble with file: " + fileName + " " + solutionVersion + " " + publishUrl, e);
		}
		writeXml(doc, file.getAbsolutePath());
	}

	/**
	 * Convenience method to find the correct OpenMRS Form object that
	 * this xsn refers to.
	 * 
	 * Currently this is a simplistic lookup of form.id in the header 
	 * 
	 * @param tempDir directory in which to look for the xsd
	 * 
	 * @return Form that this xsn refers to or null if none
	 */
	private static Form determineForm(File tempDir) {
		File xsd = FormEntryUtil.findFile(tempDir, "FormEntry.xsd");
		Form form = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xsd);
			Element parent = getSingleElement(doc.getElementsByTagName("xs:element"), "form");
			if (parent == null) {
				log.warn("Could not locate xs:element element in xsd!");
				return null;
			}
			Element elem = getSingleElement(parent.getElementsByTagName("xs:attribute"), "id");
			if (elem == null) {
				log.warn("Could not locate xs:attribute element in xsd!");
				return null;
			}

			Integer formId = Integer.valueOf(elem.getAttribute("fixed"));
			
			FormService formService = Context.getFormService();
			form = formService.getForm(formId);

		}
		catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		}
		catch (SAXException e) {
			log.error("Error parsing form data", e);
		}
		catch (IOException e) {
			log.error("Error parsing form data", e);
		}

		return form;
	}
	
	private static void modifyFormId(File tempDir, Form form) {
		File xsd = FormEntryUtil.findFile(tempDir, "FormEntry.xsd");
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(xsd);
			Element parent = getSingleElement(doc.getElementsByTagName("xs:element"), "form");
			if (parent == null) {
				log.warn("Could not locate xs:element element in xsd!");
			}
			Element elem = getSingleElement(parent.getElementsByTagName("xs:attribute"), "id");
			if (elem == null) {
				log.warn("Could not locate xs:attribute element in xsd!");
			}
			
			elem.setAttribute("fixed", form.getFormId().toString());
			
			// save the document 
			OpenmrsUtil.saveDocument(doc, xsd);
		}
		catch (ParserConfigurationException e) {
			log.error("Error building xml document", e);
		}
		catch (SAXException e) {
			log.error("Error parsing form data", e);
		}
		catch (IOException e) {
			log.error("Error parsing form data", e);
		}
		
	}

	private static void prepareManifest(File tempDir, String url, String namespace,
	    String solutionVersion, String taskPaneCaption, String taskPaneInitialUrl, String submitUrl) {
		File manifest = findManifest(tempDir);
		if (manifest == null) {
			log.warn("Missing manifest!");
			return;
		}

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(manifest);

			Element elem = getSingleElement(doc, "xsf:xDocumentClass");
			if (elem == null) {
				log.warn("Could not locate xsf:xDocumentClass element in manifest!");
				return;
			}
			elem.setAttribute("solutionVersion", solutionVersion);
			if (elem.getAttribute("name") != null)
				elem.removeAttribute("name");
			elem.setAttribute("trustSetting", "manual");
			elem.setAttribute("trustLevel", "domain");
			elem.setAttribute("publishUrl", url);
			elem.setAttribute("xmlns:openmrs", namespace);

			// Find xsf:taskpane element
			elem = getSingleElement(doc, "xsf:taskpane");
			if (elem != null) {
				elem.setAttribute("caption", taskPaneCaption);
				elem.setAttribute("href", taskPaneInitialUrl);
			}
			else {
				log.warn("Could not locate xsf:taskpane element within manifest");
			}

			elem = getSingleElement(doc, "xsf:useHttpHandler");
			if (elem != null) {
				elem.setAttribute("href", submitUrl);
			}

			writeXml(doc, manifest.getPath());

		}
		catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		}
		catch (SAXException e) {
			log.error("Error parsing form data", e);
		}
		catch (IOException e) {
			log.error("Error parsing form data", e);
		}

	}

	private static void setNamespace(File file, String tag, String namespace) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);

			Element elem = getSingleElement(doc, tag);
			if (elem == null) {
				log.warn("Could not locate " + tag + " element in " + file.getName());
				return;
			}
			elem.setAttribute("xmlns:openmrs", namespace);
			writeXml(doc, file.getAbsolutePath());
		}
		catch (ParserConfigurationException e) {
			log.error("Error parsing form data", e);
		}
		catch (SAXException e) {
			log.error("Error parsing form data", e);
		}
		catch (IOException e) {
			log.error("Error parsing form data", e);
		}
	}

	private static File findManifest(File dir) {
		return FormEntryUtil.findFile(dir, "manifest.xsf");
	}

	private static void writeXml(Document doc, String filename) {
		try {
			// Create a transformer
			Transformer xformer = TransformerFactory.newInstance().newTransformer();

			// Set the public and system id
			xformer.setOutputProperty(OutputKeys.METHOD, "xml");

			// Write the DOM document to a file
			Source source = new DOMSource(doc);
			OutputStream outputStream = new FileOutputStream(filename);
			Result result = new StreamResult(outputStream);
			xformer.transform(source, result);
			outputStream.close();

		}
		catch (TransformerConfigurationException e) {
		}
		catch (TransformerException e) {
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
			log.error("Error closing outputStream: '" + filename + "'", e);
		}
	}

	private static void setVariables(File dir, String filename, Map<String, String> vars)
	    throws IOException {
		File file = FormEntryUtil.findFile(dir, filename);
		String fileContent = readFile(file);
		for (String variableName : vars.keySet()) {
			// \s = whitespace
			String regexp = "var\\s" + variableName + "\\s=[^;]*";
			String rplcmnt = "var " + variableName + " = " + vars.get(variableName) + "";
			log.debug("replacing regexp: " + regexp + " with " + rplcmnt);
			fileContent = fileContent.replaceAll(regexp, rplcmnt);
		}
		try {
			FileWriter out = new FileWriter(file);
			out.write(fileContent);
			out.close();
		}
		catch (IOException e) {
			log.error("Could not write '" + filename + "'", e);
		}
	}

	private static String readFile(File file) throws IOException {
		FileInputStream inputStream = new FileInputStream(file);
		byte[] b = new byte[inputStream.available()];
		inputStream.read(b);
		inputStream.close();
		return new String(b);
	}

	private static Element getSingleElement(Document doc, String tagName) {
		Element elem = null;
		NodeList elemList = doc.getElementsByTagName(tagName);
		if (elemList != null && elemList.getLength() > 0)
			elem = (Element) elemList.item(0);
		return elem;
	}

	private static Element getSingleElement(NodeList elemList, String nameAttrValue) {
		Element elem = null;
		if (elemList != null) {
			if (elemList.getLength() > 0) {
				for (Integer i = 0; i < elemList.getLength(); i++) {
					elem = (Element) elemList.item(0);
					if (elem.getAttribute("name").equals(nameAttrValue))
						break;
				}
			}
			else {
				elem = (Element) elemList.item(0);
			}
		}
		return elem;
	}
	

    /**
     * Scans a directory for XSL files, applying any needed updates.
     * 
     * @param tempDir
     */
	private static void updateXslFiles(File tempDir) {
		if (tempDir.isDirectory()) {
			String[] xslFilenames = tempDir.list(getXslFilenameFilter());
			for (String xslFilename : xslFilenames) {
				appendConceptnamesInXsl(xslFilename, tempDir);
			}
		}
	}
	
	/** 
	 * Scans an XSL file for HL7 concept specifications, appending the
	 * appropriate concept-name specification. 
	 * 
	 * @param xslFilename
	 */
	private static void appendConceptnamesInXsl(String xslFilename, File tempDir) {
		ConceptService cs = Context.getConceptService();
		Locale defaultLocale = Context.getLocale();
		
		try {
			File xslFile = new File(tempDir.getAbsolutePath(), xslFilename);
			File tmpXslFile = File.createTempFile("infopath", ".xsltmp", tempDir);
			
			BufferedReader xslReader = new BufferedReader(new FileReader(xslFile));
			PrintWriter tmpXslWriter = new PrintWriter(new FileWriter(tmpXslFile));

			String line = xslReader.readLine();
		    while (line != null) {
		    	Matcher m = hl7ConceptNamePattern.matcher(line); 
		    	if (m.find()) {
		    		String conceptId = m.group(2);
		    		Concept concept = cs.getConceptByIdOrName(conceptId);
		    		String appendedHl7 = m.group(1) + FormUtil.conceptToString(concept, defaultLocale) + m.group(3);
		    		
		    		line = m.replaceFirst(appendedHl7);
		    	} else {
		    		tmpXslWriter.println(line);
		    		line = xslReader.readLine();
		    	}
		    }
		 
		    tmpXslWriter.close();
		    xslReader.close();
		    // xslFile.delete();
		    tmpXslFile.renameTo(xslFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Lazy factory method of xslFilenameFilter.
	 * 
	 * @return
	 */
	private static FilenameFilter getXslFilenameFilter() {
		if (xslFilenameFilter == null) {
			xslFilenameFilter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith("xsl");
				}};
		}
		return xslFilenameFilter;
	}
}
