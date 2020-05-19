package org.processmining.plugins.tsml.importing;

import java.io.InputStream;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.transitionsystem.TransitionSystemConnection;
import org.processmining.models.graphbased.directed.DirectedGraphElementWeights;
import org.processmining.models.graphbased.directed.transitionsystem.AcceptStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.plugins.tsml.Tsml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class TsmlImportUtils {
	public Tsml importTsmlFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {
		/*
		 * Get an XML pull parser.
		 */
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*
		 * Initialize the parser on the provided input.
		 */
		xpp.setInput(input, null);
		/*
		 * Get the first event type.
		 */
		int eventType = xpp.getEventType();
		/*
		 * Create a fresh PNML object.
		 */
		Tsml tsml = new Tsml();

		/*
		 * Skip whatever we find until we've found a start tag.
		 */
		while (eventType != XmlPullParser.START_TAG) {
			eventType = xpp.next();
		}
		/*
		 * Check whether start tag corresponds to PNML start tag.
		 */
		if (xpp.getName().equals(Tsml.TAG)) {
			/*
			 * Yes it does. Import the PNML element.
			 */
			tsml.importElement(xpp, tsml);
		} else {
			/*
			 * No it does not. Return null to signal failure.
			 */
			tsml.log(Tsml.TAG, xpp.getLineNumber(), "Expected tsml");
		}
		if (tsml.hasErrors()) {
			context.getProvidedObjectManager().createProvidedObject("Log of TSML import", tsml.getLog(), XLog.class,
					context);
			return null;
		}
		return tsml;
	}

	public Object connectTS(PluginContext context, Tsml tsml, TransitionSystem ts) {
		/*
		 * Create a fresh marking.
		 */
		StartStateSet starts = new StartStateSet();
		AcceptStateSet accepts = new AcceptStateSet();
		DirectedGraphElementWeights weights = new DirectedGraphElementWeights();
		GraphLayoutConnection layout = new GraphLayoutConnection(ts);
		/*
		 * Initialize the Petri net and marking from the PNML element.
		 */
		tsml.unmarshall(ts, starts, accepts, weights, layout);

		/*
		 * Add a connection from the Petri net to the marking.
		 */
		context.addConnection(new TransitionSystemConnection(ts, weights, starts, accepts));
		context.addConnection(layout);
		/*
		 * Set the label of the TS.
		 */
		context.getFutureResult(0).setLabel(ts.getLabel());
		context.getFutureResult(1).setLabel("PTS weights (imported from " + ts.getLabel() + ")");
		context.getFutureResult(2).setLabel("Start states (imported from " + ts.getLabel() + ")");
		context.getFutureResult(3).setLabel("Accept states (imported from " + ts.getLabel() + ")");

		/*
		 * Return the net and the marking.
		 */
		Object[] objects = new Object[4];
		objects[0] = ts;
		objects[1] = weights;
		objects[2] = starts;
		objects[3] = accepts;
		return objects;
	}
}
