package it.unicam.pros.pplg.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.HashMap; 
import java.util.Map;
import java.util.Random; 

import org.camunda.bpm.model.bpmn.impl.instance.SendTaskImpl;
import org.camunda.bpm.model.bpmn.instance.Process;

import it.unicam.pros.pplg.semanticengine.Configuration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.pplg.util.eventlogs.trace.event.Event;

public class NASendTaskBehaviour {

	public static Map<Configuration, Event> isActive(SendTaskImpl n, NodaCollabsConfiguration conf, Process process,
			int instance) {
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
