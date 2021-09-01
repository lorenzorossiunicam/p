package it.unicam.pros.purple.util.eventlogs.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class LogIO {

	public static void saveXES(EventLog log, String file) {

//		//Creating assignments
//		HashMap<String, String> assignment = new HashMap<String, String>();
//		assignment.put("A","5");
//		assignment.put("B","2");
//		
//		//Creating messages
//		ArrayList<String> message = new ArrayList<String>();
//	    message.add("Tuple position 0 in message");
//	    message.add("Tuple position 1 in message");
//	    
//	    //assigning a pool
//		String pool = "pool 1";
		// Generating the XES file
		try {
			Xlogger.exportXML(getXLog(log), file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ByteArrayOutputStream getAsStream(EventLog log){
		try {
			return Xlogger.exportXMLStream(getXLog(log));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);
	}



	public static XLog getXLog(EventLog log) {
		XLog l = Xlogger.generateNewXLog(log.getName());

		// Adding the dataStore to the log l
		if (log.getData() != null) {
			Xlogger.decorateElement(l, "Data Stores", log.getData());
		}
		// Generating traces
		for (Trace trace : log.getTraces()) {
			XTrace t = Xlogger.insertTrace(l, trace.getCaseID());
			// Adding the dataObjects
			if (trace.getData() != null) {
				Xlogger.decorateElement(t, "Data Objects", trace.getData());
			}
			for (Event event : trace.getTrace()) {
				// Generating events
				Map<String, String> assigns = null;
				if (event.getAssignments() != null) {
					assigns = new HashMap<String, String>();
					for (String a : event.getAssignments()) {
						String[] kV = a.split("=");
						assigns.put(kV[0], kV[1]);
					}
				}
				String state = null;
				if (event.getState() != null) {
					state = event.getState().name();
				}
				String sOr = null;
				if (event.isSendOrReceive() != null) {
					sOr = event.isSendOrReceive().name();
				}
				String cost = null;
				if(event.getCost() != null){
					cost = event.getCost().toString();
				}
				Xlogger.insertEvent(t, event.getProcess(), event.getEventName(), event.getTimestamp(), state, assigns,
						sOr, event.getMsgName(), event.getMessage(), event.getCounter(), cost);
			}
		}

		return l;
	}

    public static void saveCollaborativeXES(EventLog log, String s) {

    }
}
