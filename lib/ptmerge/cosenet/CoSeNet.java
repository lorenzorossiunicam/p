package org.processmining.plugins.ptmerge.cosenet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

import org.processmining.framework.util.Pair;
import org.processmining.plugins.properties.processmodel.Property;
import org.processmining.processtree.Variable;
import org.processmining.processtree.impl.VariableImpl;

public class CoSeNet {
	public String name;
	public UUID id;
	public HashSet<Activity> A = new HashSet<Activity>();
	public HashSet<Event> E = new HashSet<Event>();
	public HashSet<Operator> O = new HashSet<Operator>();
	
	public HashSet<ActivityNode> Na = new HashSet<ActivityNode>();
	public HashSet<EventNode> Ne = new HashSet<EventNode>();
	public HashSet<OperatorNode> No = new HashSet<OperatorNode>();
	public HashSet<VoidNode> Nv = new HashSet<VoidNode>();
	public HashSet<PlaceholderNode> Np = new HashSet<PlaceholderNode>();
	
	public Node root = null;
	public HashSet<Node> hidable = new HashSet<Node>();
	public HashSet<Node> blockable = new HashSet<Node>();
	
	public HashMap<ActivityNode, Activity> la = new HashMap<ActivityNode, Activity>();
	public HashMap<EventNode, Event> le = new HashMap<EventNode, Event>();
	// TODO Modify this please s.t. it is referring to an operator
	public HashMap<OperatorNode, Operator> loTemp = new HashMap<OperatorNode, Operator>();
	public HashMap<OperatorNode, OperatorType> lo = new HashMap<OperatorNode, OperatorType>();
	
	public HashMap<Node, Vector<Node>> c = new HashMap<Node, Vector<Node>>();
	
	public HashSet<Pair<PlaceholderNode, Node>> R = new HashSet<Pair<PlaceholderNode,Node>>();
	
	public HashMap<Node, Vector<Node>> Parent = new HashMap<Node, Vector<Node>>();	
	
	public boolean startEndAdded = false;
	public boolean parentsComputed = false;
	public boolean allParentsComputed = false;
	private UUID nextFreeID;
	
	//========================================================
	//=============== Properties of the CoSeNet ==============
	public HashSet<Originator> originators = new HashSet<Originator>();
	public HashMap<Property<?>, Object> PropertyModel = new HashMap<Property<?>, Object>();
	public HashMap<Property<?>, Object> PropertyModelDeduced = new HashMap<Property<?>, Object>();
	public HashMap<Property<?>, HashMap<Node, Object>> PropertyNode = new HashMap<Property<?>, HashMap<Node, Object>>();
	public HashMap<Property<?>, HashMap<Node, Object>> PropertyNodeDeduced = new HashMap<Property<?>, HashMap<Node, Object>>();
	
	// TODO: is the probability of an OperatorNode also a property? YES :-) 
	//public HashMap<OperatorNode, Vector<Double>> probabilities = new HashMap<OperatorNode, Vector<Double>>();
	
	public HashMap<Property<?>, HashMap<Role, Object>> PropertyRole = new HashMap<Property<?>, HashMap<Role,Object>>();
	public HashMap<Property<?>, HashMap<Role, Object>> PropertyRoleDeduced = new HashMap<Property<?>, HashMap<Role,Object>>();
	public HashMap<Node, List<Originator>> executor = new HashMap<Node, List<Originator>>();
	
	// TODO: should we also have to do something with data? YES... just for conversion purposes
	public HashSet<Variable> variables = new HashSet<Variable>();
	
	public CoSeNet(){
		name = null;
		
		A = new HashSet<Activity>();
		E = new HashSet<Event>();
		O = new HashSet<Operator>();
		Na = new HashSet<ActivityNode>();
		Ne = new HashSet<EventNode>();
		No = new HashSet<OperatorNode>();
		Nv = new HashSet<VoidNode>();
		Np = new HashSet<PlaceholderNode>();
		
		root = null;
		
		hidable = new HashSet<Node>();
		blockable = new HashSet<Node>();
		la = new HashMap<ActivityNode, Activity>();
		le = new HashMap<EventNode, Event>();
		lo = new HashMap<OperatorNode, OperatorType>();
		loTemp = new HashMap<OperatorNode, Operator>();
		c = new HashMap<Node, Vector<Node>>();
		R = new HashSet<Pair<PlaceholderNode, Node>>();
		//
		//
		//
		Parent = new HashMap<Node, Vector<Node>>();
		startEndAdded = false;
		parentsComputed = false;
		
		//nextFreeID = 0;
		
	}

