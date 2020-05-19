package org.processmining.plugins.ptmerge.cosenet;

public class Event {
	
	public String name;
	
	public Event(){
		
	}
	
	public Event(String n){
		name = n;
	}
	
	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		if(other instanceof Event){
			return name.equalsIgnoreCase(((Event)other).name);
		}
		return false;
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public String toString(){
		return name;
	}
}
