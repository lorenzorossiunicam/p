/**
 * 
 */
package org.processmining.plugins.aggregatedactivitiesperformancediagram;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.framework.util.Pair;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
public class AAPDStatisticTableGenerator {
	public static JTable generateStatsTable(int statType,
			Map<AAPDElement, Map<Integer, Pair<BigInteger[], Integer>>> map, GlobalSettingsData globalSettingsData,
			AAPDElement aapdFocusElements, AAPD aapd) {
		// utility
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);

		BigInteger divider = BigInteger.valueOf(globalSettingsData.getDividerValue());

		//form vector of array of string
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Element");
		columnNames.add("Min.");
		columnNames.add("Max.");
		columnNames.add("Average");
		columnNames.add("Std. Deviation");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector which collects all

		// special treatment for intersection time
		if (statType == AAPD.BAR_INTERSECTION_TIME) {
			for (AAPDElement currElement : map.keySet()) {
				Pair<BigInteger[], Integer> stats = map.get(currElement).get(statType);

				if (stats.getSecond() > 0) {
					Vector<Object> rowItem = new Vector<Object>();
					rowItem.add(currElement.getLabel()); // node

					if ((stats.getFirst()[AAPD.MIN_VALUE].compareTo(BigInteger.valueOf(Long.MAX_VALUE)) == 0)
							|| (stats.getSecond() == 0)) {
						rowItem.add("NaN"); // min
						rowItem.add("NaN"); // max
						rowItem.add("NaN"); // average
						rowItem.add("NaN"); // std deviation
					} else {
						// to deal with intersection time calculation
						int occurenceCurrentElement = aapd.getRelationTime().get(aapdFocusElements).get(currElement)
								.get(AAPD.BAR_START_TIME).getSecond();
						int occurenceFocusElement = aapd.getRelationTime().get(aapdFocusElements)
								.get(aapdFocusElements).get(AAPD.BAR_START_TIME).getSecond();

						BigInteger[] minimum;
						if ((occurenceCurrentElement * occurenceFocusElement) > stats.getSecond()) {
							minimum = new BigInteger[] { BigInteger.ZERO, BigInteger.ZERO };
						} else {
							minimum = stats.getFirst()[AAPD.MIN_VALUE].divideAndRemainder(divider);
						}
						rowItem.add(nf.format(minimum[0].doubleValue()
								+ (minimum[1].doubleValue() / divider.doubleValue()))); // min

						BigInteger[] maximum = stats.getFirst()[AAPD.MAX_VALUE].divideAndRemainder(divider);
						rowItem.add(nf.format(maximum[0].doubleValue()
								+ (minimum[1].doubleValue() / divider.doubleValue()))); // max

						//BigInteger newDivider = divider.multiply(BigInteger.valueOf(stats.getSecond()));
						BigInteger newDivider = divider.multiply(BigInteger.valueOf(occurenceCurrentElement
								* occurenceFocusElement));
						BigInteger[] average = stats.getFirst()[AAPD.SUM].divideAndRemainder(newDivider);
						rowItem.add(nf.format(average[0].doubleValue()
								+ (average[1].doubleValue() / newDivider.doubleValue()))); // average

						// calculate stdDeviation
						BigInteger sumSquaredBigInt = stats.getFirst()[AAPD.SUM].multiply(stats.getFirst()[AAPD.SUM]);
						//BigInteger[] sumSquaredDivided = sumSquaredBigInt.divideAndRemainder(BigInteger.valueOf(stats.getSecond()));
						BigInteger[] sumSquaredDivided = sumSquaredBigInt.divideAndRemainder(BigInteger
								.valueOf(occurenceCurrentElement * occurenceFocusElement));
						double sumSquaredDividedDouble = sumSquaredDivided[0].doubleValue()
								+ (sumSquaredDivided[1].doubleValue() / stats.getSecond());

						double midVariance = stats.getFirst()[AAPD.SUM_SQUARE].doubleValue() - sumSquaredDividedDouble;
						//double variance = midVariance / stats.getSecond();
						double variance = midVariance / (occurenceCurrentElement * occurenceFocusElement);
						double stdDev = Math.sqrt(variance);

						rowItem.add(nf.format(stdDev / divider.doubleValue())); // standard deviation
					}
					data.add(rowItem);
				}
			}
		} else { // not intersection between bars
			for (AAPDElement focusElement : map.keySet()) {
				Pair<BigInteger[], Integer> stats = map.get(focusElement).get(statType);

				if (stats.getSecond() > 0) {
					Vector<Object> rowItem = new Vector<Object>();
					rowItem.add(focusElement.getLabel()); // node

					BigInteger[] minimum = stats.getFirst()[AAPD.MIN_VALUE].divideAndRemainder(divider);
					rowItem.add(nf
							.format(minimum[0].doubleValue() + (minimum[1].doubleValue() / divider.doubleValue()))); // min

					BigInteger[] maximum = stats.getFirst()[AAPD.MAX_VALUE].divideAndRemainder(divider);
					rowItem.add(nf
							.format(maximum[0].doubleValue() + (minimum[1].doubleValue() / divider.doubleValue()))); // max

					BigInteger newDivider = divider.multiply(BigInteger.valueOf(stats.getSecond()));

					BigInteger[] average = stats.getFirst()[AAPD.SUM].divideAndRemainder(newDivider);
					rowItem.add(nf.format(average[0].doubleValue()
							+ (average[1].doubleValue() / newDivider.doubleValue()))); // average

					// calculate stdDeviation
					BigInteger sumSquaredBigInt = stats.getFirst()[AAPD.SUM].multiply(stats.getFirst()[AAPD.SUM]);
					BigInteger[] sumSquaredDivided = sumSquaredBigInt.divideAndRemainder(BigInteger.valueOf(stats
							.getSecond()));
					double sumSquaredDividedDouble = sumSquaredDivided[0].doubleValue()
							+ (sumSquaredDivided[1].doubleValue() / stats.getSecond());

					double midVariance = stats.getFirst()[AAPD.SUM_SQUARE].doubleValue() - sumSquaredDividedDouble;
					double variance = midVariance / stats.getSecond();
					double stdDev = Math.sqrt(variance);

					rowItem.add(nf.format(stdDev / divider.doubleValue())); // standard deviation
					data.add(rowItem);
				}
			}
		}

