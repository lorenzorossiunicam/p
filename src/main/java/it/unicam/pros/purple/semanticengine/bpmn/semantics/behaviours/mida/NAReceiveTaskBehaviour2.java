package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process; 
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class NAReceiveTaskBehaviour2 {
 
	public static Map<Configuration, Event> isActive(Task n, MidaCollabsConfiguration c, Process process,
                                                     int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		Couple<MessageFlow, String[]> msg = null;
		try {
			msg = ModelUtils.haveMsg(n, conf, cConf);
		} catch (ScriptException e1) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString());
		}
		String[] payload = msg.getV();
		if(Auxiliaries.isActive(conf, n) && payload != null) {
			try {
				ModelUtils.readMsg(n, msg, conf, cConf);
			} catch (ScriptException e1) { 
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString());
			}
			Auxiliaries.decActive(conf, n.getId(), 1);
			Auxiliaries.incReceiving(conf, n.getId(), 1);  
			ret.put(cConf, LogUtil.receiveNActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	} 
}
