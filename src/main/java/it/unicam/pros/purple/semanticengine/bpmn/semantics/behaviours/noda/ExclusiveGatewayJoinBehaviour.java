package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.HashMap;
import java.util.Map;

public class ExclusiveGatewayJoinBehaviour {
 
	public static Map<Configuration, Event> isActive(ExclusiveGateway n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, sF.getId());   
			ret.put(cConf, EventImpl.emptyEvent());  
		}
		return ret; 
	}

}