	@SuppressWarnings("unchecked")
	public CoSeNet(CoSeNet net) throws Exception{
		id = net.id;
		name = net.name;
		HashMap<Activity, Activity> act2act = new HashMap<Activity, Activity>();
		for(Activity a: net.A){
			Activity a2 = new Activity(a);
			act2act.put(a, a2);
			A.add(a2);
		}
		HashMap<Event, Event> eve2eve = new HashMap<Event, Event>();
		for(Event e: net.E){
			Event e2 = new Event(e.name);
			eve2eve.put(e, e2);
			E.add(e2);
		}
		
		HashMap<Operator, Operator> op2op = new HashMap<Operator, Operator>();
		for(Operator o: net.O){
			Operator o2 = new Operator(o);
			op2op.put(o, o2);
			O.add(o2);
		}
		
		HashMap<Node, Node> node2node = new HashMap<Node, Node>();
		for(ActivityNode n: net.Na){
			ActivityNode n2 = new ActivityNode(n);
			Na.add(n2);
			la.put(n2, act2act.get(net.la.get(n)));
			node2node.put(n, n2);
		}
		
		for(EventNode n: net.Ne){
			EventNode n2 = new EventNode(n);
			Ne.add(n2);
			le.put(n2, eve2eve.get(net.le.get(n)));
			node2node.put(n, n2);
		}
		
		for(OperatorNode n: net.No){
			if(!net.Nv.contains(n)){
				OperatorNode n2 = new OperatorNode(n);
				No.add(n2);
				lo.put(n2, net.lo.get(n));
				loTemp.put(n2, net.loTemp.get(n));
				node2node.put(n, n2);
			}
		}
		
		for(VoidNode n: net.Nv){
			VoidNode n2 = new VoidNode(n);
			Nv.add(n2);
			No.add(n2);
			lo.put(n2, net.lo.get(n));
			loTemp.put(n2, net.loTemp.get(n));
			node2node.put(n, n2);
		}
		
		for(PlaceholderNode n: net.Np){
			PlaceholderNode n2 = new PlaceholderNode(n);
			Np.add(n2);
			node2node.put(n, n2);
		}
				
		for(Node n: net.No){
			Vector<Node> children = new Vector<Node>();
			for(Node child: net.c.get(n)){
				children.add(node2node.get(child));
			}
			c.put(node2node.get(n), children);
		}
		
		for(Node n: net.Ne){
			Vector<Node> children = new Vector<Node>();
			for(Node child: net.c.get(n)){
				children.add(node2node.get(child));
			}
			c.put(node2node.get(n), children);
		}
		
		root = node2node.get(net.root);
		
		for(Node n: net.blockable){
			blockable.add(node2node.get(n));
		}
		
		for(Node n: net.hidable){
			hidable.add(node2node.get(n));
		}
		
		for(Pair<PlaceholderNode, Node> repl: net.R){
			R.add(new Pair<PlaceholderNode, Node>((PlaceholderNode)node2node.get(repl.getFirst()), node2node.get(repl.getSecond())));
		}
		
		if(net.parentsComputed){
			for(Node n: net.Parent.keySet()){
				Parent.put(node2node.get(n), new Vector<Node>());
				for(Node parent: net.Parent.get(n)){
					Parent.get(n).add(node2node.get(parent));
				}
			}
		}
		startEndAdded = net.startEndAdded;
		parentsComputed = net.parentsComputed;
		nextFreeID = net.nextFreeID;
		
		HashMap<Originator, Originator> orig2orig = new HashMap<Originator, Originator>();
		for(Originator o: net.originators){
			if(o instanceof Role){
				Role r = new Role(o.ID, o.Name);
				originators.add(r);
				orig2orig.put(o, r);
			}
			else if(o instanceof Group){
				// TODO: finish me
				throw new NullPointerException();
			}
			else if(o instanceof Resource){
				// TODO: finish me
				throw new NullPointerException();
			}
			else{
				throw new NullPointerException();
			}
		}
		//PropertyModel = (HashMap<CoSeNetProperty, Object>) net.PropertyModel.clone();
		for(Property<?> p: net.PropertyModel.keySet()){
			PropertyModel.put(p, net.PropertyModel.get(p));
		}
		
		for(Property<?> p: net.PropertyModelDeduced.keySet()){
			PropertyModelDeduced.put(p, net.PropertyModelDeduced.get(p));
		}
		
		
		for(Property<?> p: net.PropertyNode.keySet()){
			HashMap<Node, Object> hm = new HashMap<Node, Object>();
			for(Node n: net.PropertyNode.get(p).keySet()){
				hm.put(node2node.get(n), net.PropertyNode.get(p).get(n));
			}
			PropertyNode.put(p, hm);
		}
		
		for(Property<?> p: net.PropertyNodeDeduced.keySet()){
			HashMap<Node, Object> hm = new HashMap<Node, Object>();
			for(Node n: net.PropertyNodeDeduced.get(p).keySet()){
				hm.put(node2node.get(n), net.PropertyNodeDeduced.get(p).get(n));
			}
			PropertyNodeDeduced.put(p, hm);
		}
		/*
		for(OperatorNode n: net.probabilities.keySet()){
			probabilities.put((OperatorNode)node2node.get(n), (Vector<Double>)net.probabilities.get(n).clone());
		}
		*/
		for(Property<?> p: net.PropertyRole.keySet()){
			HashMap<Role, Object> ro = new HashMap<Role, Object>();
			for(Role r: net.PropertyRole.get(p).keySet()){
				ro.put((Role)orig2orig.get(r), net.PropertyRole.get(p).get(r));
			}
			PropertyRole.put(p, ro);
		}
		
		for(Property<?> p: net.PropertyRoleDeduced.keySet()){
			HashMap<Role, Object> ro = new HashMap<Role, Object>();
			for(Role r: net.PropertyRoleDeduced.get(p).keySet()){
				ro.put((Role)orig2orig.get(r), net.PropertyRoleDeduced.get(p).get(r));
			}
			PropertyRoleDeduced.put(p, ro);
		}
		
		for(Node n: net.executor.keySet()){
			ArrayList<Originator> orgs = new ArrayList<Originator>();
			for(Originator org:net.executor.get(n)){
				orgs.add(orig2orig.get(org));
			}
			executor.put(node2node.get(n), orgs);
		}
		
		for(Variable var: net.variables){
			this.variables.add(new VariableImpl(var));
		}
		
		//computeParents();
		
	}
	
