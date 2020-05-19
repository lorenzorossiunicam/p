package org.processmining.plugins.transitionsystem.miner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.transitionsystem.converter.TSConverterInput;
import org.processmining.plugins.transitionsystem.miner.modir.TSMinerModirInput;
import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;
import org.processmining.plugins.transitionsystem.miner.util.TSDirections;

/**
 * Transition System Miner Input.
 * 
 * @author Eric Verbeek
 * @version 0.1
 */

public class TSMinerInput {

	/**
	 * The log and classifiers to operate on.
	 */
	private final XLog log;
	private final XLogInfo logInfo;
	private final Collection<XEventClassifier> classifiers;
	private final XEventClassifier transitionClassifier;
	private int maxStates;

	/**
	 * The 'modir' settings (for every MODe and every DIRection). Note that MODe
	 * has been replaced by XEventClassifier by now.
	 */
	private final Map<XEventClassifier, Map<TSDirections, TSMinerModirInput>> modirSettings = new HashMap<XEventClassifier, Map<TSDirections, TSMinerModirInput>>();

	/**
	 * Whether the attribute settings have been selected for use.
	 */
	private Boolean useAttributeSettings;

	/**
	 * The attribute settings.
	 */
	private Collection<String> attributeFilter;

	/**
	 * The transition label filter.
	 */
	private Collection<String> visibleFilter;

	/**
	 * The converter settings.
	 */
	private TSConverterInput converterSettings;

	/**
	 * Create default settings for the transition system miner.
	 * 
	 * @param log
	 *            The log used.
	 * @param summary
	 *            The summary for the log used.
	 */
	public TSMinerInput(PluginContext context, XLog log, Collection<XEventClassifier> classifiers,
			XEventClassifier transitionClassifier) {

		/**
		 * Store the log and classifiers to operate on.
		 */
		this.log = log;
		this.classifiers = classifiers;
		this.transitionClassifier = transitionClassifier;
		this.maxStates = 200;
		
		/**
		 * Initialize modir settings.
		 */
		for (XEventClassifier classifier : classifiers) {
			modirSettings.put(classifier, new HashMap<TSDirections, TSMinerModirInput>(3));
			for (TSDirections direction : TSDirections.values()) {
				modirSettings.get(classifier).put(direction, new TSMinerModirInput());
			}
		}

		/**
		 * Initially, do not use the attribute settings.
		 */
		useAttributeSettings = false;
		/**
		 * Initialize attribute settings.
		 */
		attributeFilter = new TreeSet<String>();

		/**
		 * Get the necessary info from the log.
		 */
		logInfo = XLogInfoImpl.create(log, transitionClassifier, classifiers);

		/**
		 * Collect attribute information from the log.
		 */
		attributeFilter.addAll(logInfo.getEventAttributeInfo().getAttributeKeys());

		/**
		 * Initialize the transition label filter.
		 */
		visibleFilter = new TreeSet<String>();
		for (XEventClass eventClass : logInfo.getEventClasses().getClasses()) {
			visibleFilter.add(eventClass.toString());
		}

		/**
		 * Initialize all modir filters.
		 */
		for (XEventClassifier classifier : classifiers) {
			for (TSDirections direction : TSDirections.values()) {
				for (XEventClass eventClass : logInfo.getEventClasses(classifier).getClasses()) {
					modirSettings.get(classifier).get(direction).getFilter().add(eventClass.toString());
				}
			}
		}

		/**
		 * Initialize the converter settings.
		 */
		converterSettings = new TSConverterInput();

		/**
		 * By default, the payload contains only the last values of all
		 * classifiers that lead to the state.
		 */
		for (XEventClassifier classifier : classifiers) {
			TSMinerModirInput modirInput = modirSettings.get(classifier).get(TSDirections.BACKWARD);
			modirInput.setAbstraction(TSAbstractions.SET);
			modirInput.setUse(true);
			modirInput.setFilteredHorizon(1);
		}
	}

	public Collection<XEventClassifier> getClassifiers() {
		return classifiers;
	}

	public XEventClassifier getTransitionClassifier() {
		return transitionClassifier;
	}

	/**
	 * Gets the given modir setting.
	 * 
	 * @param direction
	 *            Direction (BACKWARD, FORWARD).
	 * @param mode
	 *            Mode (MODELELEMENT, ORIGINATOR, EVENTTYPE).
	 * @return Settings for this horizon.
	 */
	public TSMinerModirInput getModirSettings(TSDirections direction, XEventClassifier classifier) {
		return modirSettings.get(classifier).get(direction);
	}

	/**
	 * Sets the given modir setting.
	 * 
	 * @param direction
	 *            Direction (BACKWARD, FORWARD).
	 * @param mode
	 *            Mode (MODELELEMENT, ORIGINATOR, EVENTTYPE).
	 * @param newSettings
	 *            The new settings.
	 * @return The old settings.
	 */
	public TSMinerModirInput setModirSettings(TSDirections direction, XEventClassifier classifier,
			TSMinerModirInput newSettings) {
		TSMinerModirInput oldSettings = modirSettings.get(classifier).get(direction);
		modirSettings.get(classifier).put(direction, newSettings);
		return oldSettings;
	}

	/**
	 * Gets whether the attribute settings have been selected for use.
	 * 
	 * @return Whether the attribute settings have been selected for use.
	 */
	public boolean getUseAttributes() {
		return useAttributeSettings;
	}

	/**
	 * Sets whether the attribute settings have been selected for use.
	 * 
	 * @param newUse
	 *            The new value.
	 * @return The old value.
	 */
	public boolean setUseAttributes(boolean newUse) {
		boolean oldUse = useAttributeSettings;
		useAttributeSettings = newUse;
		return oldUse;
	}

	/**
	 * Gets the filter for attributes.
	 * 
	 * @return The filter for attributes.
	 */
	public Collection<String> getAttributeFilter() {
		return attributeFilter;
	}

	/**
	 * Sets the filter for attributes.
	 * 
	 * @param newFilter
	 *            The new filter.
	 * @return The old filter.
	 */
	public Collection<String> SetAttributeFilter(Collection<String> newFilter) {
		Collection<String> oldFilter = attributeFilter;
		attributeFilter = newFilter;
		return oldFilter;
	}

	/**
	 * Gets the transition label filter.
	 * 
	 * @return The filter general for model elements.
	 */
	public Collection<String> getVisibleFilter() {
		return visibleFilter;
	}

	/**
	 * Sets the transition label filter.
	 * 
	 * @param newFilter
	 *            The new filter.
	 * @return The old filter.
	 */
	public Collection<String> SetVisibleFilter(Collection<String> newFilter) {
		Collection<String> oldFilter = visibleFilter;
		visibleFilter = newFilter;
		return oldFilter;
	}

	public TSConverterInput getConverterSettings() {
		return converterSettings;
	}

	public TSConverterInput setConverterSettings(TSConverterInput newSettings) {
		TSConverterInput oldSettings = converterSettings;
		converterSettings = newSettings;
		return oldSettings;
	}

	public XLog getLog() {
		return log;
	}

	public XLogInfo getLogInfo() {
		return logInfo;
	}

	public int getMaxStates() {
		return maxStates;
	}

	public void setMaxStates(int maxStates) {
		this.maxStates = maxStates;
	}
}
