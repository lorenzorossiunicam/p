package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.noda;
 
import java.util.HashMap; 
import java.util.Map; 

import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogUtil;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public class NASendTaskBehaviour2 {
 
	public static Map<Configuration, Event> isActive(SendTaskImpl n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (Auxiliaries.isActive(conf, n)) {
			Auxiliaries.decActive(conf, n.getId(), 1);
			Auxiliaries.incSending(conf, n.getId(), 1); 
			ret.put(cConf, LogUtil.sendNActivity(process.getId(), String.valueOf(instance), n, conf, cConf));  
		}
		return ret;
	}

}
