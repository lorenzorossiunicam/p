package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;


import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.StartEvent;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class MessageStartEventBehaviour { 
	
	public static Map<Configuration, Event> isActive(StartEvent n, MidaCollabsConfiguration c,
			Process process, int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		Couple<MessageFlow, String[]> msg = null;
		try {
			msg = ModelUtils.haveMsg(n, conf, cConf);
		} catch (ScriptException e) {
			throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString());
		} 
		if (msg != null) {
			try {
				ModelUtils.readMsg(n, msg, conf, cConf);
			} catch (ScriptException e) { 
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimpleTemplate(n).toString()); 
			}
			Auxiliaries.inc(conf, n.getOutgoing()); 
			MidaStatus.setTokChange(conf.getProcID(), instance , n.getOutgoing()); 
			ret.put(cConf, LogUtil.receiveEvent(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}

}
