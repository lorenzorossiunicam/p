package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.HashMap;
import java.util.Map;

public class MIIntTaskBehaviour {
	public static Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> isActive(IntTask n, NodaCollabsConfiguration c, Process process,
                                                     int instance) {
		Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> ret = new HashMap<Configuration, Event>();
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
			ret.put(cConf, LogUtil.MIActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		if (conf.getSigmaC().get(n.getId()) > 0) {
			conf.getSigmaC().put(n.getId(), conf.getSigmaC().get(n.getId()) - 1); 
			ret.put(cConf, LogUtil.MIActivity(process.getId(), String.valueOf(instance), n, conf, cConf));   
		}
		return ret;
	} 
}
