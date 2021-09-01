package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class IntermediateThrowEventBehaviour {

	public static Map<Configuration, Event> isActive(IntermediateThrowEvent n, NodaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			ModelUtils.sendMsg(n, conf, cConf);
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.inc(conf, sF.getId());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	}

}
