package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;

public class MIIntSendTaskBehaviour {
 
	public static Map<Configuration, Event> isActive(IntSendTask n, MidaCollabsConfiguration c, Process process,
                                                     int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		Task miTask = (Task) n.getMiTask(); 
		boolean completionCondition;
		try {
			completionCondition = Data.checkCompletionCondition(cConf, conf, ModelUtils.getCompletionCondition(miTask));
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance,
					ModelUtils.getCompletionCondition(miTask));
		}
		boolean noMore = true;
		for(IntActivity copy : n.getCopies()) {
			noMore &= conf.getSigmaC().get(copy.getId()) == 0;
		}
		if (completionCondition || noMore) {
			for(IntActivity copy : n.getCopies()) {
				conf.getSigmaC().put(copy.getId(), 0);
			}
			conf.removeIntActivities(n.getCopies());
			Auxiliaries.inc(conf, miTask.getOutgoing());
			MidaStatus.setTokChange(conf.getProcID(), instance, miTask.getOutgoing());
			ret.put(cConf, LogUtil.MIsendActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		} 
		boolean guard = false;
		try {
			guard = Data.checkGuard(cConf, conf, ModelUtils.getGuard(miTask));
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance, ModelUtils.getGuard(miTask));
		}
		if (conf.getSigmaC().get(n.getId()) > 0 && guard) {
			try {
				Data.makeAssignments(cConf, conf, ModelUtils.getAssignments(miTask));
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance,
						ModelUtils.getAssignments(miTask).toString());
			}
			try {
				ModelUtils.sendMsg(miTask, conf, cConf);
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance, ModelUtils.getSimplePayload(miTask).toString()); 
			}
			conf.getSigmaC().put(n.getId(), conf.getSigmaC().get(n.getId()) - 1);
			for(MessageFlow mF : ModelUtils.getIOUTMsgS(miTask, cConf)) {
				MidaStatus.setMsgChange(instance, mF.getId());
			}
			ret.put(cConf, LogUtil.MIsendActivity(process.getId(), String.valueOf(instance), n, conf, cConf));  
		}
		return ret;
	}

}
