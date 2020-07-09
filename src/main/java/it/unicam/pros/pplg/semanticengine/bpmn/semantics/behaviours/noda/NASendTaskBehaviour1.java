package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.util.eventlogs.utils.LogUtil;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public class NASendTaskBehaviour1 {
 
	public static Map<Configuration, Event> isActive(SendTaskImpl n, NodaCollabsConfiguration c, Process process,
			int instance) { 
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null && Auxiliaries.isInactive(conf, n)) {
			Auxiliaries.incActive(conf, n.getId(), 1);
			Auxiliaries.dec(conf, sF.getId()); 
			ret.put(cConf, LogUtil.sendNActivity(process.getId(), String.valueOf(instance), n,conf, cConf)); 
		}
		return ret;
	}


}
