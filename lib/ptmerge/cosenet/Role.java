package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;


public class Role extends Originator {
	
	public Role(){
		
	}
	
	public Role(UUID id, String n){
		ID = id;
		Name = n;
	}
	
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof Role){
			if(((Role)other).ID == this.ID){
				return true;
			}
		}
		return false;
	}
}
