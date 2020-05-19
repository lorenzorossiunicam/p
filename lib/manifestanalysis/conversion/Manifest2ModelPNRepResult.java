/**
 * 
 */
package org.processmining.plugins.manifestanalysis.conversion;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * Create a PNRepResult object of only reliable results from manifest
 * 
 * @author aadrians Jun 1, 2012
 * 
 */
public class Manifest2ModelPNRepResult {
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
	public static void createLogMapForManifest(Manifest manifest, XFactory factory, XLog resultLog,
			XEventClass dummyEvClass, TransEvClassMapping resultMap) {
		resultLog.getClassifiers().addAll(manifest.getLog().getClassifiers());
		XConceptExtension conceptExtension = XConceptExtension.instance();

		String[] attrKeys = manifest.getEvClassifier().getDefiningAttributeKeys();

		// create log and mapping from replay result by iterating the manifest
		// temp variables
		XEventClasses eclasses = XLogInfoFactory.createLogInfo(manifest.getLog(), manifest.getEvClassifier())
				.getEventClasses();

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
					trace.add(it.next());
					pointer++;
				} else if (caseEncoded[pointer] == Manifest.MOVEMODEL) {
					pointer += 2;
				} else if (caseEncoded[pointer] == Manifest.MOVESYNC) {
					// only if it has been printed before, add it
					if (!countedManifest.contains(caseEncoded[pointer + 1])) {
						XEvent evt = (XEvent) it.next().clone(); // event class name is not used

						// override defining attribute keys
						Transition t = manifest.getTransitionOf(manifest
								.getEncTransOfManifest(caseEncoded[pointer + 1]));

						String nameEvClass = t.getLabel() + "[" + t.getId().toString() + "]";
						XAttribute attr = factory.createAttributeLiteral(attrKeys[0], nameEvClass, conceptExtension);
						evt.getAttributes().put(attrKeys[0], attr);
						// the other attribute keys
						for (int j = 1; j < attrKeys.length; j++) {
							XAttribute attrNext = factory.createAttributeLiteral(attrKeys[j], "X", conceptExtension);
							evt.getAttributes().put(attrKeys[j], attrNext);
						}

						// update event class mapping if needed
						XEventClass ec = resultMap.get(t);
						if (ec == null) {
							// create new event class
							ec = eclasses.getClassOf(evt);
							resultMap.put(t, ec);
						}

						// add the event to newTrace
						trace.add(evt);
						countedManifest.add(caseEncoded[pointer + 1]);
					} else {
						it.next();
					}
					pointer += 2;
				}
			}

			resultLog.add(trace);
			i++;
		}

		// map all other unmapped transitions to dummy
		for (Transition t : manifest.getNet().getTransitions()) {
			if (resultMap.get(t) == null) {
				resultMap.put(t, dummyEvClass);
			}
		}
	}

	/**
	 * Convert manifest to PNRepResult
	 * @param manifest
	 * @return
	 */
	public static PNRepResult convert(Manifest manifest) {
		// temporary variable
		XLog log = manifest.getLog();
		XLogInfo logInfo = XLogInfoImpl.create(log, manifest.getEvClassifier());
		XEventClasses ec = logInfo.getEventClasses();
		final NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		final NumberFormat nfi = NumberFormat.getInstance();
		nfi.setMaximumFractionDigits(0);
		nfi.setMinimumFractionDigits(0);

		Map<Integer, SyncReplayResult> mappedManifests = new HashMap<Integer, SyncReplayResult>();

		int[] casePointer = manifest.getCasePointers();
		for (int i = 0; i < casePointer.length; i++) {
			SyncReplayResult res = mappedManifests.get(casePointer[i]);
			if (res != null) {
				// just add the new trace id
				res.addNewCase(i);
			} else {
				// construct alignment from manifest
				int[] caseEncoded = manifest.getManifestForCase(i);

				List<StepTypes> stepTypesLst = new ArrayList<StepTypes>(caseEncoded.length / 2);
				List<Object> nodeInstanceLst = new ArrayList<Object>(caseEncoded.length / 2);
				int pointer = 0;
				Iterator<XEvent> it = log.get(i).iterator();
				TIntSet countedManifest = new TIntHashSet(caseEncoded.length / 2);

				while (pointer < caseEncoded.length) {
					if (caseEncoded[pointer] == Manifest.MOVELOG) {
						stepTypesLst.add(StepTypes.L);
						nodeInstanceLst.add(ec.getClassOf(it.next()));
						pointer++;
					} else if (caseEncoded[pointer] == Manifest.MOVEMODEL) {
						Transition t = manifest.getTransitionOf(caseEncoded[pointer + 1]);
						//eventClassLst.add(manifest.getTransClassOf(t).getId());
						nodeInstanceLst.add(t);
						if (t.isInvisible()) {
							stepTypesLst.add(StepTypes.MINVI);
						} else {
							stepTypesLst.add(StepTypes.MREAL);
						}
						pointer += 2;
					} else if (caseEncoded[pointer] == Manifest.MOVESYNC) {
						it.next(); // event class name is not used

						// only if it has been printed before, add it
						if (!countedManifest.contains(caseEncoded[pointer + 1])) {
							stepTypesLst.add(StepTypes.LMGOOD);
							nodeInstanceLst.add(manifest.getTransitionOf(manifest
									.getEncTransOfManifest(caseEncoded[pointer + 1])));
							countedManifest.add(caseEncoded[pointer + 1]);
						}
						pointer += 2;
					}
				}
				res = new SyncReplayResult(nodeInstanceLst, stepTypesLst, i);

				// add stats
				res.setReliable(manifest.isCaseReliable(i));
				res.addInfo(PNRepResult.TRACEFITNESS, manifest.getTraceFitness(i));
				res.addInfo(PNRepResult.RAWFITNESSCOST, manifest.getRawCostFitness(i));
				res.addInfo(PNRepResult.MOVELOGFITNESS, manifest.getMoveLogFitness(i));
				res.addInfo(PNRepResult.MOVEMODELFITNESS, manifest.getMoveModelFitness(i));
				res.addInfo(PNRepResult.NUMSTATEGENERATED, manifest.getNumStates(i));
				res.addInfo(PNRepResult.TIME, manifest.getComputationTime(i));

				mappedManifests.put(casePointer[i], res);
			}
		}
		return new PNRepResult(mappedManifests.values());
	}
}
