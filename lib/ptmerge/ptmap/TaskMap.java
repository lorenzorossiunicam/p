package org.processmining.plugins.ptmerge.ptmap;

import java.util.HashMap;

import org.processmining.processtree.Node;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.Task;

public class TaskMap extends Mapping {	
	public void computeMap(ProcessTree pt1, ProcessTree pt2){
		// trivial, just combine all the tasks with the same label
		mappingLR = new HashMap<Node, Node>();
		mappingRL = new HashMap<Node, Node>();
		for(Node n: pt1.getNodes()){
			for(Node np: pt2.getNodes()){
				if(n instanceof Task && np instanceof Task){
					if(n.getName().equalsIgnoreCase(np.getName())){
						mappingLR.put(n, np);
						mappingRL.put(np, n);
					}
				}
			}
		}
	}
	
}
