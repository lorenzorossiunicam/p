package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.EventBasedGateway;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class EventBasedGatewayBehaviour { 
	
	public static Map<Configuration, Event> isActive(EventBasedGateway n, MidaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		Couple<MessageFlow, String[]> msg = null;
		FlowNode catchEvent = null;
		for (FlowNode fN : n.getSucceedingNodes().list()) {
			 try {
				msg = ModelUtils.haveMsg(fN, conf, cConf);
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, fN.getId(), instance, ModelUtils.getSimpleTemplate(fN).toString());
			}
			if (msg != null) {
				catchEvent = fN;
				break;
			}
		}
		if (sF != null && msg != null) {
//			try {
//				ModelUtils.readMsg(catchEvent, msg, conf, cConf);
//			} catch (ScriptException e) {
//				throw new MidaException(MidaStatus.INVALIDSYNTAX, catchEvent.getId(), instance,  ModelUtils.getSimpleTemplate(catchEvent).toString()); 
//			}
			Auxiliaries.inc(conf, catchEvent.getIncoming());
			Auxiliaries.dec(conf, sF.getId());
			MidaStatus.setTokChange(conf.getProcID(), instance,  catchEvent.getOutgoing());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	}
}
