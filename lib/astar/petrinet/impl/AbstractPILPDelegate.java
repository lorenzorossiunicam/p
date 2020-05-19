package org.processmining.plugins.astar.petrinet.impl;

import gnu.trove.iterator.TShortIterator;
import gnu.trove.procedure.TShortShortProcedure;

import java.util.HashMap;
import java.util.Map;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import nl.tue.astar.Tail;
import nl.tue.astar.impl.State;
import nl.tue.astar.util.LPProblemProvider;
import nl.tue.astar.util.LPResult;
import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.CompressedHashSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.ResetArc;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

public abstract class AbstractPILPDelegate<T extends Tail> extends AbstractPDelegate<T> {

	public static final double INFEASIBLE = -100.0;
	public static final int INFEASIBLE_INT = -1;

	protected final LPProblemProvider solvers;
	protected final Marking[] finalMarkings;
	protected final int threads;

	static {
		try {
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Default constructor, assuming that integer is used to estimate cost
	 * 
	 * @param net
	 * @param log
	 * @param classes
	 * @param map
	 * @param mapTrans2Cost
	 * @param mapEvClass2Cost
	 * @param delta
	 * @param threads
	 * @param set
	 */
	public AbstractPILPDelegate(PetrinetGraph net, XLog log, XEventClasses classes, TransEvClassMapping map,
			Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost, int delta, int threads,
			Marking... set) {
		this(net, log, classes, map, mapTrans2Cost, mapEvClass2Cost, delta, threads, true, set);
	}

	/**
	 * Constructor where estimate value can be changed to real (not integer)
	 * 
	 * @param net
	 * @param log
	 * @param classes
	 * @param map
	 * @param mapTrans2Cost
	 * @param mapEvClass2Cost
	 * @param delta
	 * @param threads
	 * @param useInts
	 * @param set
	 */
	public AbstractPILPDelegate(PetrinetGraph net, XLog log, XEventClasses classes, TransEvClassMapping map,
			Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost, int delta, int threads,
			boolean useInts, Marking... set) {
		this(net, log, classes, map, mapTrans2Cost, mapEvClass2Cost, new HashMap<Transition, Integer>(0), delta,
				threads, useInts, set);
	}

	/**
	 * Constructor where the cost for synchronous moves is set
	 * 
	 * @param net
	 * @param log
	 * @param classes
	 * @param map
	 * @param mapTrans2Cost
	 * @param mapEvClass2Cost
	 * @param mapSync2Cost
	 * @param delta
	 * @param threads
	 * @param useInts
	 * @param set
	 */
	public AbstractPILPDelegate(PetrinetGraph net, XLog log, XEventClasses classes, TransEvClassMapping map,
			Map<Transition, Integer> mapTrans2Cost, Map<XEventClass, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, Marking... set) {
		super(net, log, classes, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, set);

		//		System.out.println("---");

		this.threads = threads;

		if (set.length == 0) {
			throw new IllegalArgumentException("Cannot use ILP without final markings");

		}

		//	createBaseLPProblem();
		// Setup the LP problem

		LpSolve solver = null;
		LPProblemProvider solvers = null;
		try {
			// The variables (columns):
			// The number of transitions for modelmove only
			// The number of transitions for sync move
			// The number of activities for log-move only
			// One transition foe each final marking 

			// The rows (constraints)
			// The number of places
			// The number of activities
			// one constraint summing the final marking transitions to 1

			// The structure of the LP matrix is as follows:
			//    A A 0 C D        mc (current marking)
			//    0 B I 0 0 . x =  pv (parkikh vector)  
			//    0 0 0 1 0        1
			//
			// With A the negative incidence matrix, B the mapping between events and transitions, 
			// C the incidence matrix for final markings and D the incidence matrix of "vacuum" transitions for each reset arc.

			solver = LpSolve.makeLp(places + activities + 1, 2 * transitions + activities + resetArcs + set.length);

			int resets = 0;
			// First, matrix A
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> e : net.getEdges()) {
				if (e instanceof Arc) {
					short p, t;
					int dir;
					if (e.getSource() instanceof Place) {
						p = place2int.get(e.getSource());
						t = trans2int.get(e.getTarget());
						dir = ((Arc) e).getWeight();
					} else {
						t = trans2int.get(e.getSource());
						p = place2int.get(e.getTarget());
						dir = -((Arc) e).getWeight();
					}
					if (set.length > 0) {
						// final markings are given, i.e. the
						// net should be emptied
						solver.setConstrType(p + 1, LpSolve.EQ);
					} else {
						// no final markings are given, i.e. any
						// final marking is acceptable
						solver.setConstrType(p + 1, LpSolve.LE);
					}
					solver.setMat(p + 1, t + 1, solver.getMat(p + 1, t + 1) + dir);
					solver.setMat(p + 1, transitions + t + 1, solver.getMat(p + 1, transitions + t + 1) + dir);
				} else if (e instanceof ResetArc) {
					//  1+ 2 * transitions + activities + set.length
					short p = place2int.get(e.getSource());
					// there is a reset arc on p. Just add a vacuum transition
					int column = 1 + 2 * transitions + activities + set.length + resets;
					resets++;
					solver.setMat(p + 1, column, 1);
					solver.setInt(column, useInts);
					solver.setMat(0, column, 0);

				}
			}
			for (short t = 0; t < transitions; t++) {
				solver.setInt(t + 1, useInts);
				solver.setInt(transitions + t + 1, useInts);
				solver.setLowbo(t + 1, 0);
				solver.setLowbo(transitions + t + 1, 0);
				solver.setUpbo(t + 1, Short.MAX_VALUE - 1);
				solver.setUpbo(transitions + t + 1, Short.MAX_VALUE - 1);
				solver.setMat(0, t + 1, getCostForMoveModel(t));
				//solver.setMat(0, transitions + t + 1, 1); AA: replaced by cost of move sync
				solver.setMat(0, transitions + t + 1, getCostForMoveSync(t));
			}

			// Then, matrix B
			for (short a = 0; a < activities; a++) {
				TShortIterator it = actIndex2trans.get(a).iterator();
				while (it.hasNext()) {
					short t = it.next();
					solver.setMat(places + a + 1, transitions + t + 1, 1.0);
				}
				solver.setInt(2 * transitions + a + 1, useInts);
				solver.setLowbo(2 * transitions + a + 1, 0);
				solver.setUpbo(2 * transitions + a + 1, Short.MAX_VALUE - 1);
				solver.setConstrType(places + a + 1, LpSolve.EQ);
				solver.setMat(places + a + 1, 2 * transitions + a + 1, 1.0);
				solver.setMat(0, 2 * transitions + a + 1, getCostForMoveLog(a));
			}

			// Then, matrix C and the last row
			int c = 2 * transitions + activities + 1;
			for (Marking m : set) {
				for (Place place : m) {
					short p = place2int.get(place);
					solver.setMat(p + 1, c, 1.0);
				}
				solver.setMat(places + activities + 1, c, 1.0);
				solver.setBinary(c, true);
				c++;
			}
			solver.setConstrType(places + activities + 1, LpSolve.EQ);

			solver.setMinim();

			solver.setVerbose(1);

			solver.setScaling(LpSolve.SCALE_GEOMETRIC | LpSolve.SCALE_EQUILIBRATE | LpSolve.SCALE_INTEGERS);
			solver.setScalelimit(5);
			solver.setPivoting(LpSolve.PRICER_DEVEX | LpSolve.PRICE_ADAPTIVE);
			solver.setMaxpivot(250);
			solver.setBbFloorfirst(LpSolve.BRANCH_AUTOMATIC);
			solver.setBbRule(LpSolve.NODE_PSEUDONONINTSELECT | LpSolve.NODE_GREEDYMODE | LpSolve.NODE_DYNAMICMODE
					| LpSolve.NODE_RCOSTFIXING);
			solver.setBbDepthlimit(-50);
			solver.setAntiDegen(LpSolve.ANTIDEGEN_FIXEDVARS | LpSolve.ANTIDEGEN_STALLING);
			solver.setImprove(LpSolve.IMPROVE_DUALFEAS | LpSolve.IMPROVE_THETAGAP);
			solver.setBasiscrash(LpSolve.CRASH_NOTHING);
			solver.setSimplextype(LpSolve.SIMPLEX_DUAL_PRIMAL);

			solvers = new LPProblemProvider(solver, threads);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.solvers = solvers;
			this.finalMarkings = set;
		}

	}

	public void deleteLPs() {
		solvers.deleteLps();
	}

	public LPResult estimate(ShortShortMultiset marking, ShortShortMultiset parikh) {
		//		calls++;
		final double[] rhs = new double[places + activities + 2];

		marking.forEachEntry(new TShortShortProcedure() {

			public boolean execute(short a, short b) {
				rhs[a + 1] = b;
				return true;
			}
		});

		parikh.forEachEntry(new TShortShortProcedure() {

			public boolean execute(short a, short b) {
				rhs[places + a + 1] = b;
				return true;
			}
		});
		rhs[places + activities + 1] = 1;

		LpSolve solver = solvers.firstAvailable();
		try {
			//solver.defaultBasis();
			solver.setRhVec(rhs);
			// do not take longer than 3 seconds to produce the answer, use a naive estimate
			// otherwise
			solver.setTimeout(1);
			int r = solver.solve();
			if (r == LpSolve.OPTIMAL) {
				LPResult res = new LPResult(solver.getNcolumns(), solver.getObjective(), r);
				solver.getVariables(res.getVariables());
				return res;
			}
			// try again with default basis
			solver.defaultBasis();
			r = solver.solve();
			if (r == LpSolve.OPTIMAL) {
				LPResult res = new LPResult(solver.getNcolumns(), solver.getObjective(), r);
				solver.getVariables(res.getVariables());
				return res;
			} else if (r == LpSolve.INFEASIBLE) {
				// TODO: Handle this special case?
				LPResult res = new LPResult(solver.getNcolumns(), INFEASIBLE);
				return res;
			} else {//if (r == LpSolve.NOMEMORY) {
				LPResult res = new LPResult(solver.getNcolumns(), parikh.getNumElts(), r);
				return res;
			}
			//return null;
		} catch (LpSolveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} finally {

			//			long start = System.currentTimeMillis();
			//			int i = 0;
			//			while (System.currentTimeMillis() < start + 2) {
			//				i++;
			//			}

			solvers.finished(solver);
		}

	}

	public void setStateSpace(CompressedHashSet<State<PHead, T>> statespace) {

	}

	public int numFinalMarkings() {
		return finalMarkings.length;
	}

}