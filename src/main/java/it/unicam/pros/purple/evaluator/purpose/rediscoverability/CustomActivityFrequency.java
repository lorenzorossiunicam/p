package it.unicam.pros.purple.evaluator.purpose.rediscoverability;

import it.unicam.pros.purple.evaluator.Delta;
import it.unicam.pros.purple.evaluator.Evaluator;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.EventLog;
import it.unicam.pros.purple.util.eventlogs.EventLogImpl;
import it.unicam.pros.purple.util.eventlogs.trace.Trace;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CustomActivityFrequency  implements Evaluator {

    private Map<String, Double> frequencies, reached;
    private EventLog lastLog;

    public CustomActivityFrequency(Map<String, Double> frequencies){
        this.frequencies = frequencies;
        reached = new HashMap<>();
        for (String t : this.frequencies.keySet()){
            reached.put(t, 0d);
        }
        this.lastLog = new EventLogImpl("",null);
    }

    @Override
    public Delta evaluate(EventLog disc, Double tau) {
        Map<String, Double> current = new HashMap<>();
        int logSize = disc.size();
        for(Trace t : disc.getTraces()){
            for (Event e : t.getTrace()){
                if(!current.containsKey(e.getEventName())){
                    current.put(e.getEventName(), 0d);
                }
                current.put(e.getEventName(), current.get(e.getEventName())+1);
            }
        }
        for (String t : current.keySet()){
            current.put(t, current.get(t)/logSize);
        }
        return null;
    }
}
