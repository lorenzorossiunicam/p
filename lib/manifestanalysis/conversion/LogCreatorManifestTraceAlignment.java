/**
 * 
 */
package org.processmining.plugins.manifestanalysis.conversion;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;
import org.processmining.plugins.pnalignanalysis.visualization.tracealignment.ActivityColorMap;

/**
 * @author aadrians Jun 2, 2012
 * 
 */
public class LogCreatorManifestTraceAlignment {
	/**
	 * Create logs in higher level of hierarchy from a manifest whose events
	 * have one to one mapping to transitions of the petri net inside manifest
	 * 
	 * @param manifest
	 * @param factory
	 * @param resultLog
	 * @param dummyEvClass
	 * @param resultMap
	 */
	public static void createTraceAlignmentLogForManifest(Manifest manifest, XFactory factory, XLog resultLog,
			ActivityColorMap acm) {
		XConceptExtension conceptExtension = XConceptExtension.instance();

		// create log and mapping from replay result by iterating the manifest
		int[] casePtr = manifest.getCasePointers();
		List<XTrace> listXTrace = new ArrayList<XTrace>(casePtr.length);

		// temp variables
		int i = 0; // pointer to trace
		for (XTrace origTrace : manifest.getLog()) {
			// construct alignment from manifest
			int[] caseEncoded = manifest.getManifestForCase(i);

			int pointer = 0;
			Iterator<XEvent> it = origTrace.iterator();
			TIntSet countedManifest = new TIntHashSet(caseEncoded.length / 2);

			// result
			XTrace trace = factory.createTrace(origTrace.getAttributes());

			while (pointer < caseEncoded.length) {
				if (caseEncoded[pointer] == Manifest.MOVELOG) {
					// preserve the event
					XEvent evt = it.next();
					String label = evt.getAttributes().get("concept:name").toString() + AlignmentConstants.MOVELOG;
					trace.add(createEvent(factory, conceptExtension, label));
					
					acm.put(label + "-" + AlignmentConstants.LIFECYCLETRANSSTR, AlignmentConstants.MOVELOGCOLOR);
					pointer++;
				} else if (caseEncoded[pointer] == Manifest.MOVEMODEL) {
					// add the event to newTrace
					Transition t = manifest.getTransitionOf(caseEncoded[pointer + 1]);
					String label = t.getLabel();
					label += t.isInvisible() ? AlignmentConstants.MOVEMODELINVI
							: AlignmentConstants.MOVEMODELREAL;
					trace.add(createEvent(factory, conceptExtension, label));
					
					acm.put(label + "-" + AlignmentConstants.LIFECYCLETRANSSTR, t.isInvisible() ? AlignmentConstants.MOVEMODELINVICOLOR
								: AlignmentConstants.MOVEMODELREALCOLOR);
					pointer += 2;
				} else if (caseEncoded[pointer] == Manifest.MOVESYNC) {
					// only if it has been printed before, add it
					if (!countedManifest.contains(caseEncoded[pointer + 1])) {
						// add the event to newTrace
						Transition t = manifest.getTransitionOf(manifest
								.getEncTransOfManifest(caseEncoded[pointer + 1]));
						String label = t.getLabel() + AlignmentConstants.MOVESYNC;
						trace.add(createEvent(factory, conceptExtension, label));
						
						acm.put(label + "-" + AlignmentConstants.LIFECYCLETRANSSTR, AlignmentConstants.MOVESYNCCOLOR);
						countedManifest.add(caseEncoded[pointer + 1]);
					}
					it.next();
					pointer += 2;
				}
			}
			listXTrace.add(trace);

			i++;
		}

		// create log
		resultLog.addAll(listXTrace);
	}

	private static XEvent createEvent(XFactory factory, XConceptExtension conceptExtension, String nameEvClass) {
		XEvent outputEvent = factory.createEvent();
		
		XAttribute attr = factory.createAttributeLiteral("concept:name", nameEvClass, conceptExtension);
		outputEvent.getAttributes().put("concept:name", attr);
		
		XAttribute transition = factory.createAttributeLiteral("lifecycle:transition", AlignmentConstants.LIFECYCLETRANSSTR,
				XConceptExtension.instance());
		outputEvent.getAttributes().put("lifecycle:transition", transition);
		
		return outputEvent;
	}

}
