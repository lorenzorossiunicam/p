package it.unicam.pros.purple.semanticengine.bpmn.configuration.data;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaCollabsConfiguration;
import it.unicam.pros.purple.semanticengine.bpmn.configuration.MidaProcConfiguration;

public final class Data {

	static ScriptEngine engine;

	public static ScriptEngine getEngine() {
		return engine;
	}

	public Data() {
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName("javascript");
	}

	public static Object globalEval(String script, Bindings local, Bindings global) throws ScriptException {
		Bindings merged = mergeScopes(local, global);
		Object ret = engine.eval(script, merged);
		unmergeScopes(merged, local);
		unmergeScopes(merged, global);
		return ret;
	}

	private static void unmergeScopes(Bindings from, Bindings to) {
		for (String s : to.keySet()) {
			to.put(s, from.get(s));
		}
	}

	private static Bindings mergeScopes(Bindings local, Bindings global) {
		Bindings b = engine.createBindings();
		b.putAll(local);
		b.putAll(global);
		return b;
	}

	public static boolean checkGuard(MidaCollabsConfiguration cConf, MidaProcConfiguration conf, String guard) throws ScriptException {
		return (boolean) globalEval(guard, conf.getSigmaDO().getScope(), cConf.getSigmaDS().getScope());

	}

	public static void makeAssignments(MidaCollabsConfiguration cConf, MidaProcConfiguration conf, List<String> assignments) throws ScriptException {
		Bindings scope = conf.getSigmaDO().getScope();
		if (assignments == null)
			return;
		for (String s : assignments) {
			globalEval(s, scope, cConf.getSigmaDS().getScope());
		}
	}

	public static boolean flowCondition(String condition, MidaCollabsConfiguration cConf, MidaProcConfiguration conf) throws ScriptException {
		return Data.checkGuard(cConf, conf, condition);
	}

	public static String[] evalTuple(String[] tuple, MidaCollabsConfiguration cConf, MidaProcConfiguration conf) throws ScriptException {
		String[] ret = new String[tuple.length];
		for (int i = 0; i < tuple.length; i++) {
			ret[i] = String.valueOf(globalEval(tuple[i], conf.getSigmaDO().getScope(), cConf.getSigmaDS().getScope()));
		}
		return ret;
	}

	public static boolean match(String[] payload, Field[] template) {
		if (payload.length != template.length)
			return false;
		for (int i = 0; i < template.length; i++) {
			if (template[i].isCorrelation() && !template[i].getContent().equals(payload[i])) {
				return false;
			}
		}
		return true;
	}

	public static void exchangeMsg(String[] payload, Field[] template, MidaCollabsConfiguration cConf,
			MidaProcConfiguration conf) throws ScriptException {
		List<String> assignments = new ArrayList<String>();
		for (int i = 0; i < template.length; i++) {
			if (!template[i].isCorrelation()) {
				assignments.add(template[i].getContent() + " = " + payload[i]);
			}
		}
		Data.makeAssignments(cConf, conf, assignments);
	}

	public static Field[] evalFieldTuple(Field[] tuple, MidaCollabsConfiguration cConf, MidaProcConfiguration pConf) throws ScriptException {
		Field[] ret = new Field[tuple.length];
		for (int i = 0; i < tuple.length; i++) {
			if (tuple[i].isCorrelation()) {
				ret[i] = new Field((String) globalEval(tuple[i].getContent(), pConf.getSigmaDO().getScope(),
						cConf.getSigmaDS().getScope()), true);
			} else {
				ret[i] = new Field(tuple[i].getContent(), false);
			}
		}
		return ret;
	}

	public static int evalLoopCardinality(MidaProcConfiguration conf, MidaCollabsConfiguration cConf, String loopCardinality) throws ScriptException { 
		return (int) globalEval(loopCardinality, conf.getSigmaDO().getScope(), cConf.getSigmaDS().getScope());
	}

	public static boolean checkCompletionCondition(MidaCollabsConfiguration cConf, MidaProcConfiguration conf,
			String completionCondition) throws ScriptException {
		return (boolean) globalEval(completionCondition, conf.getSigmaDO().getScope(), cConf.getSigmaDS().getScope());
	}
	
	
}
