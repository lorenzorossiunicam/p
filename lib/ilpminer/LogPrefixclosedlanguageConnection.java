package org.processmining.plugins.ilpminer;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;

public class LogPrefixclosedlanguageConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String LANGUAGE = "Prefix-Closed Language";

	public LogPrefixclosedlanguageConnection(XLog log,
			PrefixClosedLanguage language) {
		super("Prefix-Closed Language generated from "
				+ XConceptExtension.instance().extractName(log));
		put(LOG, log);
		put(LANGUAGE, language);
	}
}
