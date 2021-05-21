package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class NASendTaskBehaviour1 {
 
	public static Map<Configuration, Event> isActive(SendTaskImpl n, NodaCollabsConfiguration c, Process process,
			int instance) { 
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null && Auxiliaries.isInactive(conf, n)) {
			double initTime = ModelUtils.getPredecessorTime(n);
			Map<String, Couple<Long, Long>> mappaTempi = ModelUtils.getMappaTempi();

			if(!mappaTempi.containsKey(n.getId())){
				mappaTempi.put(n.getId(),new Couple<Long, Long>((long) 0.0,(long) 0.0));
			}
			mappaTempi.get(n.getId()).setE((long) initTime);
			Auxiliaries.incActive(conf, n.getId(), 1);
			Auxiliaries.dec(conf, sF.getId()); 
			ret.put(cConf, LogUtil.sendNActivity(process.getId(), String.valueOf(instance), n,conf, cConf)); 
		}
		return ret;
	}


}
