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
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.HashMap;
import java.util.Map;

public class NAReceiveTaskBehaviour2 {

	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration c, Process process,
			int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		MessageFlow msg = ModelUtils.haveMsg(n, conf, cConf);
		if (Auxiliaries.isActive(conf, n) && msg != null) {
			ModelUtils.readMsg(n, msg, conf, cConf);
			Auxiliaries.decActive(conf, n.getId(), 1);
			Auxiliaries.incReceiving(conf, n.getId(), 1);  
			ret.put(cConf, LogUtil.receiveNActivity(process.getId(), String.valueOf(instance), n, conf, cConf));    
		}
		return ret;
	}

}
