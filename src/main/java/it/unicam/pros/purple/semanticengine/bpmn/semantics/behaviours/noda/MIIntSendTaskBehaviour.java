package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;

public class MIIntSendTaskBehaviour {
 
	public static Map<Configuration, Event> isActive(IntSendTask n, NodaCollabsConfiguration c, Process process,
			int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		Task miTask = (Task) n.getMiTask();
		boolean noMore = true;
		for (IntActivity copy : n.getCopies()) {
			noMore &= conf.getSigmaC().get(copy.getId()) == 0;
		}
		if (noMore) {
			for (IntActivity copy : n.getCopies()) {
				conf.getSigmaC().put(copy.getId(), 0);
			}
			conf.removeIntActivities(n.getCopies());
			Auxiliaries.inc(conf, miTask.getOutgoing());  
			ret.put(cConf, LogUtil.MIsendActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		if (conf.getSigmaC().get(n.getId()) > 0) {
			ModelUtils.sendMsg(miTask, conf, cConf);
			conf.getSigmaC().put(n.getId(), conf.getSigmaC().get(n.getId()) - 1);  
			ret.put(cConf, LogUtil.MIsendActivity(process.getId(), String.valueOf(instance), n, conf, cConf));  
		}
		return ret;
	}

}
