package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public final class StartEventBehaviour {

	public static Map<Configuration, Event> isActive(StartEvent n, MidaCollabsConfiguration c, Process process,
                                                     int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (ModelUtils.hasIncoming(conf, n) != null) {
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, n.getId());
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	}
}
