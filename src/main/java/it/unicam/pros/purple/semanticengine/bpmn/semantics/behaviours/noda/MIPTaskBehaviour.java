package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.noda;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MIPTaskBehaviour {
 
	public static Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> isActive(Task n, NodaCollabsConfiguration c, Process process,
																									  int instance) {
		Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> ret = new HashMap<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event>();
		NodaCollabsConfiguration cConf = (NodaCollabsConfiguration) DeepCopy.copy(c);
		NodaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			int loopCardinality = Integer.valueOf(ModelUtils.getLoopCardinality(n)); 
			Collection<IntActivity> parallels = new HashSet<IntActivity>();
			if (n instanceof SendTask) {
				for (int i = 0; i < loopCardinality; i++) {
					IntSendTask miIntTask = ModelUtils.createIntSendTask(ModelUtils.getModel(), n);
					parallels.add(miIntTask);
				}
			} else if (n instanceof ReceiveTask) {
				for (int i = 0; i < loopCardinality; i++) {
					IntReceiveTask miIntTask = ModelUtils.createIntReceiveTask(ModelUtils.getModel(), n);
					parallels.add(miIntTask);
				}
			} else {
				for (int i = 0; i < loopCardinality; i++) {
					IntTask miIntTask = ModelUtils.createIntTask(ModelUtils.getModel(), n);
					parallels.add(miIntTask);
				}
			}
			for (IntActivity m : parallels) {
				conf.getSigmaC().put(m.getId(), 1);
				m.setCopies(parallels);
			}
			conf.addIntActivities(parallels);
			Auxiliaries.dec(conf, sF.getId());
			ret.put(cConf, EventImpl.emptyEvent());
		}
		return ret;
	}
}
