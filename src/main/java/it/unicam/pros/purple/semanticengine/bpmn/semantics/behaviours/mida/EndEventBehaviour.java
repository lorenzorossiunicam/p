package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public final class EndEventBehaviour {
	
	public static Map<Configuration, Event> isActive(EndEvent n, MidaCollabsConfiguration c, Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if(sF != null) {
			Auxiliaries.dec(conf, sF.getId());
			MidaStatus.setTokChange(conf.getProcID(), instance, new HashSet<SequenceFlow>());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	} 

}
