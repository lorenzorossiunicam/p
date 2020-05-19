package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;

public class VoidNode extends OperatorNode {
	public VoidNode(){
		
	}
	
	public VoidNode(UUID id){
		this.ID = id;
	}
	
	public VoidNode(VoidNode vn){
		this.ID = vn.ID;
	}

	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof VoidNode){
			if(((VoidNode)other).ID.equals(this.ID)){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}

}
