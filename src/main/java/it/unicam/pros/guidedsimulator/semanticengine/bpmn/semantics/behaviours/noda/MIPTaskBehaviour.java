package it.unicam.pros.guidedsimulator.semanticengine.bpmn.semantics.behaviours.noda;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import it.unicam.pros.guidedsimulator.semanticengine.Configuration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.guidedsimulator.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.guidedsimulator.util.deepcopy.DeepCopy;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.Event;
import it.unicam.pros.guidedsimulator.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.ReceiveTask;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Task;

public final class MIPTaskBehaviour {
 
	public static Map<Configuration, Event> isActive(Task n, NodaCollabsConfiguration c, Process process,
                                                     int instance) {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
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
