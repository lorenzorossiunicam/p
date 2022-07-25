package it.unicam.pros.purple.semanticengine.ptnet.semantics;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.ptnet.configuration.PnmlConfiguration;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PnmlSemantics {


    public Map<Configuration, Set<Event>> getNexts(Configuration c, Petrinet petrinet) {
        PnmlConfiguration conf = (PnmlConfiguration) c;
        Map<String, Integer> marking = conf.getMarking();
        Map<Configuration, Set<Event>> ret = new HashMap<Configuration, Set<Event>>();

        for(Transition t : petrinet.getTransitions()){
            Set<Place> inPlaces = new HashSet<Place>();
            boolean isActive = false;
            for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : petrinet.getInEdges(t)){
                Place p = (Place) edge.getSource();
                inPlaces.add(p);
                if(marking.get(p.getId().toString()) > 0){
                    isActive = true;
                }else{
                    isActive = false;
                    break;
                }
            }
            if(isActive){
                PnmlConfiguration newConf = (PnmlConfiguration) DeepCopy.copy(conf);
                for (Place p : inPlaces){
                    newConf.getMarking().put(p.getId().toString(), newConf.getMarking().get(p.getId().toString()) - 1);
                    //marking.put(p.getId().toString(), marking.get(p) - 1);
                }
                for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> outEdge : petrinet.getOutEdges(t)){
                    Place p = (Place) outEdge.getTarget();
                    newConf.getMarking().put(p.getId().toString(), newConf.getMarking().get(p.getId().toString()) + 1);
                    //marking.put(p.getId().toString(), marking.get(p) + 1);
                }
                //PnmlConfiguration newConf = new PnmlConfiguration(marking);
                if(ret.get(newConf) == null){
                    ret.put(newConf, new HashSet<Event>());
                }
                ret.get(newConf).add(LogUtil.PnmlTransition(t));
            }
        }
        return ret;
    }
}
