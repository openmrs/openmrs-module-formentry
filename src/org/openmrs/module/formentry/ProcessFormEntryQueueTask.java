package org.openmrs.module.formentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

/**
 * Implementation of a task that process all form entry queues.
 * 
 * NOTE: This class does not need to be StatefulTask as we create the context in
 * the constructor.
 * 
 * @author Justin Miranda
 * @version 1.0
 */
public class ProcessFormEntryQueueTask extends AbstractTask {

	// Logger
	private static Log log = LogFactory.getLog(ProcessFormEntryQueueTask.class);
	
	// Instance of form processor
	private FormEntryQueueProcessor processor = null;
	
	/**
	 * Default Constructor (Uses SchedulerConstants.username and
	 * SchedulerConstants.password
	 * 
	 */
	public ProcessFormEntryQueueTask() {
		if (processor == null)
			processor = new FormEntryQueueProcessor();
	}

	/**
	 * Process the next form entry in the database and then remove the form
	 * entry from the database.
	 */
	public void execute() {
		Context.openSession();
		log.debug("Processing form entry queue ... ");
		try {
			if (Context.isAuthenticated() == false)
				authenticate();
			processor.processFormEntryQueue();
		} catch (APIException e) {
			log.error("Error running form entry queue task", e);
			throw e;
		} finally {
			Context.closeSession();
		}
	}
	
	/**
	 * Clean up any resources here
	 *
	 */
	public void shutdown() {
		log.debug("Shutting down ProcessFormEntryQueue task ...");
		processor = null;
	}

}
