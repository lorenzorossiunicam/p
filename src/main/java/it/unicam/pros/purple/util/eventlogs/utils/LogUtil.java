package it.unicam.pros.purple.util.eventlogs.utils;

import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.NodaProcConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntActivity;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.utils.ModelUtils;
import it.unicam.pros.purple.semanticengine.ptnet.PTNetUtil;
import it.unicam.pros.purple.util.eventlogs.trace.event.ActivityState;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import it.unicam.pros.purple.util.eventlogs.trace.event.EventImpl;
import it.unicam.pros.purple.util.eventlogs.trace.event.MsgType;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import javax.script.ScriptException;
import java.util.*;

public class LogUtil {

	public static Map<String, String> getTemplateLog(FlowNode n, MidaProcConfiguration pConf,
			MidaCollabsConfiguration conf) throws ScriptException {
		String[] simpleTmpl = ModelUtils.getSimpleTemplate(n);
		String[] evaluatedTmpl = Data.evalTuple(simpleTmpl, conf, pConf);
		Map<String, String> ret = new HashMap<String, String>(simpleTmpl.length);
		for (int i = 0; i < simpleTmpl.length; i++) {
			ret.put(simpleTmpl[i], evaluatedTmpl[i]);
		}
		return ret;
	}

	public static Map<String, String> getPayloadLog(FlowNode n, MidaProcConfiguration pConf,
			MidaCollabsConfiguration conf) throws ScriptException {
		String[] simplePayl = ModelUtils.getSimplePayload(n);
		String[] evaluatedPayl = Data.evalTuple(simplePayl, conf, pConf);
		Map<String, String> ret = new HashMap<String, String>(simplePayl.length);
		for (int i = 0; i < simplePayl.length; i++) {
			ret.put(simplePayl[i], evaluatedPayl[i]);
		}
		return ret;
	}

	public static List<String> getAssignmentsLog(FlowNode n, MidaProcConfiguration pconf,
			MidaCollabsConfiguration conf) {
		List<String> assigns = ModelUtils.getAssignments(n);
		List<String> ret = new ArrayList<String>(assigns.size());
		for (String x : assigns) {
			String[] s = x.split("=");
			try {
				ret.add(s[0] + " = "
						+ Data.globalEval(s[1], pconf.getSigmaDO().getScope(), conf.getSigmaDS().getScope()));
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	public static boolean compareEvents(Event real, Event template) {

		if (template.getEventName() != null && !template.getEventName().equals(real.getEventName())) {
			return false;
		}
		if (template.getTimestamp() != null && !template.getTimestamp().equals(real.getTimestamp())) {
			return false;
		}
		if (template.getState() != null && !template.getState().equals(real.getState())) {
			return false;
		}
		if (template.getAssignments() != null && !template.getAssignments().equals(real.getAssignments())) {
			return false;
		}
		if (template.getCounter() != null && !template.getCounter().equals(real.getCounter())) {
			return false;
		}
		if (template.isSendOrReceive() != null && !template.isSendOrReceive().equals(real.isSendOrReceive())) {
			return false;
		}
		if (template.getMsgName() != null && !template.getMsgName().equals(real.getMsgName())) {
			return false;
		}
		if (template.getMessage() != null && !template.getMessage().equals(real.getMessage())) {
			return false;
		}
		return true;
	}

	public static Event receiveEvent(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, null, new Date(), ActivityState.NONE, null, MsgType.RECEIVE,
				ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(), null, null, null);
	}

	public static Event sendEvent(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, null, new Date(), ActivityState.NONE, null, MsgType.SEND,
				ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(), null, null, null);
	}

	public static Event sendActivity(String process, String instance,FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE, null, MsgType.SEND,
				ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(), null, null, ModelUtils.getCost(flowNode));
	}

