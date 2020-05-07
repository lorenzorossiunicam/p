package it.unicam.pros.guidedsimulator.util.mining;
import java.io.File;

import it.unicam.pros.guidedsimulator.util.mining.prom.discoverability.Alphas;
import it.unicam.pros.guidedsimulator.util.models.IO.BPMNIO;
import it.unicam.pros.guidedsimulator.util.models.conversions.BPMNConverter;
import it.unicam.pros.guidedsimulator.util.models.conversions.PetrinetConverter;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;


public class MiningTest {

//	public static HeuristicsNet getNet(PluginContext context, String logFile, Double dependencyThreshold) throws Exception {
//
//		ByteArrayOutputStream pipeOut = new ByteArrayOutputStream();
//		//PrintStream old_out = System.out;
//		//System.setOut(new PrintStream(pipeOut));
//
//		HeuristicsMinerSettings configuration = new HeuristicsMinerSettings();
//		configuration.setDependencyThreshold(dependencyThreshold);
//		configuration.setUseAllConnectedHeuristics(true);
//		XesXmlParser parser = new XesXmlParser();
//		XLog log = parser.parse(new File(logFile)).get(0);
//
//		System.out.println("Getting log info");
//		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
//
//		System.out.println("Setting classifier");
//		 XEventClassifier classifier = logInfo.getEventClassifiers().iterator().next();
//		 System.out.println("111"+logInfo.getEventClassifiers());
//		configuration.setClassifier(classifier);
//
//
//
//
//
//		//AlphaClassicMinerImpl.run(log, classifier, params);
//
//		HeuristicsMiner hm = new HeuristicsMiner(context, log, configuration);
//		HeuristicsNet net = hm.mine();
//		//System.setOut(old_out);
//
//		return net;
//	}

	public static void main(String[] args) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		XLog log = parser.parse(new File("log.xes")).get(0);
		Pair<Petrinet, Marking> pt = Alphas.alpha(log, AlphaVersion.CLASSIC);

		System.out.println((
				pt.getFirst().getPlaces().iterator().next()
				));
		//BPMNIO.export(BPMNConverter.toBPMNModelInstance(PetrinetConverter.toBPMNDiagram(pt)), "questo.bpmn");
	}
}



