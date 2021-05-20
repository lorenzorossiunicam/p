package it.unicam.pros.purple.semanticengine.bpmn.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.*;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Data;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.data.Field;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntReceiveTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntSendTask;
import it.unicam.pros.purple.semanticengine.bpmn.elements.IntTask;
import it.unicam.pros.purple.semanticengine.bpmn.exceptions.MidaException;
import it.unicam.pros.purple.util.Couple;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;


import javax.script.ScriptException;
import java.util.*;

public final class ModelUtils {

	private static int mintid = 0;
	private static BpmnModelInstance model;
	private static Map<String, Double> costs = null;
	private static Map<String, Double> taskDuration = null;
	private static int instanceID = 0;


	public static Map<String, Couple<Double, Double>> getMappaTempi() {
		return mappaTempi;
	}

	private static Map<String, Couple<Double, Double>> mappaTempi = new HashMap<>();



	/**
	 * Metodo per ricercare il task precedente
	 * @param n
	 * @return il tempo presente del task immediatamente precedente
	 */

	  public static Double getPredecessorTime(FlowNode n){

	  	Set<Task> candidates = new HashSet<>();
	  	Set<FlowNode> toVisit = new HashSet<>();

	  	toVisit.add(n);
	  	customDFS(toVisit, candidates);
	  	return maxTimeOfCandidates(candidates);
	  }



	  //Prendi i current time di tutti i (task) candidati e mi ritorna il massimo
	  // per fare questo devo implmentare il metodo che mi ritorna il current time dei task
	  public static Double maxTimeOfCandidates(Set<Task> candidates) {
		if(candidates.isEmpty()){
			return 0.0;
		}

	  	double max = -1;

	  	for (Task t : candidates){
	  		double tmp = mappaTempi.get(t.getId()).getV();
	  		if (tmp>max) {
	  			max = tmp;
			}

		}
	  	return  max;
	  }


	public static void customDFS(Set<FlowNode> toVisit, Set<Task> candidates) {
	  	List<FlowNode> predecessor = new ArrayList<>();
	  	Set<FlowNode> toRemove = new HashSet<>(toVisit);
	  	for (FlowNode n : toVisit) {
	  		if(n.getPreviousNodes().count() >= 1) {
				predecessor.addAll(n.getPreviousNodes().list());
			}
		}
		toVisit.removeAll(toRemove);
	  	for(FlowNode p : predecessor) {
	  		if(p instanceof Task && mappaTempi.get(p.getId()) != null) { //implementa metodo getCurrentTime();
				candidates.add((Task) p);
			}
	  		else {
	  			toVisit.add(p);
			}
		}
	  	if(toVisit.isEmpty()) {
	  		return;
		}
	  	else {
	  		customDFS(toVisit,candidates);
		}
	}

	/******
	 *
	 * Fa qui la mappa.
	 * In input ho un task. Devo creare l'hash map, prendere il tempo corrente dal task e
	 * inserirlo dentro l'hashmap
	 * t = task
	 *
	 * */


//	public double getCurrentTime(FlowNode t) {
//		//Map<String, Couple<Double, Double>> mappaTempi = new HashMap<>();
//		//La mappa è stata creata all'inizio della classe.
//
//		mappaTempi.put(t.getId(), new Couple(tempoPassato, tempoPresente));
//		mappaTempi.get(tempoPresente);
//		//mappaTempi.get(new Couple(null, tempoPresente));
//		//return mappaTempi.get(tempoPresente);
//		return tempoPresente;
//
//		//gli devo dire di tornare l'elemento che si trova in TempoPresente.
//	}



	public static BpmnModelInstance getModel() {
		return model;
	}
	private static Integer getNewID() {
		return ModelUtils.instanceID++;
	}
	
	public static boolean isMessageEvent(FlowNode s) {
		return !s.getChildElementsByType(MessageEventDefinition.class).isEmpty();
	}

	public static boolean isNested(StartEvent sE) {
		return sE.getParentElement() instanceof SubProcess;
	}

