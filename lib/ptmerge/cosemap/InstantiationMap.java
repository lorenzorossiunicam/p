package org.processmining.plugins.ptmerge.cosemap;

import java.util.HashMap;
import java.util.Vector;

import lpsolve.LpSolve;

import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.plugins.ptmerge.cosenet.Node;
import org.processmining.plugins.ptmerge.cosenet.OperatorNode;
import org.processmining.plugins.ptmerge.cosenet.OperatorType;
import org.processmining.plugins.ptmerge.cosenet.TauNode;
import org.processmining.plugins.ptmerge.cosenet.TimeOutNode;


public class InstantiationMap {
	static {
		System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
	}
	
	public CoSeMap cosemap;
	
	public InstantiationMap(){
		cosemap = new CoSeMap();
	}
	
	public CoSeMap instanceOf(CoSeNet D, CoSeNet D1) throws Exception{
		if(cosemap == null){
			cosemap = new CoSeMap();
		}
		cosemap.activityCoSeMap(D, D1);
		LpSolve lp;
		// first exstract all nodes:
		Vector<Node> nodes = new Vector<Node>();
		nodes.addAll(D.getNodes());
		HashMap<Node, Integer> mapN = new HashMap<Node, Integer>();
		
		Vector<Node> nodes1 = new Vector<Node>();
		nodes1.addAll(D1.getNodes());
		HashMap<Node, Integer> mapN1 = new HashMap<Node, Integer>();
		
		// lets create the decision variables, this entails a mapping from pairs of nodes to integers
		Integer[][] mapping = new Integer[D.getNodes().size()][D1.getNodes().size()];
		
		// lets make a map to quickly perform lookups
		int startValue = 0;
		for(Node n: nodes){
			mapN.put(n, startValue);
			startValue++;
		}
		
		startValue = 0;
		for(Node n1: nodes1){
			mapN1.put(n1, startValue);
			startValue++;
		}
		
		startValue = 0;
		for(Node n: nodes){
			for(Node n1: nodes1){
				if(mayBeMapped(D, n, D1, n1, cosemap)){
					mapping[mapN.get(n)][mapN1.get(n1)] = startValue;
					startValue++;
				}
				else{
					mapping[mapN.get(n)][mapN1.get(n1)] = -1;
				}
			}
		}
		
		int numVar = startValue;
		
		if(startValue > D.getNodes().size() * D1.getNodes().size()){
			System.out.println("We have an error in the size: " + startValue);
		}
		
		lp = LpSolve.makeLp(0, numVar);
		
		assert(lp.getLp() != 0);
		lp.setAddRowmode(true);	
		
		int[] colno = new int[numVar];
		double[] row = new double[numVar + 1];
		
		int index = 0;
		// set objective function
		for(Node n: nodes){
			for(Node n1: nodes1){
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					colno[index] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
					row[index] = 1;
					index++;
				}
			}
		}
		lp.setObjFnex(index, row, colno);
		lp.setMaxim();

