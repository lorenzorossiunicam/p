package org.processmining.plugins.psd.utils;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class PSDLogUtils {

	public static final String TASKID = "Task ID";
	public static final String ORIGINATOR = "Originator";
	
	public static String getEltOfAte(XEvent event, String dataEltType) {
		if (dataEltType.equalsIgnoreCase(TASKID)) {
			//get task name of the audit trail entries
			return ((XAttributeLiteral) event.getAttributes().get(XConceptExtension.KEY_NAME)).getValue();
		} else if (dataEltType.equalsIgnoreCase(ORIGINATOR)) {
			//get originator of the audit trail entries
			return ((XAttributeLiteral) event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE)).getValue();
		} else {
			//get data-element which can be found in the data-part of the audit trail entries
			return ((XAttributeLiteral) event.getAttributes().get(dataEltType)).getValue();
		}
	}

	public static Date getTimestamp(XEvent event) {
		return ((XAttributeTimestamp) event.getAttributes().get(XTimeExtension.KEY_TIMESTAMP)).getValue();
	}
	
	public static String getOriginator(XEvent event) {
		return ((XAttributeLiteral) event.getAttributes().get(XOrganizationalExtension.KEY_RESOURCE)).getValue();
	}
	
	public static String getName(XTrace pi) {
		return ((XAttributeLiteral) pi.getAttributes().get(XConceptExtension.KEY_NAME)).getValue();
	}
}
