package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;
import java.util.Random; 

import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

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
