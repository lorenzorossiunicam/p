package org.processmining.logabstractions.models.abstr;

import java.util.Collection;
import java.util.HashSet;

import org.processmining.logabstractions.models.ColumnAbstraction;

public abstract class AbstractColumnAbstraction<E> extends AbstractAbstraction<E> implements ColumnAbstraction<E> {

	private final double[] column;
	private final double threshold;
	private Collection<E> geq = null;
	private Collection<E> less = null;

	public AbstractColumnAbstraction(final E[] eventClasses, final double[] column, final double threshold) {
		super(eventClasses);
		this.column = column;
		this.threshold = threshold;
	}

	public strictfp double getValue(E e) {
		return column[getIndex(e)];
	}

	public strictfp double getValue(int index) {
		return column[index];
	}

	public strictfp double getThreshold() {
		return threshold;
	}

	public double[] getColumn() {
		return column;
	}

	public Collection<E> getAllGEQThreshold() {
		if (geq == null) {
			geq = new HashSet<E>();
			for (int i = 0; i < column.length; i++) {
				if (column[i] >= threshold) {
					geq.add(getEventClass(i));
				}
			}
		}
		return geq;
	}

	public Collection<E> getAllLessThanThreshold() {
		if (less == null) {
			less = new HashSet<E>();
			for (int i = 0; i < column.length; i++) {
				if (column[i] >= threshold) {
					less.add(getEventClass(i));
				}
			}
		}
		return less;
	}

	public boolean holds(int index) {
		return column[index] >= threshold;
	}

	public boolean holds(E index) {
		return getValue(index) >= threshold;
	}

}
