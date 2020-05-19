package org.processmining.plugins.ilpminer.netproperties;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.plugin.PluginContext;

import com.fluxicon.slickerbox.factory.SlickerFactory;

//@Plugin(name = "Transition Fitness Visualizer", returnLabels = { "Visualized Transition Fitness" }, returnTypes = { JComponent.class }, parameterLabels = { "Transition Fitness" }, userAccessible = false)
//@Visualizer
public class NetPropertiesVisualizer {
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, NetPropertiesResult result) {
		SlickerFactory sf = SlickerFactory.instance();
		JPanel panel = sf.createRoundedPanel();
		double[][] sizes = { { 200, TableLayoutConstants.FILL }, { 30, 30 } };
		TableLayout l = new TableLayout(sizes);
		panel.setLayout(l);
		panel.add(sf.createLabel("<html>Average connector degree: </html>"), "0,0");
		panel.add(sf.createLabel("<html>"+result.getAverageConnectorDegree()+"</html>"), "1,0");
		panel.add(sf.createLabel("<html>Density: </html>"), "0,1");
		panel.add(sf.createLabel("<html>"+result.getDensity()+"</html>"), "1,1");
		JScrollPane pane = new JScrollPane(panel);
		return pane;
	}
}
