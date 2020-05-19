/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;

/**
 * @author aadrians Mar 5, 2012
 * 
 */
@Plugin(name = "Performance Projection to Model", level = PluginLevel.PeerReviewed, returnLabels = { "Projected Manifest-based-performance value onto Model" }, returnTypes = { JComponent.class }, parameterLabels = { "Pattern Manifestation" }, userAccessible = true)
@Visualizer
public class ManifestPerfVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Manifest manifest) {
		System.gc();

		JPanel dumPanel = new JPanel();
		// browse through all possible time attribute
		try {
			// only propose attribute that exists in all events
			Set<String> attributes = new HashSet<String>();

			for (Entry<String, XAttribute> entry : manifest.getLog().iterator().next().iterator().next()
					.getAttributes().entrySet()) {
				if ((entry.getValue() instanceof XAttributeTimestamp)
						|| (entry.getValue() instanceof XAttributeDiscrete)
						|| (entry.getValue() instanceof XAttributeContinuous)) {
					attributes.add(entry.getKey());
				}
			}
			;
			Iterator<String> it = null;
			traceIterator: for (XTrace t : manifest.getLog()) {
				for (XEvent e : t) {
					if (attributes.size() > 0) {
						it = attributes.iterator();
						nextAttribute: while (it.hasNext()) {
							String checkAttr = it.next();
							// check per attribute
							for (Entry<String, XAttribute> eSet : e.getAttributes().entrySet()) {
								if (checkAttr.equals(eSet.getKey())) {
									continue nextAttribute;
								}
							}
							it.remove();
						}
					} else {
						break traceIterator;
					}
				}
			}

			if (attributes.size() == 0) {
				throw new IllegalArgumentException(
						"There is no timestamp/discrete/continuous attributes found in the original log.");
			}

			Object[] arrAttributes = attributes.toArray();
			int selectedIndex = 0;
			for (int i = 0; i < arrAttributes.length; i++) {
				if (arrAttributes[i].toString().toLowerCase().startsWith("time")) {
					selectedIndex = i;
					break;
				}
			}
			String timeAttr = (String) JOptionPane.showInputDialog(new JPanel(),
					"Choose which data type will be accounted as \"timestamp\"", "Timestamp attribute selection",
					JOptionPane.PLAIN_MESSAGE, null, arrAttributes, arrAttributes[selectedIndex]);

			// choose counter
			PerfCounter counter;
			Object[] attCounter = new Object[] { "Yes, I'd like to have strictly reliable results",
					"No, move on models are assumed to be firing transitions as soon as they are enabled" };
			if (0 == JOptionPane.showOptionDialog(new JPanel(),
					"Would you only consider performance between two synchronous moves?",
					"Strictly reliable towards move on model", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, attCounter, attCounter[0])) {
				counter = new ReliablePerfCounter();
			} else {
				counter = new PerfCounter();
			}

			// identify unreliable cases
			boolean[] caseRel = manifest.getCaseReliability();
			for (boolean b : caseRel) {
				if (!b) {
					int selection = JOptionPane.showConfirmDialog(new JPanel(),
							"Unreliable replay results are found, include them in visualization?",
							"Unreliable Results", JOptionPane.YES_NO_OPTION);

					if (manifest instanceof ManifestEvClassPattern) {
						ManifestPerfPanel<ManifestEvClassPattern, PerfCounter> panel = new ManifestPerfPanel<ManifestEvClassPattern, PerfCounter>(
								//								context, (ManifestEvClassPattern) manifest, new PerfCounter(), timeAttr,
								context, (ManifestEvClassPattern) manifest, counter, timeAttr,
								selection == JOptionPane.YES_OPTION);
						return panel;
					}

				}
			}

			if (manifest instanceof ManifestEvClassPattern) {
				ManifestPerfPanel<ManifestEvClassPattern, PerfCounter> panel = new ManifestPerfPanel<ManifestEvClassPattern, PerfCounter>(
						context, (ManifestEvClassPattern) manifest, counter, timeAttr, false);
				return panel;
			}

			JPanel panel = new JPanel();
			panel.add(new JLabel("Visualization for this type of manifest has not been implemented yet"));
			return panel;

		} catch (IllegalArgumentException argExc) {
			// JOptionPane.showMessageDialog(new JPanel(), argExc.getMessage());
			dumPanel.add(new JLabel(argExc.getMessage()));
		} catch (Exception exc) {
			//JOptionPane.showMessageDialog(new JPanel(), exc.getMessage());
			dumPanel.add(new JLabel(exc.getMessage()));
		}
		return dumPanel;
	}
}