	public void addStartEnd(){
		if(!startEndAdded){
			Activity astart = new Activity("start");
			Activity aend = new Activity("end");
			ActivityNode nstart = new ActivityNode();
			ActivityNode nend = new ActivityNode();
			OperatorNode seq = new OperatorNode();
			A.add(astart);
			A.add(aend);
			Na.add(nstart);
			Na.add(nend);
			la.put(nstart, astart);
			la.put(nend, aend);
			Vector<Node> child = new Vector<Node>();
			child.add(nstart);
			child.add(root);
			child.add(nend);
			c.put(seq, child);
			No.add(seq);
			lo.put(seq, OperatorType.SEQ);
			loTemp.put(seq, getOperator(OperatorType.SEQ));
			root = seq;
			startEndAdded = true;
		}
	}
	
	public HashSet<Node> getNodes(){
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(Na);
		nodes.addAll(Ne);
		nodes.addAll(No);
		nodes.addAll(Np);
		return nodes;
	}
	
	public Node getNode(UUID ID){
		for(Node n: this.getNodes()){
			if(n.ID.equals(ID)){
				return n;
			}
		}
		throw new NullPointerException();
	}
	
	public boolean hasNode(UUID ID){
		for(Node n: this.getNodes()){
			if(n.ID.equals(ID)){
				return true;
			}
		}
		return false;
	}
	
	public Activity getAct(String name){
		for(Activity a: A){
			if(a.name.equalsIgnoreCase(name)){
				return a;
			}
		}
		Activity act = new Activity(name);
		A.add(act);
		return act;
	}
	
	public Event getEve(String name){
		for(Event e: E){
			if(e.name.equalsIgnoreCase(name)){
				return e;
			}
		}
		Event eve = new Event(name);
		E.add(eve);
		return eve;
	}
	
	public void giveIDs(){
		// can be void I guess
		/*
		for(ActivityNode n: Na){
			if(n.ID == -1){
				n.ID = getNextFreeID();
			}
		}
		for(EventNode n: Ne){
			if(n.ID == -1){
				n.ID = getNextFreeID();
			}
		}
		for(OperatorNode n: No){
			if(n.ID == -1){
				n.ID = getNextFreeID();
			}
		}
		for(PlaceholderNode n: Np){
			if(n.ID == -1){
				n.ID = getNextFreeID();
			}
		}
		*/
	}
	
	public UUID getNextFreeID(){
		//UUID id = UUID.randomUUID();
		//if(getNode(id) != null){
		//	System.out.print("Double UUID\n");
		//}
		return UUID.randomUUID();
	}
	
	public <E extends Node> void removeSubTree(Collection<E> colNode, boolean recurse) throws Exception{
		for(Node n: colNode){
			removeSubTree(n, recurse);
		}
	}
	
	public void removeSubTree(Node n, boolean recurse) throws Exception{
		HashSet<Pair<PlaceholderNode, Node>> toBeRemoved = new HashSet<Pair<PlaceholderNode,Node>>();
		for(Pair<PlaceholderNode, Node> r: this.R){
			if(r.getFirst().equals(n) || r.getSecond().equals(n)){
				toBeRemoved.add(r);
			}
		}
		this.R.removeAll(toBeRemoved);
		for(Node o: this.No){
			while(this.c.get(o).contains(n)){
				this.c.get(o).remove(n);
			}
		}
		// remove the node n, from all sets
		this.Na.remove(n);
		this.Ne.remove(n);
		this.No.remove(n);
		this.Nv.remove(n);
		this.Np.remove(n);
		
		this.la.remove(n);
		this.le.remove(n);
		this.lo.remove(n);
		
		this.c.remove(n);
		if(recurse){
			removeDisconnectedParts();
		}
	}
	
