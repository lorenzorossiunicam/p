package org.processmining.plugins.tsml;

import java.util.Map;

import org.jgraph.graph.GraphConstants;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.plugins.tsml.extensions.TsmlInscription;
import org.processmining.plugins.tsml.graphics.TsmlArcGraphics;
import org.xmlpull.v1.XmlPullParser;

public class TsmlTransition extends TsmlBasicObject {

	public final static String TAG = "transition";

	/**
	 * Id attribute.
	 */
	private String id;
	/**
	 * Source attribute.
	 */
	private String source;
	/**
	 * Target attribute.
	 */
	private String target;
	/**
	 * Inscription element.
	 */
	private String style;
	private TsmlInscription inscription;
	/**
	 * Graphics element.
	 */
	private TsmlArcGraphics graphics;

	/**
	 * Creates a fresh PNML arc.
	 */
	public TsmlTransition() {
		super(TAG);
		id = null;
		source = null;
		target = null;
		style = null;
		inscription = null;
		graphics = null;
	}

	/**
	 * Imports all known attributes.
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		/*
		 * Import known basic object attributes.
		 */
		super.importAttributes(xpp, tsml);
		/*
		 * Import id attribute.
		 */
		importId(xpp, tsml);
		/*
		 * Import source attribute.
		 */
		importSource(xpp, tsml);
		/*
		 * Import target attribute.
		 */
		importTarget(xpp, tsml);
		importStyle(xpp, tsml);
	}

	/**
	 * Exports all attributes.
	 */
	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportId(tsml) + exportSource(tsml) + exportTarget(tsml)
				+ exportStyle(tsml);
	}

	private void importId(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "id");
		if (value != null) {
			id = value;
		}
	}

	/**
	 * Imports source attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importSource(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "source");
		if (value != null) {
			source = value;
		}
	}

	private String exportId(Tsml tsml) {
		if (id != null) {
			return exportAttribute("id", id, tsml);
		}
		return "";
	}

	/**
	 * Exports source attribute.
	 * 
	 * @return
	 */
	private String exportSource(Tsml tsml) {
		if (source != null) {
			return exportAttribute("source", source, tsml);
		}
		return "";
	}

	/**
	 * Imports target attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importTarget(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "target");
		if (value != null) {
			target = value;
		}
	}

	/**
	 * Exports target attribute.
	 * 
	 * @return
	 */
	private String exportTarget(Tsml tsml) {
		if (target != null) {
			return exportAttribute("target", target, tsml);
		}
		return "";
	}

	/**
	 * Imports style attribute.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	private void importStyle(XmlPullParser xpp, Tsml tsml) {
		String value = xpp.getAttributeValue(null, "style");
		if (value != null) {
			style = value;
		}
	}

	/**
	 * Exports target attribute.
	 * 
	 * @return
	 */
	private String exportStyle(Tsml tsml) {
		if (style != null) {
			return exportAttribute("style", style, tsml);
		}
		return "";
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
			/*
			 * Start tag corresponds to a known child element of a PNML basic
			 * object.
			 */
			return true;
		}
		if (xpp.getName().equals(TsmlInscription.TAG)) {
			/*
			 * Inscription element. create inscription object and import
			 * inscription element.
			 */
			inscription = new TsmlInscription();
			inscription.importElement(xpp, tsml);
			return true;
		}
		if (xpp.getName().equals(TsmlArcGraphics.TAG)) {
			/*
			 * Graphics element. create graphics object and import graphics
			 * element.
			 */
			graphics = new TsmlArcGraphics();
			graphics.importElement(xpp, tsml);
			return true;
		}
		return false;
	}

	/**
	 * Exports all child elements.
	 */
	protected String exportElements(Tsml tsml) {
		String s = super.exportElements(tsml);
		if (inscription != null) {
			s += inscription.exportElement(tsml);
		}
		if (graphics != null) {
			s += graphics.exportElement(tsml);
		}
		return s;
	}

	/**
	 * Checks validity. Should have a source and a target.
	 */
	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if ((id == null) || (source == null) || (target == null)) {
			tsml.log(tag, lineNumber, "Expected id, source, and target");
		}
	}

	/**
	 * Converts this transition to a regular TS transition.
	 */
	public void unmarshall(TransitionSystem ts, DirectedGraphElementWeights weights, Map<String, State> idStateMap,
			GraphLayoutConnection layout) {
		/*
		 * Create arc (if source and target can be found).
		 */
		Object sourceId = idStateMap.get(source).getIdentifier();
		Object targetId = idStateMap.get(target).getIdentifier();
		Transition transition = (ts.addTransition(sourceId, targetId, id) ? ts.findTransition(sourceId, targetId, id)
				: null);
		if (transition != null) {
			super.unmarshall(transition, sourceId, targetId, weights);
			if (graphics != null) {
				graphics.unmarshall(transition, layout);
			}
			if (inscription != null) {
				inscription.unmarshall(transition);
			}
			transition.getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_SPLINE);
			if (style != null) {
				if (style.equalsIgnoreCase("bezier")) {
					transition.getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_BEZIER);
				} else if (style.equalsIgnoreCase("spline")) {
					transition.getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_SPLINE);
				} else if (style.equalsIgnoreCase("orthogonal")) {
					transition.getAttributeMap().put(AttributeMap.STYLE, GraphConstants.STYLE_ORTHOGONAL);
				}
			}
		}
	}

	public TsmlTransition marshall(Transition transition, DirectedGraphElementWeights weights,
			Map<State, String> stateIdMap, String id, GraphLayoutConnection layout) {
		super.marshall(
				transition.getLabel(),
				weights.get(transition.getSource().getIdentifier(), transition.getTarget().getIdentifier(),
						transition.getIdentifier(), -1));
		this.id = id;
		State sourceState = transition.getSource();
		State targetState = transition.getTarget();
		source = stateIdMap.get(sourceState);
		target = stateIdMap.get(targetState);
		inscription = new TsmlInscription().marshall(transition);
		graphics = new TsmlArcGraphics().marshall(transition, layout);
		switch (transition.getAttributeMap().get(AttributeMap.STYLE, 0)) {
			case GraphConstants.STYLE_BEZIER :
				style = "bezier";
				break;
			case GraphConstants.STYLE_SPLINE :
				style = "spline";
				break;
			case GraphConstants.STYLE_ORTHOGONAL :
				style = "orthogonal";
				break;
			default :
		}
		return this;
	}
}
