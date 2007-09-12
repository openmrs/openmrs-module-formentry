package org.openmrs.module.formentry.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.formentry.FormEntryService;

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
	
	/**
	 * Whether or activity should continue with this thread
	 */
	protected boolean active = true;

	/**
	 * @param userContext current user's context (to continue on the
	 *        authorization, etc)
	 */
	public MigrateFormEntryXsnsThread(UserContext userContext) {
		this.userContext = userContext;
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
				if (isActive())
					service.migrateXsnsToDatabase();
				
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
			
		}

	}
	
	/**
     * @return the active
     */
    public boolean isActive() {
    	return active;
    }

	/**
     * @param active the active to set
     */
    public void setActive(boolean active) {
    	this.active = active;
    }

}
