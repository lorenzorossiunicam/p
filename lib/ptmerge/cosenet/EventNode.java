package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;

public class EventNode extends Node {

	public EventNode(){
		
	}
	
	public EventNode(UUID id) {
		this.ID = id;
	}
	
	public EventNode(EventNode eve) {
		this.ID = eve.ID;
	}
	
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof EventNode){
			if(((EventNode)other).ID.equals(this.ID)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
