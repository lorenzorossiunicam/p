package org.processmining.plugins.fuzzymodel.miner.filter;

import java.util.Date;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;

public class FMEventTimeInjectionFilter {

	protected long refTime = System.currentTimeMillis();
	protected long interInstanceIncrement = 60000;
	protected long interEventIncrement = 10000;

	public static final String EVENT_TIME_KEY = "time:timestamp";

	public void doFiltering(XTrace oldTrace) {
		for (int eventIndex = 0; eventIndex < oldTrace.size(); eventIndex++) {
			XEvent oldEvent = oldTrace.get(eventIndex);
			XEvent newEvent = (XEvent) oldEvent.clone();
			refTime += interInstanceIncrement;
		//	System.out.println("The Attributes count is " + newEvent.getAttributes().size() );
			XAttributeMap attMap = newEvent.getAttributes();			
			XAttributeTimestampImpl timestampAttr = new XAttributeTimestampImpl(EVENT_TIME_KEY,new Date(refTime));
			attMap.put(EVENT_TIME_KEY, timestampAttr);
			newEvent.setAttributes(attMap);
//			if(newEvent.getAttributes().containsKey(EVENT_TIME_KEY))
//			{
//				((XAttributeTimestampImpl) newEvent.getAttributes().put(EVENT_TIME_KEY,new Date(refTime));
//			}
			oldTrace.set(eventIndex, newEvent);
			refTime += interEventIncrement;
		}
	}
}
