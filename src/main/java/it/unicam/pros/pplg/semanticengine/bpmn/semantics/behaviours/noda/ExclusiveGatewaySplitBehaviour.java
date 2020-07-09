package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap;
import java.util.Map;

import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;
import it.unicam.pros.pplg.util.eventlogs.trace.event.EventImpl;

public class ExclusiveGatewaySplitBehaviour {

	public static Map<Configuration, Event> isActive(ExclusiveGateway n, NodaCollabsConfiguration c, Process process,
			int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, c);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			for (SequenceFlow out : n.getOutgoing()) {
				NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
				conf = ModelUtils.getProcessConf(process, instance, cConf); 
				Auxiliaries.inc(conf, out.getId());
				Auxiliaries.dec(conf, sF.getId());
				ret.put(cConf, EventImpl.emptyEvent());
			}
		} 
		return ret;
	}

}
