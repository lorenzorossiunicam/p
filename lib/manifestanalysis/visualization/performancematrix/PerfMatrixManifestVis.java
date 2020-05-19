/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performancematrix;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.ProMSplitPane;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.manifestanalysis.conversion.Manifest2ModelPNRepResult;
import org.processmining.plugins.performancematrix.PerformanceMatrixPanel;
import org.processmining.plugins.petrinet.manifestreplayer.conversion.Manifest2PNRepResult;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.performancematrix.PerfMatrixAlignmentVis;
import org.processmining.plugins.pnalignanalysis.visualization.performancematrix.PerfMatrixStats;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.FiveColorsLegendPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.ProMScrollPane;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author aadrians Jul 24, 2012
 * 
 */
@Plugin(name = "Time Between Transition Class Analysis", level = PluginLevel.PeerReviewed, returnLabels = { "Performance Matrix" }, returnTypes = { JComponent.class }, parameterLabels = { "Alignment" }, userAccessible = true)
@Visualizer
public class PerfMatrixManifestVis {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(final PluginContext context, Manifest manifest) {
		System.gc();

		// visualize it
		if (manifest instanceof ManifestEvClassPattern) {
			// there is a choice to 			
			Object[] arrAttributes = new Object[] {
					"Take only first occurrences of transition class instances",
					"Take pairwise occurrences of transition class instances. Measure from the begin of a manifest to the end of another manifest.",
					"Take pairwise occurrences of transition class instances. Measure from the begin of a manifest to the begin of another manifest." };
			String measurement = (String) JOptionPane.showInputDialog(new JPanel(), "Select measurement method",
					"Matrix measurement", JOptionPane.PLAIN_MESSAGE, null, arrAttributes, arrAttributes[1]);

			if (measurement == null) {
				return new JLabel("Visualization cancelled by user");
			} else if (measurement.equals(arrAttributes[0])) {
				// visualize it
				int option = JOptionPane.showConfirmDialog(new JPanel(), "Show invisible transitions in the matrix?",
						"Performance Matrix Setting", JOptionPane.YES_NO_OPTION);
				return createBasedOnFirstOccurrencesOnly(manifest, option == 0);
			} else if (measurement.equals(arrAttributes[1])) {
				final PerfMatrixManifestProvider provider = new PerfMatrixManifestProvider(
						(ManifestEvClassPattern) manifest);
				return createBasedOnAllPairwise(provider);
			} else {
				final PerfMatrixManifestProviderBetweenStarts provider = new PerfMatrixManifestProviderBetweenStarts(
						(ManifestEvClassPattern) manifest);
				return createBasedOnAllPairwise(provider);
			}
		} else {
			// only classical way: measure by projection of the first occurrence, and then measure between the first occurrence of 'manifests'
			int option = JOptionPane.showConfirmDialog(new JPanel(), "Show invisible transitions in the matrix?",
					"Performance Matrix Setting", JOptionPane.YES_NO_OPTION);
			return createBasedOnFirstOccurrencesOnly(manifest, option == 0);
		}

	}

