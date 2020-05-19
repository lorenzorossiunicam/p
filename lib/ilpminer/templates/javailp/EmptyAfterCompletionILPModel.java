package org.processmining.plugins.ilpminer.templates.javailp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import net.sf.javailp.Linear;
import net.sf.javailp.Operator;
import net.sf.javailp.Problem;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.plugins.ilpminer.ILPMinerStrategy;
import org.processmining.plugins.ilpminer.ILPModelSettings;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;

/**
 * Extends the WorkflowILPModel class. Looks for a workflow net where it is
 * guaranteed that after completion the net is empty. (i.e. after firing any
 * trace in the log, the net is empty)
 * 
 * @author T. van der Wiel
 * 
 */
@ILPMinerStrategy(description = "Constructs a Petri net that guarantees there are no tokens left.", name = "Petri Net (Empty after Completion)")
public class EmptyAfterCompletionILPModel extends PetriNetILPModel {
	protected ArrayList<int[]> maxWords;

	public EmptyAfterCompletionILPModel(Class<?>[] extensions, Map<SolverSetting, Object> solverSettings,
			ILPModelSettings settings) {
		super(extensions, solverSettings, settings);
	}

	public void makeData() {
		// generate workflow data
		super.makeData();

		maxWords = new ArrayList<int[]>();
		for (XTrace t : r.getLog()) {
			int[] alphabetCounts = new int[trans];
			Arrays.fill(alphabetCounts, 0);
			for (XEvent e : t) {
				alphabetCounts[m.get(r.getEventClasses().getClassOf(e))]++;
			}
			maxWords.add(alphabetCounts);
		}
		removePrefixes(maxWords);
	}

	/**
	 * removes absolute prefixes from the set of words to obtain the set of
	 * maximal words
	 * 
	 * @param words
	 *            - the set of all words
	 */
	private void removePrefixes(ArrayList<int[]> words) {
		ArrayList<int[]> removing = new ArrayList<int[]>();
		for (int[] prefix : words) {
			for (int[] word : words) {
				// when two words are equal, don't check since they would both be deleted. The solver will remove the identical constraint.
				if (prefix.length < word.length) {
					boolean isPrefix = true;
					for (int i = 0; i < prefix.length; i++) {
						if (prefix[i] != word[i]) {
							isPrefix = false;
							break;
						}
					}
					if (isPrefix) {
						removing.add(prefix);
					}
				}
			}
		}
		// only remove the items after searching to keep the iterator consistent. This code is executed only once, so little performance issues here.
		for (int[] prefix : removing) {
			words.remove(prefix);
		}
	}

	protected void addConstraints(Problem p) {
		super.addConstraints(p);
		// forall(mw in MaxWords) ctEmptyAfterCompletion: c + sum(t in Trans) mw.PV[t] * x[t] == sum(t in Trans) mw.PV[t] * y[t];
		for (int[] mw : maxWords) {
			Linear l = new Linear();
			l.add(1, "c");
			for (int t = 0; t < trans; t++) {
				if (mw[t] > 0) {
					l.add(mw[t], "x" + t);
					l.add(-mw[t], "y" + t);
				}
			}
			p.add(l, Operator.EQ, 0);
		}
	}
}
