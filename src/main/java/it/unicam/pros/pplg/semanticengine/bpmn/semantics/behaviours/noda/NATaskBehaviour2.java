package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.noda;
 
import java.util.HashMap; 
import java.util.Map;

import it.unicam.pros.pplg.util.eventlogs.utils.LogUtil;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.pplg.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public final class NATaskBehaviour2 {
	 
	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		if(Auxiliaries.isActive(conf, n)) { 
			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.decActive(conf, n.getId(), 1); 
			ret.put(cConf, LogUtil.NActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}
		return ret;
	}
}
