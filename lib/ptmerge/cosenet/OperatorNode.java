package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;


public class OperatorNode extends Node {
	public OperatorNode(){
		
	}
	
	public OperatorNode(UUID id){
		this.ID = id;
	}
	
	public OperatorNode(OperatorNode op){
		this.ID = op.ID;
	}

	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof OperatorNode){
			if(((OperatorNode)other).ID.equals(this.ID)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
