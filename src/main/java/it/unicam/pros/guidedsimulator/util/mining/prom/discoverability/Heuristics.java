package it.unicam.pros.guidedsimulator.util.mining.prom.discoverability;

import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogIO;
import it.unicam.pros.guidedsimulator.util.mining.prom.framework.ContextsFactory;
import it.unicam.pros.guidedsimulator.util.models.IO.BPMNIO;
import it.unicam.pros.guidedsimulator.util.models.conversions.BPMNConverter;
import it.unicam.pros.guidedsimulator.util.models.conversions.PetrinetConverter;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.heuristicsnet.miner.heuristics.converter.HeuristicsNetToPetriNetConverter;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMiner;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.HeuristicsMiner;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.settings.HeuristicsMinerSettings;

import java.io.File;

public class Heuristics {

//    public static Pair<Petrinet, Marking> heuristics(XLog log, XEventClassifier classifier) throws Exception {
//        return getAlpha(log, version, classifier);

//    }

    private static Pair<Petrinet, Marking> heuristic(XLog log) throws Exception {

        PluginContext pluginContext = ContextsFactory.getPluginContext();

        HeuristicsMinerSettings settings = new HeuristicsMinerSettings();
        settings.setClassifier( new XEventAttributeClassifier("EventLog", "concept:name"));
        FlexibleHeuristicsMiner miner = new FlexibleHeuristicsMiner(pluginContext, log, settings);
        HeuristicsNet net = miner.mine();

        Object[] g = HeuristicsNetToPetriNetConverter.converter(pluginContext,  net);
        return new Pair<Petrinet, Marking>((Petrinet) g[0],(Marking) g[1]);
    }

    public static void main(String[] args) throws Exception {
        XLog log = LogIO.parseXES("log.xes");
        //BPMNIO.export(BPMNConverter.toBPMNModelInstance(PetrinetConverter.toBPMNDiagram(heuristic(log))), "questo.bpmn");

    }
}
