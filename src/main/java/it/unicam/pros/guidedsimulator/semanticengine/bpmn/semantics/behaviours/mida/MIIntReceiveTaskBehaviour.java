package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.guidedsimulator.util.Couple;
import it.unicam.pros.guidedsimulator.util.eventlogs.utils.LogUtil;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public class MIIntReceiveTaskBehaviour { 
	
	public static Map<Configuration, Event> isActive(IntReceiveTask n, MidaCollabsConfiguration c, Process process,
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
		for (IntActivity copy : n.getCopies()) {
			noMore &= conf.getSigmaC().get(copy.getId()) == 0;
		}
		if (completionCondition || noMore) {
			for (IntActivity copy : n.getCopies()) {
				conf.getSigmaC().put(copy.getId(), 0);
			}
			conf.removeIntActivities(n.getCopies());
			Auxiliaries.inc(conf, miTask.getOutgoing());
			MidaStatus.setTokChange(conf.getProcID(), instance, miTask.getOutgoing());
			ret.put(cConf, LogUtil.MIreceiveActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		boolean guard = false;
		try {
			guard = Data.checkGuard(cConf, conf, ModelUtils.getGuard(miTask));
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance, ModelUtils.getGuard(miTask));
		}
		Couple<MessageFlow, String[]> msg = null;
		try {
			msg = ModelUtils.haveMsg(miTask, conf, cConf);
		} catch (ScriptException e1) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance,
					ModelUtils.getSimpleTemplate(miTask).toString());
		}
		String[] payload = msg.getV();
		if (conf.getSigmaC().get(n.getId()) > 0 && guard && payload != null) {
			try {
				ModelUtils.readMsg(miTask, msg, conf, cConf);
			} catch (ScriptException e1) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance,
						ModelUtils.getSimpleTemplate(miTask).toString());
			}
			try {
				Data.makeAssignments(cConf, conf, ModelUtils.getAssignments(miTask));
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, miTask.getId(), instance,
						ModelUtils.getAssignments(miTask).toString());
			}
			conf.getSigmaC().put(n.getId(), conf.getSigmaC().get(n.getId()) - 1);
			ret.put(cConf, LogUtil.MIreceiveActivity(process.getId(), String.valueOf(instance), n, conf, cConf));  
		}
		return ret;
	}

}
