package org.processmining.plugins.interactivevisualization;

import javax.swing.JComponent;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;

/**
 * The class that supports the initialization of interactive visualizations for the preprocessing framework.
 * 
 * This class is used for the initialization of the interactive visualization.
 * 
 * @author Danny van Heumen
 */
public interface OverviewVisualizationInitializer {
	
	/**
	 * The category name for the interactive visualization. This name can be used to group visualizations
	 * by function. For example: use 'General' for a general visualization. 'Control Flow' can be used
	 * for control flow-centric visualizations such as process mining visualizations or trace alignment. 
	 * 
	 * @return Returns the name of the category.
	 */
	public String getCategory();
	
	/**
	 * The name of the interactive visualization as it is shown in the preprocessing framework.
	 * 
	 * @return Returns the name of the interactive visualization.
	 */
	public String getName();
	
	/**
	 * The method that initializes the visualizations and returns its main UI component to the
	 * preprocessing framework.
	 * 
	 * @param pluginContext The (ProM) plugin context that can be used during initialization.
	 * @param log The event log on which to base the visualization.
	 * @param summary The event log summary for the provided event log.
	 * @return Returns the main UI component of the visualization.
	 */
	public JComponent initialize(UIPluginContext pluginContext, XLog log, XLogInfo summary);
}
