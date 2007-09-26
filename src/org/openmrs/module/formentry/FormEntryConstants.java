package org.openmrs.module.formentry;

import java.util.Hashtable;

public class FormEntryConstants {

	public static final Integer FIELD_TYPE_CONCEPT = 1;
	public static final Integer FIELD_TYPE_DATABASE = 2;
	public static final Integer FIELD_TYPE_TERM_SET = 3;
	public static final Integer FIELD_TYPE_MISC_SET = 4;
	public static final Integer FIELD_TYPE_SECTION = 5;

	public static final String HL7_TEXT = "ST";
	public static final String HL7_CODED = "CE";
	public static final String HL7_CODED_WITH_EXCEPTIONS = "CWE";
	public static final String HL7_NUMERIC = "NM";
	public static final String HL7_DATE = "DT";
	public static final String HL7_TIME = "TM";
	public static final String HL7_DATETIME = "TS";
	public static final String HL7_BOOLEAN = "BIT";

	public static final Integer CLASS_DRUG = 3;

	public static final String HL7_LOCAL_CONCEPT = "99DCT";
	public static final String HL7_LOCAL_DRUG = "99RX";

	// List of datatypes that do not require complex definitions
	public static final Hashtable<String, String> simpleDatatypes = new Hashtable<String, String>();
	static {
		simpleDatatypes.put(HL7_TEXT, "xs:string");
		simpleDatatypes.put(HL7_DATE, "xs:date");
		simpleDatatypes.put(HL7_TIME, "xs:time");
		simpleDatatypes.put(HL7_DATETIME, "xs:dateTime");

		// We make a special boolean type with an extra attribute
		// to get InfoPath to treat booleans properly
		simpleDatatypes.put(HL7_BOOLEAN, "_infopath_boolean");
	}

	public static final int INDENT_SIZE = 2;

	/* FormEntry Queue baked-in prileges */
	public static final String PRIV_VIEW_FORMENTRY_QUEUE = "View FormEntry Queue";
	public static final String PRIV_ADD_FORMENTRY_QUEUE = "Add FormEntry Queue";
	public static final String PRIV_EDIT_FORMENTRY_QUEUE = "Edit FormEntry Queue";
	public static final String PRIV_DELETE_FORMENTRY_QUEUE = "Delete FormEntry Queue";
	public static final String PRIV_VIEW_FORMENTRY_ARCHIVE = "View FormEntry Archive";
	public static final String PRIV_ADD_FORMENTRY_ARCHIVE = "Add FormEntry Archive";
	public static final String PRIV_EDIT_FORMENTRY_ARCHIVE = "Edit FormEntry Archive";
	public static final String PRIV_DELETE_FORMENTRY_ARCHIVE = "Delete FormEntry Archive";
	public static final String PRIV_VIEW_FORMENTRY_ERROR = "View FormEntry Error";
	public static final String PRIV_ADD_FORMENTRY_ERROR = "Add FormEntry Error";
	public static final String PRIV_EDIT_FORMENTRY_ERROR = "Edit FormEntry Error";
	public static final String PRIV_DELETE_FORMENTRY_ERROR = "Delete FormEntry Error";
	public static final String PRIV_MANAGE_FORMENTRY_XSN = "Manage FormEntry XSN";
	
	/* FormEntry Queue status values for entries in the queue */
	public static final int FORMENTRY_QUEUE_STATUS_PENDING = 0;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSING = 1;
	public static final int FORMENTRY_QUEUE_STATUS_PROCESSED = 2;
	public static final int FORMENTRY_QUEUE_STATUS_ERROR = 3;

	/* Default name for InfoPath components */
	public static final String FORMENTRY_DEFAULT_SCHEMA_NAME = "FormEntry.xsd";
	public static final String FORMENTRY_DEFAULT_TEMPLATE_NAME = "template.xml";
	public static final String FORMENTRY_DEFAULT_SAMPLEDATA_NAME = "sampledata.xml";
	public static final String FORMENTRY_DEFAULT_DEFAULTS_NAME = "defaults.xml";
	public static final String FORMENTRY_DEFAULT_JSCRIPT_NAME = "openmrs-infopath.js";
	public static final String FORMENTRY_SERVER_URL_VARIABLE_NAME = "SERVER_URL";
	public static final String FORMENTRY_TASKPANE_URL_VARIABLE_NAME = "TASKPANE_URL";
	public static final String FORMENTRY_SUBMIT_URL_VARIABLE_NAME = "SUBMIT_URL";
	
	public static final String PRIV_FORM_ENTRY = "Form Entry";
	
	// These variables used to be non-final and editable by runtime properties.
	// Users should not need to modify these settings.
	public static final String FORMENTRY_INFOPATH_PUBLISH_PATH = "/moduleServlet/formentry/forms/";
	public static final String FORMENTRY_INFOPATH_SUBMIT_PATH = "/moduleServlet/formentry/formUpload";
	public static final String FORMENTRY_INFOPATH_TASKPANE_INITIAL_PATH = "/formTaskpane.htm";
	public static final String FORMENTRY_STARTER_XSN_FOLDER_PATH = "/org/openmrs/module/formentry/forms/starter/";
	
	// Global properties used in the formentry module
	public static final String FORMENTRY_GP_SERVER_URL = "formentry.infopath_server_url";
	public static final String FORMENTRY_GP_TASKPANE_KEEPALIVE = "formentry.infopath_taskpane_keepalive_min";
	
	public static final String FORMENTRY_GP_QUEUE_DIR = "formentry.queue_dir";
	public static final String FORMENTRY_GP_QUEUE_DIR_DEFAULT = "formentry/queue";
	public static final String FORMENTRY_GP_QUEUE_ARCHIVE_DIR = "formentry.queue_archive_dir";
	public static final String FORMENTRY_GP_QUEUE_ARCHIVE_DIR_DEFAULT = "formentry/archive/%Y/%M";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_INFOPATH_OUTPUT_DIR = "formentry.infopath_output_dir";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_INFOPATH_ARCHIVE_DIR = "formentry.infopath_archive_dir";
	
	/**
	 * @deprecated As of 2.6, xsns are stored in the formentry_xsn table
	 */
	public static final String FORMENTRY_GP_ARCHIVE_DATE_FORMAT = "formentry.infopath_archive_date_format";
	
	public static final String STARTUP_USERNAME = "formentry.startup_username";
	public static final String STARTUP_PASSWORD = "formentry.startup_password";

	// runtime properties for the cabextract and lcab locations
	// these are not global properties as they could be a security risk for demo sites
	public static final String FORMENTRY_RP_CABEXTRACT_LOCATION = "formentry.cabextract_location";
	public static final String FORMENTRY_RP_LCAB_LOCATION = "formentry.lcab_location";
	public static String FORMENTRY_CABEXTRACT_LOCATION = null; // value of the runtime property loaded at startup
	public static String FORMENTRY_LCAB_LOCATION = null; // value of the runtime property loaded at startup
	
}