	public static Event receiveActivity(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE, null, MsgType.RECEIVE,
				ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(), null, null, ModelUtils.getCost(flowNode));
	}

	public static Event Activity(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE, null, null, null, null, null, ModelUtils.getCost(flowNode));
	}

	public static Event sendNActivity(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getId()).getState();
		return new EventImpl(process, instance, flowNode.getName(), new Date(), actState, null, MsgType.SEND,
				ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(), null, null, ModelUtils.getCost(flowNode));
	}

	public static Event receiveNActivity(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf,
			NodaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getId()).getState();
		return new EventImpl(process, instance, flowNode.getName(), new Date(), actState, null, MsgType.RECEIVE,
				ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(), null, null, ModelUtils.getCost(flowNode));
	}

	public static Event NActivity(String process, String instance, FlowNode flowNode, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getId()).getState();
		long time = (long) 0.0;
		if(actState.equals(ActivityState.START)){
			time = ModelUtils.getMappaTempi().get(flowNode.getId()).getE();
		} else if (actState.equals(ActivityState.COMPLETE)){
			time = ModelUtils.getMappaTempi().get(flowNode.getId()).getV();
		}
		return new EventImpl(process, instance, flowNode.getName(), new Date(time), actState, null, null, null, null, null, ModelUtils.getCost(flowNode));
	}

	public static Event MIActivity(String process, String instance, IntActivity intAct, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE, null, null, null, null,
				pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
	}

	public static Event MIsendActivity(String process, String instance, IntSendTask intAct, NodaProcConfiguration pConf, NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE, null, MsgType.SEND,
				ModelUtils.getIOUTMsgS(intAct.getMiTask(), conf).iterator().next().getName(), null,
				pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
	}

	public static Event MIreceiveActivity(String process, String instance, IntReceiveTask intAct, NodaProcConfiguration pConf,
                                          NodaCollabsConfiguration conf) {
		return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE, null, MsgType.RECEIVE,
				ModelUtils.getINMsgS(intAct.getMiTask(), conf).iterator().next().getName(), null,
				pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
	}

	public static Event receiveEvent(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, null, new Date(), ActivityState.NONE, null, MsgType.RECEIVE,
					ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getTemplateLog(flowNode, pConf, conf), null, null);
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event sendEvent(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, null, new Date(), ActivityState.NONE, null, MsgType.SEND,
					ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getPayloadLog(flowNode, pConf, conf), null, null);
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event sendActivity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE,
					LogUtil.getAssignmentsLog(flowNode, pConf, conf), MsgType.SEND,
					ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getPayloadLog(flowNode, pConf, conf), null, ModelUtils.getCost(flowNode));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event receiveActivity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE,
					LogUtil.getAssignmentsLog(flowNode, pConf, conf), MsgType.RECEIVE,
					ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getTemplateLog(flowNode, pConf, conf), null, ModelUtils.getCost(flowNode));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event Activity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		return new EventImpl(process, instance, flowNode.getName(), new Date(), ActivityState.COMPLETE,
				LogUtil.getAssignmentsLog(flowNode, pConf, conf), null, null, null, null, ModelUtils.getCost(flowNode));
	}

	public static Event sendNActivity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getName()).getState();
		try {
			return new EventImpl(process, instance, flowNode.getName(), new Date(), actState,
					LogUtil.getAssignmentsLog(flowNode, pConf, conf), MsgType.SEND,
					ModelUtils.getIOUTMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getPayloadLog(flowNode, pConf, conf), null, ModelUtils.getCost(flowNode));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event receiveNActivity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf,
			MidaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getName()).getState();
		try {
			return new EventImpl(process, instance, flowNode.getName(), new Date(), actState,
					LogUtil.getAssignmentsLog(flowNode, pConf, conf), MsgType.RECEIVE,
					ModelUtils.getINMsgS(flowNode, conf).iterator().next().getName(),
					LogUtil.getTemplateLog(flowNode, pConf, conf), null, ModelUtils.getCost(flowNode));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event NActivity(String process, String instance, FlowNode flowNode, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		ActivityState actState = pConf.getSigmaT().get(flowNode.getName()).getState();
		return new EventImpl(process, instance, flowNode.getName(), new Date(), actState, LogUtil.getAssignmentsLog(flowNode, pConf, conf),
				null, null, null, null, ModelUtils.getCost(flowNode));
	}

	public static Event MIActivity(String process, String instance, IntActivity intAct, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE,
				LogUtil.getAssignmentsLog(intAct.getMiTask(), pConf, conf), null, null, null,
				pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
	}

	public static Event MIsendActivity(String process, String instance, IntSendTask intAct, MidaProcConfiguration pConf, MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE,
					LogUtil.getAssignmentsLog(intAct.getMiTask(), pConf, conf), MsgType.SEND,
					ModelUtils.getIOUTMsgS(intAct.getMiTask(), conf).iterator().next().getName(),
					LogUtil.getPayloadLog(intAct.getMiTask(), pConf, conf), pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event MIreceiveActivity(String process, String instance, IntReceiveTask intAct, MidaProcConfiguration pConf,
			MidaCollabsConfiguration conf) {
		try {
			return new EventImpl(process, instance, intAct.getMiTask().getName(), new Date(), ActivityState.COMPLETE,
					LogUtil.getAssignmentsLog(intAct.getMiTask(), pConf, conf), MsgType.RECEIVE,
					ModelUtils.getINMsgS(intAct.getMiTask(), conf).iterator().next().getName(),
					LogUtil.getTemplateLog(intAct.getMiTask(), pConf, conf), pConf.getSigmaC().get(intAct.getId()), ModelUtils.getCost(intAct.getMiTask()));
		} catch (ScriptException e) {
			e.printStackTrace();
			return EventImpl.emptyEvent();
		}
	}

	public static Event PnmlTransition(Transition t) {
		return new EventImpl("p", "i", PTNetUtil.getTransitionName(t), new Date(), ActivityState.COMPLETE,
				null, null, null, null, null,null);
		//return new EventImpl("p", "i", (String) t.getLabel(), new Date(), ActivityState.COMPLETE,
		//				null, null, null, null, null,null);
	}
}