	public void substitute(Node element, Node substitute) throws Exception{
		// we have to substitute every occurrence of element with substitute
		// basically only in the child relations
		Set<Node> parents = this.c.keySet();
		for(Node parent: parents){
			for(Node child: this.c.get(parent)){
				if(child.equals(element)){
					this.c.get(parent).set(this.c.get(parent).indexOf(child), substitute);
				}
			}
		}
		if(root.equals(element)){
			root = substitute;
		}
	}
	
	public void removeDisconnectedParts() throws Exception{
		// so we start with the root
		if(root == null){
			throw new Exception("Root is null\n");
		}
		HashSet<Node> connected = new HashSet<Node>();
		Stack<Node> toBeProcessed = new Stack<Node>();
		connected.add(this.root);
		toBeProcessed.push(this.root);
		while(!toBeProcessed.empty()){
			Node workingOn = toBeProcessed.pop();
			connected.add(workingOn);
			if(workingOn instanceof OperatorNode){
				toBeProcessed.addAll(this.c.get(workingOn));
			}
			if(workingOn instanceof PlaceholderNode){
				for(Pair<PlaceholderNode, Node> r: this.R){
					if(r.getFirst().equals(workingOn)){
						toBeProcessed.add(r.getSecond());
					}
				}
			}
		}
		
		// so we have all the connected parts, remove the other nodes
		HashSet<Node> toBeRemoved = new HashSet<Node>();
		for(Node n: this.getNodes()){
			if(!connected.contains(n)){
				toBeRemoved.add(n);
			}
		}
		removeSubTree(toBeRemoved, false);
		
	}
	
	public HashSet<Pair<Node, Node>> getRelationChild(){
		HashSet<Pair<Node, Node>> rc = new HashSet<Pair<Node,Node>>();
		for(Node key: c.keySet()){
			for(Node value: c.get(key)){
				rc.add(new Pair<Node, Node>(key, value));
			}
		}
		return rc;
	}
	
	public HashSet<Node> getReplacementOptions(PlaceholderNode p){
		HashSet<Node> ret = new HashSet<Node>();
		for(Pair<PlaceholderNode, Node> r: R){
			if(r.getFirst().equals(p)){
				ret.add(r.getSecond());
			}
		}
		return ret;
	}
	
	public boolean smallerThan(Node n, Node n1){
		for(Node key: c.keySet()){
			if(c.get(key).contains(n) && c.get(key).contains(n1)){
				Vector<Node> children = c.get(key);
				if(children.indexOf(n) < children.indexOf(n1)){
					return true;
				}
			}
		}
		return false;
	}	
	
	public Operator getOperator(OperatorType ot){
		for(Operator o: O){
			if(o.name.equals(ot)){
				return o;
			}
		}
		Operator o = new Operator();
		o.name = ot;
		O.add(o);
		return o;
	}
	
	@Deprecated
	public String toString(){
		String ret = new String();
		for(Activity a: A){
			ret += a;
		}
		
		for(ActivityNode na: Na){
			ret += na + "\n";
		}
		
		for(OperatorNode no: No){
			ret += no + "\n";
		}
		
		for(PlaceholderNode np: Np){
			ret += np + "\n";
		}
		
		for(ActivityNode a: la.keySet()){
			ret += a + "->" + la.get(a);
		}
		
		for(OperatorNode o: lo.keySet()){
			ret += o + "->" + lo.get(o) + "\n";
		}
		for(Originator o: originators){
			ret += "Orig: " + o.ID + " " + o.Name + "\n";
		}
		
		return ret;
	}


	public int getSizeChildrenEdges() {
		int ret = 0;
		for(OperatorNode o: No){
			ret += c.get(o).size();
		}
		return ret;
	}
	
	/**
	 * Get all nodes of subgraph rooted at n (except n)
	 * @param n the root of the subgraph
	 * @return
	 */
	public HashSet<Node> getNodesInSubgraph(Node rootOfSubgraph){
		HashSet<Node> ret = new HashSet<Node>();
		if(No.contains(rootOfSubgraph) || Ne.contains(rootOfSubgraph)){
			// we know we actually have children
			Vector<Node> vector = c.get(rootOfSubgraph);
			for(Node n: vector){
				ret.addAll(getNodesInSubgraph(n));
				ret.add(n);
			}
		}
		return ret;
	}
	
