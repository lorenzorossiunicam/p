/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.StatisticTableGenerator;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.TextualInfoPanelGenerator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 19, 2009
 */
public class EdgePerformancePanel extends JPanel {
	private static final long serialVersionUID = -5056368633173179924L;

	// reference to "parents" panel
	private final ElementPerformancePanel elementPerformancePanel;

	// textual info
	private JPanel textualInfoPanel;
	private JPanel textualInfoPanelTop;
	private JPanel textualInfoPanelBottom;

	// kpi panel
	private JPanel KPIPanel;

	// buttons
	private JButton movingButtonLow;
	private JButton movingButtonHigh;

	// performance adjustment panel
	private JPanel minBoundPerformanceIndicatorPanel;

	// constants
	private static final String MOVINGTIME = "Moving Time";

	private FPDEdge<? extends FPDNode, ? extends FPDNode> fpdEdge;

	// utility
	private final NumberFormat nf;
	private final GlobalSettingsData globalSettingsData;

	public EdgePerformancePanel(ElementPerformancePanel elementPerformancePanel, GlobalSettingsData globalSettingsData) {
		this.elementPerformancePanel = elementPerformancePanel;
		this.globalSettingsData = globalSettingsData;

		// init utility
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);

		initiateTextualInfo();
		initiateKPIPanel();
		initiateMinBoundPerformanceIndicatorPanel();
	}

	private void initiateKPIPanel() {
		KPIPanel = new JPanel();
		KPIPanel.setPreferredSize(new Dimension(460, 135));
		add(KPIPanel);
	}

	private void initiateTextualInfo() {
		// add all
		textualInfoPanel = new JPanel();
		textualInfoPanel.setPreferredSize(new Dimension(350, 250));
		textualInfoPanel.setAlignmentY(LEFT_ALIGNMENT);

		textualInfoPanelTop = new JPanel();
		textualInfoPanelBottom = new JPanel();
		textualInfoPanelBottom.setPreferredSize(new Dimension(350, 30));
		JLabel label = new JLabel();
		label.setText("* Click an element in FPD to show its KPI measurement");
		textualInfoPanelBottom.add(label);
		textualInfoPanel.add(textualInfoPanelTop);
		textualInfoPanel.add(textualInfoPanelBottom);
		add(textualInfoPanel);
	}

	private void initiateMinBoundPerformanceIndicatorPanel() {
		minBoundPerformanceIndicatorPanel = new JPanel();
		minBoundPerformanceIndicatorPanel.setPreferredSize(new Dimension(350, 100));

		// TOP PANEL
		JLabel labelTitle = new JLabel();
		labelTitle.setText("Max. bound for performance indicator");

		// MIDDLE PANEL
		JPanel middlePanelMinBoundPerformanceIndicatorPanel = new JPanel();
		middlePanelMinBoundPerformanceIndicatorPanel.setLayout(new GridLayout(1, 3));

		// labels
		JLabel movingLabel = new JLabel();
		movingLabel.setText(MOVINGTIME);

		// performance color boxes
		movingButtonLow = new JButton();
		movingButtonLow.setBackground(Color.GREEN);

		movingButtonHigh = new JButton();
		movingButtonHigh.setBackground(Color.YELLOW);

		middlePanelMinBoundPerformanceIndicatorPanel.add(movingLabel);
		middlePanelMinBoundPerformanceIndicatorPanel.add(movingButtonLow);
		middlePanelMinBoundPerformanceIndicatorPanel.add(movingButtonHigh);

		// apply change setting button
		JLabel infoLabel = new JLabel();
		infoLabel.setText("** Click color to change the value");

		// create all
		minBoundPerformanceIndicatorPanel.add(labelTitle);
		minBoundPerformanceIndicatorPanel.add(middlePanelMinBoundPerformanceIndicatorPanel);
		minBoundPerformanceIndicatorPanel.add(infoLabel);

		// actionlisteners
		movingButtonLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog("Insert maximum bound for moving time (good performance)",
						fpdEdge.getMovingTimeBoundaryLow() / globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() > fpdEdge.getMovingTimeBoundaryHigh()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for good performance cannot be higher than boundary for medium performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdEdge.setMovingTimeBoundaryLow(newValue * globalSettingsData.getDividerValue());
							fpdEdge.updateEdgeInterface();
							movingButtonLow.setText(nf.format(newValue));
							movingButtonLow.repaint();

							// refresh the figure
							elementPerformancePanel.refreshModel();
						}
					} catch (Exception exc) {
						JOptionPane.showMessageDialog(new JPanel(), "Please insert double value", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		movingButtonHigh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for moving time (medium performance)", fpdEdge
								.getMovingTimeBoundaryHigh()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() < fpdEdge.getMovingTimeBoundaryLow()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for medium performance cannot be lower than boundary for good performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdEdge.setMovingTimeBoundaryHigh(newValue * globalSettingsData.getDividerValue());
							fpdEdge.updateEdgeInterface();
							movingButtonHigh.setText(nf.format(newValue));
							movingButtonHigh.repaint();

							// refresh the figure
							elementPerformancePanel.refreshModel();
						}
					} catch (Exception exc) {
						JOptionPane.showMessageDialog(new JPanel(), "Please insert double value", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		add(minBoundPerformanceIndicatorPanel);
	}

	public void populateData(FPDEdge<? extends FPDNode, ? extends FPDNode> fpdEdge, double[] data,
			GlobalSettingsData globalSettingsData) {
		this.fpdEdge = fpdEdge;

		populateTextualInfoPanel();
		populateKPIPanel(data, globalSettingsData);
		populateMinBoundPerformanceIndicatorPanel(globalSettingsData);
		repaint();
	}

	private void populateMinBoundPerformanceIndicatorPanel(GlobalSettingsData globalSettingsData) {
		// performance color boxes
		movingButtonLow.setText(nf.format(fpdEdge.getMovingTimeBoundaryLow() / globalSettingsData.getDividerValue()));
		movingButtonHigh.setText(nf.format(fpdEdge.getMovingTimeBoundaryHigh() / globalSettingsData.getDividerValue()));
		minBoundPerformanceIndicatorPanel.repaint();
	}

	private void populateKPIPanel(double[] data, GlobalSettingsData globalSettingsData) {
		KPIPanel.removeAll();
		JTable table = StatisticTableGenerator.generateStatsTable("Move Time", data, globalSettingsData);
		table.setPreferredScrollableViewportSize(new Dimension(450, Math.round((float) table.getPreferredSize()
				.getHeight())));
		JScrollPane scrollPane = new JScrollPane(table);
		KPIPanel.add(scrollPane);
		KPIPanel.repaint();
	}

	private void populateTextualInfoPanel() {
		if (textualInfoPanelTop.getComponentCount() > 0) {
			textualInfoPanelTop.removeAll();
		}

		// generate textual info panel
		TextualInfoPanelGenerator textualInfoPanelGenerator = new TextualInfoPanelGenerator();
		textualInfoPanelGenerator.addInfo("Selected Element*", "Arc from " + fpdEdge.getSource().getLabel() + " to "
				+ fpdEdge.getTarget().getLabel());
		textualInfoPanelGenerator.addInfo("TIME UNIT", globalSettingsData.getTimeUnitLabel());
		textualInfoPanelGenerator.addInfo("Frequency", String.valueOf(fpdEdge.getFrequency()));
		textualInfoPanelGenerator.addInfo("Violating frequency", String.valueOf(fpdEdge.getViolatingFrequency()));
		textualInfoPanelTop.add(textualInfoPanelGenerator.generatePanelInfo(2, 2, 10, 10, 350, 160, 100));

		textualInfoPanelTop.repaint();
	}
}
