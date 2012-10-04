
if (window.external != null && window.external.Window != null) {
	// Reference to InfoPath document
	var oXDocument = window.external.Window.XDocument;
}

if (typeof oXDocument != 'undefined') {
	// Reference to XML DOM in InfoPath's active window
	var oDOM = oXDocument.DOM;
}

// Clear xsi:nil entry (needed before adding children to nodes)
function clearNil(node) {
	// The xsi:nil needs to be removed before we set the value.
	if (node.getAttribute("xsi:nil"))
		node.removeAttribute("xsi:nil");
}

// Add xsi:nil entry
function addNil(node) {
	// The xsi:nil 
	if (node.getAttribute("xsi:nil") == null) {
		var xmlNil = node.ownerDocument.createNode(2, "xsi:nil", "http://www.w3.org/2001/XMLSchema-instance");
		xmlNil.text = "true";
		node.setAttributeNode(xmlNil);
	}
}

// set nodeName's value to obj
function setObj(nodeName, obj) {
	// Fetch reference to the node
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);

	// Set value of node to the obj
	node.text = obj.key + '^' + obj.value;

	closeTaskPane();
}

// returns HL7 version of problem for inserting into form
function getConceptNodeValue(obj) {
	var str = obj.key + "^" + obj.value.toUpperCase() + "^99DCT";
	
	// if the obj has a name key in it, make the triplet into a sextuplet
	if (obj.nameKey)
		str = str +  "^" + obj.nameKey + "^" + obj.nameValue + "^99NAM";
	
	return str;
}

// add problem (diagnosis) concept to a list (new problems or resolved problems)
function pickProblem(mode, nodeName, obj) {
	var node = oDOM.selectSingleNode(nodeName);
	clearNil(node);
	var newProblem;
	var nodeName;
	if (mode == 'add') {
		newProblem = true;
		nodeName = "problem_added";
	} else if (mode == 'remove') {
		newProblem = false;
		nodeName = "problem_resolved";
	} else {
		return;
	}
	
	var refNode = node.selectSingleNode(nodeName);
	var valueNode = refNode.selectSingleNode("value");
	if (valueNode.text == "") {
		clearNil(valueNode);
		valueNode.text = getConceptNodeValue(obj);
	} else {
		// create new elem as clone
		var newElem = refNode.cloneNode(true);
		var newElemValue = newElem.selectSingleNode("value");
		clearNil(newElemValue);

		// insert *before* setting value to avoid a bug where value is corrupted during insert
		if (newProblem) {
			var firstResolved = node.selectSingleNode("problem_resolved");
			node.insertBefore(newElem, firstResolved);
		} else {
			node.appendChild(newElem);
		}

		// value must be set *after* inserting node; otherwise it gets munged
		newElemValue.text = getConceptNodeValue(obj);
	}

	closeTaskPane();
}

/**
* add this concept as an answer to the given node
*
* nodeName
* concept
* createConceptList if true, will add new row.  if false, will replace current value at `nodePath`
* extraMap key-value mapping of path to value
* hasOtherSiblings if true, will assume `nodePath` is in obsGroup and duplicate parent element, if
*			false, will duplicate only single element.  (redundant if extraMap is non-null) 
*/
function pickConcept(nodeName, concept, createConceptList, extraMap, hasOtherSiblings) {
	var node = oDOM.selectSingleNode(nodeName);
	if (node == null) {
		alert("ERROR 2345: Node '" + nodeName + "' was not found");
		return;
	}
	clearNil(node);
	var valueNode = node.selectSingleNode("value");
	if (valueNode == null) {
		alert("ERROR 2444: 'value' node inside of node '" + nodeName + "' was not found");
		return;
	}
	clearNil(valueNode);
	if (valueNode.text == "" || createConceptList == "") {
		valueNode.text = getConceptNodeValue(concept);
		if (extraMap) {
			for (var i=0; i < extraMap.length; i++) {
				var entry = extraMap[i];
				/* Only update the extra info if the user puts in a value.  This gets around
				   infopath's problem with empty strings in integer fields */
				if (entry != null && entry.value != null && entry.value.length > 0) {
					var entryNode = node.selectSingleNode(entry.key);
					if (entryNode == null)
						alert("entryNode not found: " + entry.key);
					else {
						var valueNode = entryNode.selectSingleNode("value");
						clearNil(valueNode);
						valueNode.text = entry.value;
					}
				}
			}
		}
	}
	else {
		if (extraMap || hasOtherSiblings) {
			var newParentNode = cloneAndInsertNode(node.parentNode);
			
			var newConceptNode = newParentNode.selectSingleNode(node.nodeName);
			var newConceptNodeValueNode = newConceptNode.selectSingleNode("value");
			clearNil(newConceptNodeValueNode);
			newConceptNodeValueNode.text = getConceptNodeValue(concept);
			
			// if there are other siblings, clear the values in those so as to 
			// require the user to fill them in 
			if (hasOtherSiblings) {
				var childNodes = newParentNode.childNodes;
				
				for (var i=0; i < childNodes.length; i++) {
					var childNode = childNodes[i];
					
					// if not blank text and not the concept we just filled in, erase the text content
					if (childNode && childNode != newConceptNode) {
						var innerValueNode = childNode.selectSingleNode("value");
						if (innerValueNode) {
							innerValueNode.text = "";
							addNil(innerValueNode);
						}
					}
				}
			}
			
			if (extraMap) {
				for (var i=0; i < extraMap.length; i++) {
					var entry = extraMap[i];
					// get the first node defined for this type of extra value (just for nodeName of it)
					var refNode = node.parentNode.selectSingleNode(entry.key);
					
					// this extraValue's node
					var newExtraValueNode = newParentNode.selectSingleNode(refNode.nodeName);
					// set the extraValue as text
					valueNode = newExtraValueNode.selectSingleNode("value");
					clearNil(valueNode);
					valueNode.text = entry.value;
				}
			}
		}
		else {
			// create new elem as clone
			var newElem = cloneAndInsertNode(node);
			
			// get and clear the value of the new cloned node
			var newElemValue = newElem.selectSingleNode("value");
			clearNil(newElemValue);
		
			// value must be set *after* inserting node; otherwise it gets munged
			newElemValue.text = getConceptNodeValue(concept);
		}
	}
	
	

	closeTaskPane();
}

