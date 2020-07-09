package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import it.unicam.pros.pplg.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import org.camunda.bpm.model.bpmn.impl.instance.TaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process; 

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public final class NATaskBehaviour {

	public static Map<Configuration, Event> isActive(TaskImpl n, MidaCollabsConfiguration conf, Process process,
                                                     int instance) throws MidaException {
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
