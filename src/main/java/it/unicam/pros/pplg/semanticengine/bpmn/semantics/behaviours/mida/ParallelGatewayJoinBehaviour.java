package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.MidaStatus;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;
import it.unicam.pros.pplg.util.eventlogs.trace.event.EventImpl;

public class ParallelGatewayJoinBehaviour { 
	
	public static Map<Configuration, Event> isActive(ParallelGateway n, MidaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (ModelUtils.areMarked(conf, n.getIncoming())) {
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, n.getIncoming());
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	}

	
}
