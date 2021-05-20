package it.unicam.pros.purple.util.eventlogs.utils;


import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.deckfour.xes.out.XesXmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class Xlogger {
	
	
	private static XFactory xesFactory = new XFactoryNaiveImpl();
	private static XExtensionManager xesExtensionManager = XExtensionManager.instance();
	
	/* This method generates and returns a new log
	 * 
	 * @param logName the name of the new log
	 * @return the generated log
	 */
	public static XLog generateNewXLog(String logName) {
		XLog log;
		log = xesFactory.createLog();
		decorateElement(log, "concept:name", logName, "Concept");
		log.getExtensions().add(xesExtensionManager.getByName("Concept"));
		log.getExtensions().add(xesExtensionManager.getByName("Organizational"));
		log.getExtensions().add(xesExtensionManager.getByName("Time"));
		log.getExtensions().add(xesExtensionManager.getByName("Identity"));
		
		return log;
	}
	
	/*
	 * This method generates and returns a new trace
	 * 
	 * @param log: the log which is a parent to the trace
	 * @param caseid: the name of the trace
	 * @return the generated trace
	 */
	public static XTrace insertTrace(XLog log, String caseId) {
		if (log == null) {
			return null;
		}
		XTrace trace = xesFactory.createTrace();
		decorateElement(trace, "concept:name", caseId, "Concept");
		log.add(trace);
		return trace;
	}
	
	/*
	 * This method generates and returns a new event
	 * 
	 * @param trace: the trace which is a parent to the event
	 * @param timestamp: the time at which the event occurred
	 * @param activityName: the name of the task or activity the event is referred to
	 * @param guard: the boolean expression which determines whether the event should be triggered
	 * @param assignments: The hashmap of variables which is being reassigned during the event
	 * @param pool: the ID of the pool which the event is a part of
	 * @param msgID: the ID of the message which is being send of received
	 * @param message: the message that is send or received during the event
	 * @param msgType: a boolean indicating if the message is being send or received
	 * @param counter: a counter used for multi-instance tasks
	 * returns the generated event
	 */
	public static XEvent insertEvent(XTrace trace, String pool, String activityName, Date timestamp, 
			 String state, Map<String, String>assignments, String msgType,
			String msgName, Map<String, String> message, Integer counter, String cost) {
		if (trace == null) {
			return null;
		}
		XEvent event = xesFactory.createEvent();
		decorateElement(event, "org:group", pool, "Organizational");
		decorateElement(event, "concept:name", activityName, "Concept");
		decorateElement(event, "time:timestamp", timestamp, "Time");
		decorateElement(event, "lifecycle:transition", state);
		decorateElement(event, "assignments", assignments);
		decorateElement(event, "messageType", msgType);
		decorateElement(event, "messageID", msgName);
		decorateElement(event, "message", message);
		decorateElement(event, "counter", counter);
		decorateElement(event, "fixedCost", cost);
		trace.add(event);
		return event;
	}
	
	
	/*
	 * The method decorates an attribute with a long
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 * @param extensionName: the extension name
	 */
	public static void decorateElement(XAttributable element, String attributeName, Long value, String extensionName) {
		if (element == null || value == null) {
			return;
		}
		XAttributeDiscrete attribute = xesFactory.createAttributeDiscrete(attributeName, value, xesExtensionManager.getByName(extensionName));
		XAttributeMap attributes = element.getAttributes();
		if (attributes == null) {
			attributes = xesFactory.createAttributeMap();
		}
		attributes.put(attributeName, attribute);
		element.setAttributes(attributes);
	}
	
	/*
	 * The method decorates an attribute with a boolean
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 */
	public static void decorateElement(XAttributable element, String attributeName, Boolean value) {
		if (value == null) {
			return;
		}
		decorateElement(element, attributeName, (long) (value?1:0), null);
	}
	
	/*
	 * The method decorates an attribute with an integer
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 */
	public static void decorateElement(XAttributable element, String attributeName, Integer value) {
		if (value == null) {
			return;
		}
		decorateElement(element, attributeName, value.longValue(), null);
	}
	
	/*
	 * The method decorates an attribute with a list
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param map: the hashmap which is being added as a list
	 */
	public static void decorateElement(XAttributable element, String attributeName, Map<String, String> map) {
		if (element == null || map == null) {
			return;
		}
		
		XAttributeList attributeList = xesFactory.createAttributeList(attributeName, null);
		
		for (String key : map.keySet()) {
				XAttributeLiteral attribute = xesFactory.createAttributeLiteral(key, map.get(key), null);
				attributeList.addToCollection(attribute);
			
		}
		
		XAttributeMap attributes = element.getAttributes();
		if (attributes == null || attributes.isEmpty()) {
			attributes = xesFactory.createAttributeMap();
		}
		attributes.put(attributeName, attributeList);
		element.setAttributes(attributes);
	}
	
	/*
	 * The method decorates an attribute with a boolean
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param list: the list which is being added as a list
	 */
	public static void decorateElement(XAttributable element, String attributeName, ArrayList<String> list) {
		if (element == null  || list == null) {
			return;
		}
		
		XAttributeList attributeList = xesFactory.createAttributeList(attributeName, null);
		
		for (String s : list) {
			XAttributeLiteral attribute = xesFactory.createAttributeLiteral(s, "", null);
			attributeList.addToCollection(attribute);
		}
		
		XAttributeMap attributes = element.getAttributes();
		if (attributes == null || attributes.isEmpty()) {
			attributes = xesFactory.createAttributeMap();
		}
		attributes.put(attributeName, attributeList);
		element.setAttributes(attributes);
	}
	
	
	
	/*
	 * The method decorates an attribute with a string and an extension
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 * @param extensionName: the name of the extension
	 */
	public static void decorateElement(XAttributable element, String attributeName, String value, String extensionName) {
		if (element == null  || value == null) {
			return;
		}
		XAttributeLiteral attribute = xesFactory.createAttributeLiteral(attributeName, value, xesExtensionManager.getByName(extensionName));
		XAttributeMap attributes = element.getAttributes();
		if (attributes == null || attributes.isEmpty()) {
			attributes = xesFactory.createAttributeMap();
		}
		attributes.put(attributeName, attribute);
		element.setAttributes(attributes);
	}
	
	/*
	 * The method decorates an attribute with a string and an extension
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 */
	public static void decorateElement(XAttributable element, String attributeName, String value) {
		decorateElement(element, attributeName, value, null);
	}
	
	
	/*
	 * The method decorates an attribute with a timestamp
	 * 
	 * @param element: the element which is being decorated
	 * @param attributeName: the attribute name
	 * @param value: the attribute value
	 * @param extensionName: the name of the extension
	 */
	public static void decorateElement(XAttributable element, String attributeName, Date value, String extensionName) {
		if (element == null || value == null) {
			return;
		}
		XAttributeTimestamp attribute = xesFactory.createAttributeTimestamp(attributeName, value, xesExtensionManager.getByName(extensionName));
		XAttributeMap attributes = element.getAttributes();
		if (attributes == null) {
			attributes = xesFactory.createAttributeMap();
		}
		attributes.put(attributeName, attribute);
		element.setAttributes(attributes);
	}
	
	
	/*
	 * The method exports a log to an XML file
	 * 
	 * @param log: the log which is being exported
	 * @param location: the location of the generated file
	 */
	public static void exportXML(XLog log, String location) throws IOException {
		XesXmlSerializer XS = new XesXmlSerializer();
	  	OutputStream out = new FileOutputStream(location);
	  	XS.serialize(log, out);
	}

	public static ByteArrayOutputStream exportXMLStream(XLog log) throws IOException {
		XesXmlSerializer XS = new XesXmlSerializer();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		XS.serialize(log, out);
		return out;
	}



}




