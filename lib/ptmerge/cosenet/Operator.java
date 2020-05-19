package org.processmining.plugins.ptmerge.cosenet;


public class Operator {
	
	//public String name;
	public OperatorType name;
	
	public Operator(){
		
	}
	
	public Operator(Operator o){
		name = o.name;
	}
	
	public Operator(String s){
		name = OperatorType.valueOf(s);
	}
	
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof Operator){
			if(((Operator)other).name == this.name){
				return true;
			}
		}
		return false;
	}
	
	public String toString(){
		return name.name();
	}
}
