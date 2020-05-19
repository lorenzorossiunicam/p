package org.processmining.plugins.interactivevisualization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import org.processmining.framework.util.Pair;

public class InteractivityManagerContext {
	private final Map<InteractiveVisualization, String> views = new HashMap<InteractiveVisualization, String>();
	private final Map<String, Map<String, SortedSet<EID>>> tags;
	private final Set<Pair<String, String>> enabledTags = new HashSet<Pair<String, String>>();
	
	public InteractivityManagerContext() {
		this(new TreeMap<String, Map<String, SortedSet<EID>>>());
	}
	
	public InteractivityManagerContext(Map<String, Map<String, SortedSet<EID>>> tags) {
		this.tags = tags;
	}
	
	public Set<InteractiveVisualization> getViews() {
		return views.keySet();
	}
	
	public Map<InteractiveVisualization, String> getViewNames() {
		return views;
	}
	
	public void addVisualization(String category, String name, InteractiveVisualization view) {
		views.put(view, category+": "+name);
	}
	
	public void removeVisualization(InteractiveVisualization view) {
		views.remove(view);
	}
	
	public void updateViews(boolean logChanged, boolean tagsChanged) {
		
		for(InteractiveVisualization view : views.keySet()) {
			view.updateVisualization(logChanged, tagsChanged);
		}
	}
	
	public Map<String, SortedSet<EID>> getVisible() {
		return getTag("Visible");
	}
	
	public void setVisible(Map<String, SortedSet<EID>> visibleElements) {
		setTag("Visible", visibleElements);
	}
	
	public Set<String> getTags() {
		return tags.keySet();
	}
	
	public Set<String> getInstances(String tagName) {
		
		if(tagName == null || tagName.isEmpty()) {
			throw new IllegalArgumentException("The specified tagName cannot be 'null' or an empty string.");
		}
		
		Map<String, SortedSet<EID>> tag = tags.get(tagName);
		
		if(tag == null) {
			throw new NoSuchElementException();
		}
		
		return tag.keySet();
	}
	
	public Map<String, SortedSet<EID>> getTag(String name) {
		
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException("This tag name is invalid. The tag name should be a non-empty string.");
		}
		
		return tags.get(name);
	}
	
	/**
	 * Set the tag with the provided set of events.
	 * 
	 * When an existing tag is again set, the instances could be changed. Therefore we disable
	 * any currently enabled tag instances for the provided tag name.
	 * 
	 * @param name Name of the tag. (Cannot be 'null' or an empty string)
	 * @param collection Collection of instances of events (EIDs)
	 */
	public void setTag(String name, Map<String, SortedSet<EID>> collection) {
		
		if(collection == null) {
			throw new IllegalArgumentException("The provided collection cannot be 'null'.");
		}
		
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException("This tag name is invalid. The tag name should be a non-empty string.");
		}
		
		//Disable enabled tag instances, since they are not guaranteed to be available after setting.
		this.setTagEnabled(name, false);
		
		tags.put(name, collection);
	}
	
	/**
	 * Remove the tag with the specified name.
	 * @param name Name of the tag to be removed.
	 */
	public void removeTag(String name) {
		
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException("'null' is an invalid value. The tag name should be a non-empty string.");
		}
		
		//Remove all enabled tag collection instances.
		Iterator<Pair<String, String>> it = enabledTags.iterator();
		while(it.hasNext()) {
			Pair<String, String> instance = it.next();
			
			if(name.equals(instance.getFirst())) {
				it.remove();
			}
		}
		
		//Disable any currently enabled tag instances of the removed tag.
		this.setTagEnabled(name, false);
		
		tags.remove(name);
	}
	
	/**
	 * Enable/disable a tag instance.
	 * @param tagName Name of the tag.
	 * @param enable Set whether or not to enable the tag collection instance.
	 * @throws IllegalArgumentException Throws an illegal argument exception for 'null' and empty strings.
	 */
	public void setTagEnabled(String tagName, boolean enable) {
		
		try {
			
			for(String instanceName : getInstances(tagName)) {
				setTagInstanceEnabled(tagName, instanceName, enable);
			}
		}
		catch(NoSuchElementException e) {
			//The tag does not exist, so it cannot be enabled.
		}
	}
	
	/**
	 * Enable/disable an instance within a tag.
	 * @param tagName Name of the tag.
	 * @param instanceName Name of the instance.
	 * @param enable Enable or disable.
	 */
	public void setTagInstanceEnabled(String tagName, String instanceName, boolean enable) {
		
		if(tagName == null || tagName.isEmpty()) {
			throw new IllegalArgumentException("'null' is an invalid value. The tag name should be a non-empty string.");
		}
		
		Pair<String, String> key = new Pair<String, String>(tagName, instanceName);
		
		if(enable) {
			
			if(!tags.containsKey(tagName)) {
				throw new NoSuchElementException("Tag '"+tagName+"' does not exist.");
			}
			
			enabledTags.add(key);
		}
		else {
			enabledTags.remove(key);
		}
	}
	
	/**
	 * Check whether or not a tag is enabled.
	 * @param tagName Name of the tag.
	 * @param instanceName Name of the instance.
	 * @return Returns true if the tag is enabled, or false if it is not enabled.
	 */
	public boolean isTagEnabled(String tagName, String instanceName) {
		
		if(tagName == null || tagName.isEmpty()) {
			throw new IllegalArgumentException("'null' is an invalid value. The tag name should be a non-empty string.");
		}
		
		return enabledTags.contains(new Pair<String, String>(tagName, instanceName));
	}
	
	/**
	 * Retrieve the set of enabled tags.
	 * @return Set of enabled tags.
	 */
	public Set<Pair<String, String>> getEnabledTags() {
		return enabledTags;
	}
}
