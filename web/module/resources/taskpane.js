
if (window.external != null) {
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

// Close the InfoPath TaskPane
function closeTaskPane() {
	window.external.Window.XDocument.View.Window.TaskPanes.Item(0).Visible = false;
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
	return obj.key + "^" + obj.value.toUpperCase() + "^99DCT";
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

// add this concept as an answer to the given node
function pickConcept(nodeName, concept, createConceptList, extraMap) {
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
	else {
		if (extraMap) {
			var newParentNode = cloneAndInsertNode(node.parentNode);
			
			var newConceptNode = newParentNode.selectSingleNode(node.nodeName);
			var newConceptNodeValueNode = newConceptNode.selectSingleNode("value");
			clearNil(newConceptNodeValueNode);
			newConceptNodeValueNode.text = getConceptNodeValue(concept);

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
