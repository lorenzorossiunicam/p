package org.processmining.plugins.ptmerge.conversions;

import java.util.HashMap;
import java.util.UUID;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ptmerge.cosenet.ActivityNode;
import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.plugins.ptmerge.cosenet.EventNode;
import org.processmining.plugins.ptmerge.cosenet.Group;
import org.processmining.plugins.ptmerge.cosenet.Node;
import org.processmining.plugins.ptmerge.cosenet.OperatorNode;
import org.processmining.plugins.ptmerge.cosenet.OperatorType;
import org.processmining.plugins.ptmerge.cosenet.Originator;
import org.processmining.plugins.ptmerge.cosenet.PlaceholderNode;
import org.processmining.plugins.ptmerge.cosenet.Resource;
import org.processmining.plugins.ptmerge.cosenet.Role;
import org.processmining.plugins.ptmerge.cosenet.VoidNode;
import org.processmining.processtree.Block;
import org.processmining.processtree.Edge;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.Variable;
import org.processmining.processtree.impl.AbstractBlock;
import org.processmining.processtree.impl.AbstractEvent;
import org.processmining.processtree.impl.AbstractOriginator;
import org.processmining.processtree.impl.AbstractTask;
import org.processmining.processtree.impl.EdgeImpl;
import org.processmining.processtree.impl.ProcessTreeImpl;
import org.processmining.processtree.impl.VariableImpl;

public class CoSeNet2PT {
	public static ProcessTree convert(PluginContext context, CoSeNet net) throws InstantiationException, IllegalAccessException{
		return convert(net);
	}
	
