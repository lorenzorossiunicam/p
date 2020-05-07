package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;

public class ParallelGatewaySplitBehaviour {
 
	
	public static Map<Configuration, Event> isActive(ParallelGateway n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, sF.getId());  
			ret.put(cConf, EventImpl.emptyEvent());     
		}
		return ret; 
	}
}