/** 
 * adds a relationship to the patient node in the form
 * 
 * @param relationship JSON object containing relationship data and relative description 
 */
function pickRelationship(relationship) {
	var refNode = oDOM.selectSingleNode("//patient/patient_relationship");
	
	// freak out if a patient_relationship node cannot be found
	if (refNode == null) {
		alert("ERROR: No existing patient_relationship element to copy");
		closeTaskPane();
		return;
	}

	// add relationship specifics
	var newNode = cloneAndInsertNode(refNode);
	newNode.selectSingleNode("patient_relationship.relationship_type_id").text = nullToBlank(relationship.type);
	newNode.selectSingleNode("patient_relationship.a_or_b").text = nullToBlank(relationship.aOrB);
	newNode.selectSingleNode("patient_relationship.description").text = nullToBlank(relationship.description);
	newNode.selectSingleNode("patient_relationship.reverse_description").text = nullToBlank(relationship.reverseDescription);
	newNode.selectSingleNode("patient_relationship.exists").text = 0;
	newNode.selectSingleNode("patient_relationship.voided").text = "false";

	// add relative information
	var relativeNode = newNode.selectSingleNode("relative"); 
	relativeNode.selectSingleNode("relative.uuid").text = nullToBlank(relationship.relative.uuid);
	relativeNode.selectSingleNode("relative.identifier").text = ("identifier" in relationship.relative) ? nullToBlank(relationship.relative.identifier) : "";
	relativeNode.selectSingleNode("relative.identifier_type").text = ("identifierTypeName" in relationship.relative) ? nullToBlank(relationship.relative.identifierTypeName) : "";
	relativeNode.selectSingleNode("relative.birthdate").text = formatHL7Date(relationship.relative.birthdate);
	relativeNode.selectSingleNode("relative.gender").text = nullToBlank(relationship.relative.gender);
	relativeNode.selectSingleNode("relative.given_name").text = nullToBlank(relationship.relative.givenName);
	relativeNode.selectSingleNode("relative.middle_name").text = nullToBlank(relationship.relative.middleName);
	relativeNode.selectSingleNode("relative.family_name").text = nullToBlank(relationship.relative.familyName);
	// TODO: somehow acquire location from PatientService
	relativeNode.selectSingleNode("relative.location").text = "";

	// get out
	closeTaskPane();
}
/**
 * formats a date object into HL7 format (i.e. 3/29/2010 --> 20100329)
 * 
 * @param datetime the date object to format
 * @return the formatted date as a string
 */
function formatHL7Date(datetime) {
	if (datetime == null) { return ""; }
	out = '' + datetime.getFullYear();
	out += (datetime.getMonth() + 1 < 10) ? '0' + (datetime.getMonth() + 1) : datetime.getMonth() + 1;
	out += (datetime.getDate() < 10) ? '0' + datetime.getDate() : datetime.getDate();
	return out;
}

/**
 * converts an object to blank text if null, otherwise sends it back
 * 
 * @param text string to check for null
 * @return empty string if text is null, otherwise text
 */
function nullToBlank(text) {
	return (text == null) ? "" : text;
}

function cloneAndInsertNode(node) {
	var newNode = node.cloneNode(true);
	// if node isn't the last node in parent's list, find who to insert before
	var nextSibling = node.nextSibling;
	while (nextSibling != null && (nextSibling.nodeName == node.nodeName || nextSibling.nodeType != node.nodeType))
		nextSibling = nextSibling.nextSibling;
	
	if (nextSibling == null)
		node.parentNode.appendChild(newNode);
	else
		node.parentNode.insertBefore(newNode, nextSibling);
	
	return newNode;
}

//	hide taskpane
function closeTaskPane() {
     window.location = "index.htm";
}

function reloadPage() {
  document.location = document.location
}
