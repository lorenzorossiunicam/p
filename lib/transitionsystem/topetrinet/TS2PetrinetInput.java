package org.processmining.plugins.transitionsystem.topetrinet;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;

/*
 * HV 20100507 Is this class being used at all? Seems not...
 * 
 * Anyways, the "extends TSMinerOutput" blocked the move of the TSMIner to its
 * own package. Therefore, I removed the extends. In case this is needed after
 * all, this class (and its siblings) should be moved to a new package that
 * depends on the TSMiner.
 */
public class TS2PetrinetInput /* extends TSMinerOutput */{
	public TS2PetrinetInput() {

	}

	public void exportTemp(FileOutputStream stream, TransitionSystem ts) {

		HashMap<State, String> mapst = new HashMap<State, String>();
		int i = 0;
		String s = "s";
		for (State state : ts.getNodes()) {
			mapst.put(state, s + i);
			++i;
		}

		// a naive way of generating the TS
		for (org.processmining.models.graphbased.directed.transitionsystem.Transition transTS : ts.getEdges()) {

			String s1 = mapst.get(transTS.getSource()) + " " + transTS.getLabel() + " "
					+ mapst.get(transTS.getTarget());
			try {
				stream.write(s1.getBytes());
			} catch (IOException e) {
				/*
				 * context.log("Unable to write into intermidiate file" +
				 * e.toString(), MessageLevel.DEBUG);
				 */
			}
		}
		try {
			stream.close();
		} catch (IOException e) {
		}
	}
}
