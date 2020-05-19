package org.processmining.plugins.dottedchartanalysis;

import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;

public class LogUtils {

	public final static String NONAME = "<no name>";
	public final static String NORESOURCE = "<no resource>";
	public final static String NOTRANSITION = "<no transition>";
	public final static Date NODATE = new Date();
	
	public static String extractName(XAttributable attributable) {
		String name = XConceptExtension.instance().extractName(attributable);
		if (name == null) {
			name = NONAME;
		}
		return name;
	}

	public static String extractResource(XEvent event) {
		String name = XOrganizationalExtension.instance().extractResource(event);
		if (name == null) {
			name = NORESOURCE;
		}
		return name;
	}

	public static String extractTransition(XEvent event) {
		String name = XLifecycleExtension.instance().extractTransition(event);
		if (name == null) {
			name = NOTRANSITION;
		}
		return name;
	}

	public static Date extractTimestamp(XEvent event) {
		Date date = XTimeExtension.instance().extractTimestamp(event);
		if (date == null) {
			date = NODATE;
		}
		return date;
	}
}
