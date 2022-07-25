package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public final class NATaskBehaviour {

	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration conf, Process process,
			int instance) {
		Random r = new Random();
		int choice = r.nextInt(1);
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		if (choice == 0) {
			ret = NATaskBehaviour1.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NATaskBehaviour2.isActive(n, conf, process, instance);
			return ret;
		} else {
			ret = NATaskBehaviour2.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NATaskBehaviour1.isActive(n, conf, process, instance);
			return ret;
		}
	}
}
