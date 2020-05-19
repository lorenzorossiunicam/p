package org.processmining.plugins.petrify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * Supports the reading of a Petri net from a Petrify/Genet Petri net file.
 * 
 * @author HVERBEEK
 * 
 */
public class PetrifyGReader {

	public static final char COMMENT = '#';
	public static final char DOT = '.';

	public static final String COMMA = ",";
	public static final String DUMMY = ".dummy ";
	public static final String END = "END";
	public static final String EQUALS = "=";
	public static final String INPUTS = ".inputs ";
	public static final String INTERNAL = ".internal ";
	public static final String LPAREN = "(";
	public static final String MARKING = ".marking ";
	public static final String OUTPUTS = ".outputs ";
	public static final String MODEL = ".model ";
	public static final String RPAREN = ")";

	/**
	 * Will map transition identifiers to transitions.
	 */
	private final HashMap<String, Transition> transitions;
	private HashSet<String> declaredTransitions;
	
	/**
	 * Will map place identifiers to places.
	 */
	private final HashMap<String, Place> places;

	/**
	 * Creates a new reader.
	 */
	public PetrifyGReader() {
		transitions = new HashMap<String, Transition>();
		declaredTransitions = new HashSet<String>();
		places = new HashMap<String, Place>();
	}

	/**
	 * Reads a Petri net and its initial marking from the given input stream.
	 * 
	 * @param input
	 *            The given input stream.
	 * @param pn
	 *            The Petri net.
	 * @param marking
	 *            The initial marking.
	 * @return The Petri net.
	 * @throws IOException
	 */
	public Petrinet read(InputStream input, Petrinet pn, Marking marking) throws IOException {
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
				if (sRead.startsWith(OUTPUTS)) {
					/**
					 * A list of transition ids follows this tag. Read these ids
					 * and add the transitions.
					 */
					ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.substring(OUTPUTS.length())
							.split(" ")));
					for (String id : ids) {
						declaredTransitions.add(id);
						//addTransition(pn, id);
					}
				} else if (sRead.startsWith(INTERNAL)) {
					/**
					 * A list of (invisible?) transition ids follows this tag.
					 * Read these ids and add the transitions.
					 */
					ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.substring(INTERNAL.length())
							.split(" ")));
					for (String id : ids) {
						declaredTransitions.add(id);
						//addTransition(pn, id);
					}
				} else if (sRead.startsWith(MODEL)) {
					/**
					 * the name of the petrinet
					 */
					String id = sRead.substring(MODEL.length()).trim();
					pn.getAttributeMap().put(AttributeMap.LABEL, id);
				} else if (sRead.startsWith(INPUTS)) {
					/**
					 * A list of transition ids follows this tag. Read these ids
					 * and add the transitions.
					 */
					ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.substring(INPUTS.length()).split(
							" ")));
					for (String id : ids) {
						declaredTransitions.add(id);
						//addTransition(pn, id);
					}
				} else if (sRead.startsWith(DUMMY)) {
					/**
					 * A list of (invisible?) transition ids follows this tag.
					 * Read these ids and add the transitions.
					 */
					ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.substring(DUMMY.length()).split(
							" ")));
					for (String id : ids) {
						declaredTransitions.add(id);
						//addTransition(pn, id);
					}
				} else if (sRead.startsWith(MARKING)) {
					/**
					 * A multiset of place ids "{ p0 p1=3 p5 }" follows this
					 * tag. Read the initial marking from this.
					 */
					ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.substring(MARKING.length())
							.split(" ")));
					for (int i = 0; i < ids.size(); i++) {
						String s = ids.get(i).replace("{", "").replace("}", "");
						String id;
						if (s.length() > 0) {
							int weight;
							int equalsSign = s.indexOf(EQUALS);
							if (equalsSign >= 0) {
								id = s.substring(0, equalsSign);
								weight = Integer.valueOf(s.substring(equalsSign + EQUALS.length(), s.length()));
							} else {
								id = s;
								weight = 1;
							}
							if (places.keySet().contains(id)) {
								marking.add(places.get(id), weight);
							}
						}
					}
				}
			} else {
				/**
				 * This a line containing either a place-transition arc or a
				 * transition place arc However, the arc may be a weighted arc!
				 * Example: "p11 transid(3)"
				 */
				ArrayList<String> ids = new ArrayList<String>(Arrays.asList(sRead.split(" ")));
				/**
				 * Check whether place-transition or transition-place.
				 */
				if (isPlace(ids.get(0))) {
					/**
					 * Place-transition arc. Add the first identifier as a
					 * place.
					 */
					addPlace(pn, ids.get(0));
					/**
					 * In theory, there could follow a list of transitions, but
					 * this seems theory. Nevertheless, cope with it, just in
					 * case.
					 */
					for (int i = 1; i < ids.size(); i++) {
						String id;
						int weight;
						int parenOpen = ids.get(i).indexOf(LPAREN);
						int parenClose = ids.get(i).indexOf(RPAREN);
						if ((parenOpen > 0) && (parenClose > parenOpen)) {
							/**
							 * There seems to be a weight involved.
							 */
							id = ids.get(i).substring(0, parenOpen);
							weight = Integer.valueOf(ids.get(i).substring(parenOpen + LPAREN.length(), parenClose));
						} else {
							/**
							 * No weight, use 1 as default.
							 */
							id = ids.get(i);
							weight = 1;
						}
						/**
						 * Add the transition and the place-transition arc.
						 */
						addTransition(pn, id);
						addPTArc(pn, ids.get(0), id, weight);
					}
				} else {
					/**
					 * In a similar way: transition-place arc.
					 */
					addTransition(pn, ids.get(0));
					for (int i = 1; i < ids.size(); i++) {
						String id;
						int weight;
						int parenOpen = ids.get(i).indexOf(LPAREN);
						int parenClose = ids.get(i).indexOf(RPAREN);
						if ((parenOpen > 0) && (parenClose > parenOpen)) {
							id = ids.get(i).substring(0, parenOpen);
							weight = Integer.valueOf(ids.get(i).substring(parenOpen + LPAREN.length(), parenClose));
						} else {
							id = ids.get(i);
							weight = 1;
						}
						if (isPlace(id)) {
							/**
							 * Transition-place pair: Add place and arc.
							 */
							addPlace(pn, id);
							addTPArc(pn, ids.get(0), id, weight);
						} else {
							/**
							 * Transition-transition pair: add implicit place
							 * and two arcs.
							 */
							String pId = "<" + ids.get(0) + COMMA + id + ">";
							addPlace(pn, pId);
							addTPArc(pn, ids.get(0), pId, weight);
							addPTArc(pn, pId, id, weight);
						}
					}
				}
			}
			/**
			 * Read next line.
			 */
			sRead = in.readLine();
		}
		/**
		 * A left-over from the previous import by Vladimir Rubin: If some
		 * transition id starts with "END", create a new "END" place and create
		 * a transition-place arc from the first to the second.
		 */
		for (String id : transitions.keySet()) {
			if ((id.length() > END.length() - 1) && id.substring(0, END.length()).equals(END)) {
				addPlace(pn, END);
				addTPArc(pn, id, END, 1);
			}
		}
		/**
		 * Done. Returns the Petri net.
		 */
		return pn;
	}

	/**
	 * Adds a place with the given id to the given Petri net.
	 * 
	 * @param pn
	 *            The given Petri net.
	 * @param id
	 *            The given id.
	 * @return The added place with the given id.
	 */
	private Place addPlace(Petrinet pn, String id) {
		if (!places.keySet().contains(id)) {
			places.put(id, pn.addPlace(id));
		}
		return places.get(id);
	}

	/**
	 * Adds a transition with the given encoded id to the given Petri net.
	 * 
	 * @param pn
	 *            The given Petri net.
	 * @param id
	 *            The given encoded id.
	 * @return The added transition with the given (decoded) id.
	 */
	private Transition addTransition(Petrinet pn, String id) {
		/**
		 * Decode the id.
		 */
		String decodedId = PetrifyConstants.decode(id);
		/**
		 * Add the transition.
		 */
		if (!transitions.keySet().contains(decodedId)) {
			transitions.put(decodedId, pn.addTransition(decodedId));
		}
		return transitions.get(decodedId);
	}

	/**
	 * Adds an arc to the given Petri net from the place with given id to the
	 * transition with given encoded id.
	 * 
	 * @param pn
	 *            The given Petri net.
	 * @param from
	 *            The place id.
	 * @param to
	 *            The encoded transition id.
	 * @param weight
	 *            The arc weight to use.
	 * @return The added arc.
	 */
	private Arc addPTArc(Petrinet pn, String from, String to, int weight) {
		Place place = addPlace(pn, from);
		Transition trans = addTransition(pn, to);
		return pn.addArc(place, trans, weight);
	}

	/**
	 * Adds an arc to the given Petri net from the transition with given encoded
	 * id to the place with given id.
	 * 
	 * @param pn
	 *            The given Petri net.
	 * @param from
	 *            The encoded transition id.
	 * @param to
	 *            The place id.
	 * @param weight
	 *            The arc weight to use.
	 * @return The added arc.
	 */
	private Arc addTPArc(Petrinet pn, String from, String to, int weight) {
		Transition trans = addTransition(pn, from);
		Place place = addPlace(pn, to);
		return pn.addArc(trans, place, weight);
	}

	/**
	 * Returns whether the given string correspond to a place id. If not, it has
	 * to be a transition id. A string corresponds to a place id iff it is not
	 * listed as a transition id.
	 * 
	 * @param st
	 *            The string to check whether a place id.
	 * @return
	 */
	private boolean isPlace(String st) {
		/**
		 * If listed as transition id: false.
		 */
		String id = st.split("[-+/]")[0];
		if (declaredTransitions.contains(id)) {
			return false;
		}
		return true;
	}
}
