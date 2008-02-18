package org.openmrs.module.formentry.web;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 */
public class XsnDownloadServlet extends HttpServlet {

	public static final long serialVersionUID = 123424L;

	private Log log = LogFactory.getLog(this.getClass());

	/**
	 * The filename pattern matcher.  Matches the first n numbers in the filename
	 */
	private static final Pattern pattern = Pattern.compile(".*/formentry/forms/(\\d+).*");
	
	/**
	 * This method is called by Infopath to get the compiled and uploaded xsn from
	 * openmrs's xsn repository
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

		response.setHeader("Content-Type", "text/plain; charset=utf-8");

		// since we've got a "/formentry/form/*" servlet-mapping,
		// getServletPath() will only return /formentry/form.
		String filename = request.getRequestURI();
		
		// get only the file name out of path
		//filename = filename.substring(filename.lastIndexOf("/") + 1);
		
		Matcher matcher = pattern.matcher(filename);
		
		Integer formId = null;
		
		if (matcher.matches() == false) {
			log.warn("Unable to find the form id in the url: " + filename);
		}
		else {
			// get the form id out of the matcher.  This should be the first parentheses
			String formIdString = matcher.group(1);
			formId = Integer.valueOf(formIdString);
		}
		
		//File file = FormEntryUtil.getXSNFile(filename);
		FormEntryService formEntryService = (FormEntryService)Context.getService(FormEntryService.class);
		
		FormEntryXsn xsn = formEntryService.getFormEntryXsn(formId);
		
		if (xsn != null) {
			Date dateModified = xsn.getDateCreated();
			Long timeModified = dateModified.getTime();
			
			if (log.isDebugEnabled()) {
				log.debug("xsn modified date: " + dateModified);
				log.debug("xsn etag: " + timeModified);
				log.debug("length of xsn.getXsnData(): " + xsn.getXsnData().length);
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
			        "The xsn for formId '"
			        	+ formId
			            + "' cannot be found.  More than likely the XSN has not been uploaded (via Upload XSN in Form Entry administration).");
			response.sendError(404);
		}
	}

	

}
