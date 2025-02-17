package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.ExclusiveGateway;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class ExclusiveGatewaySplitBehaviour {
 

	public static Map<Configuration, Event> isActive(ExclusiveGateway n, MidaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		SequenceFlow truePath = null;
		try {
			truePath = ModelUtils.hasTrueOutgoing(cConf, conf, n);
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, "");
		}
		if (sF != null && truePath != null) {
			Auxiliaries.inc(conf, truePath.getId());
			Auxiliaries.dec(conf, sF.getId());
			MidaStatus.setTokChange(conf.getProcID(), instance, truePath.getId());
		ret.put(cConf, EventImpl.emptyEvent());
	}
	return ret;
	}

}