	/**
	 * Measure performance really based on pairwise. Distance between two
	 * transition instances A and B is measured from the moment A begins until
	 * the moment B ends
	 * 
	 * @param manifest
	 * @param showInviTrans
	 * @return
	 */
	private JComponent createBasedOnAllPairwise(final PerfMatrixManifestProvider provider) {
		// create matrix canvas and wrap it with scrollpane
		final double[] data = provider.getAllInfo();
		String[] transLabels = provider.getTransLabel();
		final PerformanceMatrixPanel matrixPanel = new PerformanceMatrixPanel(transLabels, transLabels, data);

		final JScrollPane scroller = new ProMScrollPane(matrixPanel);
		scroller.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() < 0) {
					// scroll up
					Point currViewPos = scroller.getViewport().getViewPosition();
					currViewPos.setLocation(currViewPos.getX(), currViewPos.getY() + 1);
					scroller.getViewport().setViewPosition(currViewPos);
				} else if (e.getWheelRotation() > 0) {
					// scroll down
					Point currViewPos = scroller.getViewport().getViewPosition();
					currViewPos.setLocation(currViewPos.getX(), currViewPos.getY() - 1);
					scroller.getViewport().setViewPosition(currViewPos);
				}
			}
		});
		// create statistic panel
		SlickerFactory factory = SlickerFactory.instance();

		final ProMTextField threshold = new ProMTextField("0.01");
		threshold.setPreferredSize(new Dimension(75, 30));

		final ProMTextField divisor = new ProMTextField("1.00");
		divisor.setPreferredSize(new Dimension(100, 30));

		JButton updateThreshold = factory.createButton("Update");
		updateThreshold.setPreferredSize(new Dimension(100, 30));
		updateThreshold.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					double t = Double.parseDouble(threshold.getText());
					double d = Double.parseDouble(divisor.getText());
					if ((Double.compare(t, 0.00) >= 0) && (Double.compare(d, 0.00) != 0)) {
						matrixPanel.setThreshold(t);
						matrixPanel.setDivisor(d);
						matrixPanel.repaint();
					}
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});

		JButton exportBtn = factory.createButton("Export to CSV...");
		exportBtn.setPreferredSize(new Dimension(120, 30));
		exportBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser();
				fc.setAcceptAllFileFilterUsed(false);
				fc.setFileFilter(new CSVFilter());
				if (JFileChooser.APPROVE_OPTION == fc.showSaveDialog(new JPanel())) {
					if (fc.getSelectedFile().getName().toLowerCase().endsWith(".csv")) {
						PerfMatrixManifestExporter.exportAsMatrix(provider.getInt2TransClasses(), provider,
								matrixPanel.getDivisor(), fc.getSelectedFile());
					} else {
						PerfMatrixManifestExporter.exportAsMatrix(provider.getInt2TransClasses(), provider,
								matrixPanel.getDivisor(), new File(fc.getSelectedFile().getAbsolutePath() + ".csv"));
					}

				}
				;
			}
		});

		final JCheckBox showValues = factory.createCheckBox("Show values, divided by ", true);
		showValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				matrixPanel.setShowValues(showValues.isSelected());
				matrixPanel.repaint();
			}
		});

		final JCheckBox showMarks = factory.createCheckBox("Mark values lower than ", true);
		showMarks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				matrixPanel.setShowMarks(showMarks.isSelected());
				matrixPanel.repaint();
			}
		});

		Object[] timeUnit = new Object[] { "[raw]", "ms", "seconds", "minutes", "hours", "days", "month" };
		final JComboBox timeUnitCbBox = factory.createComboBox(timeUnit);
		timeUnitCbBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (timeUnitCbBox.getSelectedIndex() <= 1) {
					divisor.setText("1.00");
				} else if (timeUnitCbBox.getSelectedIndex() == 2) {
					divisor.setText("1000");
				} else if (timeUnitCbBox.getSelectedIndex() == 3) {
					divisor.setText("60000");
				} else if (timeUnitCbBox.getSelectedIndex() == 4) {
					divisor.setText("" + (60 * 60000));
				} else if (timeUnitCbBox.getSelectedIndex() == 5) {
					divisor.setText("" + (60 * 60000 * 24));
				} else if (timeUnitCbBox.getSelectedIndex() == 6) {
					divisor.setText("" + ((double) 60 * (double) 60000 * 24 * 30));
				}
				matrixPanel.setDivisor(Double.parseDouble(divisor.getText()));
				matrixPanel.repaint();
			}
		});

		final JComboBox changeData = factory.createComboBox(new Object[] { "Minimum Value", "Maximum Value", "Average",
				"Standard Deviation", "Frequency" });
		changeData.setPreferredSize(new Dimension(150, 30));
		changeData.setSelectedIndex(2);
		changeData.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				PerfMatrixStats m = null;
				int selectedIdx = changeData.getSelectedIndex();
				if (selectedIdx == 4) {
					timeUnitCbBox.setEnabled(false);
					timeUnitCbBox.setSelectedIndex(0);
					divisor.setText("1");

					// create data
					int ptr = 0;
					for (int i = 0; i < provider.getNumTransClasses(); i++) {
						for (int j = 0; j < provider.getNumTransClasses(); j++) {
							data[ptr] = provider.getFrequency(i, j);
							if (Double.compare(data[ptr], 0.0) == 0) {
								data[ptr] = Double.NaN;
							}
							ptr++;
						}
					}
					matrixPanel.setDivisor(1.00);
					matrixPanel.setData(data, false, Double.parseDouble(threshold.getText()));
				} else {
					timeUnitCbBox.setEnabled(true);
					if (selectedIdx == 0) {
						m = PerfMatrixStats.MIN;
					} else if (selectedIdx == 1) {
						m = PerfMatrixStats.MAX;
					} else if (selectedIdx == 2) {
						m = PerfMatrixStats.AVG;
					} else if (selectedIdx == 3) {
						m = PerfMatrixStats.STDDEV;
					}
					// create data
					int ptr = 0;
					for (int i = 0; i < provider.getNumTransClasses(); i++) {
						for (int j = 0; j < provider.getNumTransClasses(); j++) {
							data[ptr] = provider.getTimeBetween(i, j, m);
							ptr++;
						}
					}
					matrixPanel.setData(data);
				}
				// set matrix panel
				matrixPanel.repaint();

			}
		});

		Dimension smallDim = new Dimension(5, 0);
		JPanel statisticPanel = new JPanel();
		statisticPanel.setBackground(WidgetColors.PROPERTIES_BACKGROUND);
		statisticPanel.setLayout(new BoxLayout(statisticPanel, BoxLayout.X_AXIS));

		statisticPanel.add(factory.createLabel("Coloring criteria:"));
		statisticPanel.add(changeData);
		statisticPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		statisticPanel.add(factory.createLabel("Time:"));
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(timeUnitCbBox);
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(showValues);
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(divisor);
		statisticPanel.add(Box.createRigidArea(smallDim));

		statisticPanel.add(showMarks);
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(threshold);
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(updateThreshold);
		statisticPanel.add(exportBtn);
		statisticPanel.add(Box.createRigidArea(smallDim));
		statisticPanel.add(new FiveColorsLegendPanel());
		statisticPanel.add(Box.createRigidArea(smallDim));

		// MAIN PANEL
		ProMSplitPane splitPanel = new ProMSplitPane(ProMSplitPane.VERTICAL_SPLIT);
		splitPanel.setBorder(BorderFactory.createEmptyBorder());
		splitPanel.setOneTouchExpandable(true);
		splitPanel.setLeftComponent(statisticPanel);
		splitPanel.setRightComponent(scroller);

		return splitPanel;
	}

	private JComponent createBasedOnFirstOccurrencesOnly(Manifest manifest, boolean showInviTrans) {
		// do it classical way: create a new log, only take the first occurrences of transition instances and measure performance

		// create pnRep result from manifest
		PNRepResult pnRepResult = Manifest2PNRepResult.convert(manifest);

		// util class
		XFactory factory = XFactoryRegistry.instance().currentDefault();

		// result
		XLog cLog = factory.createLog(manifest.getLog().getAttributes());
		XEventClass dummyEvClass = new XEventClass("DUMMY", -1);
		TransEvClassMapping map = new TransEvClassMapping(manifest.getEvClassifier(), dummyEvClass); // new mapping constructed from manifest

		// call method that create logs for PNRepResult
		Manifest2ModelPNRepResult.createLogMapForManifest(manifest, factory, cLog, dummyEvClass, map);

		return PerfMatrixAlignmentVis.createPerformanceMatrix(pnRepResult, manifest.getNet(), cLog,
				manifest.getInitMarking(), showInviTrans);
	}
}

class CSVFilter extends FileFilter {

	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(".csv");
	}

	public String getDescription() {
		return ".CSV file";
	}

}