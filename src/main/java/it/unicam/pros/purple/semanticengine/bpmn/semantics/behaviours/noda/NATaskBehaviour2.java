package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;
 
import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.Couple;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.utils.LogUtil;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.HashMap;
import java.util.Map;

public final class NATaskBehaviour2 {
	 
	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration c,
			Process process, int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);

		if(Auxiliaries.isActive(conf, n)) {

			Map<String, Couple<Long, Long>>  mappaTempi = ModelUtils.getMappaTempi();
			if(!mappaTempi.containsKey(n.getId())){
				mappaTempi.put(n.getId(),new Couple<Long, Long>((long) 0.0,(long) 0.0));
			}
			Long iniTime = mappaTempi.get(n.getId()).getE();
			mappaTempi.get(n.getId()).setV((long) (iniTime + ModelUtils.getTaskDuration(n.getId())));

			Auxiliaries.inc(conf, n.getOutgoing());
			Auxiliaries.decActive(conf, n.getId(), 1);
			ret.put(cConf, LogUtil.NActivity(process.getId(), String.valueOf(instance), n, conf, cConf));
		}


		return ret;
	}

}
