package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NASendTaskBehaviour {

	public static Map<Configuration, Event> isActive(SendTaskImpl n, MidaCollabsConfiguration conf, Process process,
			int instance) throws MidaException {
		Random r = new Random();
		int choice = r.nextInt(5);
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		switch (choice) {
		case 0:
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			return ret;
		case 1:
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			return ret;
		case 2:
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			return ret;
		case 3:
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			return ret;
		case 4:
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			return ret;
		case 5:
			ret = NASendTaskBehaviour3.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour2.isActive(n, conf, process, instance);
			if (!ret.isEmpty()) {
				return ret;
			}
			ret = NASendTaskBehaviour1.isActive(n, conf, process, instance);
			return ret;
		default:
			return ret;
		}
	} 

}
