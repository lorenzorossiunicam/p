package org.processmining.plugins.ptmerge.cosenet;

public class Activity {

	public String name;
	
	public Activity(){
		
	}
	
	public Activity(String n){
		name = n;
	}
	
	public Activity(Activity act){
		name = act.name;
	}
	
	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		if(other instanceof Activity){
			return name.equalsIgnoreCase(((Activity)other).name);
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
