package it.unicam.pros.purple.evaluator.purpose.conformance;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlignCostConformance implements Evaluator {

    private int nTraces;
    private double alignCost, reachedCost;
    private Set<Trace> noised;
    public boolean done = false;
    private int lastSize;

    public AlignCostConformance(double alignCost, int nTraces){
        this.alignCost = alignCost;
        this.nTraces = nTraces;
        this.reachedCost = 0;
        this.noised = new HashSet<>();
    }

    public Set<Trace> getNoised(){
        return this.noised;
    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        int newSize = disc.size() + noised.size();
        reachedCost = (reachedCost*lastSize)/newSize;
        tau = 1/tau;
        int times = 0;
        if(reachedCost<alignCost){
            times = (int) (10 - (reachedCost/alignCost));
            addNoisedTraces(disc, times);
        }else if (reachedCost>alignCost){
            //do nothing
        }
        if (newSize + times > nTraces)  {
            done = true;
        }
        return null;
    }

    private void addNoisedTraces(EventLog disc, int times) {
        for (int i = 0; i<times; i++){
            addNoisedTrace(disc);
        }
    }

    private void addNoisedTrace(EventLog disc) {
        int rand = (int) (Math.random()*(disc.getTraces().size()-1));
        Trace t = (Trace) DeepCopy.copy((disc.getTraces().toArray()[rand]));
        int noiseType = (int) (Math.random()*4);
        Couple<Trace, Integer> res = noisifyTrace(t, noiseType);
        noised.add(res.getE());
        lastSize = noised.size() + disc.size();
        reachedCost = (reachedCost*(lastSize-1)+ res.getV())/(1.0*(lastSize));
    }

    private Couple<Trace, Integer> noisifyTrace(Trace t, int noiseType) {
        Trace ret = new TraceImpl(null);
        int size = t.getTrace().size();
        int st = (int) (Math.random()*(size/2));
        int en = ((int) (Math.random()*(size/2))+(size/2));
        int roundAlign = (int) alignCost ;
        switch (noiseType){
            case 0:
                if (roundAlign>=size-1){
                    ret.appendEvent(t.get(size-1));
                }else{
                    for (int i= roundAlign; i<size; i++){
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 1:
                if (roundAlign>=size-1){
                    ret.appendEvent(t.get(0));
                }else{
                    for (int i= 0; i<(size - roundAlign); i++){
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 2:
                if (roundAlign>=size-1){
                    ret.appendEvent(t.get(size/2));
                }else{
                    int init = (int) (Math.random()*(size-roundAlign));
                    for (int i= 0; i< init; i++){
                        ret.appendEvent(t.get(i));
                    }
                    for (int i= init + roundAlign; i<size; i++){
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 3:
                for (int i= 0; i<size; i++){
                    if (i == st){
                        ret.appendEvent(t.get(en));
                    } else if (i == en){
                        ret.appendEvent(t.get(st));
                    } else{
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 4:
                int times = (roundAlign>size) ? size/2 : roundAlign/2;
                for (int i= 0; i<size; i++){
                    if (i % 2 == 0){
                        Event alien = t.get(i);
                        alien.setEventName(RandomStringUtils.random(10,true,true));
                        ret.appendEvent(alien);
                    } else {
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            default:
                break;
        }
        int cost = getAlignCost(t, ret);
        return new Couple<>(ret,cost);
    }

    private int getAlignCost(Trace a, Trace b) {
        int charIndex = 33;
        String sA = "", sB = "";
        Map<String, Character> dictionary = new HashMap<>();
        for(Event e : a.getTrace()){
            if(dictionary.containsKey(e.getEventName())){
                sA += dictionary.get(e.getEventName());
            }else {
                sA += (char) charIndex;
                dictionary.put(e.getEventName(), (char) charIndex);
                charIndex ++;
            }
        }
        for(Event e :b.getTrace()){
            if(dictionary.containsKey(e.getEventName())){
                sB += dictionary.get(e.getEventName());
            }else {
                sB += (char) charIndex;
                dictionary.put(e.getEventName(), (char) charIndex);
                charIndex ++;
            }
        }
        return com.helger.commons.string.util.LevenshteinDistance.getDistance(sA, sB,1,1,2);
    }
}
