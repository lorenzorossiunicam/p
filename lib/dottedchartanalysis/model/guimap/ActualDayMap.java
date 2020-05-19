package org.processmining.plugins.dottedchartanalysis.model.guimap;

public class ActualDayMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((int) ((time + timeOffset) / 86400000L));
	};

}
