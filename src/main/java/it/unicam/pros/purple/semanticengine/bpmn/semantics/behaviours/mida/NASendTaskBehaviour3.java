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
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;

import javax.script.ScriptException;
import java.util.HashMap;
import java.util.Map;

public class NASendTaskBehaviour3 { 
	
	public static Map<Configuration, Event> isActive(SendTaskImpl n, MidaCollabsConfiguration c, Process process,
                                                     int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if (Auxiliaries.isSending(conf, n)) {
			try {
				ModelUtils.sendMsg(n, conf, cConf);
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getSimplePayload(n).toString());
			}
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.decSending(conf, n.getId(), 1);
			for(MessageFlow mF : ModelUtils.getIOUTMsgS(n, cConf)) {
				MidaStatus.setMsgChange(instance, mF.getId());
			} 
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, LogUtil.sendNActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}

}
