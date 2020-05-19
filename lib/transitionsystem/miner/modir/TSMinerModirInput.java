package org.processmining.plugins.transitionsystem.miner.modir;

import java.util.Collection;
import java.util.TreeSet;

import org.processmining.plugins.transitionsystem.miner.util.TSAbstractions;

/**
 * Transition System Mode-Direction Input.
 * 
 * Transition system miner settings for a single combination of Mode and
 * Direction.
 * 
 * @author Eric Verbeek
 * @version 0.1
 */

public class TSMinerModirInput {

	/**
	 * Whether this 'modir' (MODe and DIRection) has been selected for use or
	 * not.
	 */
	private boolean use;
	/**
	 * Maximal number of steps to take. If -1, take all steps.
	 */
	private int horizon;
	/**
	 * Abstraction, see above.
	 */
	private TSAbstractions abstraction;
	/**
	 * Filter.
	 */
	private final Collection<String> filter;
	/**
	 * Maximal number of filtered steps to take.
	 */
	private int filteredHorizon;

	/**
	 * Default settings.
	 */
	public TSMinerModirInput() {
		use = false;
		horizon = -1;
		abstraction = TSAbstractions.SET;
		filter = new TreeSet<String>();
		filteredHorizon = 1;
	}

	/**
	 * Returns whether these settings are selected for use.
	 * 
	 * @return Whether these settings are selected for use.
	 */
	public boolean getUse() {
		return use;
	}

	/**
	 * Sets whether these settings should be selected for use.
	 * 
	 * @param newUse
	 *            The new value.
	 * @return The old value.
	 */
	public boolean setUse(boolean newUse) {
		boolean oldUse = use;
		use = newUse;
		return oldUse;
	}

	/**
	 * Returns the number of steps to take.
	 * 
	 * @return The number of steps to take.
	 */
	public int getHorizon() {
		return horizon;
	}

	/**
	 * Sets the number of steps to take.
	 * 
	 * @param newHorizon
	 *            The new value.
	 * @return The old value.
	 */
	public int setHorizon(int newHorizon) {
		int oldHorizon = horizon;
		horizon = newHorizon;
		return oldHorizon;
	}

	/**
	 * Returns the abstraction.
	 * 
	 * @return The abstraction.
	 */
	public TSAbstractions getAbstraction() {
		return abstraction;
	}

	/**
	 * Sets the abstraction.
	 * 
	 * @param newAbstraction
	 *            The new value.
	 * @return The old value.
	 */
	public TSAbstractions setAbstraction(TSAbstractions newAbstraction) {
		TSAbstractions oldAbstraction = abstraction;
		abstraction = newAbstraction;
		return oldAbstraction;
	}

	/**
	 * Returns the filter.
	 * 
	 * @return The filter.
	 */
	public Collection<String> getFilter() {
		return filter;
	}

	/**
	 * Returns the number of filtered steps to take.
	 * 
	 * @return The number of filtered steps to take.
	 */
	public int getFilteredHorizon() {
		return filteredHorizon;
	}

	/**
	 * Sets the number of filtered steps to take.
	 * 
	 * @param newFilteredHorizon
	 *            The new value.
	 * @return The old value.
	 */
	public int setFilteredHorizon(int newFilteredHorizon) {
		int oldFilteredHorizon = filteredHorizon;
		filteredHorizon = newFilteredHorizon;
		return oldFilteredHorizon;
	}

}
