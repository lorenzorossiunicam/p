package it.unicam.pros.purple.semanticengine.ptnet.configuration;

import it.unicam.pros.purple.semanticengine.Configuration;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

import java.io.Serializable;
import java.util.*;

public class PnmlConfiguration implements Configuration, Serializable {

    private Map<String, Integer> marking;
    private Set<String> p = new HashSet<String>();
    private Map<String, Set<String>> i = new HashMap<String,Set<String>>();
    public PnmlConfiguration(Map<String, Integer> marking){
        this.marking = marking;
        p.add("p");
        i.put("p",new HashSet<String>());
        i.get("p").add("i");
    }

    @Override
    public Map<String, String> getGlobalData() {
        return new HashMap<String, String>();
    }

    @Override
    public Map<String, String> getLocalData() {
        return new HashMap<String, String>();
    }

    @Override
    public Set<String> getInstances(String proc) {
        return i.get(proc);
    }

    @Override
    public Set<String> getProcesses() {

        return p;
    }

    public Map<String, Integer> getMarking(){return marking;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PnmlConfiguration that = (PnmlConfiguration) o;
        return marking.equals(that.marking);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marking);
    }

    @Override
    public String toString() {
        return marking.toString();
    }
}
