package org.processmining.plugins.tsanalyzer.annotation;

import java.util.Date;

import org.deckfour.xes.extension.std.XExtendedEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.transitionsystem.miner.util.TSEventCache;
import org.processmining.plugins.tsanalyzer.annotation.time.Duration;

public class AnnotationTimeConfiguration implements AnnotationConfiguration {

	private TSEventCache eventCache;

	public AnnotationTimeConfiguration() {
		eventCache = new TSEventCache();
	}

	public long getValue(XTrace trace, int eventIndex) {
		Date timestamp = getExtendedEvent(trace, eventIndex).getTimestamp();
		if (timestamp != null) {
			return timestamp.getTime();
		}
		return -1;
	}

	public long getMinValue(XTrace trace) {
		for (int i = 0; i < trace.size(); i++) {
			Date timestamp = getExtendedEvent(trace, i).getTimestamp();
			if (timestamp != null) {
				return timestamp.getTime();
			}
		}
		return -1;
	}

	public long getMaxValue(XTrace trace) {
		for (int i = 0; i < trace.size(); i++) {
			Date timestamp = getExtendedEvent(trace, trace.size() - i - 1)
					.getTimestamp();
			if (timestamp != null) {
				return timestamp.getTime();
			}
		}
		return -1;
	}

	private XExtendedEvent getExtendedEvent(XTrace trace, int index) {
		return XExtendedEvent.wrap(eventCache.get(trace, index));
	}
	
	public String getString(long value) {
		eventCache = null;
		return new Duration(value).toString();
	}
	
	public String getName(String name) {
		if (name.equalsIgnoreCase("sojourn")) {
			return "Sojourn time";
		} else if (name.equalsIgnoreCase("remaining")) {
			return "Remaining time";
		} else if (name.equalsIgnoreCase("elapsed")) {
			return "Elapsed time";
		} else if (name.equalsIgnoreCase("duration")) {
			return "Duration";
		}
		return null;
	}
}
