package org.processmining.plugins.interactivevisualization;

/**
 * The Interactivity Context can be used by visualizations implementing
 * the InteractiveVisualization interface for storing and further
 * communication. This context stores the reference to the InteractivityManager
 * managing this visualization, among others.
 * 
 * @author Danny van Heumen
 */
public class InteractivityContext {
	/**
	 * The Interactivity Manager managing the Interactive Visualization.
	 */
	private InteractivityManager manager;
	
	/**
	 * Constructor for the InteractivityContext.
	 */
	public InteractivityContext() {
		manager = null;
	}
	
	/**
	 * Set the Interactivity Manager (which is the class managing the
	 * interaction between the interactive visualizations).
	 * 
	 * @param manager The interactivity manager.
	 */
	public void setManager(InteractivityManager manager) {
		
		if(manager == null) {
			throw new IllegalArgumentException("The parameter 'manager' cannot be 'null'.");
		}
		
		this.manager = manager;
	}
	
	/**
	 * Get the current interactivity manager. (Or 'null' if it has not been
	 * registered yet.)
	 * 
	 * @return Returns the current interactivity manager or 'null' if no
	 * InteractivityManager has been set.
	 */
	public InteractivityManager getManager() {
		return manager;
	}
}
