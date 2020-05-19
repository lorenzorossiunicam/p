package org.processmining.plugins.interactivevisualization;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.deckfour.xes.info.XLogInfo;

/**
 * Interface for Interactivity Manager.
 * The Interactivity Manager manages the updates and tags that are communicated
 * between interactive visualizations.
 * 
 * @author Danny van Heumen
 */
public interface InteractivityManager {
	
	/**
	 * Trigger an update of all interactive views.
	 * Provided parameters specify which types of data have been changed.
	 * 
	 * @param logChanged Indicate whether or not the log has been changed.
	 * (e.g. by filtering)
	 * @param tagsChanged Indicate whether or not tags have been changed.
	 */
	public void updateInteractiveViews(boolean logChanged, boolean tagsChanged);
	
	/**
	 * Retrieve the stored visible elements.
	 * 
	 * @return Returns the collection of instances that define the visible
	 * elements.
	 */
	public Map<String, SortedSet<EID>> getVisible();
	
	/**
	 * Stores a collection that defines which elements are currently visible in
	 * the active visualization to a tag 'Visible'.
	 * 
	 * @param collection The collection of instances that are currently visible
	 * in the visualization.
	 */
	public void setVisible(Map<String, SortedSet<EID>> collection);
	
	/**
	 * Get a set of available tags.
	 * 
	 * @return Set of tag names.
	 */
	public Set<String> getTags();
	
	/**
	 * Get a set of instance names available for the specified tag.
	 * 
	 * @param tagName Name of the tag.
	 * @return Returns a set of instance names.
	 */
	public Set<String> getInstances(String tagName);
	
	/**
	 * Get a tag.
	 * 
	 * @param name Name of the tag.
	 * @return Returns the collection of instances of the tag.
	 */
	public Map<String, SortedSet<EID>> getTag(String name);
	
	/**
	 * Set a new tag or overwrite an existing tag.
	 * 
	 * @param name Name of the tag.
	 * @param collection Collection of instances to be tagged.
	 */
	public void setTag(String name, Map<String, SortedSet<EID>> collection);
	
	/**
	 * Delete an existing tag.
	 * 
	 * @param name Name of the tag.
	 */
	public void removeTag(String name);
	
	/**
	 * Enable or disable a complete tag (i.e. all instances of the tag).
	 * 
	 * @param name Name of the tag.
	 * @param enable Enable (true) or disable (false).
	 */
	public void setTagEnabled(String name, boolean enable);
	
	/**
	 * Enable or disable an instance of a tag.
	 * 
	 * @param tagName Name of the tag.
	 * @param instanceName Name of the instance.
	 * @param enable Enable (true) or disable (false).
	 */
	public void setTagInstanceEnabled(String tagName, String instanceName, boolean enable);
	
	/**
	 * Check if a tag is currently enabled.
	 * 
	 * @param name Name of the tag.
	 * @param instance Name of the instance.
	 * @return Returns true if the instance of a tag is enabled, or false if it
	 * is disabled.
	 */
	public boolean isTagEnabled(String name, String instance);
	
	//TODO Do we still need 'getAllTaggedEvents()' for anything???
	/**
	 * Get a set of all tagged events. (Of tags that are currently enabled.)
	 * 
	 * @return Returns an ordered set of events (Event IDs).
	 */
	public SortedSet<EID> getAllTaggedEvents();
	
	//-------------------------------------------------------------------------------
	//Some methods that are provided for a practical nature ...
	
	/**
	 * Get the XLogInfo instance corresponding to the log.
	 * 
	 * @return Returns the XLogInfo (log summary) for the log.
	 */
	public XLogInfo getLogInfo();
}
