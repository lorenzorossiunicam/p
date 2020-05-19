package org.processmining.plugins.tsml;

import java.util.ArrayList;
import java.util.List;

import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsml.toolspecific.TsmlToolSpecific;
import org.xmlpull.v1.XmlPullParser;

public class TsmlBasicObject extends TsmlElement {

	/**
	 * Name element.
	 */
	protected TsmlName name;
	/**
	 * ToolSpecifics elements (there may be multiple).
	 */
	protected List<TsmlToolSpecific> toolSpecificList;
	private String weight;

	/**
	 * Creates a fresh basic PNML object.
	 * 
	 * @param tag
	 */
	public TsmlBasicObject(String tag) {
		super(tag);
		name = null;
		weight = null;
		toolSpecificList = new ArrayList<TsmlToolSpecific>();
	}

	public String getName(String defaultName) {
		if (name != null) {
			return name.getName(defaultName);
		}
		return defaultName;
	}

	/**
	 * Checks whether the current start tag is known. If known, it imports the
	 * corresponding child element and returns true. Otherwise, it returns
	 * false.
	 * 
	 * @return Whether the start tag was known.
	 */
	protected boolean importElements(XmlPullParser xpp, Tsml tsml) {
		if (super.importElements(xpp, tsml)) {
			return true;
		}
		if (xpp.getName().equals(TsmlName.TAG)) {
			/*
			 * Name element. Create name object and import name element.
			 */
			name = new TsmlName();
			name.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlToolSpecific.TAG)) {
			/*
			 * Tool specifics element. Create tool specifics object and import
			 * tool specifics element.
			 */
			TsmlToolSpecific toolSpecific = new TsmlToolSpecific();
			toolSpecific.importElement(xpp, tsml);
			toolSpecificList.add(toolSpecific);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	/**
	 * Exports all elements.
	 */
	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		if ((name != null) && (name.text != null) && !name.text.getText().isEmpty()) {
			s += name.exportElement(tsml);
		}
		for (TsmlToolSpecific toolSpecific : toolSpecificList) {
			s += toolSpecific.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		importWeight(xpp, tsml);
	}

	private void importWeight(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "weight");
		if (value != null) {
			weight = value;
		}
	}

	private String exportWeight(Tsml tsml) {
		if (weight != null) {
			return exportAttribute("weight", weight, tsml);
		}
		return "";
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportWeight(tsml);
	}

	/**
	 * Checks validity. Should have a source and a target.
	 */
	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if (weight != null) {
			try {
				Integer.valueOf(weight);
			} catch (NumberFormatException e) {
				tsml.log(tag, lineNumber, "Expected integer transition weight, got " + weight);
			}
		}
	}

	protected void unmarshall(State state, DirectedGraphElementWeights weights) {
		if (name != null) {
			name.unmarshall(state);
		}
		if (weight != null) {
			weights.add(state.getIdentifier(), Integer.valueOf(weight));
		}
	}

	protected void unmarshall(Transition transition, Object sourceId, Object targetId,
			DirectedGraphElementWeights weights) {
		if (name != null) {
			name.unmarshall(transition);
		}
		if (weight != null) {
			weights.add(sourceId, targetId, transition.getIdentifier(), Integer.valueOf(weight));
		}
	}

	public TsmlBasicObject marshall(String label, int intWeight) {
		try {
			name = new TsmlName(label);
		} catch (Exception ex) {
		}
		if (intWeight >= 0) {
			weight = String.valueOf(intWeight);
		}
		return this;
	}
}
