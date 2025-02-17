package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import java.util.HashMap;
import java.util.Map;

public class MessageStartEventBehaviour {

 
	public static Map<Configuration, Event> isActive(StartEvent n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		MessageFlow msg = null;
		msg = ModelUtils.haveMsg(n, conf, cConf);
		if (msg != null) {
			ModelUtils.readMsg(n, msg, conf, cConf);
			Auxiliaries.inc(conf, n.getOutgoing()); 
			ret.put(cConf, LogUtil.receiveEvent(process.getId(), String.valueOf(instance), n, conf, cConf));    
		}
		return ret;
	}

}
