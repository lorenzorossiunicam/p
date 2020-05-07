package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.mida;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import it.unicam.pros.guidedsimulator.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;

public class NAReceiveTaskBehaviour {

	public static Map<Configuration, Event> isActive(Task n, MidaCollabsConfiguration conf, Process process, int instance) throws MidaException {
		Random r = new Random();
		int choice = r.nextInt(5);
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		switch (choice) {
		case 0:
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
			return ret;
		case 1:
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
			return ret;
		case 2:
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
			return ret;
		case 3:
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
			return ret;
		case 4:
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
			return ret;
		case 5:
			ret = NAReceiveTaskBehaviour3.isActive( n, conf, process, instance);
			if(!ret.isEmpty()) { 
				return ret;
			}
			ret = NAReceiveTaskBehaviour2.isActive( n, conf, process, instance);
				if(!ret.isEmpty()) { 
					return ret;
				}
			ret = NAReceiveTaskBehaviour1.isActive( n, conf, process, instance);
			return ret;
		default:
			return ret;
		}
	}
}
