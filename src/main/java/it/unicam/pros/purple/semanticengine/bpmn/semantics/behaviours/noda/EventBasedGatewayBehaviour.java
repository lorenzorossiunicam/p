package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.HashMap;
import java.util.Map;

public class EventBasedGatewayBehaviour {

	 
	public static Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> isActive(EventBasedGateway n, NodaCollabsConfiguration c,
																									  Process process, int instance) {
		Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> ret = new HashMap<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		MessageFlow  msg = null;
		FlowNode catchEvent = null;
		for (FlowNode fN : n.getSucceedingNodes().list()) {
				msg = ModelUtils.haveMsg(fN, conf, cConf);
			if (msg != null) {
				catchEvent = fN;
				break;
			}
		}
		if (sF != null && msg != null) {
			//ModelUtils.readMsg(catchEvent, msg, conf, cConf);
			Auxiliaries.inc(conf, catchEvent.getIncoming());
			Auxiliaries.dec(conf, sF.getId());  
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret; 
	}
	
}
