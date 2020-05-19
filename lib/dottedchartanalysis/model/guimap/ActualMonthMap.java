package org.processmining.plugins.dottedchartanalysis.model.guimap;

public class ActualMonthMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time + timeOffset) / 2592000000L);
	};

}