	@Deprecated
	public Vector<Node> pre(OperatorNode n){
		Vector<Node> ret = new Vector<Node>();
		Stack<Node> toBeProcessed = new Stack<Node>();
		// initialize to be processed
		for(Node o: No){
			if(c.get(o).contains(n)){
				toBeProcessed.add(o);
				ret.add(o);
			}
		}
		
		while(!toBeProcessed.isEmpty()){
			Node f = toBeProcessed.pop();
			for(Node o: No){
				if(c.get(o).contains(f)){
					toBeProcessed.push(o);
					ret.add(o);
				}
			}
		}
				
		return ret;
	}
	
	@Deprecated
	public Vector<Node> suc(OperatorNode n){
		Vector<Node> ret = new Vector<Node>();
		Vector<Node> toBeProcessed = new Vector<Node>();
		// initialize to be processed
		for(Node n1: c.get(n)){
			toBeProcessed.add(n1);
			ret.add(n1);		
		}
		
		while(!toBeProcessed.isEmpty()){
			Node f = toBeProcessed.firstElement();
			toBeProcessed.remove(f);
			if(No.contains(f)){
				for(Node n1: c.get(f)){
					toBeProcessed.add(n1);
					ret.add(n1);
				}
			}
		}
		
		return ret;
	}
	
	/**
	 * This function normalises the DAG
	 * @throws Exception 
	 */
	public void normalise() throws Exception{
		// first check whether the Operator VOID has been included
		
		// make a vector of void nodes we have added
		Vector<VoidNode> voids = new Vector<VoidNode>();
		for(OperatorNode o: No){
			// we have an operator node
			// check whether this is not a void node
			if(!lo.get(o).equals(OperatorType.VOID) && !lo.get(o).equals(OperatorType.DEF)){
				// now we can iterate over the children of this non-void node
				// first we have to create the vector which will replace the current list of children
				Vector<Node> vector = new Vector<Node>();
				for(Node n: c.get(o)){
					// check if we do not have a void node
					if(Nv.contains(n)){
						// just add this voidnode to the list of void nodes
						vector.add(n);
					}
					else{
						// we do not have a void node so lets add it
						VoidNode voidNode = new VoidNode(getNextFreeID());
						voids.add(voidNode);
						lo.put(voidNode, OperatorType.VOID);
						loTemp.put(voidNode, getOperator(OperatorType.VOID));
						//lo.put(voidNode, getOperator(OperatorType.VOID));
						Vector<Node> v = new Vector<Node>();
						v.add(n);
						c.put(voidNode, v);
						vector.add(voidNode);
					}
				}
				c.put(o, vector);
			}
		}
		// now add all the newly create voidnode
		No.addAll(voids);
		Nv.addAll(voids);
		
		
		// now we have included all void nodes, lets now remove some things
		/*
		Vector<OperatorNode> toBeRemoved = new Vector<OperatorNode>();
		for(OperatorNode no: No){
			if(c.get(no).size() == 1 && loTemp.get(no).name != OperatorType.VOID && !lo.get(no).equals(OperatorType.VOID)){
				// not much use for this operator
				//toBeRemoved.add(no);
			}
		}
		//for(OperatorNode no: toBeRemoved){
			//removeNode(no);
		//}
		*/
	}
	/*
	private void removeNode(Node n){
		if(!allParentsComputed){
			computeAllParents();
		}
		if(n instanceof ActivityNode){
			Na.remove(n);
			la.remove(n);
			for(Node no: Parent.get(n)){
				Vector<Node> children = c.get(no);
				children.remove(n);
				c.put(no, children);
				if((children.size() == 1 && !loTemp.get(no).name.equals(OperatorType.VOID)) || children.size() == 0){
					removeNode(no);
				}
			}
		}
		if(n instanceof VoidNode){
			//just contract the nodes
		}
		if(n instanceof OperatorNode){
			Vector<Node> parents = Parent.get(n);
		}
		
	}
	*/
	
