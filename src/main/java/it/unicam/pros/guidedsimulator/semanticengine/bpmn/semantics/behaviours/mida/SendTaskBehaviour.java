package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.exceptions.MidaException;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process; 
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogUtil;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public class SendTaskBehaviour { 

	public static Map<Configuration, Event> isActive(SendTaskImpl n, MidaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		boolean guard = false;
		try {
			guard = Data.checkGuard(cConf, conf, ModelUtils.getGuard(n));
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getGuard(n)); 
		}
		if (sF != null  && guard) {
			try {
				Data.makeAssignments(cConf, conf, ModelUtils.getAssignments(n));
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getAssignments(n).toString()); 
			}
			try {
				ModelUtils.sendMsg(n, conf, cConf);
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimplePayload(n).toString()); 
			}
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, sF.getId());
			for(MessageFlow mF : ModelUtils.getIOUTMsgS(n, cConf)) {
				MidaStatus.setMsgChange(instance, mF.getId());
			} 
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, LogUtil.sendActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}

}
