package org.processmining.plugins.ptmerge.cosenet;

import java.util.UUID;

public abstract class Originator {
	public UUID ID;
	public String Name;
	
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof Originator){
			if(((Originator)other).ID == this.ID){
				return true;
			}
		}
		return false;
	}
	
	public int hashCode(){
		return this.ID.hashCode();
	}
}
