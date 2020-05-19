package org.processmining.logabstractions.models;

import java.util.Collection;

import org.processmining.framework.util.Pair;

/**
 * Abstraction representable in matrix form.
 * The premises of the interface are:
 * 1. indices of the eventclass array match the matrix.
 * @author svzelst
 *
 * @param <E>
 */
public interface MatrixAbstraction<E> extends Abstraction<E> {

	Collection<E> getAllGeqForColumn(E e);

	Collection<E> getAllGeqForColumn(int col);

	Collection<E> getAllGeqForColumns(Collection<E> e);

	Collection<E> getAllGeqForColumns(int[] cols);

	Collection<E> getAllGeqForRow(E e);

	Collection<E> getAllGeqForRow(int row);

	Collection<E> getAllGeqForRows(Collection<E> e);

	Collection<E> getAllGeqForRows(int[] rows);

	Collection<Pair<E, E>> getAllGEQThreshold();

	int[] getAllIndicesGeqForRow(int row);
	
	int[] getAllIndicesGeqForColumn(int column);

	Collection<Pair<E, E>> getAllLessThanThreshold();

	double[][] getMatrix();

	int getNumberOfColumns();

	int getNumberOfRows();

	double[] getRow(int row);

	double getThreshold();

	double getValue(E row, E col);

	double getValue(int row, int col);

	boolean holds(E r, E c);
	
	boolean holds(int r, int c);

}
