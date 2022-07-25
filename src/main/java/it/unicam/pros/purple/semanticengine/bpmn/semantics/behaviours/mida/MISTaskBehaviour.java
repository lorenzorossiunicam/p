package it.unicam.pros.purple.semanticengine.bpmn.semantics.behaviours.mida;


import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.semanticengine.bpmn.utils.Auxiliaries;
import it.unicam.pros.purple.semanticengine.bpmn.utils.MidaStatus;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.util.deepcopy.DeepCopy;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;

import javax.script.ScriptException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class MISTaskBehaviour { 
	
	public static Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> isActive(Task n, MidaCollabsConfiguration c, Process process,
                                                                                         int instance) throws MidaException {
		Map<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event> ret = new HashMap<Configuration, it.unicam.pros.purple.util.eventlogs.trace.event.Event>();
		MidaCollabsConfiguration cConf = (MidaCollabsConfiguration) DeepCopy.copy(c);
		MidaProcConfiguration conf = ModelUtils.getProcessConf(process, instance, cConf);
		SequenceFlow sF = ModelUtils.hasIncoming(conf, n) ;
		if(sF!=null) {
			int loopCardinality = 0;
			try {
				loopCardinality =Data.evalLoopCardinality(conf, cConf, ModelUtils.getLoopCardinality(n));
			} catch (ScriptException e1) {
				throw new MidaException(MidaStatus.INVALIDSYNTAX, n.getId(), instance, ModelUtils.getLoopCardinality(n));
			} 
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
