package org.processmining.plugins.ilpminer.fitness;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.TextField;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;

import com.fluxicon.slickerbox.factory.SlickerFactory;

//@Plugin(name = "ten fold crap Visualizer", returnLabels = { "Visualized ten fold crap" }, returnTypes = { JComponent.class }, parameterLabels = { "ten fold crap" }, userAccessible = false)
//@Visualizer
public class tenfoldCrossValidationVisualizer {
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ILPFitnessResult[][] fitness) {
		SlickerFactory sf = SlickerFactory.instance();
		JPanel panel = sf.createRoundedPanel();
		double[][] sizes = { { 200, TableLayoutConstants.FILL }, new double[10] };
		for (int i = 0; i < 10; i++) {
			sizes[1][i] = 30;
		}
		TableLayout l = new TableLayout(sizes);
		panel.setLayout(l);
		for (int i = 0; i < 10; i++) {
			String label = "";
			double avg = 0;
			panel.add(sf.createLabel("<html>" + ((double)i+1)/10 + ":</html>"),
					"0," + i);
			for (int j = 0; j < 10; j++) {
				double missing = 0, total = 0;
				for(int m : fitness[j][i].getFailedEvents().values()) { missing += m; } 
				for(int t : fitness[j][i].getTotalEvents().values()) { total += t; } 
				double value = missing/total; 
				label += "\t"+(1-value);
				avg += value;
			}
			panel.add(new TextField((1-(avg/10)) + label),
					"1," + i);
		}
		JScrollPane pane = new JScrollPane(panel);
		return pane;
	}
}
