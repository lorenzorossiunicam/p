package it.unicam.pros.purple.evaluator.purpose.rediscoverability;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintMatrix;
import it.unicam.pros.purple.evaluator.purpose.rediscoverability.metrics.FootprintRelations;
import it.unicam.pros.purple.simulation.Simulator;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

import java.util.*;

public class CustomTracesFrequency implements Evaluator {

    private final int minTraces;
    private final Map<Trace, int[]> loopFrequencies;
    private Map<Trace, Double>  frequencies;
    private EventLog log;

    public CustomTracesFrequency(Map<Trace, Double> freq, Map<Trace, int[]> loopFrequencies, int minTraces){
        this.minTraces = minTraces;
        this.frequencies = freq;
        this.loopFrequencies = loopFrequencies;
    }

    public static Couple<Set<Trace>,Map<Trace, int[]>> generateSimpleLogAndLoops(Simulator s, BpmnModelInstance model) throws Exception {
        FootprintMatrix m = new FootprintMatrix(model);
        Set<Trace> frequencies = new HashSet<>();
        Map<String, Map<String, FootprintRelations>> matrix = m.getFootprintMatrix();
        EventLog log = new EventLogImpl("",null);
        Delta delta = new  Delta(FootprintMatrix.compareRelations(m, log, 100.0));
        while (!delta.isEmpty()){
            log = s.simulate(delta);
            delta = new  Delta(FootprintMatrix.compareRelations(m, log, 100.0));
        }
        Set<String> present = new HashSet<>();
        for(Trace t : log.getTraces()){
            String simpleTrace = LogUtil.toSimpleTrace(t);
            if(present.contains(simpleTrace)){
                continue;
            }
            present.add(simpleTrace);
            frequencies.add(t);
        }
        Map<List<String>, int[]> loops = m.getLoops();
        Map<String, List<String>> map = LogUtil.toSimpleTraceMap(loops.keySet());
        Set<Trace> toRemove = new HashSet<>(), toAdd = new HashSet<>();
        Map<Trace, int[]> cycleBoundaries = new HashMap<>();
        for(String l : map.keySet()){
            for(Trace t : frequencies){
                String lT = LogUtil.toSimpleTrace(t);
                int st = loops.get(map.get(l))[0], en = loops.get(map.get(l))[1];
                if (lT.contains(l)){
                    int init = -1; boolean found = false;
                    Set<Event> toRem = new HashSet<>();
                    for(int i = 0; i<t.getTrace().size(); i++){
                        Event e = t.getTrace().get(i);
                        for (String eC : map.get(l)){
                            if (e.getEventName().equals(eC)){
                                toRem.add(e);
                                if (!found){
                                    found = true;
                                    init = i;
                                }
                            }
                        }
                    }
                    toRemove.add(t);
                    Trace tWithPrefix = (Trace) DeepCopy.copy(t);
                    tWithPrefix.getTrace().removeAll(toRem);


                    List<Event> prefix = new ArrayList<>(), suffix = new ArrayList<>();
                    if(st == -1){
                        suffix.add(new EventImpl(null,null,map.get(l).get(0),null,null,null,null,null,null,null,null));
                    }else if (en == -1){
                        prefix.add(new EventImpl(null,null,map.get(l).get(0),null,null,null,null,null,null,null,null));
                    }
                    else if(st>en){
                        for(int i = st; i< map.get(l).size(); i++){
                            prefix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                        for(int i = 0; i< en; i++){
                            prefix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                        for(int i = en; i< st; i++){
                            suffix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                    } else if (st<en){
                        for(int i = st; i< en; i++){
                            prefix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                        for(int i = en; i< map.get(l).size(); i++){
                            suffix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                        for(int i = 0; i< st; i++){
                            suffix.add(new EventImpl(null,null,map.get(l).get(i),null,null,null,null,null,null,null,null));
                        }
                    }
                    int inPre = init;
                    tWithPrefix.insert(prefix,init,1);
                    toAdd.add((Trace) DeepCopy.copy(tWithPrefix));
                    init += prefix.size();
                    tWithPrefix.insert(suffix,init,1);
                    init += suffix.size();
                    tWithPrefix.insert(prefix,init,1);
                    toAdd.add(tWithPrefix);
                    cycleBoundaries.put(tWithPrefix, new int[]{inPre, inPre+prefix.size(),prefix.size(),suffix.size()});
                }
            }
        }
        frequencies.removeAll(toRemove);
        frequencies.addAll(toAdd);
        present = new HashSet<>();
        Set<Trace> ret = new HashSet<>();
        for(Trace t : frequencies){
            String simpleTrace = LogUtil.toSimpleTrace(t);
            if(present.contains(simpleTrace)){
                continue;
            }
            present.add(simpleTrace);
            ret.add(t);
        }
        return new Couple(ret, cycleBoundaries);
    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        log  = new EventLogImpl("", null);
        Set<Trace> l = new HashSet<>();
        for (Trace t : frequencies.keySet()){
            Trace newT;
            int[] loopInfo = loopFrequencies.get(t);
            int times = (int) (minTraces*(frequencies.get(t)/100));
            for (int i = 0; i<times; i++){
                newT = (Trace) DeepCopy.copy(t);
                l.add(randomize(newT, loopInfo));
            }
        }

        log.setTraces(l);

        return null;
    }

//    private int[] getLoopInfo(Trace trace) {
//        String t = LogUtil.toSimpleTrace(trace);
//        Map<String, List<String>> cycles = LogUtil.toSimpleTraceMap(loopFrequencies.keySet());
//        for(String c : cycles.keySet()){
//            if(t.contains(c)){
//                List<String> cycle = cycles.get(c);
//                String se = cycle.get(0);
//                String ee = cycle.get(cycle.size()-1);
//                int s = 0,e = 0;
//                boolean sFound = false;
//                for(int i = 0; i<trace.getTrace().size(); i++){
//                    if (trace.getTrace().get(i).getEventName().equals(se) && !sFound){
//                        s=i;
//                        sFound = true;
//                    }
//                    if (trace.getTrace().get(i).getEventName().equals(ee)){
//                        e=i;
//                        break;
//                    }
//                }
//                return new int[]{s,e,loopFrequencies.get(cycles.get(c))};
//            }
//        }
//        return null;
//    }

    private Trace randomize(Trace t, int[] info) {
        long now = System.currentTimeMillis();
        t.setCaseId("case_"+now);
        List<Event> repetitions = new ArrayList<>();
        if(info!= null){
            for(int i = info[0]; i<= info[1];i++){
                repetitions.add(t.getTrace().get(i));
            }
            double looprep = Math.random() * info[2];
            t.insert(repetitions, info[1]+1, looprep);
        }

        for (Event e : t.getTrace()){
            now += Math.random()*3600000;
            e.setTimestamp(new Date(now));
        }
        return t;
    }

    public EventLog getLog(){
        return log;
    }
}
