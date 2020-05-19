/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;

import org.processmining.plugins.performancemeasurement.util.BasicStatisticCalculator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 19, 2009
 */
public class StatisticTableGenerator {
	public static JTable generateStatsTable(String property, double fastestPercentage, double slowestPercentage,
			List<Long> values) {
		// calculate basic statistics
		double[] stats = BasicStatisticCalculator.calculateBasicStatisticsLong(values, fastestPercentage,
				slowestPercentage);
		return generateStatsTable(property, fastestPercentage, slowestPercentage, stats);
	}

	public static JTable generateStatsTable(String property, double fastestPercentage, double slowestPercentage,
			double[] stats) {
		// utility
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);

		//form vector of array of string
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Property");
		columnNames.add(property);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector which collects all

		Vector<Object> min = new Vector<Object>();
		min.add("Min");
		if (stats[BasicStatisticCalculator.MIN] >= 0) {
			min.add(stats[BasicStatisticCalculator.MIN]);
		} else {
			min.add("Unavailable");
		}

		Vector<Object> max = new Vector<Object>();
		max.add("Max");
		if (stats[BasicStatisticCalculator.MAX] >= 0) {
			max.add(stats[BasicStatisticCalculator.MAX]);
		} else {
			max.add("Unavailable");
		}

		Vector<Object> average = new Vector<Object>();
		average.add("Average");
		if (stats[BasicStatisticCalculator.AVERAGE] >= 0) {
			average.add(stats[BasicStatisticCalculator.AVERAGE]);
		} else {
			average.add("Unavailable");
		}

		Vector<Object> stdDev = new Vector<Object>();
		stdDev.add("Std Dev");
		if (stats[BasicStatisticCalculator.STDDEV] >= 0) {
			stdDev.add(stats[BasicStatisticCalculator.STDDEV]);
		} else {
			stdDev.add("Unavailable");
		}

		Vector<Object> fast = new Vector<Object>();
		fast.add("Fastest " + nf.format(slowestPercentage) + "%");
		if (stats[BasicStatisticCalculator.FAST] >= 0) {
			fast.add(stats[BasicStatisticCalculator.FAST]);
		} else {
			fast.add("Unavailable");
		}

		Vector<Object> slow = new Vector<Object>();
		slow.add("Slowest " + nf.format(fastestPercentage) + "%");
		if (stats[BasicStatisticCalculator.SLOW] >= 0) {
			slow.add(stats[BasicStatisticCalculator.SLOW]);
		} else {
			slow.add("Unavailable");
		}

		Vector<Object> rest = new Vector<Object>();
		if (100 - slowestPercentage - fastestPercentage > 0) {
			rest.add("Rest " + nf.format(100 - slowestPercentage - fastestPercentage) + "%");
			if (stats[BasicStatisticCalculator.REST] >= 0) {
				rest.add(stats[BasicStatisticCalculator.REST]);
			} else {
				rest.add("Unavailable");
			}
		} else {
			rest.add("Rest " + "0%");
			rest.add("Unavailable");
		}
		data.add(min);
		data.add(max);
		data.add(average);
		data.add(stdDev);
		data.add(fast);
		data.add(slow);
		data.add(rest);

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
