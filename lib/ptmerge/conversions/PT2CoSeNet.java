package org.processmining.plugins.ptmerge.conversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.ptmerge.cosenet.Activity;
import org.processmining.plugins.ptmerge.cosenet.ActivityNode;
import org.processmining.plugins.ptmerge.cosenet.CoSeNet;
import org.processmining.plugins.ptmerge.cosenet.Event;
import org.processmining.plugins.ptmerge.cosenet.EventNode;
import org.processmining.plugins.ptmerge.cosenet.Node;
import org.processmining.plugins.ptmerge.cosenet.Operator;
import org.processmining.plugins.ptmerge.cosenet.OperatorNode;
import org.processmining.plugins.ptmerge.cosenet.OperatorType;
import org.processmining.plugins.ptmerge.cosenet.Originator;
import org.processmining.plugins.ptmerge.cosenet.PlaceholderNode;
import org.processmining.plugins.ptmerge.cosenet.Role;
import org.processmining.plugins.ptmerge.cosenet.VoidNode;
import org.processmining.processtree.Block;
import org.processmining.processtree.Block.And;
import org.processmining.processtree.Block.Def;
import org.processmining.processtree.Block.DefLoop;
import org.processmining.processtree.Block.Or;
import org.processmining.processtree.Block.PlaceHolder;
import org.processmining.processtree.Block.Xor;
import org.processmining.processtree.Block.XorLoop;
import org.processmining.processtree.Edge;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;
import org.processmining.processtree.Variable;
import org.processmining.processtree.impl.AbstractBlock.Seq;
import org.processmining.processtree.impl.AbstractTask.Manual;
import org.processmining.processtree.impl.VariableImpl;

public class PT2CoSeNet {
	public static CoSeNet convert(PluginContext context, ProcessTree dag){
		return convert(dag);
	}
	
	public static CoSeNet convert(ProcessTree dag){
		CoSeNet net = new CoSeNet();
		net.id = dag.getID();
		net.name = dag.getName();
		// lets fill the CoSeNet
		HashMap<Edge, Node> voidNodes = new HashMap<Edge, Node>();
		// not every edge is a void node
		for(Edge edge: dag.getEdges()){
			// every edge is a void node
			VoidNode vn = new VoidNode(edge.getID());
			net.Nv.add(vn);
			net.No.add(vn);
			Operator op = net.getOperator(OperatorType.VOID);
			net.loTemp.put(vn, op);
			voidNodes.put(edge, vn);
			if(edge.isHideable()){
				net.hidable.add(vn);
			}
			if(edge.isBlockable()){
				net.blockable.add(vn);
			}
		}
		
		for(org.processmining.processtree.Node node: dag.getNodes()){
			if(node instanceof Task){
				ActivityNode an = new ActivityNode(node.getID());
				net.Na.add(an);
				if(node instanceof Manual){
					Activity act = net.getAct(node.getName());
					net.la.put(an, act);
				}
				else{
					// we have an automatic task
					Activity act = net.getAct("tau");
					net.la.put(an, act);
				}
			}
			else if(node instanceof Block){
				if(node instanceof org.processmining.processtree.Event){
					// lets create a new event
					EventNode en = new EventNode(node.getID());
					Event eve = net.getEve(node.getName());
					net.Ne.add(en);
					net.le.put(en, eve);
				}
				else{
					// we have an operator, or placeholder, have fun :-)
					if(node instanceof Seq){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.SEQ);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof And){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.AND);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof Xor){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.XOR);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof Def){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.DEF);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof Or){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.OR);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof XorLoop){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.LOOPXOR);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof DefLoop){
						OperatorNode on = new OperatorNode(node.getID());
						Operator op = net.getOperator(OperatorType.LOOPDEF);
						net.No.add(on);
						net.loTemp.put(on, op);
					}
					else if(node instanceof PlaceHolder){
						PlaceholderNode pn = new PlaceholderNode(node.getID());
						net.Np.add(pn);
					}
					else{
						throw new NullPointerException();
					}
				}
			}
			else{
				throw new NullPointerException();
			}
		}
		// we have added all the nodes, set the root
		net.root = net.getNode(dag.getRoot().getID());
		// set the children and replacement options
		for(org.processmining.processtree.Node node: dag.getNodes()){
			if(node instanceof Block){
				if(!(node instanceof PlaceHolder)){
					Vector<Node> children = new Vector<Node>();
					for(Edge edge: ((Block) node).getOutgoingEdges()){
						children.add(voidNodes.get(edge));
						Vector<Node> childVoid = new Vector<Node>();
						childVoid.add(net.getNode(edge.getTarget().getID()));
						net.c.put(voidNodes.get(edge), childVoid);
					}
					net.c.put(net.getNode(node.getID()), children);
				}
				else{
					for(Edge edge: ((Block) node).getOutgoingEdges()){
						net.R.add(new Pair<PlaceholderNode, Node>((PlaceholderNode)net.getNode(node.getID()), voidNodes.get(edge)));
						Vector<Node> childVoid = new Vector<Node>();
						childVoid.add(net.getNode(edge.getTarget().getID()));
						net.c.put(voidNodes.get(edge), childVoid);
					}
				}
			}
		}
		
		for(Variable var: dag.getVariables()){
			net.variables.add(new VariableImpl(var));
		}
		// we have the control-flow, add the other bits and pieces
		// first do the roles
		copyOriginators(dag, net);
		
		return net;
	}
	
	private static void copyOriginators(ProcessTree tree, CoSeNet net){
		HashMap<org.processmining.processtree.Originator, Originator> org2org = new HashMap<org.processmining.processtree.Originator, Originator>();
		for(org.processmining.processtree.Originator org: tree.getOriginators()){
			if(org instanceof org.processmining.processtree.impl.AbstractOriginator.Role){
				Role r = new Role(org.getID(), org.getName());
				org2org.put(org, r);
				net.originators.add(r);
			}
			else{
				throw new NullPointerException("Not yet implemented\n");
			}
		}
		for(org.processmining.processtree.Node n: tree.getNodes()){
			if(n instanceof Manual){
				Collection<org.processmining.processtree.Originator> orgs = ((Manual)n).getOriginators();
				ArrayList<Originator> CoSeOrg = new ArrayList<Originator>();
				for(org.processmining.processtree.Originator org: orgs){
					CoSeOrg.add(org2org.get(org));
				}
				net.executor.put(net.getNode(n.getID()), CoSeOrg);
			}
		}
	}

}
