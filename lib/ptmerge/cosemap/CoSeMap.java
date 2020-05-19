package org.processmining.plugins.ptmerge.cosemap;

import java.util.HashMap;

import org.processmining.plugins.ptmerge.cosenet.ActivityNode;
import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.plugins.ptmerge.cosenet.Node;

public class CoSeMap {
	
	public HashMap<Node, Node> cosemap;
	
	public CoSeMap(){
		cosemap = new HashMap<Node, Node>();
	}
	
	public void activityCoSeMap(CoSeNet D, CoSeNet D1){
		SMap smap = new SMap(D, D1);
		cosemap.putAll(smap.smap);
	}
	
	public void extendedCoSeMap(CoSeNet D, CoSeNet D1) throws Exception{
		if(cosemap.isEmpty()){
			activityCoSeMap(D, D1);
		}
		CMap cmap = new CMap(D, D1, this);
		cosemap.putAll(cmap.cmap);
	}
	
	public void gedCoSeMap(CoSeNet D, CoSeNet D1) throws Exception{
		if(cosemap.isEmpty()){
			activityCoSeMap(D, D1);
		}
		// now we have to include the right constraints
		LpConstraints lps = new LpConstraints(D, D1, this);
		
		lps.initialise();
		lps.maxMapping();
		lps.maximalSubgraphs();
		lps.binaryMapping();
		lps.encodeCoSeMap();
		lps.maintainAncestors();
		lps.maintainOrder();
		lps.mapLoops();
		lps.oneOnOne();
		lps.solve();
		cosemap = lps.cosemap.cosemap;
	}
	
	/**
	 * Only yields the mapping of activities from the cosemap
	 * @return the nodes related to the activity cosemap
	 */
	public CoSeMap getActivityCoSeMap(){
		CoSeMap amap = new CoSeMap();
		for(Node n: cosemap.keySet()){
			if(n instanceof ActivityNode && cosemap.get(n) instanceof ActivityNode){
				amap.cosemap.put(n, cosemap.get(n));
			}
		}
		return amap;
	}
	
	@SuppressWarnings("unchecked")
	public <K extends Node> K mapL(K np){
		if(cosemap.containsValue(np)){
			for(Node n: cosemap.keySet()){
				if(cosemap.get(n).equals(np)){
					return (K)n;
				}
			}
			assert(false);
		}
		return np;
	}
	
	@SuppressWarnings("unchecked")
	public <K extends Node> K mapR(K n){
		if(cosemap.containsKey(n)){
			return (K) cosemap.get(n);
		}
		else{
			return n;
		}
	}
}
