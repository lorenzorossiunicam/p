package org.processmining.plugins.transitionsystem.converter;

import org.processmining.plugins.transitionsystem.miner.TSMinerOutput;

/**
 * Transition System Converter Output.
 * 
 * Holds the result of the Transition System Converter.
 * 
 * @author hverbeek
 * @version 0.1
 * 
 */
public class TSConverterOutput extends TSMinerOutput {
	public TSConverterOutput(TSConverterInput settings) {
		super();
		setTransitionSystem(settings.getTransitionSystem());
		setWeights(settings.getWeights());
		setStarts(settings.getStarts());
		setAccepts(settings.getAccepts());
	}
}
