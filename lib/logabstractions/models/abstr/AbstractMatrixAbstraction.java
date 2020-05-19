package org.processmining.logabstractions.models.abstr;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.MatrixAbstraction;

public abstract class AbstractMatrixAbstraction<E> extends AbstractAbstraction<E> implements MatrixAbstraction<E> {

	private Collection<Pair<E, E>> geq = null;
	private Collection<Pair<E, E>> less = null;
	private final double[][] matrix;
	private final double threshold;

	public AbstractMatrixAbstraction(final E[] eventClasses, final double[][] matrix, final double threshold) {
		super(eventClasses);
		this.matrix = matrix;
		this.threshold = threshold;
	}

	public Collection<E> getAllGeqForColumn(E t) {
		return getAllGeqForColumn(getIndex(t));
	}

	public Collection<E> getAllGeqForColumn(int c) {
		Collection<E> result = new HashSet<>();
		for (int r = 0; r < getMatrix().length; r++) {
			if (holds(r, c))
				result.add(getEventClass(r));
		}
		return result;
	}

	public Collection<E> getAllGeqForColumns(Collection<E> coll) {
		int[] indices = new int[coll.size()];
		int i = 0;
		for (E e : coll) {
			indices[i] = getIndex(e);
			i++;
		}
		return getAllGeqForColumns(indices);
	}

	public Collection<E> getAllGeqForColumns(int[] cols) {
		Collection<E> result = new HashSet<>();
		for (int c : cols) {
			for (int r = 0; r < getMatrix().length; r++) {
				if (holds(r, c))
					result.add(getEventClass(r));
			}
		}
		return result;
	}

	public Collection<E> getAllGeqForRow(E t) {
		return getAllGeqForRow(getIndex(t));
	}

	public Collection<E> getAllGeqForRow(int r) {
		Collection<E> result = new HashSet<>();
		for (int c = 0; c < getMatrix().length; c++) {
			if (getValue(r, c) >= getThreshold())
				result.add(getEventClass(c));

		}
		return result;
	}

	public Collection<E> getAllGeqForRows(Collection<E> rows) {
		int[] indices = new int[rows.size()];
		int i = 0;
		for (E e : rows) {
			indices[i] = getIndex(e);
			i++;
		}
		return getAllGeqForColumns(indices);
	}

	public Collection<E> getAllGeqForRows(int[] rows) {
		Collection<E> result = new HashSet<>();
		for (int r : rows) {
			for (int c = 0; c < getMatrix().length; c++) {
				if (getValue(r, c) >= getThreshold()) {
					result.add(getEventClass(c));
				}
			}
		}
		return result;
	}

	public Collection<Pair<E, E>> getAllGEQThreshold() {
		if (geq == null) {
			geq = new HashSet<Pair<E, E>>();
			for (int row = 0; row < matrix.length; row++) {
				for (int col = 0; col < matrix[row].length; col++) {
					if (matrix[row][col] >= threshold) {
						geq.add(new Pair<E, E>(getEventClass(row), getEventClass(col)));
					}
				}
			}

		}
		return geq;
	}

	public int[] getAllIndicesGeqForColumn(int column) {
		int[] res = new int[0];
		for (int r = 0; r < matrix.length; r++) {
			if (getValue(r, column) >= getThreshold()) {
				res = Arrays.copyOf(res, res.length + 1);
				res[res.length - 1] = r;
			}
		}
		return res;
	}

	public int[] getAllIndicesGeqForRow(int row) {
		int[] res = new int[0];
		double[] values = getRow(row);
		for (int i = 0; i < values.length; i++) {
			if (values[i] >= getThreshold()) {
				res = Arrays.copyOf(res, res.length + 1);
				res[res.length - 1] = i;
			}
		}
		return res;
	}

	public Collection<Pair<E, E>> getAllLessThanThreshold() {
		if (less == null) {
			less = new HashSet<Pair<E, E>>();
			for (int row = 0; row < matrix.length; row++) {
				for (int col = 0; col < matrix[row].length; col++) {
					if (matrix[row][col] < threshold) {
						less.add(new Pair<E, E>(getEventClass(row), getEventClass(col)));
					}
				}
			}
		}
		return less;
	}

	public strictfp double[][] getMatrix() {
		return matrix;
	}

	public int getNumberOfColumns() {
		return getNumberOfRows() == 0 ? 0 : matrix[0].length;
	}

	public int getNumberOfRows() {
		return matrix.length;
	}

	public double[] getRow(int r) {
		return matrix[r];
	}

	public strictfp double getThreshold() {
		return threshold;
	}

	public strictfp double getValue(final E row, final E col) {
		return matrix[getIndex(row)][getIndex(col)];
	}

	public strictfp double getValue(final int row, final int col) {
		return matrix[row][col];
	}

	public boolean holds(E r, E c) {
		return getValue(r, c) >= threshold;
	}

	public boolean holds(int r, int c) {
		return matrix[r][c] >= threshold;
	}
}
