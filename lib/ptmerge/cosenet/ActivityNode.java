package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;

public class ActivityNode extends Node {
	public ActivityNode(){
		
	}
	
	public ActivityNode(UUID id){
		this.ID = id;
	}
	
	public ActivityNode(ActivityNode act){
		this.ID = act.ID;
	}

	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof ActivityNode){
			if(((ActivityNode)other).ID.equals(this.ID)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
