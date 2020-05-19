package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;


public abstract class Node {
	public UUID ID = UUID.randomUUID();
	
	public Node(){
	
	}

	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof Node){
			if(((Node)other).ID == this.ID){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
