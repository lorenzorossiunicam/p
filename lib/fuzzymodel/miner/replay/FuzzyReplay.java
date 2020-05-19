/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.plugins.fuzzymodel.miner.replay;

import java.io.IOException;
import java.util.ArrayList;

import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;

/**
 * @author christian
 * @author Jiafei Li (jiafei@jlu.edu.cn)
 *
 */

public class FuzzyReplay {
	
	protected FuzzyGraph graph;
	protected XLog log;
	protected ArrayList<TraceReplay> traceReplays;
	protected double value;
	protected ReplayListener listener;
	
	public FuzzyReplay(FuzzyGraph graph, XLog log, ReplayListener listener) 
			throws IndexOutOfBoundsException, IOException {
		this.graph = graph;
		this.log = log;
		this.listener = listener;
		replay(this.log);
	}
	
	public double getValue() {
		return value;
	}
	
	public int numberOfTraces() {
		return traceReplays.size();
	}
	
	public TraceReplay getTraceReplay(int traceIndex) {
		return traceReplays.get(traceIndex);
	}
	
	public void replay(XLog reader) 
			throws IndexOutOfBoundsException, IOException {
		listener.setProgress(0.0);
		XLog log = this.log;
		if(reader != null) {
			log = reader;
		}
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log);
		int numberOfInstances = logInfo.getNumberOfTraces();
		// replay all instances
		double aggregated = 0.0;
		this.traceReplays = new ArrayList<TraceReplay>();
	//	FMLogEvents events = FuzzyMinerLog.getLogEvents(log);
		FMLogEvents events = graph.getLogEvents();
		for(int i=0; i<numberOfInstances; i++) {
			listener.setProgress((double)i / (double)numberOfInstances);
			TraceReplay replay = new TraceReplay(this.graph, log, events, i);
			aggregated += replay.getCoverage();
			traceReplays.add(replay);
		}
		// rectify aggregated value
		value = aggregated / traceReplays.size();
	}

}