	public static NodaProcConfiguration getProcessConf(Process process, Integer instance,
                                                       NodaCollabsConfiguration conf) {
		return conf.getSigmaI().get(process.getId()).get(instance);
	}

	public static MidaProcConfiguration getProcessConf(Process process, int instance, MidaCollabsConfiguration conf) {
		return conf.getSigmaI().get(process.getId()).get(instance);
	}

	public static SequenceFlow hasIncoming(ProcConfiguration conf, FlowNode n) {
		if (n instanceof StartEvent && conf.getSigmaE().get(n.getId()) > 0) {
			return n.getOutgoing().iterator().next();
		} else {
			for (SequenceFlow sF : n.getIncoming()) {
				if (conf.getSigmaE().get(sF.getId()) > 0) {
					return sF;
				}
			}
			return null;
		}
	}

	public static String getGuard(FlowNode n) {
		ExtensionElements exts = n.getExtensionElements();
		if (exts == null) {
			return "true";
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.GUARD)) {
				if (melDom.getChildElements().size() == 0)
					return "true";
				melDom = melDom.getChildElements().iterator().next();
				if (melDom.getLocalName().equals(MidaExtensionType.EXP)) {
					return melDom.getTextContent();
				} else {
					return "true";// No guard set
				}
			}
		}
		return "true"; // No extension elements
	}

	public static List<String> getAssignments(FlowNode n) {
		List<String> ret = new ArrayList<String>();
		ExtensionElements exts = n.getExtensionElements();
		if (exts == null) {
			return null;
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.ASSIGNMENTS)) {
				for (DomElement mD : melDom.getChildElements()) {
					if (mD.getLocalName().equals(MidaExtensionType.ASSIGNMENT)) {
						ret.add(mD.getAttribute(MidaExtensionType.ASSIGNMENT));
					} else {
						// do nothing
					}
				}
				return ret;
			}
		}
		return null; // No extension elements
	}

	public static boolean isMultiInstance(FlowNode s) {
		return !s.getChildElementsByType(MultiInstanceLoopCharacteristics.class).isEmpty();
	}

	public static boolean isTerminate(FlowNode s) {
		return !s.getChildElementsByType(TerminateEventDefinition.class).isEmpty();
	}

	public static boolean isSplit(FlowNode flowNode) {
		return flowNode.getOutgoing().size() > 1;
	}

	public static SequenceFlow hasTrueOutgoing(NodaCollabsConfiguration cConf, NodaProcConfiguration conf,
			ExclusiveGateway n) {
		List<SequenceFlow> nexts = new ArrayList<SequenceFlow>(n.getOutgoing());
		Collections.shuffle(nexts);
		return nexts.iterator().next();
	}

	public static SequenceFlow hasTrueOutgoing(MidaCollabsConfiguration cConf, MidaProcConfiguration conf,
			ExclusiveGateway n) throws ScriptException {
		for (SequenceFlow sF : n.getOutgoing()) {
			if (Data.flowCondition(ModelUtils.getCondition(sF), cConf, conf)) {
				return sF;
			}
		}
		return null;
	}

	public static String getCondition(SequenceFlow sF) {
		Collection<ConditionExpression> exp = sF.getChildElementsByType(ConditionExpression.class);
		if (exp == null || exp.size() == 0)
			return "true";
		return exp.iterator().next().getRawTextContent();
	}

	public static void readMsg(FlowNode n, MessageFlow m, NodaProcConfiguration conf, NodaCollabsConfiguration cConf) {
		String msg = m.getId();
		int i = cConf.getSigmaM().get(msg);
		cConf.getSigmaM().put(msg, i - 1);
	}

	public static void readMsg(FlowNode n, Couple<MessageFlow, String[]> msg, MidaProcConfiguration pConf,
                               MidaCollabsConfiguration cConf) throws ScriptException {
		Field[] template = ModelUtils.getTemplate(n, pConf, cConf);
		String[] payload = msg.getV();
		cConf.getSigmaM().get(msg.getE().getId()).remove(payload);
		Data.exchangeMsg(payload, template, cConf, pConf);

	}

	public static String[] getSimpleTemplate(FlowNode n) {
		ExtensionElements exts = n.getExtensionElements();
		ArrayList<String> ret = new ArrayList<String>();
		if (exts == null) {
			return null;
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.MESSAGE)) {
				for (DomElement domEl : melDom.getChildElements()) {
					if (domEl.getLocalName().equals(MidaExtensionType.FIELD)) {
						ret.add(domEl.getAttribute(MidaExtensionType.FIELD));
					}
				}
			}
		}
		return (String[]) ret.toArray();
	}

	private static Field[] getTemplate(FlowNode n, MidaProcConfiguration pConf, MidaCollabsConfiguration cConf)
			throws ScriptException {
		ExtensionElements exts = n.getExtensionElements();
		ArrayList<Field> ret = new ArrayList<Field>();
		if (exts == null) {
			return null;
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.MESSAGE)) {
				for (DomElement domEl : melDom.getChildElements()) {
					if (domEl.getLocalName().equals(MidaExtensionType.FIELD)) {
						ret.add(new Field(domEl.getAttribute(MidaExtensionType.FIELD),
								Boolean.getBoolean(domEl.getAttribute(MidaExtensionType.CORRELATION))));
					}
				}
			}
		}
		Field[] tmp = new Field[ret.size()];
		for (int i = 0; i < ret.size(); i++) {
			tmp[i] = ret.get(i);
		}
		return Data.evalFieldTuple(tmp, cConf, pConf);
	}

	public static Collection<MessageFlow> getINMsgS(FlowNode n, MidaCollabsConfiguration conf) {
		Collection<MessageFlow> ret = new HashSet<MessageFlow>();
		for (MessageFlow mF : model.getModelElementsByType(MessageFlow.class)) {
			if (mF.getTarget().getId().equals(n.getId())) {
				ret.add(mF);
			}
		}
		return ret;
	}

	public static Collection<MessageFlow> getINMsgS(FlowNode n, NodaCollabsConfiguration conf) {
		Collection<MessageFlow> ret = new HashSet<MessageFlow>();
		for (MessageFlow mF : model.getModelElementsByType(MessageFlow.class)) {
			if (mF.getTarget().getId().equals(n.getId())) {
				ret.add(mF);
			}
		}
		return ret;
	}

	public static void sendMsg(FlowNode n, NodaProcConfiguration conf, NodaCollabsConfiguration cConf) {
		for (MessageFlow mF : ModelUtils.getIOUTMsgS(n, cConf)) {
			int i = cConf.getSigmaM().get(mF.getId());
			cConf.getSigmaM().put(mF.getId(), i + 1);
			InteractionNode target = mF.getTarget();
			if (target instanceof StartEvent) {// Instantiate
				try {
					cConf.createInstance(((Process) target.getParentElement()), target.getId());
				} catch (MidaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void sendMsg(FlowNode n, MidaProcConfiguration conf, MidaCollabsConfiguration cConf)
			throws ScriptException {
		Collection<MessageFlow> msgS = ModelUtils.getIOUTMsgS(n, cConf);
		String[] payload = ModelUtils.getPayload(n, conf, cConf);
		for (MessageFlow mF : msgS) {
			cConf.getSigmaM().get(mF.getId()).add(payload);
			InteractionNode target = mF.getTarget();
			if (target instanceof StartEvent) {// Instantiate
				try {
					cConf.createInstance(((Process) target.getParentElement()).getId(), target.getId());
				} catch (MidaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static String[] getSimplePayload(FlowNode n) {
		ExtensionElements exts = n.getExtensionElements();
		ArrayList<String> ret = new ArrayList<String>();
		if (exts == null) {
			return null;
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.MESSAGE)) {
				for (DomElement domEl : melDom.getChildElements()) {
					if (domEl.getLocalName().equals(MidaExtensionType.FIELD)) {
						ret.add(domEl.getAttribute(MidaExtensionType.FIELD));
					}
				}
			}
		}
		String[] tmp = new String[ret.size()];
		for (int i = 0; i < ret.size(); i++) {
			tmp[i] = ret.get(i);
		}
		return tmp;
	}

	private static String[] getPayload(FlowNode n, MidaProcConfiguration conf, MidaCollabsConfiguration cConf)
			throws ScriptException {
		return Data.evalTuple(getSimplePayload(n), cConf, conf);
	}

	public static Collection<MessageFlow> getIOUTMsgS(FlowNode n, MidaCollabsConfiguration conf) {
		Collection<MessageFlow> ret = new HashSet<MessageFlow>();
		for (MessageFlow mF : model.getModelElementsByType(MessageFlow.class)) {
			if (mF.getSource().getId().equals(n.getId())) {
				ret.add(mF);
			}
		}
		return ret;
	}

	public static Collection<MessageFlow> getIOUTMsgS(FlowNode n, NodaCollabsConfiguration conf) {
		Collection<MessageFlow> ret = new HashSet<MessageFlow>();
		for (MessageFlow mF : model.getModelElementsByType(MessageFlow.class)) {
			if (mF.getSource().getId().equals(n.getId())) {
				ret.add(mF);
			}
		}
		return ret;
	}

	public static MessageFlow haveMsg(FlowNode n, NodaProcConfiguration conf, NodaCollabsConfiguration cConf) {
		Collection<MessageFlow> msgS = ModelUtils.getINMsgS(n, cConf);
		for (MessageFlow mF : msgS) {
			if (cConf.getSigmaM().get(mF.getId()) > 0) {
				return mF;
			}
		}
		return null;
	}

	public static Couple<MessageFlow, String[]> haveMsg(FlowNode n, MidaProcConfiguration conf,
			MidaCollabsConfiguration cConf) throws ScriptException {
		Collection<MessageFlow> msgS = ModelUtils.getINMsgS(n, cConf);
		Field[] template = ModelUtils.getTemplate(n, conf, cConf);
		for (MessageFlow mF : msgS) {
			for (String[] payload : cConf.getSigmaM().get(mF.getId())) {
				if (Data.match(payload, template)) {
					return new Couple<MessageFlow, String[]>(mF, payload);
				}
			}
		}
		return null;
	}

	public static String getChanges(MidaCollabsConfiguration from, MidaCollabsConfiguration to) {
		JsonObject changes = new JsonObject();
		changes.add("tokens", MidaStatus.getTokChange());
		changes.add("messages", MidaStatus.getMsgChange());
		JsonObject processData = new JsonObject();
		for (String p : to.getSigmaI().keySet()) {
			for (Participant par : model.getModelElementsByType(Participant.class)) {
				if (par.getAttributeValue("processRef").equals(p) && par.getName() != null) {
					processData.add(par.getName(), getChanges(to.getSigmaI().get(p)));
					break;
				} else {
					processData.add(p, getChanges(to.getSigmaI().get(p)));
					break;
				}
			}

		}
		changes.add("processData", processData);
		JsonObject globalData = new JsonObject();
		for (String s : to.getSigmaDS().getScope().keySet()) {
			globalData.addProperty(s, to.getSigmaDS().getScope().get(s).toString());
		}
		changes.add("globalData", globalData);
		return changes.toString();
	}

	private static JsonElement getChanges(Map<Integer, MidaProcConfiguration> map) {
		JsonObject ret = new JsonObject();

		for (Integer i : map.keySet()) {
			JsonObject inst = new JsonObject();
			for (String key : map.get(i).getSigmaDO().getScope().keySet()) {
				inst.addProperty(key, map.get(i).getSigmaDO().getScope().get(key).toString());

			}
			ret.add(String.valueOf(i), inst);
		}
		return ret;
	}

	public static boolean areMarked(MidaProcConfiguration conf, Collection<SequenceFlow> incoming) {
		for (SequenceFlow sF : incoming) {
			if (conf.getSigmaE().get(sF.getId()) < 1) {
				return false;
			}
		}
		return true;
	}

	public static boolean areMarked(NodaProcConfiguration conf, Collection<SequenceFlow> incoming) {
		for (SequenceFlow sF : incoming) {
			if (conf.getSigmaE().get(sF.getId()) < 1) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAtomic(Task n) {
		ExtensionElements exts = n.getExtensionElements();
		if (exts == null) {
			return true;
		}
		Collection<ModelElementInstance> extEls = exts.getElements();
		for (ModelElementInstance mel : extEls) {
			DomElement melDom = mel.getDomElement();
			if (melDom.getLocalName().equals(MidaExtensionType.TASKTYPE)) {
				if (melDom.getChildElements().size() == 0)
					return true;
				melDom = melDom.getChildElements().iterator().next();
				if (melDom.getLocalName().equals(MidaExtensionType.TYPE)) {
					String s = melDom.getTextContent();
					if (s.equals(MidaExtensionType.NA_NC) || s.equals(MidaExtensionType.NA_C)) {
						return false;
					}
				} else {
					return true;
				}
			}
		}
		return true;
	}

	public static String getLoopCardinality(Task n) {
		String c = "";
		for (MultiInstanceLoopCharacteristics mIlC : n.getChildElementsByType(MultiInstanceLoopCharacteristics.class)) {
			for (LoopCardinality lC : mIlC.getChildElementsByType(LoopCardinality.class)) {
				c = lC.getTextContent();
				break;
			}
			break;
		}
		return c;
	}

	public static String getCompletionCondition(Task t) {
		String c = "";
		for (MultiInstanceLoopCharacteristics mIlC : t.getChildElementsByType(MultiInstanceLoopCharacteristics.class)) {
			for (CompletionCondition lC : mIlC.getChildElementsByType(CompletionCondition.class)) {
				c = lC.getTextContent();
				break;
			}
			break;
		}
		return c;
	}

	public static boolean isSequential(Task t) {
		Collection<MultiInstanceLoopCharacteristics> lC = t
				.getChildElementsByType(MultiInstanceLoopCharacteristics.class);
		if (lC != null && lC.iterator().next().getAttributeValue("isSequential").equals("true")) {
			return true;
		}
		return false;
	}

	public static boolean isParallel(Task t) {
		Collection<MultiInstanceLoopCharacteristics> lC = t
				.getChildElementsByType(MultiInstanceLoopCharacteristics.class);
		if (lC != null && lC.iterator().next().getAttributeValue("isSequential").equals("false")) {
			return true;
		}
		return false;
	}

	public static IntTask createIntTask(BpmnModelInstance modelInstance, Task t) {
		IntTask element = new IntTask(t.getId() + "-" + getMIIntTaskID(), t);
		return element;
	}

	public static IntSendTask createIntSendTask(BpmnModelInstance modelInstance, Task t) {
		IntSendTask element = new IntSendTask(t.getId() + "-" + getMIIntTaskID(), t);
		return element;
	}

	public static IntReceiveTask createIntReceiveTask(BpmnModelInstance modelInstance, Task t) {
		IntReceiveTask element = new IntReceiveTask(t.getId() + "-" + getMIIntTaskID(), t);
		return element;
	}

	private static int getMIIntTaskID() {
		return mintid++;
	}

	public static <T extends BpmnModelElementInstance> T createElement(BpmnModelInstance modelInstance,
			BpmnModelElementInstance parentElement, String id, Class<T> elementClass) {
		T element = modelInstance.newInstance(elementClass);
		element.setAttributeValue("id", id, true);
		parentElement.addChildElement(element);
		return element;
	}

	public static void setModel(BpmnModelInstance mi) {
		model = mi;
	}

	public static void setCosts(Map<String, Double> costMap){
		costs = costMap;
	}
	public static Double getCost(FlowNode flowNode) {
		//Get cost directly from model??
		if (costs == null){
			return null;
		}
		return costs.get(flowNode.getId());
	}

	public static void setDurations(Map<String, Double> actDur) {
		taskDuration = actDur;
	}

	public static double getTaskDuration(String id){
		return taskDuration.get(id);
	}
}
