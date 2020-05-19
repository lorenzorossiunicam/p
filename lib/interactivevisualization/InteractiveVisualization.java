package org.processmining.plugins.interactivevisualization;

import java.util.Map;
import java.util.SortedSet;

/**
 * Interface for the Interactive Visualization
 * 
 * The interactive visualization can provide a collection of events that are currently
 * visible and a collection of events that have been selected by the user. It also
 * keeps a reference to the most recent InteractivityManager instance. This manager
 * can be used for further access to tags and instances.
 * 
 * @author Danny van Heumen
 */
public interface InteractiveVisualization {
	
	/**
	 * Trigger an update (repaint) of the visualization. (e.g. because a tag has been updated.)
	 * 
	 * @param logChanged Indicates that the log has been changed (e.g. filtered).
	 * @param tagsChanged Indicates that one or more tags have been changed.
	 */
	public void updateVisualization(boolean logChanged, boolean tagsChanged);
	
	/**
	 * Set the manager that manages the interactivity between visualizations.
	 * 
	 * @param manager The instance of the Interactivity Manager that manages interaction between visualizations.
	 */
	public void setManager(InteractivityManager manager);
	
	/**
	 * Get the current Interactivity Manager instance.
	 * 
	 * @return Returns the interactivity manager.
	 */
	public InteractivityManager getManager();
	
	/**
	 * Get the elements that are selected in this visualization.
	 * (Representation in a set of Event IDs, but the visualization
	 * may of course display completely different things as long as
	 * they can be mapped to events.)
	 * 
	 * @return Returns a collection of instances that define a specific set of events.
	 * 
	 * @throws UnsupportedOperationException In the case where this operation
	 * is not supported by the plugin implementing the InteractiveVisualization
	 * interface, this exception will be thrown. (E.g. when the plugin does not
	 * provide any means of selecting elements.)
	 */
	public Map<String, SortedSet<EID>> getSelectedElements();
	
	/**
	 * Get the elements that are visible in this visualization.
	 * (e.g., one may be zoomed in on a particular area,
	 * this set represents the elements visible at that
	 * moment.)
	 * 
	 * @return Returns a collection of instances that define the visible set of events.
	 * 
	 * @throws  UnsupportedOperationException In the case this operation
	 * is not supported by the plugin implementing the InteractiveVisualization
	 * interface, this exception will be thrown.
	 */
	public Map<String, SortedSet<EID>> getVisibleElements();
}
