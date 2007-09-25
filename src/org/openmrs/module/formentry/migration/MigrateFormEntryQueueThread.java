package org.openmrs.module.formentry.migration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.formentry.FormEntryService;

/**
 * Separate thread to migrate the formentry queue and archive tables to the filesystem
 */
public class MigrateFormEntryQueueThread extends Thread {
	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * User with which to authenticate and perform actions in this thread
	 */
	protected UserContext userContext;
	
	/**
	 * Whether or not activity should continue with this thread
	 */
	protected static boolean active = false;

	/**
	 * @param userContext current user's context (to continue on the
	 *        authorization, etc)
	 */
	public MigrateFormEntryQueueThread(UserContext userContext) {
		this.userContext = userContext;
		log.debug("Migrate formentry queue thread created");
	}

	/**
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		
		log.debug("Running the migrate formentry queue thread");
		
		Context.setUserContext(userContext);
		
		// loop until the service is fetched successfully
		boolean finishedSuccessfully = false;
		
		while (isActive() && !finishedSuccessfully) {
			try {
				// send this concept up to the OCC
				FormEntryService service = (FormEntryService)Context.getService(FormEntryService.class);
				
				// the getService call may be blocking until the services are refreshed, if 
				// that happens, we need to check the active var again here
				if (isActive())
					service.migrateQueueAndArchiveToFilesystem();
				
				finishedSuccessfully = true;
				
			} catch (APIException api) {
				// log this as a debug, because we want to swallow anything that has to do with 
				log.debug("Unable to migrate formentry queue", api);
				
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					log.error("Sleeping was interrupted", e);
				}
			}
			catch (Exception e) {
				log.warn("Error while migrating formentry queue items", e);
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
