package org.openmrs.module.formentry.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Field;
import org.openmrs.FieldType;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.api.FormService;
import org.openmrs.api.context.Context;
import org.openmrs.module.formentry.FormEntryUtil;
import org.openmrs.util.FormConstants;
import org.openmrs.web.WebConstants;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Allows the user to add the needed form schema elements to selected forms
 * 
 */
public class SetupRelationshipSchemasFormController extends SimpleFormController {
	
    /** Logger for this class and subclasses */
    protected final Log log = LogFactory.getLog(getClass());

    /**
     * @see org.springframework.web.servlet.mvc.AbstractFormController#formBackingObject(javax.servlet.http.HttpServletRequest)
     */
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
    	
    	// the form backing object is all UNretired forms in the database
    	return Context.getFormService().getAllForms();
    }
    
    /**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#onSubmit(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
     */
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object object, BindException bindException) throws Exception {
	    
    	// user must be authenticated (avoids auth errors)
    	if (Context.isAuthenticated()) {
    		HttpSession httpSession = request.getSession();
			
    		FormService formService = Context.getFormService();
    		
    		FormConverter formConverter = new FormConverter();
    		
    		String[] formIds = request.getParameterValues("formId");
    		if (formIds != null) {
	    		for (String formId : formIds) {
	    			Form form = formService.getForm(Integer.valueOf(formId));
	    			
	    			// make sure the form is unpublished
    				formConverter.addOrUpdateSchema(form);
	    			formService.saveForm(form);
	    			
	    			// get the new form fields into the xsn
	    			try {
	    				FormEntryUtil.rebuildXSN(form);
	    			}
	    			catch (IOException io) {
	    				log.warn("unable to rebuild the xsn for form" + form, io);
	    			}
	    			
	    			// at least one form was successful
	    			httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "formentry.relationships.formsSaved");
	    			// at least one form was not successful
	    			//httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "formentry.relationships.publishedAlready");
	    		}
    		}
    		else
    			// error. nothing to loop over.
    			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "formentry.relationships.selectAForm");
    		
    	}
    	
	    return showForm(request, response, bindException);
    }

	/**
     * @see org.springframework.web.servlet.mvc.SimpleFormController#referenceData(javax.servlet.http.HttpServletRequest, java.lang.Object, org.springframework.validation.Errors)
     */
    protected Map referenceData(HttpServletRequest arg0, Object arg1, Errors arg2) throws Exception {
    	return new HashMap<String, Object>();
}
	
    /**
     * Converts the form to have the fields/formFields necessary to be a remote form
     * NOTE: stolen from RemoteFormEntry
     * TODO: do something about this ... perhaps refactor it to be useful as a form schema utility?
     */
    private class FormConverter {
    	// node names for relationship schema elements
    	private static final String PATIENT = "patient";
    	private static final String PATIENT_RELATIONSHIP = "patient_relationship";
    	private static final String PATIENT_RELATIONSHIP_TYPE = "patient_relationship.relationship_type_id";
    	private static final String PATIENT_RELATIONSHIP_A_OR_B = "patient_relationship.a_or_b";
    	private static final String PATIENT_RELATIONSHIP_DESCRIPTION = "patient_relationship.description";
    	private static final String PATIENT_RELATIONSHIP_REVERSE_DESCRIPTION = "patient_relationship.reverse_description";
    	private static final String PATIENT_RELATIONSHIP_EXISTS = "patient_relationship.exists";
    	private static final String PATIENT_RELATIONSHIP_VOIDED = "patient_relationship.voided";
    	private static final String RELATIVE = "relative";
    	private static final String RELATIVE_UUID = "relative.uuid";
    	private static final String RELATIVE_IDENTIFIER = "relative.identifier";
    	private static final String RELATIVE_IDENTIFIER_TYPE = "relative.identifier_type";
    	private static final String RELATIVE_IDENTIFIER_LOC = "relative.location";
    	private static final String RELATIVE_GIVEN_NAME = "relative.given_name";
    	private static final String RELATIVE_MIDDLE_NAME = "relative.middle_name";
    	private static final String RELATIVE_FAMILY_NAME = "relative.family_name";
    	private static final String RELATIVE_GENDER = "relative.gender";
    	private static final String RELATIVE_BIRTHDATE = "relative.birthdate";

    	// all first level property nodes can be found in this section:

    	private Log log = LogFactory.getLog(getClass());
    	
    	// incremented value for the sort weights so that the fields show up in the order that they are in the hashmap
    	private Integer formFieldSortWeight = 0;

    	// cached FormService object
    	private FormService formService;
    	
    	/**
    	 * Adds the schema required by the remote form entry module to the given
    	 * form
    	 * 
    	 * If the form already has the root formField, it and all child elements are
    	 * removed and new formfields are added.
    	 * 
    	 * @param form Form to update
    	 */
    	public void addOrUpdateSchema(Form form) {

    		Map<String, FormField> map = getFormFieldMap(form);

			FormField relationshipFormField = map.get(PATIENT_RELATIONSHIP.toUpperCase());

    		while(relationshipFormField != null) {
    			// there is a PATIENT_RELATIONSHIP element, remove it so
    			// we can just add all of the new ones
    			removeFormFieldAndChildren(form, relationshipFormField);
    			// cannot refer to map variable because it is not updated
    			relationshipFormField = getFormFieldMap(form).get(PATIENT_RELATIONSHIP.toUpperCase());
    		}

    		// check for patient field before adding relationship fields
			FormField patientFormField = map.get(PATIENT.toUpperCase());
    		if (patientFormField == null) {
    			// add the PATIENT field if it does not exist
    			// TODO make this optional
				patientFormField = getNewFormField(PATIENT.toUpperCase(), null, true, null, null, "");
				form.addFormField(patientFormField);
			}
    		
    		addAllFields(form, patientFormField);
    	}

    	/**
    	 * Recursively removes the given FormField object and its children
    	 * formFields on the given form
    	 * 
    	 * @param form Form which to remove to formField from
    	 * @param parentFormField FormField to remove
    	 */
    	private void removeFormFieldAndChildren(Form form, FormField parentFormField) {

    		for (FormField ff : form.getFormFields().toArray(new FormField[] {})) {
    			if (ff.getParent() != null && ff.getParent().equals(parentFormField))
    				removeFormFieldAndChildren(form, ff);
    		}

    		form.removeFormField(parentFormField);
    	}

    	/**
    	 * Creates blank formFields and adds them to the form. If a field already
    	 * exists in the db, that is used
    	 * 
    	 * @param form Form to add the fields to
    	 */
    	private void addAllFields(Form form, FormField rootFormField) {
    		log.debug("Adding relationship fields to form: " + form);
    		
    		// TODO surely there is a better way to do this

    		// example schema layout
    		// PATIENT (assumed)
    		//  PATIENT_RELATIONSHIP
    		//   PATIENT_RELATIONSHIP_TYPE_ID
    		//   PATIENT_RELATIONSHIP_SIDE
    		//   PATIENT_RELATIONSHIP_VOIDED
    		//   RELATIVE
    		//    RELATIVE.UUID
    		//    RELATIVE.IDENTIFIER
    		//   ...
    		// ...
    		FormField tmpFormField = null;
    		
    		// mapping from field name to default value
    		Map<String, String> fieldNames = new LinkedHashMap<String, String>();
    		
    		// create the Relationships
			FormField relationshipFormField = getNewFormField(
					PATIENT_RELATIONSHIP, rootFormField, true, 0, -1,
					"$!{relationships}");
			form.addFormField(relationshipFormField);
    		
    		fieldNames.clear();
    		fieldNames.put(PATIENT_RELATIONSHIP_TYPE, "$!{listItem.getRelationshipType().getRelationshipTypeId()}");
    		fieldNames.put(PATIENT_RELATIONSHIP_A_OR_B, "#if($!{listItem.getPersonA().getPersonId()} == ${patient.getPersonId()})" +
    			"B #set($otherPerson=${listItem.getPersonB()}) " +
    			"#set($relDescription=${listItem.getRelationshipType().getbIsToA()}) " +
    			"#set($relReverseDescription=${listItem.getRelationshipType().getaIsToB()}) " +
    			"#else " +
    			"A #set($otherPerson=$listItem.getPersonA()) " +
    			"#set($relDescription=${listItem.getRelationshipType().getaIsToB()}) " +
    			"#set($relReverseDescription=${listItem.getRelationshipType().getbIsToA()}) " +
    			"#end");
    		fieldNames.put(PATIENT_RELATIONSHIP_DESCRIPTION, "$!{relDescription}");
    		fieldNames.put(PATIENT_RELATIONSHIP_REVERSE_DESCRIPTION, "$!{relReverseDescription}");
    		fieldNames.put(PATIENT_RELATIONSHIP_EXISTS, "1");
    		fieldNames.put(PATIENT_RELATIONSHIP_VOIDED, "$!{listItem.isVoided()}");
    		for (Map.Entry<String, String> entry : fieldNames.entrySet()) {
    			tmpFormField = getNewFormField(entry.getKey(),
    			                               relationshipFormField, false, null, null,
    			                               entry.getValue());
    			form.addFormField(tmpFormField);
    		}

    		FormField relativeFormField = getNewFormField(RELATIVE,
                relationshipFormField, true, 1, 1, "");
    		form.addFormField(relativeFormField);

    		fieldNames.clear();
    		fieldNames.put(RELATIVE_UUID, "$!{otherPerson.getUuid()}");
    		fieldNames.put(RELATIVE_IDENTIFIER, "$!{otherPerson.getPatientIdentifier()}");
    		fieldNames.put(RELATIVE_IDENTIFIER_TYPE, "$!{otherPerson.getPatientIdentifier().getIdentifierType().getPatientIdentifierTypeId()}");
    		fieldNames.put(RELATIVE_IDENTIFIER_LOC, "$!{otherPerson.getPatientIdentifier().getLocation().getLocationId()}");
    		fieldNames.put(RELATIVE_BIRTHDATE, "$!{date.format($otherPerson.getBirthdate())}");
    		fieldNames.put(RELATIVE_GENDER, "$!{otherPerson.getGender()}");
    		fieldNames.put(RELATIVE_GIVEN_NAME, "$!{otherPerson.getGivenName()}");
    		fieldNames.put(RELATIVE_MIDDLE_NAME, "$!{otherPerson.getMiddleName()}");
    		fieldNames.put(RELATIVE_FAMILY_NAME, "$!{otherPerson.getFamilyName()}");
    		for (Map.Entry<String, String> entry : fieldNames.entrySet()) {
    			tmpFormField = getNewFormField(entry.getKey(),
    			                               relativeFormField, false, null, null,
    			                               entry.getValue());
    			form.addFormField(tmpFormField);
    		}
    	
    	}

    	/**
    	 * Creates a new form field with its field having the given name. The db is
    	 * searched for the given field name and if found, that field is used
    	 * instead
    	 * 
    	 * If this is not a section, database element is assumed and the db/attr names are derived from the fieldName.  
    	 * Assumed to be DB.ATTR
    	 * 
    	 * @param fieldName name of the Field to fetch or create
    	 * @param parentFormField FormField this object will be the parent of the
    	 *        newly created/returned formField
    	 * @param isSection true/false whether or not this field should be a section type.
    	 * @param minOccurs Integer minimum number of occurances for this formfield.  If 
    	 * 		  not required, should be 0, otherwise, null
    	 * @param maxOccurs Integer maximum occurances for this formfield. Commonly -1 or null    
    	 * @param defaultValue String to be to the default value of the field if it
    	 *        doesn't exist
    	 * @return FormField with a null formFieldId
    	 */
    	private FormField getNewFormField(String fieldName, FormField parentFormField, 
    			boolean isSection, Integer minOccurs, Integer maxOccurs, String defaultValue) {
    		
    		String upperFieldName = fieldName.toUpperCase();
    		
    		// try and find the field with the given fieldName
    		Field field = null;
    		List<Field> fields = getFormService().getFields(upperFieldName);
    		for (Field tmpField : fields) {
    			if (tmpField.getName().equals(upperFieldName)) {
    				field = tmpField;
    				break;
    			}
    		}

    		// create a blank field with this name and value if none exists
    		if (field == null) {
    			field = new Field();
    			field.setName(upperFieldName);
    			field.setCreator(Context.getAuthenticatedUser());
    			field.setDateCreated(new Date());
    			field.setUuid(UUID.randomUUID().toString());
    		}
    		field.setDefaultValue(defaultValue);
    		
    		if (isSection)
    			field.setFieldType(new FieldType(FormConstants.FIELD_TYPE_SECTION));
    		else {
    			field.setFieldType(new FieldType(FormConstants.FIELD_TYPE_DATABASE));
    			// all non-section names are expected to be in format db.attr
    			String[] tableAndAttr = fieldName.split("\\.");
    			field.setTableName(tableAndAttr[0]);
    			field.setAttributeName(tableAndAttr[1]);
    		}
    		
    		// create the new formfield object
    		FormField newFormField = new FormField();
    		newFormField.setField(field);
    		newFormField.setMinOccurs(minOccurs);
    		newFormField.setMaxOccurs(maxOccurs);
    		newFormField.setParent(parentFormField);
    		newFormField.setSortWeight(Float.valueOf(formFieldSortWeight++));

    		return newFormField;
    	}

    	/**
    	 * Turns all of the formFields in the given form into a map from name to
    	 * formfield object
    	 * 
    	 * @param form Form to get the formFields from
    	 * @return Map<String, FormFiel> mapping from each formField's field.name
    	 *         to the formField object
    	 */
    	protected Map<String, FormField> getFormFieldMap(Form form) {

    		Map<String, FormField> map = new HashMap<String, FormField>();

    		for (FormField formField : form.getFormFields()) {
    			String name = formField.getField().getName();
    			map.put(name, formField);
    		}

    		return map;
    	}

    	/**
    	 * Get and cache the OpenMRS FormService
    	 * 
    	 * @return FormService from Context
    	 */
    	private FormService getFormService() {
    		if (formService == null)
    			formService = Context.getFormService();

    		return formService;
    	}
    	
    }

}