	/**
	 * Visualization function  
	 */
	public void splitActs(){
		// we are going to split every activity node which is shared
		HashSet<ActivityNode> newChilds = new HashSet<ActivityNode>();
		HashMap<ActivityNode, ActivityNode> mappedOldChilds = new HashMap<ActivityNode, ActivityNode>();
		HashMap<ActivityNode, Activity> newLa = new HashMap<ActivityNode, Activity>();
		for(OperatorNode n: No){
			Vector<Node> children = new Vector<Node>();
			for(Node child: c.get(n)){
				if(Na.contains(child)){
					// lets remake the child
					ActivityNode act = new ActivityNode();
					newLa.put(act, la.get(child));
					newChilds.add(act);
					children.add(act);
					mappedOldChilds.put(act, (ActivityNode)child);
					if(hidable.contains(child)){
						hidable.add(act);
					}
					if(blockable.contains(child)){
						blockable.add(act);
					}
				}
				else{
					children.add(child);
				}
			}
			// lets put the new children
			c.put(n, children);
		}
		
		HashSet<Pair<PlaceholderNode, Node>> newR = new HashSet<Pair<PlaceholderNode,Node>>(); 
		// we now also have to update the reference of the replacement options
		for(Pair<PlaceholderNode, Node> pair: R){
			if(Na.contains(pair.getSecond())){
				ActivityNode act = new ActivityNode();
				la.put(act, la.get(pair.getSecond()));
				newChilds.add(act);
				newR.add(new Pair<PlaceholderNode, Node>(pair.getFirst(), act));
			}
			else{
				newR.add(pair);
			}
		}
		
		// but only the nodes which have been substituted
		Na.removeAll(mappedOldChilds.values());
		Na.addAll(newChilds);
		// now empty the child nodes
		//Na = newChilds;
		for(ActivityNode a: mappedOldChilds.values()){
			la.remove(a);
		}
		la.putAll(newLa);
		R = newR;
	}
	
	/**
	 * Make a tree out of the DAG
	 * @throws Exception 
	 */
	public void makeTree() throws Exception{
		// lets make a tree out of it
		// Whenever we have a node with multiple parent we have to split this node
		computeAllParents();
		boolean restart = false;
		for(Node n: getNodes()){
			boolean firstOccurence = true;
			for(Node parent: Parent.get(n)){
				// we now have the parent
				for(Node child: c.get(parent)){
					if(child.equals(n) && firstOccurence){
						// do nothing
						firstOccurence = false;
					}
					else if(child.equals(n) && !firstOccurence){
						// we have to duplicate the subtree from here
						Node rootOfDuppl = duplicateSubGraph(n);
						c.get(parent).set(c.get(parent).indexOf(child), rootOfDuppl);
						restart = true;
					}
					if(restart){
						break;
					}
				}
				if(restart){
					break;
				}			
			}
			if(restart){
				break;
			}
		}
		if(restart){
			makeTree();
		}
	}
	
	public void duplicateTau(){
		for(Node n: getNodes()){
			if(n instanceof ActivityNode){
				if(la.get(n).name.equals("tau")){
					// we have found the tau
					computeAllParents();
					boolean foundFirst = false;
					for(Node parent: Parent.get(n)){
						// we are in VOID nodes
						for(int i = 0; i < c.get(parent).size(); i++){
							Node child = c.get(parent).elementAt(i);
							if(child.equals(n)){
								if(foundFirst){
									// skip we are keeping this one
									foundFirst = false;
								}
								else{
									ActivityNode tau = new ActivityNode();
									tau.ID = getNextFreeID();
									Na.add(tau);
									la.put(tau, getAct("tau"));
									c.get(parent).add(i, tau);
								}
							}
						}
						if(Parent.get(parent).size() > 1){
							throw new NullPointerException();
						}
					}
				}
			}
		}
	}
	
	public Node duplicateSubGraph(Node n) throws Exception{
		if(n instanceof OperatorNode){
			OperatorNode dup = new OperatorNode();
			dup.ID = getNextFreeID();
			No.add(dup);
			lo.put(dup, lo.get(n));
			loTemp.put(dup, loTemp.get(n));
			Vector<Node> children = new Vector<Node>();
			for(Node child: c.get(n)){
				children.add(duplicateSubGraph(child));
			}
			c.put(dup, children);
			return dup;
		}
		else if(n instanceof VoidNode){
			VoidNode dup = new VoidNode();
			dup.ID = getNextFreeID();
			No.add(dup);
			Nv.add(dup);
			lo.put(dup, lo.get(n));
			loTemp.put(dup, loTemp.get(n));
			Vector<Node> children = new Vector<Node>();
			for(Node child: c.get(n)){
				children.add(duplicateSubGraph(child));
			}
			c.put(dup, children);
			return dup;
		}
		else if(n instanceof PlaceholderNode){
			// TODO: implement me			
		}
		else if(n instanceof EventNode){
			EventNode dup = new EventNode();
			dup.ID = getNextFreeID();
			Ne.add(dup);
			le.put(dup, le.get(n));
			Vector<Node> children = new Vector<Node>();
			for(Node child: c.get(n)){
				children.add(duplicateSubGraph(child));
			}
			c.put(dup, children);
			return dup;
		}
		else if(n instanceof ActivityNode){
			// we have an activity node
			ActivityNode dup = new ActivityNode();
			dup.ID = getNextFreeID();
			Na.add(dup);
			la.put(dup, la.get(n));
			return dup;			
		}
		// do not get here!
		throw new Exception("Error undefined type\n");
	}
	
