package org.openmrs.module.formentry.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryService;
import org.openmrs.module.formentry.FormEntryXsn;

/**
 * Provides a servlet through which an XSN is downloaded. This class differs
 * from org.openmrs.module.formentry.FormDownloadServlet in that this class /will not/
 * modify the template or schema files inside of the xsn. This class simply
 * writes the named schema to the response
 * 
 * @author Ben Wolfe
 * @version 1.0
 */
public class XsnDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123424L;

	private Log log = LogFactory.getLog(this.getClass());

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		response.setHeader("Content-Type", "text/plain; charset=utf-8");

		// since we've got a "/formentry/form/*" servlet-mapping,
		// getServletPath() will only return /formentry/form.
		String filename = request.getRequestURI();
		// get only the file name out of path
		filename = filename.substring(filename.lastIndexOf("/") + 1);
		
		String formIdString = filename.substring(0, filename.indexOf("."));
		Integer formId = Integer.valueOf(formIdString);
		
		//File file = FormEntryUtil.getXSNFile(filename);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		FormEntryXsn xsn = formEntryService.getFormEntryXsn(formId);
		
		if (xsn != null) {
			Date dateModified = xsn.getDateCreated();
			Long timeModified = dateModified.getTime();
			
			if (log.isDebugEnabled()) {
				log.debug("testing modified date: " + dateModified);
				log.debug("testing etag: " + timeModified);
				log.debug("length of xsn.getXsn(): " + xsn.getXsnData().length);
			}
			
			// InfoPath checks one or both of these values to determine if it needs to 
			// update its internal/local cache
			response.setDateHeader("Last-Modified", timeModified);
			response.setHeader("ETag", "" + timeModified);
			
			OutputStream out = response.getOutputStream();
	    	
	    	out.write(xsn.getXsnData());
	    	
	    	out.flush();
		} 
		else {
			log.error(
			        "The request for '"
			        	+ formId
			            + "' cannot be found.  More than likely the XSN has not been uploaded (via Upload XSN in Form Entry administration).");
			response.sendError(404);
		}
	}

	

}
