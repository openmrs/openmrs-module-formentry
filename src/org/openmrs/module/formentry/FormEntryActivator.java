package org.openmrs.module.formentry;

import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Activator;
import org.openmrs.module.ModuleException;
import org.openmrs.util.OpenmrsUtil;

public class FormEntryActivator implements Activator {

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * @see org.openmrs.module.Activator#startup()
	 */
	public void startup() {
		log.info("Starting the Form Entry module");
		
		Properties p = Context.getRuntimeProperties();
		String val;
		
		String[] deprecated = {"formentry.starter_xsn_folder_path", 
			"formentry.infopath.publish_url",
			"formentry.infopath.publish_path",
			"formentry.infopath.submit_url",
			"formentry.infopath.initial_url" };
		
		for (String s : deprecated) {
			val = p.getProperty(s, null);
			if (val != null)
				log.warn("Deprecated runtime property: " + s + ".  This property is no longer read in at runtime and can be deleted.");
		}
		
		
		AdministrationService as = Context.getAdministrationService();
		
		List<String> errorMessages = new Vector<String>();
		
		// set up property requirements
		String gp = as.getGlobalProperty("formEntry.infopath_output_dir", ""); 
		if ("".equals(gp)) 
			errorMessages.add("Global property 'formEntry.infopath_output_dir' must be defined.");
		
		gp = as.getGlobalProperty("formEntry.infopath_server_url", ""); 
		if ("".equals(gp))
			errorMessages.add("Global property 'formEntry.infopath_server_url' must be defined.");
		
		gp = as.getGlobalProperty("formEntry.infopath_archive_dir", ""); 
		String gp2 = as.getGlobalProperty("formEntry.infopath_archive_date_format", "");
		if (!"".equals(gp) && "".equals(gp2))
			errorMessages.add("Global property 'formEntry.infopath_archive_date_format' must be defined if 'formEntry.infopath_archive_dir' is defined.");
		
		if (errorMessages.size() > 0) {
			throw new ModuleException(OpenmrsUtil.join(errorMessages, " \n"));
		}
		
		// Could do other code updates here.
		
	}
	
	/**
	 *  @see org.openmrs.module.Activator#shutdown()
	 */
	public void shutdown() {
		log.info("Shutting down the Form Entry module");
	}
	
}
