package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import org.camunda.bpm.model.bpmn.instance.EndEvent;
import org.camunda.bpm.model.bpmn.instance.MessageFlow;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class MessageEndEventBehaviour {

	public static Map<Configuration, Event> isActive(EndEvent n, MidaCollabsConfiguration c, Process process,
                                                     int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			try {
				ModelUtils.sendMsg(n, conf, cConf);
			} catch (ScriptException e) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance,
						ModelUtils.getSimplePayload(n).toString());
			}
			Auxiliaries.dec(conf, sF.getId());
			for (MessageFlow mF : ModelUtils.getIOUTMsgS(n, cConf)) {
				MidaStatus.setMsgChange(instance, mF.getId());
			}
			MidaStatus.setTokChange(conf.getProcID(), instance, n.getOutgoing());
			ret.put(cConf, LogUtil.sendEvent(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}

}
