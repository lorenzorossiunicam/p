package org.processmining.plugins.ptmerge.merge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.ptmerge.cosemap.CMap;
import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.plugins.ptmerge.cosenet.Node;
import org.processmining.plugins.ptmerge.cosenet.OperatorNode;
import org.processmining.plugins.ptmerge.cosenet.OperatorType;
import org.processmining.plugins.ptmerge.cosenet.PlaceholderNode;

public class Merge {	
	
	/**
	 * @see finish me :-), i.e. make me recursively applicable
	 * @param D
	 */
	private static void postprocess(CoSeNet D) {
		// lets simply map the root placeholder nodes onto eachother
		// so we only have to update the D.R 
		if(D.Np.contains(D.root)){
			Vector<PlaceholderNode> toBeRemoved = new Vector<PlaceholderNode>();
			Vector<Pair<PlaceholderNode, Node>> toBeRemovedPair = new Vector<Pair<PlaceholderNode,Node>>();
			Vector<Pair<PlaceholderNode, Node>> toBeAdded = new Vector<Pair<PlaceholderNode,Node>>();
			for(PlaceholderNode p: D.Np){
				Pair<PlaceholderNode, Node> pair = new Pair<PlaceholderNode, Node>((PlaceholderNode)D.root, p);
				if(D.R.contains(pair)){
					// lets update all R and remove p, and remove <root, p>
					toBeRemoved.add(p);
					toBeRemovedPair.add(pair);
					for(Pair<PlaceholderNode, Node> pair2: D.R){
						if(pair2.getFirst().equals(p)){
							toBeRemovedPair.add(pair2);
							toBeAdded.add(new Pair<PlaceholderNode, Node>((PlaceholderNode) D.root, pair2.getSecond()));
						}
					}
				}
			}
			D.Np.removeAll(toBeRemoved);
			D.R.removeAll(toBeRemovedPair);
			D.R.addAll(toBeAdded);
		}
		
	}
	
	
	////////////////////////// TECH. REPORT ////////////////////////////////////
	public static CoSeNet CoSeMerge(CoSeNet D, CoSeNet D1, CMap cm){
		CoSeNet D2 = new CoSeNet();
		
		PlaceholderNode p = new PlaceholderNode();
		
		// activities
		D2.A.addAll(D.A);
		D2.A.addAll(D1.A);
		
		// activity nodes
		D2.Na.addAll(D.Na);
		D2.Na.addAll(mapLSet(D1.Na, cm));
		
		// void nodes
		D2.Nv.addAll(D.Nv);
		D2.Nv.addAll(mapLSet(D1.Nv, cm));
		
		// operator nodes
		D2.No.addAll(D.No);
		D2.No.addAll(mapLSet(D1.No, cm));
		
		// placeholder nodes
		D2.Np.addAll(D.Np);
		D2.Np.addAll(mapLSet(D1.Np, cm));
		D2.Np.add(p);
		
		// root
		D2.root = p;
		
		// Hidable
		D2.hidable.addAll(D.hidable);
		D2.hidable.addAll(mapLSet(D1.hidable, cm));
		D2.hidable.addAll(mapLHidable(D, D1, cm));
		
		// Blockable
		D2.blockable.addAll(D.blockable);
		D2.blockable.addAll(mapLSet(D1.blockable, cm));
		D2.blockable.addAll(mapLBlockable(D, D1, cm));
		
		// la
		D2.la.putAll(D.la);
		D2.la.putAll(mapLLabel(D1.la, D1.Na, cm));
		
		// lo
		D2.lo.putAll(D.lo);
		D2.lo.putAll(mapLLabel(D1.lo, D1.No, cm));
		
		D2.loTemp.putAll(D.loTemp);
		D2.loTemp.putAll(mapLLabel(D1.loTemp, D1.No, cm));
		
		// c
		D2.c = mapChildren(D, D1, cm);
		
		// R
		D2.R.addAll(D.R);
		D2.R.addAll(mapLRel(D.Np, D1.R, cm));
		D2.R.add(new Pair<PlaceholderNode, Node>(p, D.root));
		D2.R.add(new Pair<PlaceholderNode, Node>(p, cm.map1(D1.root)));
		
		//System.out.print(D2);
		
		//System.out.print("We have: " + D2.getSizeChildrenEdges() + " nr of edges\n");
		
		postprocess(D2);		
		return D2;
	}
	
