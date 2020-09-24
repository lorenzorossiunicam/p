package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map; 

import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class NASendTaskBehaviour3 {
 
	public static Map<Configuration, Event> isActive(SendTaskImpl n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (Auxiliaries.isSending(conf, n)) {
			ModelUtils.sendMsg(n, conf, cConf);
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.decSending(conf, n.getId(), 1); 
			ret.put(cConf, LogUtil.sendNActivity(process.getId(), String.valueOf(instance), n, conf, cConf));   
		}
		return ret;
	} 
}
