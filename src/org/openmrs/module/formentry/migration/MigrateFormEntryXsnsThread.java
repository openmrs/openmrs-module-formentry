package org.openmrs.module.formentry.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ContextAuthenticationException;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleException;
import org.openmrs.module.formentry.FormEntryConstants;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.util.OpenmrsConstants;

/**
 * Separate thread to migrate the formentry xsns from the filesystem
 * to the database
 */
public class MigrateFormEntryXsnsThread extends Thread {
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * User with which to authenticate and perform actions in this thread
	 */
	protected UserContext userContext;
	
	protected Module formEntryModule;
	
	/**
	 * Whether or not activity should continue with this thread
	 */
	protected static boolean active = true;

	/**
	 * @param userContext current user's context (to continue on the
	 *        authorization, etc)
	 */
	public MigrateFormEntryXsnsThread(UserContext userContext, Module formEntryModule) {
		this.userContext = userContext;
		this.formEntryModule = formEntryModule;
		log.debug("Migrate formentry xsns thread created");
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		log.debug("Running the migrate formentry xsns thread");
		
		Context.setUserContext(userContext);
		
		// wait for 30 seconds (waiting for the rest of the module to be loaded)
		try {
			Thread.sleep(30000);
		}
		catch (InterruptedException e) {
			log.error("Sleeping was interrupted", e);
		}
		
		// loop until the service is fetched successfully
		boolean finishedSuccessfully = false;
		
		while (isActive() && !finishedSuccessfully) {
			try {
				// send this concept up to the OCC
				FormEntryService service = (FormEntryService)Context.getService(FormEntryService.class);
				
				// the getService call may be blocking until the services are refreshed, if 
				// that happens, we need to check the active var again here
				if (isActive()) {
					try {
						Context.addProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
						service.migrateXsnsToDatabase();
					}
					catch (APIAuthenticationException auth) {
						try {
							String username = Context.getRuntimeProperties().getProperty(FormEntryConstants.STARTUP_USERNAME, "");
							String password = Context.getRuntimeProperties().getProperty(FormEntryConstants.STARTUP_PASSWORD, "");
							
							if (!"".equals(username) && !"".equals(password)) {
								Context.authenticate(username, password);
								continue; // jump back to the while loop and try again
							}
						}
						catch (ContextAuthenticationException contextAuth) {
							throw new ModuleException("This module version migrates xsns from the filesystem to the database.  To do this it requires an authenticated user with rights to Manage the FormEntry XSNs.\n" + 
							                          "In order to start this module, either use the green 'play' button to the left (if you have the " + FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN + " privilege) \n" +
							                          "or temporarily define the " + FormEntryConstants.STARTUP_USERNAME + " and " + FormEntryConstants.STARTUP_PASSWORD + " runtime properties and restart the webapp.");
						}
						catch (APIAuthenticationException apiAuth) {
							throw new ModuleException("The user defined by the runtime properties: " + FormEntryConstants.STARTUP_USERNAME + " and " + FormEntryConstants.STARTUP_PASSWORD + " does not have the required privilege " + FormEntryConstants.PRIV_MANAGE_FORMENTRY_XSN);
						}
					}
					finally {
						Context.removeProxyPrivilege(OpenmrsConstants.PRIV_VIEW_FORMS);
					}
				}
				
				finishedSuccessfully = true;
				
			} catch (APIException api) {
				// log this as a debug, because we want to swallow anything that has to do with 
				log.debug("Unable to migrate formentry xsns", api);
				
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					log.error("Sleeping was interrupted", e);
				}
			}
			catch (ModuleException moduleException) {
				// the formentry module needs to be stopped
				if (formEntryModule != null && formEntryModule.isStarted()) {
					formEntryModule.setStartupErrorMessage(moduleException.getMessage());
					//ModuleFactory.stopModule(formEntryModule);
					//WebModuleUtil.stopModule(formEntryModule);
				}
			}
			finally {
				
				// let people know this is done
				setActive(false);
			}
		}

	}
	
	/**
     * @return the active
     */
    public static boolean isActive() {
    	return active;
    }

	/**
     * @param active the active to set
     */
    public static void setActive(boolean a) {
    	active = a;
    }

}