	@SuppressWarnings("unchecked")
	private static <K extends Node> HashSet<K> mapLSet(HashSet<K> h, CMap cmap){
		HashSet<K> ret = new HashSet<K>();
		for(K n: h){
			ret.add((K) cmap.map1(n));
		}
		return ret;
		
	}
	
	private static HashSet<Node> mapLHidable(CoSeNet D, CoSeNet D1, CMap cmap){
		HashSet<Node> ret = new HashSet<Node>();
		// first clause
		for(Node o: D.No){
			if(cmap.cmap.containsKey(o) && (D.loTemp.get(o).name.equals(OperatorType.SEQ) || D.loTemp.get(o).name.equals(OperatorType.LOOP))){
				for(Node n: D.c.get(o)){
					if(!D1.c.get(cmap.cmap.get(o)).contains(cmap.mapR(n))){
						ret.add(n);
					}
				}
			}
		}
		
		// second clause
		for(Node o1: D1.No){
			if(cmap.cmap.containsValue(o1) && (D1.loTemp.get(o1).name.equals(OperatorType.SEQ) || D1.loTemp.get(o1).name.equals(OperatorType.LOOP))){
				for(Node n1: D1.c.get(o1)){
					if(!D.c.get(cmap.map1(o1)).contains(cmap.map1(n1))){
						ret.add(cmap.map1(n1));
					}
				}
			}
		}	
		
		return ret;
	}
	
	private static HashSet<Node> mapLBlockable(CoSeNet D, CoSeNet D1, CMap cmap){
		HashSet<Node> ret = new HashSet<Node>();
		// first clause
		for(Node o: D.No){
			if(cmap.cmap.containsKey(o) && !(D.loTemp.get(o).name.equals(OperatorType.SEQ) || D.loTemp.get(o).name.equals(OperatorType.LOOP))){
				for(Node n: D.c.get(o)){
					if(!D1.c.get(cmap.cmap.get(o)).contains(cmap.mapR(n))){
						ret.add(n);
					}
				}
			}
		}
		
		// second clause
		for(Node o1: D1.No){
			if(cmap.cmap.containsValue(o1) && !(D1.loTemp.get(o1).name.equals(OperatorType.SEQ) || D1.loTemp.get(o1).name.equals(OperatorType.LOOP))){
				for(Node n1: D1.c.get(o1)){
					if(!D.c.get(cmap.map1(o1)).contains(cmap.map1(n1))){
						ret.add(cmap.map1(n1));
					}
				}
			}
		}	
		
		return ret;
	}
	
	private static <K extends Node, V> HashMap<K, V> mapLLabel(HashMap<K, V> h, HashSet<K> nodes, CMap cmap){
		HashMap<K, V> ret = new HashMap<K, V>();
		for(K n: nodes){
			if(cmap.map1(n).equals(n)){
				ret.put(n, h.get(n));
			}
		}
		return ret;
	}
	
