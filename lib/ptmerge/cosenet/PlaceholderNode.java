package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;

public class PlaceholderNode extends Node {
	public PlaceholderNode(){
		
	}
	
	public PlaceholderNode(UUID id){
		this.ID = id;
	}

	public PlaceholderNode(PlaceholderNode pn) {
		this.ID = pn.ID;
	}

	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof PlaceholderNode){
			if(((PlaceholderNode)other).ID.equals(this.ID)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