		// add the list, set it to non editable
		JTable table = new JTable(data, columnNames) {
			private static final long serialVersionUID = -5091181582425402749L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		DefaultTableCellRenderer tcrColumn = new DefaultTableCellRenderer();
		tcrColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(tcrColumn);
		table.getColumnModel().getColumn(2).setCellRenderer(tcrColumn);
		table.getColumnModel().getColumn(3).setCellRenderer(tcrColumn);
		table.getColumnModel().getColumn(4).setCellRenderer(tcrColumn);
		return table;
	}

	public static JTable generateRelativeFreqStatsTable(Map<AAPDElement, Integer> caseFreqMap,
			Map<AAPDElement, Map<Integer, Pair<BigInteger[], Integer>>> barFreqMap,
			GlobalSettingsData globalSettingsData) {
		// calculate basic statistics
		// utility
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);

		//form vector of array of string
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Element");
		columnNames.add("Act. Inst. Freq.");
		columnNames.add("Case Freq.");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector which collects all

		for (AAPDElement focusElement : caseFreqMap.keySet()) {
			Vector<Object> rowItem = new Vector<Object>();
			rowItem.add(focusElement.getLabel());
			rowItem.add(barFreqMap.get(focusElement).get(AAPD.BAR_START_TIME).getSecond());
			rowItem.add(caseFreqMap.get(focusElement));

			// add it
			data.add(rowItem);
		}

		// add the list, set it to non editable
		JTable table = new JTable(data, columnNames) {
			private static final long serialVersionUID = -5091181582425402749L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		return table;
	}
}
