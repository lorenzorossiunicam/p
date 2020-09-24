package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;

public final class StartEventBehaviour {
	
	public static Map<Configuration, Event> isActive(StartEvent n, NodaCollabsConfiguration c, Process process, int instance) { 
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (ModelUtils.hasIncoming(conf, n)!=null) {
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, n.getId());   
			ret.put(cConf, EventImpl.emptyEvent());     
		}
		return ret;
	}
}
