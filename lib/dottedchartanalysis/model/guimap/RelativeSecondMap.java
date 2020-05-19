package org.processmining.plugins.dottedchartanalysis.model.guimap;

public class RelativeSecondMap extends GuiMap {

	public String getKey(long time) {
		return String.valueOf((time) / 1000L);
	};

}
