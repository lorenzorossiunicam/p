package org.processmining.plugins.ilpminer;

/**
 * Class used to store a place found as a solution by its inputarcs, its
 * outputarcs and the number of tokens initially present.
 * 
 * @author T. van der Wiel
 * 
 */
public class ILPMinerSolution implements Comparable<ILPMinerSolution> {
	private final int[] inputSet, outputSet;
	private final int tokens;

	public ILPMinerSolution(double[] inputSet, double[] outputSet, double tokens) {
		this.inputSet = new int[inputSet.length];
		this.outputSet = new int[outputSet.length];
		for (int i = 0; i < Math.min(inputSet.length, outputSet.length); i++) {
			this.inputSet[i] = (int) Math.round(inputSet[i]);
			this.outputSet[i] = (int) Math.round(outputSet[i]);
		}
		this.tokens = (int) Math.round(tokens);
	}

	public int[] getInputSet() {
		return inputSet;
	}

	public int[] getOutputSet() {
		return outputSet;
	}

	public double getTokens() {
		return tokens;
	}

	public String toString() {
		String s1 = "", s2 = "";
		for (int i = 0; i < inputSet.length; i++) {
			s1 += inputSet[i] + " ";
			s2 += outputSet[i] + " ";
		}
		return "< [ " + s1 + "] [ " + s2 + "] " + tokens + ">";
	}

	public boolean equals(Object solution) {
		if ((solution != null)
				&& solution.getClass().equals(ILPMinerSolution.class)) {
			return toString().equals(solution.toString());
		}
		return false;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public int compareTo(ILPMinerSolution s) {
		boolean isSmaller = tokens <= s.tokens;
		for (int i = 0; i < inputSet.length; i++) {
			isSmaller = isSmaller && inputSet[i] <= s.inputSet[i]
					&& outputSet[i] <= s.outputSet[i];
		}
		return (this.equals(s) ? 0 : (isSmaller ? -1 : 1));
	}
}
