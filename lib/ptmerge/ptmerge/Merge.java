package org.processmining.plugins.ptmerge.ptmerge;

import java.util.HashMap;

import org.processmining.plugins.ptmerge.ptmap.TaskMap;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.Event;
import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.impl.AbstractBlock.And;
import org.processmining.processtree.impl.AbstractBlock.Def;
import org.processmining.processtree.impl.AbstractBlock.DefLoop;
import org.processmining.processtree.impl.AbstractBlock.Or;
import org.processmining.processtree.impl.AbstractBlock.PlaceHolder;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractBlock.Xor;
import org.processmining.processtree.impl.AbstractBlock.XorLoop;
import org.processmining.processtree.impl.AbstractEvent.Message;
import org.processmining.processtree.impl.AbstractEvent.TimeOut;
import org.processmining.processtree.impl.AbstractTask.Automatic;
import org.processmining.processtree.impl.AbstractTask.Manual;
import org.processmining.processtree.impl.EdgeImpl;
import org.processmining.processtree.impl.ProcessTreeImpl;

public class Merge {
	
	/**
	 * This merges two process trees, note that we only support at the moment TaskMappings
	 * @param pt1 {@link ProcessTree}
	 * @param pt2 {@link ProcessTree}
	 * @param map {@link TaskMap}
	 * @return {@link ProcessTree}
	 */
	public ProcessTree merge(ProcessTree pt1, ProcessTree pt2, TaskMap map){
		ProcessTree tree = new ProcessTreeImpl();
		HashMap<Node, Node> oldNew = new HashMap<Node, Node>();
		HashMap<Block, Integer> childrenSize = new HashMap<Block, Integer>();
		HashMap<Block, HashMap<Integer, Edge>> childrenMap = new HashMap<Block, HashMap<Integer,Edge>>();
		// take pt1 as start point
		for(Node n: pt1.getNodes()){
			Node np = cloneNode(n);
			oldNew.put(n, np);
			if(np instanceof Block){
				childrenMap.put((Block)np, new HashMap<Integer, Edge>());
				childrenSize.put((Block)np, ((Block)n).getOutgoingEdges().size());
			}
			np.setProcessTree(tree);
			tree.addNode(np);
		}
		for(Node n: pt2.getNodes()){
			if(!map.mappingRL.keySet().contains(n)){
				Node np = cloneNode(n);
				oldNew.put(n, np);
				if(np instanceof Block){
					childrenMap.put((Block)np, new HashMap<Integer, Edge>());
					childrenSize.put((Block)np, ((Block)n).getOutgoingEdges().size());
				}
				np.setProcessTree(tree);
				tree.addNode(np);
			}
		}
		for(Edge e: pt1.getEdges()){
			Block parent = (Block) oldNew.get(e.getSource());
			Edge ep = new EdgeImpl(e.getID(), parent, oldNew.get(e.getTarget()), e.getExpression());
			//parent.addOutgoingEdgeAt(ep, e.getSource().getOutgoingEdges().indexOf(e));
			childrenMap.get(parent).put(e.getSource().getOutgoingEdges().indexOf(e), ep);
			tree.addEdge(ep);
		}

		for(Edge e: pt2.getEdges()){
			if(map.mappingRL.keySet().contains(e.getTarget())){
				Block parent = (Block) oldNew.get(e.getSource());
				Edge ep = new EdgeImpl(e.getID(), parent, oldNew.get(map.mappingRL.get(e.getTarget())), e.getExpression());
				//parent.addOutgoingEdgeAt(ep, e.getSource().getOutgoingEdges().indexOf(e));
				childrenMap.get(parent).put(e.getSource().getOutgoingEdges().indexOf(e), ep);
				tree.addEdge(ep);
			}
			else{
				Block parent = (Block) oldNew.get(e.getSource());
				Edge ep = new EdgeImpl(e.getID(), parent, oldNew.get(e.getTarget()), e.getExpression());
				//parent.addOutgoingEdgeAt(ep, e.getSource().getOutgoingEdges().indexOf(e));
				childrenMap.get(parent).put(e.getSource().getOutgoingEdges().indexOf(e), ep);
				tree.addEdge(ep);
			}
		}
		// empty the children map
		for(Block n: childrenMap.keySet()){
			for(int i = 0; i < childrenSize.get(n); i++){
				n.addOutgoingEdge(childrenMap.get(n).get(i));
			}
		}
		if(!map.mappingRL.keySet().contains(pt2.getRoot())){
			PlaceHolder placeholder = new PlaceHolder("Placeholder");
			tree.addNode(placeholder);
			Edge e1 = new EdgeImpl(placeholder, oldNew.get(pt1.getRoot()));
			Edge e2 = new EdgeImpl(placeholder, oldNew.get(pt2.getRoot()));
			tree.addEdge(e1);
			tree.addEdge(e2);
			placeholder.addOutgoingEdge(e1);
			placeholder.addOutgoingEdge(e2);
		
			tree.setRoot(placeholder);
		}
		else{
			tree.setRoot(oldNew.get(pt1.getRoot()));
		}
		
		return tree;
	}
	
	private Node cloneNode(Node n){
		if(n instanceof Block.Seq){
			return new Seq(n.getID(), n.getName());
		}
		else if(n instanceof Block.And){
			return new And(n.getID(), n.getName());
		}
		else if(n instanceof Block.Xor){
			return new Xor(n.getID(), n.getName());
		}
		else if(n instanceof Block.Def){
			return new Def(n.getID(), n.getName());
		}
		else if(n instanceof Block.Or){
			return new Or(n.getID(), n.getName());
		}
		else if(n instanceof Block.XorLoop){
			return new XorLoop(n.getID(), n.getName());
		}
		else if(n instanceof Block.DefLoop){
			return new DefLoop(n.getID(), n.getName());
		}
		else if(n instanceof Block.PlaceHolder){
			return new PlaceHolder(n.getID(), n.getName());
		}
		else if(n instanceof Task.Automatic){
			return new Automatic(n.getID(), n.getName());
		}
		else if(n instanceof Task.Manual){
			return new Manual(n.getID(), n.getName());
		}
		else if(n instanceof Event.TimeOut){
			return new TimeOut(n.getID(), n.getName(), ((TimeOut) n).getMessage());
		}
		else if(n instanceof Event.Message){
			return new Message(n.getID(), n.getName(), ((Message) n).getMessage());
		}
		else{
			throw new NullPointerException();
		}
	}
}