		// furthermore we have only 0 or 1
		for(Node n: nodes){
			for(Node n1: nodes1){
				//colno[0] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
				//row[0] = 1;
				//lp.addConstraintex(1, row, colno, LpSolve.LE, 1);
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					lp.setBinary(mapping[mapN.get(n)][mapN1.get(n1)] + 1, true);
					// lets bound it even further by demanding that values can only be positive
					colno[0] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
					row[0] = 1;
					lp.addConstraintex(1, row, colno, LpSolve.GE, 0);
					lp.addConstraintex(1, row, colno, LpSolve.LE, 1);
				}
			}
		}
		
		// we also have to add the CoSeMap to this mapping
		index = 0;
		for(Node n: nodes){
			for(Node n1: nodes1){
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					if(cosemap.cosemap.get(n) != null && cosemap.cosemap.get(n).equals(n1)){
						colno[0] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
						row[0] = 1;
						lp.addConstraintex(1, row, colno, LpSolve.EQ, 1);
					}
				}
			}
		}
		
		// first encode we can only map if all the children are mapped
		// but we have to take placeholder nodes into account
		for(OperatorNode o: D.No){
			for(OperatorNode o1: D1.No){
				if(mapping[mapN.get(o)][mapN1.get(o1)] != -1){
					index = 0;
					for(Node n: D.c.get(o)){
						for(Node n1: D1.c.get(o1)){
							if(!exceptionNode(D, o, n) && !exceptionNode(D1, o1, n1)){
								if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
									colno[index] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
									row[index] = 1;
									index++;
								}
							}
						}
					}
					colno[index] = mapping[mapN.get(o)][mapN1.get(o1)] + 1;
					row[index] = - D1.c.get(o1).size();
					index++;
					lp.addConstraintex(index, row, colno, LpSolve.GE, 0);
				}
			}
		}
		
		
		// the children are mapped in the right order SEQ
		for(OperatorNode o: D.No){
			for(OperatorNode o1: D1.No){
				if(mapping[mapN.get(o)][mapN1.get(o1)] != -1){
					for(Node a1: D.c.get(o)){
						for(Node a2: D.c.get(o)){
							for(Node a1p: D1.c.get(o1)){
								for(Node a2p: D1.c.get(o1)){
									if(mapping[mapN.get(a1)][mapN1.get(a1p)] != -1 && mapping[mapN.get(a2)][mapN1.get(a2p)] != -1){
										if(D.lo.get(o).equals(OperatorType.SEQ) && D1.lo.get(o1).equals(OperatorType.SEQ)){
										//if((D.lo.get(o).equals(OperatorType.SEQ) && D1.lo.get(o1).equals(OperatorType.SEQ) ||
										//		D.lo.get(o).equals(OperatorType.LOOP) && D1.lo.get(o1).equals(OperatorType.LOOP))){
											if(D.c.get(o).indexOf(a1) < D.c.get(o).indexOf(a2) && D1.c.get(o1).indexOf(a2p) < D1.c.get(o1).indexOf(a1p)){
												colno[0] = mapping[mapN.get(a1)][mapN1.get(a1p)] + 1;
												row[0] = 1;
												colno[1] = mapping[mapN.get(a2)][mapN1.get(a2p)] + 1;
												row[1] = 1;
												colno[2] = mapping[mapN.get(o)][mapN1.get(o1)] + 1;
												row[2] = 1;
												lp.addConstraintex(3, row, colno, LpSolve.LE, 2);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		// do something smart for the loop nodes
		for(OperatorNode o: D.No){
			for(OperatorNode o1: D1.No){
				if(mapping[mapN.get(o)][mapN1.get(o1)] != -1){
					if((D.lo.get(o).equals(OperatorType.LOOPXOR) && D1.lo.get(o1).equals(OperatorType.LOOPXOR)) ||
							(D.lo.get(o).equals(OperatorType.LOOPDEF) && D1.lo.get(o1).equals(OperatorType.LOOPDEF))){
						index = 0;
						if(mapping[mapN.get(D.c.get(o).elementAt(0))][mapN1.get(D1.c.get(o1).elementAt(0))] != -1 &&
								mapping[mapN.get(D.c.get(o).elementAt(1))][mapN1.get(D1.c.get(o1).elementAt(1))] != -1 &&
								mapping[mapN.get(D.c.get(o).elementAt(2))][mapN1.get(D1.c.get(o1).elementAt(2))] != -1){
							colno[index] = mapping[mapN.get(D.c.get(o).elementAt(0))][mapN1.get(D1.c.get(o1).elementAt(0))] + 1;
							row[index++] = 1;
							colno[index] = mapping[mapN.get(D.c.get(o).elementAt(1))][mapN1.get(D1.c.get(o1).elementAt(1))] + 1;
							row[index++] = 1;
							colno[index] = mapping[mapN.get(D.c.get(o).elementAt(2))][mapN1.get(D1.c.get(o1).elementAt(2))] + 1;
							row[index++] = 1;
						}
						colno[index] = mapping[mapN.get(o)][mapN1.get(o1)] + 1;
						row[index++] = -3;
						lp.addConstraintex(index, row, colno, LpSolve.GE, 0);
					}
				}
			}
		}
		
		// we cannot map on nodes higher in the DAG
		for(OperatorNode o: D.No){
			for(OperatorNode o1: D1.No){
				if(mapping[mapN.get(o)][mapN1.get(o1)] != -1){
					for(Node n: D.pre(o)){
						for(Node n1: D1.suc(o1)){
							if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
								colno[0] = mapping[mapN.get(o)][mapN1.get(o1)] + 1;
								row[0] = 1;
								colno[1] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
								row[1] = 1;
								lp.addConstraintex(2, row, colno, LpSolve.LE, 1);
							}
						}
					}
				}
			}
		}
		
		// we have a 1-on-1 mapping
		for(Node n: nodes){
			index = 0;
			for(Node n1: nodes1){
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					colno[index] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
					row[index] = 1;
					index++;
				}
			}
			lp.addConstraintex(index, row, colno, LpSolve.LE, 1);
		}
		
		// we have a 1-on-1 mapping
		for(Node n1: nodes1){
			index = 0;
			for(Node n: nodes){
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					colno[index] = mapping[mapN.get(n)][mapN1.get(n1)] + 1;
					row[index] = 1;
					index++;
				}
			}
			lp.addConstraintex(index, row, colno, LpSolve.LE, 1);
		}						
		lp.setAddRowmode(false);
		
		lp.writeLp("modelInstantiation.lp");
		
		lp.setVerbose(LpSolve.IMPORTANT);
		
		lp.solve();
		
		System.out.println("Objective value: " + lp.getObjective());
		
		lp.getVariables(row);
		
		for(int i = 0; i < numVar; i++){
			System.out.println(i+1 + ": " + row[i]);
		}
		
		for(Node n: nodes){
			for(Node n1: nodes1){
				String strn = "placeholder";
				if(D.Na.contains(n)){
					strn = D.la.get(n).name;
				}
				else if(D.No.contains(n)){
					strn = D.lo.get(n).toString();
				}
				
				String strn1 = "placeholder";
				if(D1.Na.contains(n1)){
					strn1 = D1.la.get(n1).name;
				}
				else if(D1.No.contains(n1)){
					strn1 = D1.lo.get(n1).toString();
				}
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					if(row[mapping[mapN.get(n)][mapN1.get(n1)]] == 1){
						System.out.println(mapping[mapN.get(n)][mapN1.get(n1)]+1 + " " + strn + " -> " + strn1 + " " + row[mapping[mapN.get(n)][mapN1.get(n1)]]);
					}
				}
			}
		}
		
		
		// now lets create the mapping
		for(Node n: nodes){
			for(Node n1: nodes1){
				if(mapping[mapN.get(n)][mapN1.get(n1)] != -1){
					if(row[mapping[mapN.get(n)][mapN1.get(n1)]] == 1){
						cosemap.cosemap.put(n, n1);
					}
				}
			}
		}
		
		lp.deleteLp();
		
		// test whether we have done a proper mapping
		testMapping(D, D1);
		
		return cosemap;
	}
	
	private void testMapping(CoSeNet D, CoSeNet D1){
		for(Node n: D.getNodes()){
			for(Node n1: D1.getNodes()){
				if(cosemap.cosemap.containsKey(n) && cosemap.cosemap.get(n).equals(n1)){
					if(D.Na.contains(n) && !D1.Na.contains(n1)){
						System.out.println("We have an improper mapping Na !Na");
					}
					if(D.No.contains(n) && !D1.No.contains(n1)){
						System.out.println("We have an improper mapping No !No");
					}
					if(D.Np.contains(n) && !D1.Np.contains(n1)){
						System.out.println("We have an improper mapping Np !Np");
					}
				}
			}
		}
	}
	
	/**
	 * Test whether there is an exception with a particular node, holds for:
	 *  - Tau
	 *  - TimeOut
	 * @return
	 */
	private boolean exceptionNode(CoSeNet D, OperatorNode o, Node n){
		// we have the direct child but we have to check the grandchild
		// assumes we have a void node as parent
		if(D.lo.get(o).equals(OperatorType.VOID)){
			// so don't care
			return false;			
		}
		if(!D.lo.get(o).equals(OperatorType.VOID) && D.No.contains(n) && D.lo.get(n).equals(OperatorType.VOID)){
			// void nodes only have 1 child
			if(D.c.get(n).size() > 1){
				assert(false);
			}
			if(D.c.get(n).firstElement() instanceof TauNode){
				return true;
			}
			if(D.c.get(n).firstElement() instanceof TimeOutNode){
				return true;
			}
			
		}
		if(!D.lo.get(o).equals(OperatorType.VOID) && D.Na.contains(n)){
			// the child is a tau/timeout node
			if(n instanceof TauNode){
				return true;
			}
			if(n instanceof TimeOutNode){
				return true;
			}
			
		}
		return false;
	}
	
	private boolean mayBeMapped(CoSeNet D, Node n, CoSeNet D1, Node n1, CoSeMap cosemap){
		if(D.Na.contains(n) && D1.Na.contains(n1) && cosemap.cosemap.containsKey(n)){
			return cosemap.cosemap.get(n).equals(n1);
		}
		else if(D.No.contains(n) && D1.No.contains(n1)){
			return D.lo.get(n).equals(D1.lo.get(n1));
		}
		else if(D.Np.contains(n) && D1.Np.contains(n1)){
			return true;
		}		
		return false;
	}
}
