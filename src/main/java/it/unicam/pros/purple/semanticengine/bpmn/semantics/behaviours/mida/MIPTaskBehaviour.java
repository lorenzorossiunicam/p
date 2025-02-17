package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;

import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MIPTaskBehaviour { 
	
	public static Map<Configuration, Event> isActive(Task n, MidaCollabsConfiguration c, Process process,
                                                     int instance) throws MidaException {
		Map<Configuration, Event> ret = new HashMap<Configuration, Event>();
		MidaCollabsConfiguration cConf = (		MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n);
		if (sF != null) {
			int loopCardinality = 0;
			try {
				loopCardinality = Data.evalLoopCardinality(conf, cConf, ModelUtils.getLoopCardinality(n));
			} catch (ScriptException e1) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance,
						ModelUtils.getLoopCardinality(n));
			}
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
