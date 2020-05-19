package org.processmining.plugins.ilpminer.fitness;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

import com.fluxicon.slickerbox.factory.SlickerFactory;

//@Plugin(name = "Transition Fitness Visualizer", returnLabels = { "Visualized Transition Fitness" }, returnTypes = { JComponent.class }, parameterLabels = { "Transition Fitness" }, userAccessible = false)
//@Visualizer
public class ILPFitnessVisualizer {
//	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ILPFitnessResult fitness) {
		try {
			ILPFitnessConnection connection = context.getConnectionManager()
					.getFirstConnection(ILPFitnessConnection.class, context,
							fitness);

			// connection found. Create all necessary component to instantiate
			// inactive visualization panel
			Petrinet net = connection
					.getObjectWithRole(ILPFitnessConnection.PNET);
			return getVisualizationPanel(context, net, fitness);
		} catch (ConnectionCannotBeObtained e) {
			// No connections available
			context.log("Connection does not exist", MessageLevel.DEBUG);
			return null;
		}
	}

	private JComponent getVisualizationPanel(PluginContext context,
			Petrinet net, ILPFitnessResult fitness) {
		SlickerFactory sf = SlickerFactory.instance();
		JPanel panel = sf.createRoundedPanel();
		double[][] sizes = { { 200, TableLayoutConstants.FILL }, new double[net.getTransitions().size() + 2] };
		for (int i = 0; i < sizes[1].length; i++) {
			sizes[1][i] = 30;
		}
		TableLayout l = new TableLayout(sizes);
		panel.setLayout(l);
		int i = 0, totalMissing = 0, totalConsumed = 0, totalInLog = 0, totalFailed = 0;
		for (Transition t : net.getTransitions()) {
			int miss = (fitness.getMissing().get(t) == null ? 0 : fitness
					.getMissing().get(t));
			int cons = (fitness.getConsumed().get(t) == null ? 0 : fitness
					.getConsumed().get(t));
			int tot = (fitness.getTotalEvents().get(t) == null ? 0 : fitness
					.getTotalEvents().get(t));
			int fail = (fitness.getFailedEvents().get(t) == null ? 0 : fitness
					.getFailedEvents().get(t));
			panel.add(sf.createLabel("<html>" + t.getLabel() + ":</html>"),
					"0," + i);
			totalMissing += miss;
			totalConsumed += cons;
			totalInLog += tot;
			totalFailed += fail;
			if (cons == 0) {
				panel
						.add(
								sf
										.createLabel("<html>1.0 (0 missing, 0 consumed) - 0 failed, "+tot+" in log</html>"),
								"1," + i);
			} else {
				panel.add(sf.createLabel("<html>"
						+ (1.0 - (double) miss / (double) cons) + " (" + miss
						+ " missing, " + cons + " consumed) - " + fail
						+ " failed, " + tot + " in log</html>"), "1," + i);
			}
			i++;
		}
		panel.add(sf.createLabel("<html>Succesfull Execution:</html>"), "0,"
				+ i);
		panel.add(sf.createLabel("<html>"
				+ (1.0 - (double) totalMissing / (double) totalConsumed) + " ("
				+ totalMissing + " missing, " + totalConsumed
				+ " consumed)</html>"), "1," + i);
		panel.add(sf.createLabel("<html>Transition Fitness:</html>"), "0,"
				+ (i + 1));
		panel.add(sf.createLabel("<html>"
				+ (1.0 - (double) totalFailed / (double) totalInLog) + " ("
				+ totalFailed + " failed, " + totalInLog + " in log)</html>"),
				"1," + (i + 1));
		JScrollPane pane = new JScrollPane(panel);
		return pane;
	}
}
