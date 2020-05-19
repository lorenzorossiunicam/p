package org.processmining.plugins.ilpminer.templates;

import org.processmining.plugins.ilpminer.ILPModelSettings;

/**
 * Provides a storage for the petri net ILP model variant settings
 * 
 * @author T. van der Wiel
 * 
 */
public class PetriNetILPModelSettings extends ILPModelSettings {
	protected SearchType searchType = SearchType.PER_CD;
	protected boolean separateInitialPlaces = true;

	public PetriNetILPModelSettings() {
	}

	public PetriNetILPModelSettings(SearchType searchType, boolean separateInitialPlaces) {
		this.searchType = searchType;
		this.separateInitialPlaces = separateInitialPlaces;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public boolean separateInitialPlaces() {
		return separateInitialPlaces;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}

	public void setSeparateInitialPlaces(boolean separateInitialPlaces) {
		this.separateInitialPlaces = separateInitialPlaces;
	}

	public enum SearchType {
		BASIC("Basic Representation"), PER_CD("Per Causal Dependency"), PER_TRANSITION("Before & After Transition"), PRE_PER_TRANSITION(
				"Before Transition"), POST_PER_TRANSITION("After Transition");
		private String name;

		private SearchType(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	};
}
