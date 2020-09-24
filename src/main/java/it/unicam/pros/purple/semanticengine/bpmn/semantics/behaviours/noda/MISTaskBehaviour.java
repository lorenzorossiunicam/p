package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Task;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;

public final class MISTaskBehaviour { 

	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration c, Process process,
			int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n) ;
		if(sF!=null) {
			int loopCardinality = Integer.valueOf(ModelUtils.getLoopCardinality(n)); 
			Collection<IntActivity> parallels = new HashSet<IntActivity>(1);
			IntActivity miIntTask; 
			if(n instanceof SendTask) { 
				 miIntTask = ModelUtils.createIntSendTask(ModelUtils.getModel(), n); 
			}else if(n instanceof ReceiveTask) { 
				miIntTask = ModelUtils.createIntReceiveTask(ModelUtils.getModel(), n); 
			}else { 
				 miIntTask = ModelUtils.createIntTask(ModelUtils.getModel(), n); 
			}
			parallels.add(miIntTask);
			miIntTask.setCopies(parallels);
			conf.getSigmaC().put(miIntTask.getId(), loopCardinality);
			conf.addIntActivities(parallels);
			Auxiliaries.dec(conf, sF.getId()); 
			ret.put(cConf, EventImpl.emptyEvent());    
		}
		return ret;
	}
}