	public void computeParents(){
		this.Parent.clear();
		HashSet<Node> nodes = this.getNodes();
		for(Node n: nodes){
			Vector<Node> parents = new Vector<Node>();
			for(Node o: nodes){
				if(o instanceof OperatorNode){
					if(this.c.get(o).contains(n)){
						parents.add(o);
					}
				}
				if(o instanceof EventNode){
					if(this.c.get(o).contains(n)){
						parents.add(o);
					}
				}
			}
			this.Parent.put(n, parents);
		}
		parentsComputed = true;
		allParentsComputed = false;
	}
	
	public void computeAllParents(){
		this.Parent.clear();
		HashSet<Node> nodes = this.getNodes();
		for(Node n: nodes){
			Vector<Node> parents = new Vector<Node>();
			for(Node parent: nodes){
				if(parent instanceof OperatorNode){
					if(this.c.get(parent).contains(n)){
						parents.add(parent);
					}
				}
				if(parent instanceof EventNode){
					if(this.c.get(parent).contains(n)){
						parents.add(parent);
					}
				}
				if(parent instanceof PlaceholderNode){
					for(Pair<PlaceholderNode, Node> repl: R){
						if(n.equals(repl.getSecond())){
							parents.add(repl.getFirst());
						}
					}
				}
			}
			this.Parent.put(n, parents);
		}
		allParentsComputed = true;
		parentsComputed = false;
	}
	
	public boolean isTree(){
		// we just have to know that nobody has more than 1 parent
		if(!allParentsComputed){
			computeAllParents();
		}
		for(Node n: getNodes()){
			if(Parent.get(n).size() > 1){
				return false;
			}
		}
		return true;
	}
	
	public void isConsistent() throws Exception{
		// lets check the consistency
		for(OperatorNode n: No){
			// does it have a label?
			if(lo.get(n) == null){
				throw new Exception();
			}
			// does it have children?
			if(c.get(n) == null){
				throw new Exception();
			}
			if(c.get(n).size() <= 0){
				throw new Exception();
			}
		}
		if(root == null){
			throw new Exception();
		}
	}
	
	public void fillLoTemp(){
		for(OperatorNode no: No){
			if(loTemp.get(no) == null){
				loTemp.put(no, getOperator(lo.get(no)));
			}
		}
	}
	
	public Object getPropertyCoSeNet(Property<?> p){
		if(p == null){
			throw new NullPointerException();
		}
		else if(PropertyModel == null){
			return null;
		}
		else{
			Object value = PropertyModel.get(p);
			if(value == null){
				return null;
			}
			else{
				return value;
			}
		}
	}
	
	public Object getPropertyCoSeNet(Property<?> p, Node n){
		HashMap<Node, Object> properties = PropertyNode.get(p);
		if(p == null){
			throw new NullPointerException();
		}
		else if(properties == null){
			return null;
		}
		else{
			Object value = properties.get(n);
			if(value == null){
				return null;
			}
			else{
				return value;
			}
		}
	}
		
	public Object getPropertyCoSeNet(Property<?> p, Role r){
		HashMap<Role, Object> properties = PropertyRole.get(p);
		if(p == null){
			throw new NullPointerException();
		}
		else if(properties == null){
			return null;
		}
		else{
			Object value = properties.get(r);
			if(value == null){
				return null;
			}
			else{
				return value;
			}
		}
	}
		
	public Property<?> getPropertyCoSeNet(Class<? extends Property<?>> clazz, Node n, Originator o) throws InstantiationException, IllegalAccessException{
		if(PropertyNode == null){
			System.out.print("Oeps1\n");
		}
		if(PropertyNode.keySet() == null){
			System.out.print("Oeps2\n");
		}
		for(Property<?> p: PropertyNode.keySet()){
			if(clazz.isInstance(p) && n != null){
				return p;
			}
		}
		for(Property<?> p: PropertyModel.keySet()){
			if(clazz.isInstance(p) && n == null && o == null){
				return p;
			}
		}
		for(Property<?> p: PropertyRole.keySet()){
			if(clazz.isInstance(p) && o != null){
				return p;
			}
		}
		return clazz.newInstance();
	}
	// TODO: make an explicit initialiser for this if a property has not been set
	public Object getPropertyDeduced(Property<?> p, Node n){
		HashMap<Node, Object> properties = PropertyNodeDeduced.get(p);
		if(p == null){
			throw new NullPointerException();
		}
		else if(properties == null){
			return null;
		}
		else{
			Object value = properties.get(n);
			if(value == null){
				return null;
			}
			else{
				return value;
			}
		}
	}
	
	public Object getPropertyDeduced(Property<?> p, Role r){
		HashMap<Role, Object> properties = PropertyRoleDeduced.get(p);
		if(p == null){
			throw new NullPointerException();
		}
		else if(properties == null){
			return null;
		}
		else{
			Object value = properties.get(r);
			if(value == null){
				return null;
			}
			else{
				return value;
			}
		}
	}
	
