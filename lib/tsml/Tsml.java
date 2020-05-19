package org.processmining.plugins.tsml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.xmlpull.v1.XmlPullParser;

public class Tsml extends TsmlElement {

	/**
	 * TSML tag.
	 */
	public final static String TAG = "tsml";

	private String label;
	private String layout;
	private final List<TsmlState> stateList;
	private final List<TsmlTransition> transitionList;

	private XLog log;
	private XTrace trace;
	private XFactory factory;
	private XExtension conceptExtension;
	private XExtension organizationalExtension;

	boolean hasErrors;

	/**
	 * Creates a fresh default PNML object, that is, a PNML object of type PNML.
	 */
	public Tsml() {
		super(TAG);
		label = null;
		layout = null;
		stateList = new ArrayList<TsmlState>();
		transitionList = new ArrayList<TsmlTransition>();
		initializeLog();
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Creates and initializes a log to throw to the framework when importing
	 * the PNML file fails. In this log, every net will have its own trace. The
	 * first net is preceded by a preamble.
	 */
	private void initializeLog() {
		factory = XFactoryRegistry.instance().currentDefault();
		conceptExtension = XConceptExtension.instance();
		organizationalExtension = XOrganizationalExtension.instance();
		log = factory.createLog();
		log.getExtensions().add(conceptExtension);
		log.getExtensions().add(organizationalExtension);

		logNet("<preamble>");

		hasErrors = false;
	}

	public XLog getLog() {
		return log;
	}

	/**
	 * Adds a log event to the current trace in the log.
	 * 
	 * @param context
	 *            Context of the message, typically the current PNML tag.
	 * @param lineNumber
	 *            Current line number.
	 * @param message
	 *            Error message.
	 */
	public void log(String context, int lineNumber, String message) {
		XAttributeMap attributeMap = new XAttributeMapImpl();
		attributeMap.put(XConceptExtension.KEY_NAME, factory.createAttributeLiteral(XConceptExtension.KEY_NAME,
				message, conceptExtension));
		attributeMap.put(XConceptExtension.KEY_INSTANCE, factory.createAttributeLiteral(XConceptExtension.KEY_INSTANCE,
				context, conceptExtension));
		attributeMap.put(XOrganizationalExtension.KEY_RESOURCE, factory.createAttributeLiteral(
				XOrganizationalExtension.KEY_RESOURCE, "Line " + lineNumber, organizationalExtension));
		XEvent event = factory.createEvent(attributeMap);
		trace.add(event);
		hasErrors = true;
	}

	/**
	 * Adds a new trace with the given name to the log. This trace is now
	 * current.
	 * 
	 * @param name
	 *            The give name.
	 */
	public void logNet(String name) {
		trace = factory.createTrace();
		log.add(trace);
		trace.getAttributes().put(XConceptExtension.KEY_NAME,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, name, conceptExtension));
	}

	public boolean hasErrors() {
		return hasErrors;
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
		if (xpp.getName().equals(TsmlState.TAG)) {
			TsmlState state = new TsmlState();
			state.importElement(xpp, tsml);
			stateList.add(state);
			return true;
		}
		if (xpp.getName().equals(TsmlTransition.TAG)) {
			TsmlTransition transition = new TsmlTransition();
			transition.importElement(xpp, tsml);
			transitionList.add(transition);
			return true;
		}
		return false;
	}

	/**
	 * Exports the child elements to String.
	 */
	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		for (TsmlState state : stateList) {
			s += state.exportElement(tsml);
		}
		for (TsmlTransition transition : transitionList) {
			s += transition.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		importLabel(xpp, tsml);
		importLayout(xpp, tsml);
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportLabel(tsml) + exportLayout(tsml);
	}

	/**
	 * Imports label attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importLabel(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "label");
		if (value != null) {
			label = value;
		}
	}

	/**
	 * Exports label attribute.
	 * 
	 * @return
	 */
	private String exportLabel(Tsml tsml) {
		if (label != null) {
			return exportAttribute("label", label, tsml);
		}
		return "";
	}

	/**
	 * Imports layout attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importLayout(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "layout");
		if (value != null) {
			layout = value;
		}
	}

	/**
	 * Exports layout attribute.
	 * 
	 * @return
	 */
	private String exportLayout(Tsml tsml) {
		if (layout != null) {
			return exportAttribute("layout", layout, tsml);
		}
		return "";
	}

	public void unmarshall(TransitionSystem ts, StartStateSet starts, AcceptStateSet accepts,
			DirectedGraphElementWeights weights, GraphLayoutConnection graphLayout) {
		Map<String, State> idStateMap = new HashMap<String, State>();
		for (TsmlState state : stateList) {
			state.unmarshall(ts, starts, accepts, weights, idStateMap, graphLayout);
		}
		for (TsmlTransition transition : transitionList) {
			transition.unmarshall(ts, weights, idStateMap, graphLayout);
		}
		graphLayout.setLayedOut(layout.equalsIgnoreCase("true"));
	}

	public Tsml marshall(TransitionSystem ts, StartStateSet starts, AcceptStateSet accepts,
			DirectedGraphElementWeights weights, GraphLayoutConnection graphLayout) {
		Map<State, String> stateIdMap = new HashMap<State, String>();
		label = ts.getLabel();
		layout = "true";
		int id = 1;
		for (State state : ts.getNodes()) {
			stateList.add(new TsmlState().marshall(state, starts, accepts, weights, "state" + id, graphLayout));
			stateIdMap.put(state, "state" + id);
			id++;
		}
		id = 1;
		for (Transition transition : ts.getEdges()) {
			transitionList.add(new TsmlTransition().marshall(transition, weights, stateIdMap, "transition" + id, graphLayout));
			id++;
		}
		return this;
	}
	
	public void setName(String name) {
		label = name;
	}
}
