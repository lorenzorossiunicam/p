package it.unicam.pros.purple.evaluator.purpose.conformance;

import com.google.common.collect.Sets;
import it.unicam.pros.purple.PURPLE;
import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.TraceImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogIO;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.util.*;

public class CustomNoise implements Evaluator {
    private Set<String> minSet;
    private float[] nNoisedTraces;
    private int nTaces, nNormalTraces;
    private Set<Trace> noised;
    public boolean done = false;

    public CustomNoise(float[] noisesPerc, int nTraces, Set<String> minSet) {
        this.nTaces = nTraces;
        this.nNoisedTraces = getNoisedTraceNumber(noisesPerc, nTraces);
        this.nNormalTraces = nTraces;
        this.noised = new HashSet<>();
        for(int i = 0; i<nNoisedTraces.length; i++){
            nNormalTraces -= nNoisedTraces[i];
        }
        this.minSet = minSet;
    }

    private float[] getNoisedTraceNumber(float[] noisesPerc, int nTraces) {
        for(int i = 0; i<noisesPerc.length; i++){
            noisesPerc[i] = (noisesPerc[i]*nTraces)/100;
        }
        return  noisesPerc;
    }

    public Set<Trace> getNoised(){
        return this.noised;
    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        tau = 1/tau;
        if (disc.size()<nNormalTraces-tau){
            return null;
        } else {
            for(int i = 0; i < nNoisedTraces.length; i++){
                noised.addAll(generateNNoisedTraces(i, nNoisedTraces[i], disc));
            }
            done = true;
        }
        return null;
    }

    private Set<Trace> generateNNoisedTraces(int type, float number, EventLog disc) {
        Set<Trace> ret = new HashSet<>();
        for (int i = 0; i<number; i++){
            int rand = (int) (Math.random()*(disc.getTraces().size()-1));
            Trace t = (Trace) DeepCopy.copy((disc.getTraces().toArray()[rand]));
            ret.add( noisifyTrace(t, type));
        }
        return ret;
    }


    private Trace noisifyTrace(Trace t, int noiseType) {
        Trace ret = new TraceImpl(null);
        int size = t.getTrace().size();
        int roundAlign = ((int) (Math.random()*size));
        int st = (int) (Math.random()*(size/2));
        int en = ((int) (Math.random()*(size/2))+(size/2));
        switch (noiseType){
            case 0:
                if (roundAlign>=size-2){
                    ret.appendEvent(t.get(size-1));
                }else{
                    for (int i= ((int)(roundAlign+1)); i<size; i++){
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 1:
                if (roundAlign>=size-2){
                    ret.appendEvent(t.get(0));
                }else{
                    for (int i= 0; i<(size - (int)(roundAlign+1)); i++){
                        ret.appendEvent(t.get(i));
                    }
                }
                break;
            case 2:

                if (roundAlign==0){
                    roundAlign++;
                }
                if (roundAlign> size-2){
                    roundAlign = size-2;
                }
                    for (int i= 0; i< (int)(size/2.0 - roundAlign/2.0); i++){
                        ret.appendEvent(t.get(i));
                    }
                    for (int i= (int)(roundAlign/2.0 + size/2.0); i<size; i++){
                        ret.appendEvent(t.get(i));
                    }
                break;
            case 3:
                if (en == st){
                    en--;
                }
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
                int which = (int) (Math.random()*size);
                for (int i= 0; i<size; i++){
                    if (i == which){
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
        if (minSet.contains(LogUtil.toSimpleTrace(ret))){
            return noisifyTrace(t, noiseType);
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
       Set<Trace> reference = PURPLE.bpmnSimpleLog(Bpmn.readModelFromFile(new File("m.bpmn"))).getE();
       //XLog reference = LogIO.parseXES("C:\\Users\\lo_re\\Git\\Work\\Purpose Parametric Log Generator\\Validation\\Conformance Checking\\Noise frequencies\\tmp.xes");

        XLog log = LogIO.parseXES("p0.xes");
        float[] freq = new float[]{1,1,1};
        Set<List<String>> ref = new HashSet<>();
        Set<String> activities = new HashSet<>();

        for(Trace t : reference){
            List<String> tr = new ArrayList<>();
            for (Event e : t.getTrace()){
                String n =  e.getEventName();//String.valueOf(e.getAttributes().get("concept:name"));
                tr.add(String.valueOf(n));
                activities.add(String.valueOf(n));
            }
            ref.add(tr);
        }

        List<List<String>> l = new ArrayList<>();
        for(XTrace t : log){
            List<String> tr = new ArrayList<>();
            int i = 0;
            for (XEvent e : t){
                tr.add(String.valueOf(e.getAttributes().get("concept:name")));
                i++;
            }
            l.add(tr);
        }
        l.removeAll(ref);
        int[] times = new int[]{0,0,0,0,0};

        for(List<String> traccia : l){
            Set<String> acts = Sets.newHashSet(traccia);
            if (!activities.containsAll(acts)){//ALIEN
                times[4]++;
            }else{
                for(List<String> referenza : ref){
                    String[] onlyActs = onlyAct(traccia, referenza);
                   if (!Sets.newHashSet(referenza).containsAll(acts)) {
                       continue;
                   } else if (traccia.size() == referenza.size()){//SWAP
                       if ( com.helger.commons.string.util.LevenshteinDistance.getDistance(onlyActs[0], onlyActs[1],1,1,2) > 4)
                           continue;
                       times[3]++;
                       break;
                   } else {
                       int diff = referenza.size()-traccia.size();

                       if (diff < com.helger.commons.string.util.LevenshteinDistance.getDistance(onlyActs[0], onlyActs[1],1,1,2))
                           continue;
                       if (traccia.get(0).equals(referenza.get(diff))){//HEAD
                           times[0]++;
                           break;
                       }else if (traccia.get(traccia.size()-1).equals(referenza.get(referenza.size()-1-diff))){//TAIL
                           times[1]++;
                           break;
                       } else if (traccia.get(0).equals(referenza.get(0)) && traccia.get(traccia.size()-1 ).equals(referenza.get(referenza.size()-1))){//EPISODE
                           times[2]++;
                           break;
                       }
                       else{
                           continue;
                       }
                   }
                }
            }
        }
        System.out.println(times[0]);
        System.out.println(times[1]);
        System.out.println(times[2]);
        System.out.println(times[3]);
        System.out.println(times[4]);
        System.out.println((
                (Math.abs(times[0]-500)/5.0)+
                (Math.abs(times[1]-500)/5.0)+
                (Math.abs(times[2]-500)/5.0)+
                (Math.abs(times[3]-500)/5.0)+
                (Math.abs(times[4]-500)/5.0)
        )/5.0);
    }

    private static String[] onlyAct(List<String> a, List<String> b) {
        String[] ret = new String[2];
        Map<String, Integer> charBag = new HashMap<>();
        int i = 0;
        for (String s : a){
            if (!charBag.containsKey(s)){
                charBag.put(s, i);
                i++;
            }
            ret[0]+= charBag.get(s);
        }
        for (String s : b){
            if (!charBag.containsKey(s)){
                charBag.put(s, i);
                i++;
            }
            ret[1]+= charBag.get(s);
        }
        return ret;
    }
}