	public Object getPropertyDeduced(Property<?> p){
		Object property = PropertyModelDeduced.get(p);
		if(p == null){
			throw new NullPointerException();
		}
		else{
			return property;
		}
	}
	
	public void setPropertyDeduced(Property<?> p, Node n, Object value){
		HashMap<Node, Object> properties = PropertyNodeDeduced.get(p);
		if(p == null){
		}
		else if(properties == null){
			PropertyNodeDeduced.put(p, new HashMap<Node, Object>());
			// TODO: not entirely nice... :-)
			setPropertyDeduced(p, n, value);
		}
		else{
			properties.put(n, value);
		}
	}
	
	public void setPropertyCoSeNet(Property<?> p, Node n, Object value){
		HashMap<Node, Object> properties = PropertyNode.get(p);
		if(p == null){
		}
		else if(properties == null){
			PropertyNode.put(p, new HashMap<Node, Object>());
			// TODO: not entirely nice... :-)
			setPropertyCoSeNet(p, n, value);
		}
		else{
			properties.put(n, value);
		}
	}
	
	public void setPropertyDeduced(Property<?> p, Role r, Object value){
		HashMap<Role, Object> properties = PropertyRoleDeduced.get(p);
		if(p == null){
		}
		else if(properties == null){
			PropertyRoleDeduced.put(p, new HashMap<Role, Object>());
			// TODO: not entirely nice... :-)
			setPropertyDeduced(p, r, value);
		}
		else{
			properties.put(r, value);
		}
	}
	
	public void setPropertyCoSeNet(Property<?> p, Role r, Object value){
		HashMap<Role, Object> properties = PropertyRole.get(p);
		if(p == null){
		}
		else if(properties == null){
			PropertyRole.put(p, new HashMap<Role, Object>());
			// TODO: not entirely nice... :-)
			setPropertyCoSeNet(p, r, value);
		}
		else{
			properties.put(r, value);
		}
	}
	
	public void setPropertyDeduced(Property<?> p, Object value){
		Object property = PropertyModelDeduced.get(p);
		if(p == null){
			
		}
		else if(property == null){
			PropertyModelDeduced.put(p, value);
		}
		else{
			property = value;
		}
	}
	
	public void setPropertyCoSeNet(Property<?> p, Object value){
		Object property = PropertyModel.get(p);
		if(p == null){
		}
		else if(property == null){
			PropertyModel.put(p, value);
		}
		else{
			property = value;
		}
	}
	
	public Property<?> getPropertyDeduced(Class<? extends Property<?>> clazz, Node n, Originator o) throws InstantiationException, IllegalAccessException{
		for(Property<?> p: PropertyNodeDeduced.keySet()){
			if(clazz.isInstance(p) && n != null){
				return p;
			}
		}
		for(Property<?> p: PropertyModelDeduced.keySet()){
			if(clazz.isInstance(p) && n == null && o == null){
				return p;
			}
		}
		for(Property<?> p: PropertyRoleDeduced.keySet()){
			if(clazz.isInstance(p) && o != null){
				return p;
			}
		}
		return clazz.newInstance();
	}
	
	/*
	@SuppressWarnings("unchecked")
	public Double getProbability(Node parent, Node child) throws InstantiationException, IllegalAccessException{
		if(parent instanceof OperatorNode){
			if(c.get(parent).contains(child)){
				ArrayList<Double> probabilities = (ArrayList<Double>) this.getPropertyCoSeNet(this.getPropertyCoSeNet(Probability.class, parent, null), parent);
				if(probabilities != null){
					if(probabilities.size() == c.get(parent).size()){
						return probabilities.get(c.get(parent).indexOf(child));
					}
				}
			}
		}
		return 1d;
	}
	*/
	/*
	public Double getProbability(Node parent, Node child){
		if(parent instanceof OperatorNode){
			if(c.get(parent).contains(child)){
				if(probabilities.get(parent) != null){
					if(probabilities.get(parent).get(c.get(parent).indexOf(child)) != null){
						return probabilities.get(parent).get(c.get(parent).indexOf(child));
					}
				}
			}
		}
		return 1d;
	}
	*/
	public Role getRole(UUID ID){
		for(Originator o: originators){
			if(o.ID.equals(ID)){
				return (Role) o;
			}
		}
		throw new NullPointerException();
	}
	
	public void checkRootForVoid(){
		// also check if there isn't a void as root, we can remove it
		while(root instanceof VoidNode){
			Node oldRoot = root;
			root = c.get(oldRoot).firstElement();
			No.remove(oldRoot);
			Nv.remove(oldRoot);
			c.remove(oldRoot);
			lo.remove(oldRoot);
			loTemp.remove(oldRoot);
			// TODO: we also need to remove the label if necessarily
		}
	}
}
