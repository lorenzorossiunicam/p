package it.unicam.pros.purple.semanticengine.ptnet;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.SemanticEngine;
import it.unicam.pros.purple.semanticengine.ptnet.configuration.PnmlConfiguration;
import it.unicam.pros.purple.semanticengine.ptnet.semantics.PnmlSemantics;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PnmlEngine  implements SemanticEngine {

    private final String name;
    private final Petrinet petrinet;
    private final PnmlSemantics semantics;

    public PnmlEngine(String name, Petrinet petrinet){
        this.name = name;
        this.petrinet = petrinet;
        this.semantics = new PnmlSemantics();
    }
    @Override
    public Map<Configuration, Set<Event>> getNexts(Configuration c) throws Exception {
        return semantics.getNexts(c, petrinet);
    }

    @Override
    public Configuration getInitConf() {
        Map<String, Integer> mark = new HashMap<String, Integer>();
        for (Place p : petrinet.getPlaces()){
                 mark.put(p.getId().toString(), 0);
        }
        return new PnmlConfiguration(mark);
    }

    @Override
    public Configuration initData(Configuration conf) {
        return getInitConf();
    }

    @Override
    public Configuration initInstances(Configuration conf) throws Exception {
        Map<String, Integer> mark = new HashMap<String, Integer>();
        for (Place p : petrinet.getPlaces()){
            mark.put(p.getId().toString(), 0);
            boolean hasIncoming = false;
            for (Transition t : petrinet.getTransitions()){
                if (petrinet.getArc(t,p) != null){
                    hasIncoming = true;
                    break;
                }
            }
            if(!hasIncoming){
                mark.put(p.getId().toString(), 1);
            }
        }
        return new PnmlConfiguration(mark);
    }

    @Override
    public String getModelName() {
        return name;
    }
}
