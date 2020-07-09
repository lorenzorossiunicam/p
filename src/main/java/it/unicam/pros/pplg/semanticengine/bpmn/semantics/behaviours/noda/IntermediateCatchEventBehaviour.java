package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap;
import java.util.Map;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.util.eventlogs.utils.LogUtil;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

public class IntermediateCatchEventBehaviour {
  
	public static Map<Configuration, Event> isActive(IntermediateCatchEvent n, NodaCollabsConfiguration c,
                                                     Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		MessageFlow msg = null;
		msg = ModelUtils.haveMsg(n, conf, cConf);
		if (sF != null && msg != null) {
			ModelUtils.readMsg(n, msg, conf, cConf);
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, sF.getId()); 
			ret.put(cConf, LogUtil.receiveEvent(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret; 
	}

}
