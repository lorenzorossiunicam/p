package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.pplg.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.pplg.util.Couple;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.MidaStatus;
import org.camunda.bpm.model.bpmn.impl.instance.ReceiveTaskImpl;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.util.eventlogs.utils.LogUtil;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public class ReceiveTaskBehaviour { 

	public static Map<Configuration, Event> isActive(ReceiveTaskImpl n, MidaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		Couple<MessageFlow, String[]> msg = null;
		try {
			msg = ModelUtils.haveMsg(n, conf, cConf);
		} catch (ScriptException e1) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString());
		}
		String[] payload = msg.getV();
		boolean guard = false;
		try {
			guard = Data.checkGuard(cConf, conf, ModelUtils.getGuard(n));
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getGuard(n)); 
		}
		if(sF != null && guard && payload != null) {
			try {
				ModelUtils.readMsg(n, msg, conf, cConf);
			} catch (ScriptException e1) { 
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString());
			}
			try {
				Data.makeAssignments(cConf, conf, ModelUtils.getAssignments(n));
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getAssignments(n).toString()); 
			}
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.dec(conf, sF.getId());
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, LogUtil.receiveActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}

}
