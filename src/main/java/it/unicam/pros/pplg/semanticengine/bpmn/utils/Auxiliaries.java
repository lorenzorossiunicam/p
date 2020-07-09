package it.unicam.pros.pplg.semanticengine.bpmn.utils;

import java.util.Collection;

import it.unicam.pros.pplg.semanticengine.bpmn.configuration.MidaProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.ProcConfiguration;
import it.unicam.pros.pplg.semanticengine.bpmn.configuration.NodaProcConfiguration;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.bpmn.instance.Task;

/**
 *
 */
public final class Auxiliaries {

	public final static void inc(ProcConfiguration c, String e) {
		int tmp = c.getSigmaE().get(e);
		c.getSigmaE().replace(e, tmp, tmp + 1);
	}

	public final static void dec(ProcConfiguration c, String e) {
		int tmp = c.getSigmaE().get(e);
		c.getSigmaE().replace(e, tmp, tmp - 1);
	}

	public final static void inc(ProcConfiguration c, Collection<SequenceFlow> E) {
		for (SequenceFlow e : E) {
			inc(c, e.getId());
		}
	}

	public final static void dec(ProcConfiguration c, Collection<SequenceFlow> E) {
		for (SequenceFlow e : E) {
			dec(c, e.getId());
		}
	}

	public static void reset(MidaProcConfiguration conf, Collection<SequenceFlow> sFlows) {
		for (SequenceFlow sF : sFlows) {
			conf.getSigmaE().put(sF.getId(), 0);
		}
	}

	public static void reset(NodaProcConfiguration conf, Collection<SequenceFlow> sFlows) {
		for (SequenceFlow sF : sFlows) {
			conf.getSigmaE().put(sF.getId(), 0);
		}
	}
	public static boolean isInactive(NodaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getActive() == 0 && conf.getSigmaT().get(t.getId()).getReceiving() == 0
				&& conf.getSigmaT().get(t.getId()).getSending() == 0;
	}

	public static boolean isActive(NodaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getActive() > 0;
	}

	public static boolean isSending(NodaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getSending() > 0;
	}

	public static boolean isReceiving(NodaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getReceiving() > 0;
	}

	public static boolean isInactive(MidaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getActive() == 0 && conf.getSigmaT().get(t.getId()).getReceiving() == 0
				&& conf.getSigmaT().get(t.getId()).getSending() == 0;
	}

	public static boolean isActive(MidaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getActive() > 0;
	}

	public static boolean isSending(MidaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getSending() > 0;
	}

	public static boolean isReceiving(MidaProcConfiguration conf, Task t) {
		return conf.getSigmaT().get(t.getId()).getReceiving() > 0;
	}
	public static void incActive(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setActive(qnt + conf.getSigmaT().get(id).getActive());
	}

	public static void incSending(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setSending(qnt + conf.getSigmaT().get(id).getSending());
	}

	public static void incReceiving(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setReceiving(qnt + conf.getSigmaT().get(id).getReceiving());
	}

	public static void decActive(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setActive(conf.getSigmaT().get(id).getActive() - qnt);
	}

	public static void decSending(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setSending(conf.getSigmaT().get(id).getSending() - qnt);
	}

	public static void decReceiving(MidaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setReceiving(conf.getSigmaT().get(id).getReceiving() - qnt);
	}

	public static void incActive(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setActive(qnt + conf.getSigmaT().get(id).getActive());
	}

	public static void incSending(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setSending(qnt + conf.getSigmaT().get(id).getSending());
	}

	public static void incReceiving(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setReceiving(qnt + conf.getSigmaT().get(id).getReceiving());
	}

	public static void decActive(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setActive(conf.getSigmaT().get(id).getActive() - qnt);
	}

	public static void decSending(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setSending(conf.getSigmaT().get(id).getSending() - qnt);
	}

	public static void decReceiving(NodaProcConfiguration conf, String id, int qnt) {
		conf.getSigmaT().get(id).setReceiving(conf.getSigmaT().get(id).getReceiving() - qnt);
	}

}
