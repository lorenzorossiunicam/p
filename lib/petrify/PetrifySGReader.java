package org.processmining.plugins.petrify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.transitionsystem.StartStateSet;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;

/**
 * Supports the reading of a Transition System from a .sg file
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifySGReader {

	public static final char COMMENT = '#';
	public static final char DOT = '.';

	public static final String MARKING = ".marking ";
	public static final String MODEL = ".model";

	/**
	 * Reads a Petri net and its initial marking from the given input stream.
	 * 
	 * @param input
	 *            The given input stream.
	 * @param ts
	 *            The transition system
	 * @param startStates
	 *            The start states.
	 * @throws IOException
	 */
	public void read(InputStream input, TransitionSystem ts, StartStateSet startStates) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		String sRead;

		/**
		 * The files are line-based. So, read line by line.
		 */
		sRead = in.readLine();
		while (sRead != null) {
			if ((sRead.length() == 0) || (sRead.charAt(0) == COMMENT)) {
				/**
				 * Empty line or comment, ignore.
				 */
			} else if (sRead.charAt(0) == DOT) {
				/**
				 * Tag lines.
				 */
				if (sRead.startsWith(MARKING)) {
					/**
					 * A state
					 */
					String id = sRead.substring(MARKING.length()).trim().replaceAll("[\\{\\}]", "");
					startStates.add(id);

				} else if (sRead.startsWith(MODEL)) {
					/**
					 * the name of the petrinet
					 */
					String id = sRead.substring(MODEL.length()).trim();
					ts.getAttributeMap().put(AttributeMap.LABEL, id);
				}
			} else {
				/**
				 * This a line containing a triple of state transition state,
				 * e.g. "s0 a s1"
				 */
				ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.split(" ")));

				Object from = ids.get(0);
				ts.addState(from);

				for (int i = 1; i + 1 < ids.size(); i += 2) {
					Object target = ids.get(i + 1);
					ts.addState(target);
					ts.addTransition(from, target, ids.get(i));
					from = target;
				}

			}
			/**
			 * Read next line.
			 */
			sRead = in.readLine();
		}
	}

}