	public static ProcessTree convert(CoSeNet net){
		net.fillLoTemp();
		net.checkRootForVoid();
		HashMap<Node, org.processmining.processtree.Node> node2node = new HashMap<Node, org.processmining.processtree.Node>();
		HashMap<Node, Edge> node2edge = new HashMap<Node, Edge>();
		ProcessTree dag;
		if(net.id != null){
			dag = new ProcessTreeImpl(net.id, net.name);
		}
		else{
			dag = new ProcessTreeImpl(net.name);
		}
		// lets convert the different parts of a CoSeNet
		for(ActivityNode an: net.Na){
			Task t;
			if(net.la.get(an).name.equalsIgnoreCase("tau")){
				t = new AbstractTask.Automatic(an.ID, net.la.get(an).name);
			}
			else{
				t = new AbstractTask.Manual(an.ID, net.la.get(an).name);
			}
			//Task t = new AbstractTask.Automatic(an.ID, net.la.get(an).name);
			node2node.put(an, t);
			t.setProcessTree(dag);
			dag.addNode(t);
		}
		
		for(EventNode ev: net.Ne){
			Block e;
			if(net.le.get(ev).name.equalsIgnoreCase("Message")){
				e = new AbstractEvent.Message(ev.ID, net.le.get(ev).name, "Boh");
			}
			else{
				e = new AbstractEvent.TimeOut(ev.ID, net.le.get(ev).name, "Boh");
			}
			node2node.put(ev, e);
			e.setProcessTree(dag);
			dag.addNode(e);
		}
		
		for(OperatorNode on: net.No){
			// lets go through all operators
			if(net.loTemp.get(on).name.equals(OperatorType.SEQ)){
				Block block = new AbstractBlock.Seq(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.AND)){
				Block block = new AbstractBlock.And(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.XOR)){
				Block block = new AbstractBlock.Xor(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.DEF)){
				Block block = new AbstractBlock.Def(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.OR)){
				Block block = new AbstractBlock.Or(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.LOOPXOR)){
				Block block = new AbstractBlock.XorLoop(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
			else if(net.loTemp.get(on).name.equals(OperatorType.LOOPDEF)){
				Block block = new AbstractBlock.DefLoop(on.ID, net.loTemp.get(on).toString());
				node2node.put(on, block);
				block.setProcessTree(dag);
				dag.addNode(block);
			}
		}
		for(PlaceholderNode pn: net.Np){
			Block block = new AbstractBlock.PlaceHolder(pn.ID, "");
			node2node.put(pn, block);
			block.setProcessTree(dag);
			dag.addNode(block);
		}
		
		// now lets set the root
		dag.setRoot(node2node.get(net.root));
		
		// now lets make some children
		for(OperatorNode on: net.No){
			// we do not have voidnodes anymore, so we cannot convert them
			if(!(on instanceof VoidNode)){
				// goody lets compute the transitive closure
				for(Node n: net.c.get(on)){
					boolean hideable = false;
					boolean blockable = false;
					boolean hasVoid = false;
					// take the transitive closure
					Node np = n;
					UUID edgeID = UUID.randomUUID();
					if(net.Nv.contains(np)){
						edgeID = np.ID;
						hasVoid = true;
					}
					while(net.Nv.contains(np)){
						if(net.hidable.contains(np)){
							hideable = true;
						}
						if(net.blockable.contains(np)){
							blockable = true;
						}
						np = net.c.get(np).firstElement();
						if(np == null){
							throw new NullPointerException();
						}
					}
					org.processmining.processtree.Node convNode = node2node.get(on);
					Edge e;
					if(hasVoid){
						e = new EdgeImpl(UUID.randomUUID(), (Block)convNode, node2node.get(np), EdgeImpl.NOEXPRESSION);
						((Block)convNode).addOutgoingEdge(e);
						node2edge.put(net.getNode(edgeID), e);
					}
					else{
						e = ((Block)convNode).addChild(node2node.get(np));
					}
					e.setBlockable(blockable);
					e.setHideable(hideable);
					node2node.get(np).addIncomingEdge(e);
					dag.addEdge(e);
				}
			}
		}
		for(EventNode en: net.Ne){
			// goody lets compute the transitive closure
			for(Node n: net.c.get(en)){
				boolean hideable = false;
				boolean blockable = false;
				// take the transitive closure
				Node np = n;
				Node vd = null;
				while(np instanceof VoidNode){
					vd = np;
					if(net.hidable.contains(np)){
						hideable = true;
					}
					if(net.blockable.contains(np)){
						blockable = true;
					}
					np = net.c.get(np).firstElement();
				}
				org.processmining.processtree.Node convNode = node2node.get(en);
				Edge e = ((Block)convNode).addChild(node2node.get(np));
				if(vd != null){
					node2edge.put(net.c.get(n).firstElement(), e);
				}
				e.setBlockable(blockable);
				e.setHideable(hideable);
				node2node.get(np).addIncomingEdge(e);
				dag.addEdge(e);
			}
		}
		for(PlaceholderNode pn: net.Np){
			// goody lets compute the transitive closure
			for(Pair<PlaceholderNode, Node> repl: net.R){
				if(repl.getFirst().equals(pn)){
					Node n = repl.getSecond();
					Node vd = null;
					boolean hideable = false;
					boolean blockable = false;
					if(n instanceof VoidNode){
						// take the transitive closure
						Node np = n;
						while(np instanceof VoidNode){
							vd = np;
							if(net.hidable.contains(np)){
								hideable = true;
							}
							if(net.blockable.contains(np)){
								blockable = true;
							}
							np = net.c.get(np).firstElement();
						}
						n = np;
					}
					org.processmining.processtree.Node convNode = node2node.get(pn);
					Edge e = ((Block)convNode).addChild(node2node.get(n));
					if(vd != null){
						node2edge.put(vd, e);
					}
					e.setBlockable(blockable);
					e.setHideable(hideable);
					node2node.get(n).addIncomingEdge(e);
					dag.addEdge(e);
				}
			}
		}
		
		for(Variable var: net.variables){
			dag.addVariable(new VariableImpl(var));
		}
		
		// we have constructed the PT (DAG :-)), now copy the properties
		HashMap<Originator, org.processmining.processtree.Originator> org2org = new HashMap<Originator, org.processmining.processtree.Originator>();
		copyOriginators(net, dag, org2org);
		
		return dag;
	}
	
	private static void copyOriginators(CoSeNet net, ProcessTree tree, HashMap<Originator, org.processmining.processtree.Originator> org2org){
		for(Originator org: net.originators){
			if(org instanceof Role){
				org.processmining.processtree.impl.AbstractOriginator.Role role = new AbstractOriginator.Role(org.ID, org.Name);
				tree.addOriginator(role);
				org2org.put(org, role);
			}
			else if (org instanceof Group){
				org.processmining.processtree.impl.AbstractOriginator.Group group = new AbstractOriginator.Group(org.ID, org.Name);
				tree.addOriginator(group);
				org2org.put(org, group);
			}
			else if(org instanceof Resource){
				org.processmining.processtree.impl.AbstractOriginator.Resource resource = new AbstractOriginator.Resource(org.ID, org.Name);
				tree.addOriginator(resource);
				org2org.put(org, resource);
			}
		}
	}
}
