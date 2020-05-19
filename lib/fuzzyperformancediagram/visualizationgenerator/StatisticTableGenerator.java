/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator;

import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.plugins.performancemeasurement.util.BasicStatisticCalculator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 19, 2009
 */
public class StatisticTableGenerator {
	/**
	 * Generate JTable to visualize "standard" statistics (min, max, avg, std
	 * dev, fastest average, slowest) based on list of long values (the
	 * statistics still needs to be calculated)
	 * 
	 * @param property
	 * @param values
	 * @param globalSettingsData
	 * @return
	 */
	public static JTable generateStatsTable(String property, List<Long> values, GlobalSettingsData globalSettingsData) {
		// calculate basic statistics
		double[] stats = BasicStatisticCalculator.calculateBasicStatisticsLong(values, globalSettingsData
				.getFastestBoundPercentage(), globalSettingsData.getSlowestBoundPercentage());
		return generateStatsTable(property, stats, globalSettingsData);
	}

	/**
	 * Generate JTable to visualize statistics on stats array (statistic is
	 * already calculated in advance)
	 * 
	 * @param property
	 * @param stats
	 * @param globalSettingsData
	 * @return
	 */
	public static JTable generateStatsTable(String property, double[] stats, GlobalSettingsData globalSettingsData) {
		return generateStatsTable(property, stats, globalSettingsData, false);
	}

	public static JTable generateStatsTable(String property, double[] stats, GlobalSettingsData globalSettingsData,
			boolean permitNegativeValues) {
		// number format
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		//form vector of array of string
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Property");
		columnNames.add(property);

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector which collects all

		Vector<Object> min = new Vector<Object>();
		min.add("Min");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.MIN] < 0))
				|| (stats[BasicStatisticCalculator.MIN] == Double.NEGATIVE_INFINITY)) {
			min.add("Unavailable");
		} else {
			min.add(nf.format(stats[BasicStatisticCalculator.MIN] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> max = new Vector<Object>();
		max.add("Max");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.MAX] < 0))
				|| (stats[BasicStatisticCalculator.MAX] == Double.NEGATIVE_INFINITY)) {
			max.add("Unavailable");
		} else {
			max.add(nf.format(stats[BasicStatisticCalculator.MAX] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> average = new Vector<Object>();
		average.add("Average");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.AVERAGE] < 0))
				|| (stats[BasicStatisticCalculator.AVERAGE] == Double.NEGATIVE_INFINITY)) {
			average.add("Unavailable");
		} else {
			average.add(nf.format(stats[BasicStatisticCalculator.AVERAGE] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> stdDev = new Vector<Object>();
		stdDev.add("Std Dev");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.STDDEV] < 0))
				|| (stats[BasicStatisticCalculator.STDDEV] == Double.NEGATIVE_INFINITY)) {
			stdDev.add("Unavailable");
		} else {
			stdDev.add(nf.format(stats[BasicStatisticCalculator.STDDEV] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> fast = new Vector<Object>();
		fast.add("Fastest " + nf.format(globalSettingsData.getFastestBoundPercentage() * 100) + "%");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.FAST] < 0))
				|| (stats[BasicStatisticCalculator.FAST] == Double.NEGATIVE_INFINITY)) {
			fast.add("Unavailable");
		} else {
			fast.add(nf.format(stats[BasicStatisticCalculator.FAST] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> slow = new Vector<Object>();
		slow.add("Slowest " + nf.format(globalSettingsData.getSlowestBoundPercentage() * 100) + "%");
		if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.SLOW] < 0))
				|| (stats[BasicStatisticCalculator.SLOW] == Double.NEGATIVE_INFINITY)) {
			slow.add("Unavailable");
		} else {
			slow.add(nf.format(stats[BasicStatisticCalculator.SLOW] / globalSettingsData.getDividerValue()));
		}

		Vector<Object> rest = new Vector<Object>();
		if (Double.compare(globalSettingsData.getSlowestBoundPercentage()
				+ globalSettingsData.getFastestBoundPercentage(), 1.0) < 0) {
			rest.add("Rest "
					+ nf.format((1.0 - globalSettingsData.getSlowestBoundPercentage() - globalSettingsData
							.getFastestBoundPercentage()) * 100) + "%");
			if (((!permitNegativeValues) && (stats[BasicStatisticCalculator.REST] < 0))
					|| (stats[BasicStatisticCalculator.REST] == Double.NEGATIVE_INFINITY)) {
				rest.add("Unavailable");
			} else {
				rest.add(nf.format(stats[BasicStatisticCalculator.REST] / globalSettingsData.getDividerValue()));
			}
		} else {
			rest.add("Rest");
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
		DefaultTableCellRenderer tcrColumn = new DefaultTableCellRenderer();
		tcrColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(tcrColumn);
		return table;
	}
}
