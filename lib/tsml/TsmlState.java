package org.processmining.plugins.tsml;

import java.util.Map;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.xmlpull.v1.XmlPullParser;

public class TsmlState extends TsmlNode {

	public final static String TAG = "state";

	private String start;
	private String accept;

	/**
	 * Creates a fresh PNML place.
	 */
	public TsmlState() {
		super(TAG);
		start = null;
		accept = null;
	}

	/**
	 * Imports the known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		/*
		 * Import the start attribute.
		 */
		importStart(xpp, tsml);
		/*
		 * Import the accept attribute.
		 */
		importAccept(xpp, tsml);
	}

	/**
	 * Exports the dimension.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportStart(tsml) + exportAccept(tsml);
	}

	/**
	 * Imports the start attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importStart(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "start");
		if (value != null) {
			start = value;
		}
	}

	/**
	 * Exports the start attribute.
	 * 
	 * @return
	 */
	private String exportStart(Tsml tsml) {
		if (start != null) {
			return exportAttribute("start", start, tsml);
		}
		return "";
	}

	/**
	 * Imports the accept attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importAccept(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "accept");
		if (value != null) {
			accept = value;
		}
	}

	/**
	 * Exports the accept attribute.
	 * 
	 * @return
	 */
	private String exportAccept(Tsml tsml) {
		if (accept != null) {
			return exportAttribute("accept", accept, tsml);
		}
		return "";
	}

	/**
	 * Converts this state to a TS state.
	 * 
	 * @param ts
	 *            TS to add this state to.
	 * @param map
	 *            States found so far.
	 */
	public void unmarshall(TransitionSystem ts, StartStateSet starts, AcceptStateSet accepts,
			DirectedGraphElementWeights weights, Map<String, State> idStateMap, GraphLayoutConnection layout) {
		/*
		 * Add the state to the ts.
		 */
		State state = (ts.addState(id) ? ts.getNode(id) : null);
		if (state != null) {
			idStateMap.put(id, state);
			super.unmarshall(state, weights, layout);
			if (start != null) {
				starts.add(state.getIdentifier());
			}
			if (accept != null) {
				accepts.add(state.getIdentifier());
			}
		}
	}

	public TsmlState marshall(State state, StartStateSet starts, AcceptStateSet accepts,
			DirectedGraphElementWeights weights, String id, GraphLayoutConnection layout) {
		super.marshall(state, id, weights, layout);
		if (starts.contains(state.getIdentifier())) {
			start = "true";
		}
		if (accepts.contains(state.getIdentifier())) {
			accept = "true";
		}
		return this;
	}
}
