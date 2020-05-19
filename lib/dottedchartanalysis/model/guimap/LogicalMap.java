package org.processmining.plugins.dottedchartanalysis.model.guimap;

public class LogicalMap extends GuiMap {
	public String getKey(long time) {
		return String.valueOf(time);
	};
}
