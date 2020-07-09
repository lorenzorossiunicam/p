package it.unicam.pros.pplg.util.mining.prom.discoverability;

import it.unicam.pros.pplg.util.eventlogs.utils.LogIO;
import it.unicam.pros.pplg.util.mining.prom.framework.ContextsFactory;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.processmining.alphaminer.algorithms.AlphaClassicMinerImpl;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.InputStream;

public class Alphas {

    public static Pair<Petrinet, Marking> alpha(XLog log, AlphaVersion version, XEventClassifier classifier) throws Exception {
        return getAlpha(log, version, classifier);
    }

    public static Pair<Petrinet, Marking> alpha(InputStream logStream, AlphaVersion version, XEventClassifier classifier) throws Exception {
        XLog log = LogIO.parseXES("log.xes");
        return getAlpha(log, version, classifier);
    }

    public static Pair<Petrinet, Marking> alpha(InputStream logStream, AlphaVersion version) throws Exception {
        XEventClassifier c;

        XAttributeMap m = new XAttributeMapImpl();
        XLog log = LogIO.parseXES("log.xes");
        c = new XEventAttributeClassifier("EventLog", "concept:name");
        return getAlpha(log, version, c);
    }

    public static Pair<Petrinet, Marking> alpha(XLog log, AlphaVersion version) throws Exception {
        XEventAttributeClassifier c = new XEventAttributeClassifier("EventLog", "concept:name");
        return getAlpha(log, version, c);
    }

    private static Pair<Petrinet, Marking> getAlpha(XLog log, AlphaVersion version, XEventClassifier classifier) throws Exception {
        PluginContext pluginContext = ContextsFactory.getPluginContext();
        AlphaMinerParameters p = new AlphaMinerParameters(version);
        return AlphaClassicMinerImpl.run(pluginContext, log, classifier, p);
    }

}