	private static HashMap<Node, Vector<Node>> mapChildren(CoSeNet D, CoSeNet D1, CMap cmap){
		HashMap<Node, Vector<Node>> ret = new HashMap<Node, Vector<Node>>();
		for(OperatorNode n: D.No){
			if(cmap.mapR(n).equals(n)){
				ret.put(n, D.c.get(n));
			}
		}
		for(OperatorNode np: D1.No){
			if(cmap.map1(np).equals(np)){
				ret.put(np, mergeLists(new Vector<Node>(), D1.c.get(np), cmap));
			}
		}
		
		for(OperatorNode n: D.No){
			if(!cmap.mapR(n).equals(n) && !(D.loTemp.get(n).name.equals(OperatorType.SEQ) || D.loTemp.get(n).name.equals(OperatorType.LOOP))){
				ret.put(n, mergeLists(D.c.get(n), D1.c.get(cmap.mapR(n)), cmap));
			}
		}
		
		for(OperatorNode n: D.No){
			if(!cmap.mapR(n).equals(n) && (D.loTemp.get(n).name.equals(OperatorType.SEQ) || D.loTemp.get(n).name.equals(OperatorType.LOOP))){
				ret.put(n, mergeListsSeq(D.c.get(n), D1.c.get(cmap.mapR(n)), cmap));
			}
		}
		
		return ret;
		
	}
	
	private static HashSet<Node> set(Vector<Node> as){
		HashSet<Node> set = new HashSet<Node>();
		for(Node n: as){
			set.add(n);
		}
		return set;
	}
	
	private static Vector<Node> mergeLists(Vector<Node> as, Vector<Node> asp, CMap cmap){
		return mergeListsSet(as, asp, cmap, set(as));
	}
	
	private static Vector<Node> mergeListsSet(Vector<Node> as, Vector<Node> asp, CMap cmap, HashSet<Node> S){
		Vector<Node> ret = new Vector<Node>();
		for(Node n: as){
			ret.add(n);
		}
		for(Node np: asp){
			if(!S.contains(cmap.map1(np))){
				ret.add(cmap.map1(np));
			}
		}
		return ret;
	}
	
	private static Vector<Node> mergeListsSeq(Vector<Node> as, Vector<Node> asp, CMap cmap){
		return mergeListsSeqP(as, asp, cmap, set(asp));
	}
	
	private static Vector<Node> mergeListsSeqP(Vector<Node> as, Vector<Node> asp, CMap cmap, HashSet<Node> Sasp){
		if(as.isEmpty() && asp.isEmpty()){
			return new Vector<Node>();
		}
		if(!as.isEmpty() && asp.isEmpty()){
			return as;
		}
		if(as.isEmpty() && !asp.isEmpty()){
			Vector<Node> ret = new Vector<Node>();
			Node np = asp.firstElement();
			asp.remove(np);
			ret.add(cmap.map1(np));
			ret.addAll(mergeListsSeqP(as, asp, cmap, Sasp));
			return ret;
		}
		else{
			Node n = as.firstElement();
			Node np = asp.firstElement();
			if(cmap.mapR(n).equals(np)){
				Vector<Node> ret = new Vector<Node>();
				as.remove(n);
				asp.remove(np);
				ret.add(n);
				ret.addAll(mergeListsSeqP(as, asp, cmap, Sasp));
				return ret;
			}
			if(!Sasp.contains(cmap.mapR(n))){
				Vector<Node> ret = new Vector<Node>();
				as.remove(n);
				ret.add(n);
				ret.addAll(mergeListsSeqP(as, asp, cmap, Sasp));
				return ret;
			}
			else{
				Vector<Node> ret = new Vector<Node>();
				asp.remove(np);
				ret.add(cmap.map1(np));
				ret.addAll(mergeListsSeqP(as, asp, cmap, Sasp));
				return ret;
			}
		}
	}
	
	private static HashSet<Pair<PlaceholderNode, Node>> mapLRel(HashSet<PlaceholderNode> np, HashSet<Pair<PlaceholderNode, Node>> Rp, CMap cmap){
		HashSet<Pair<PlaceholderNode, Node>> ret = new HashSet<Pair<PlaceholderNode, Node>>();
		for(Pair<PlaceholderNode, Node> t: Rp){
			ret.add(new Pair<PlaceholderNode, Node>((PlaceholderNode) cmap.map1(t.getFirst()), cmap.map1(t.getSecond())));
		}		
		return ret;
	}
	
}
