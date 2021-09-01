package it.unicam.pros.purple.util;

public class Couple<E, V> {
	
	private E e;
	private V v;
	
	public Couple(E e, V v)
	{
		this.e = e;
		this.v = v;
	}

	public E getE() {
		return e;
	}

	public void setE(E e) {
		this.e = e;
	}

	public V getV() {
		return v;
	}

	public void setV(V v) {
		this.v = v;
	}

	@Override
	public String toString(){
		return "["+e.toString()+", "+v.toString()+"]";
	}
}
