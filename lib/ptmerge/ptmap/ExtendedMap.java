package org.processmining.plugins.ptmerge.ptmap;

import org.processmining.processtree.ProcessTree;

public class ExtendedMap extends Mapping{
	public void computeMap(ProcessTree pt1, ProcessTree pt2){
		TaskMap tm = new TaskMap();
		tm.computeMap(pt1, pt2);
		computeMap(pt1, pt2, tm);
	}
	
	public void computeMap(ProcessTree pt1, ProcessTree pt2, Mapping map){
		// we are starting from the activity map, hence we know which activities are equal
		mappingLR.putAll(map.mappingLR);
		mappingRL.putAll(map.mappingRL);
		// we have our initial equivalence class
		
		
	}
}
