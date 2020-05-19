/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.StatisticTableGenerator;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.TextualInfoPanelGenerator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 19, 2009
 */
public class NodePerformancePanel extends JPanel implements ItemListener {
	private static final long serialVersionUID = -3646219844208894113L;

	// reference to "parents" panel
	private final ElementPerformancePanel elementPerformancePanel;

	// internal data
	private FPDNode fpdNode;

	// textual info
	private final JPanel textualInfoPanel;
	private final JPanel textualInfoPanelTop;
	private final JPanel textualInfoPanelBottom;

	// kpi panel
	private JPanel KPIPanel;
	private JPanel topPanel;
	private JPanel middlePanel;
	private JPanel bottomPanel;
	private JComboBox comboBox;
	// buttons
	private JButton synchronizationButtonHigh;
	private JButton synchronizationButtonLow;
	private JButton waitingButtonHigh;
	private JButton waitingButtonLow;
	private JButton throughputButtonLow;
	private JButton throughputButtonHigh;

	// performance adjustment panel
	private JPanel minBoundPerformanceIndicatorPanel;

	// constants
	private static final String WAITINGTIME = "Waiting Time";
	private static final String SYNCHRONIZATIONTIME = "Sync. Time";
	private static final String THROUGHPUTTIME = "Throughput Time";

	// utility
	private final NumberFormat nf;
	private final NumberFormat statFormat;
	private GlobalSettingsData globalSettingsData;

	public NodePerformancePanel(ElementPerformancePanel elementPerformancePanel, GlobalSettingsData globalSettingsData) {
		this.elementPerformancePanel = elementPerformancePanel;
		this.globalSettingsData = globalSettingsData;

		// init utility
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		statFormat = NumberFormat.getInstance();
		statFormat.setMinimumFractionDigits(2);
		statFormat.setMaximumFractionDigits(2);

		// add all
		textualInfoPanel = new JPanel();
		textualInfoPanel.setPreferredSize(new Dimension(420, 410));

		textualInfoPanelTop = new JPanel();
		textualInfoPanelBottom = new JPanel();
		textualInfoPanelBottom.setPreferredSize(new Dimension(340, 30));
		JLabel label = new JLabel();
		label.setText("*Click an element in FPD to show its KPI measurement");
		textualInfoPanelBottom.add(label);
		textualInfoPanel.add(textualInfoPanelTop);
		textualInfoPanel.add(textualInfoPanelBottom);
		add(textualInfoPanel);

		initiateKPIPanel();
		initiateMinBoundPerformanceIndicatorPanel();
	}

	private void initiateKPIPanel() {
		KPIPanel = new JPanel();
		KPIPanel.setLayout(new BorderLayout(10, 10));

		// label and combobox
		topPanel = new JPanel();

		// add label
		JLabel label = new JLabel();
		label.setText("Select KPI");
		topPanel.add(label);

		// add combobox
		String comboBoxItems[] = { WAITINGTIME, SYNCHRONIZATIONTIME, THROUGHPUTTIME };
		comboBox = new JComboBox(comboBoxItems);
		comboBox.setEditable(false);
		comboBox.setSelectedIndex(2);
		comboBox.addItemListener(this);
		topPanel.add(comboBox);

		// table panel
		middlePanel = new JPanel();

		// changeable panel (using card layout)
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new CardLayout());

		// add all to KPI panel
		KPIPanel.add(topPanel, BorderLayout.NORTH);
		KPIPanel.add(bottomPanel, BorderLayout.CENTER);
		KPIPanel.add(middlePanel, BorderLayout.SOUTH);

		add(KPIPanel);
	}

	public void populateData(FPDNode fpdNode, List<double[]> list, GlobalSettingsData globalSettingsData) {
		this.fpdNode = fpdNode;
		this.globalSettingsData = globalSettingsData;
		populateTextualInfoPanel();
		populateKPIPanel(list, globalSettingsData.getFastestBoundPercentage(), globalSettingsData
				.getSlowestBoundPercentage(), globalSettingsData);
		populateMinBoundPerformanceIndicatorPanel(globalSettingsData);
		repaint();
	}

	private void initiateMinBoundPerformanceIndicatorPanel() {
		minBoundPerformanceIndicatorPanel = new JPanel();
		minBoundPerformanceIndicatorPanel.setPreferredSize(new Dimension(400, 160));

		// TOP PANEL
		JLabel minBoundPerformanceIndicatorPanelLabelTitle = new JLabel();
		minBoundPerformanceIndicatorPanelLabelTitle.setText("Max. bound for performance indicator**");

		// MIDDLE PANEL
		JPanel middlePanelMinBoundPerformanceIndicatorPanel = new JPanel();
		middlePanelMinBoundPerformanceIndicatorPanel.setLayout(new GridLayout(3, 3));

		// labels
		JLabel waitingLabel = new JLabel();
		waitingLabel.setText(WAITINGTIME);

		JLabel synchronizationLabel = new JLabel();
		synchronizationLabel.setText(SYNCHRONIZATIONTIME);

		JLabel throughputLabel = new JLabel();
		throughputLabel.setText(THROUGHPUTTIME);

		// performance color boxes
		waitingButtonLow = new JButton();
		waitingButtonLow.setBackground(Color.GREEN);

		waitingButtonHigh = new JButton();
		waitingButtonHigh.setBackground(Color.YELLOW);

		synchronizationButtonLow = new JButton();
		synchronizationButtonLow.setBackground(Color.GREEN);

		synchronizationButtonHigh = new JButton();
		synchronizationButtonHigh.setBackground(Color.YELLOW);

		throughputButtonLow = new JButton();
		throughputButtonLow.setBackground(Color.GREEN);

		throughputButtonHigh = new JButton();
		throughputButtonHigh.setBackground(Color.YELLOW);

		middlePanelMinBoundPerformanceIndicatorPanel.add(waitingLabel);
		middlePanelMinBoundPerformanceIndicatorPanel.add(waitingButtonLow);
		middlePanelMinBoundPerformanceIndicatorPanel.add(waitingButtonHigh);

		middlePanelMinBoundPerformanceIndicatorPanel.add(synchronizationLabel);
		middlePanelMinBoundPerformanceIndicatorPanel.add(synchronizationButtonLow);
		middlePanelMinBoundPerformanceIndicatorPanel.add(synchronizationButtonHigh);

		middlePanelMinBoundPerformanceIndicatorPanel.add(throughputLabel);
		middlePanelMinBoundPerformanceIndicatorPanel.add(throughputButtonLow);
		middlePanelMinBoundPerformanceIndicatorPanel.add(throughputButtonHigh);

		// apply change setting button
		JLabel infoLabel = new JLabel();
		infoLabel.setText("** Click colored button to change its value");

		// create all
		minBoundPerformanceIndicatorPanel.add(minBoundPerformanceIndicatorPanelLabelTitle);
		minBoundPerformanceIndicatorPanel.add(middlePanelMinBoundPerformanceIndicatorPanel);
		minBoundPerformanceIndicatorPanel.add(infoLabel);

		// button
		waitingButtonLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog("Insert maximum bound for waiting time (good performance)",
						fpdNode.getWaitingTimeLow() / globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() > fpdNode.getWaitingTimeHigh()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for good performance cannot be higher than boundary for medium performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setWaitingTimeLow(newValue * globalSettingsData.getDividerValue());
							elementPerformancePanel.refreshModel();
							waitingButtonLow.setText(nf.format(newValue));
							waitingButtonLow.repaint();

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

		waitingButtonHigh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for waiting time (medium performance)", fpdNode.getWaitingTimeHigh()
								/ globalSettingsData.getDividerValue());

				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() < fpdNode.getWaitingTimeLow()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for medium performance cannot be lower than boundary for good performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setWaitingTimeHigh(newValue * globalSettingsData.getDividerValue());
							waitingButtonHigh.setText(nf.format(newValue));
							waitingButtonHigh.repaint();

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

		synchronizationButtonLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for synchronization time (good performance)", fpdNode
								.getSynchronizationTimeLow()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() > fpdNode.getSynchronizationTimeHigh()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for good performance cannot be higher than boundary for medium performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setSynchronizationTimeLow(newValue * globalSettingsData.getDividerValue());
							synchronizationButtonLow.setText(nf.format(newValue));
							synchronizationButtonLow.repaint();

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

		synchronizationButtonHigh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for synchronization time (medium performance)", fpdNode
								.getSynchronizationTimeHigh()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() < fpdNode.getSynchronizationTimeLow()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for medium performance cannot be lower than boundary for good performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setSynchronizationTimeHigh(newValue * globalSettingsData.getDividerValue());
							synchronizationButtonHigh.setText(nf.format(newValue));
							synchronizationButtonHigh.repaint();

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

		throughputButtonLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for throughput time (good performance)", fpdNode.getThroughputTimeLow()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() > fpdNode.getThroughputTimeHigh()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for good performance cannot be higher than boundary for medium performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setThroughputTimeLow(newValue * globalSettingsData.getDividerValue());
							throughputButtonLow.setText(nf.format(newValue));
							throughputButtonLow.repaint();

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

		throughputButtonHigh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for throughput time (medium performance)", fpdNode
								.getThroughputTimeHigh()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() < fpdNode.getThroughputTimeLow()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for medium performance cannot be lower than boundary for good performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							fpdNode.setThroughputTimeHigh(newValue * globalSettingsData.getDividerValue());
							throughputButtonHigh.setText(nf.format(newValue));
							throughputButtonHigh.repaint();

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

	private void populateMinBoundPerformanceIndicatorPanel(GlobalSettingsData globalSettingsData) {
		waitingButtonLow.setText(statFormat.format(fpdNode.getWaitingTimeLow() / globalSettingsData.getDividerValue()));
		waitingButtonHigh.setText(statFormat
				.format(fpdNode.getWaitingTimeHigh() / globalSettingsData.getDividerValue()));
		synchronizationButtonLow.setText(statFormat.format(fpdNode.getSynchronizationTimeLow()
				/ globalSettingsData.getDividerValue()));
		synchronizationButtonHigh.setText(statFormat.format(fpdNode.getSynchronizationTimeHigh()
				/ globalSettingsData.getDividerValue()));
		throughputButtonLow.setText(statFormat.format(fpdNode.getThroughputTimeLow()
				/ globalSettingsData.getDividerValue()));
		throughputButtonHigh.setText(statFormat.format(fpdNode.getThroughputTimeHigh()
				/ globalSettingsData.getDividerValue()));
		minBoundPerformanceIndicatorPanel.repaint();
	}

	private void populateKPIPanel(List<double[]> list, double defaultSlowBoundary, double defaultFastBoundary,
			GlobalSettingsData globalSettingsData) {
		// changeable panel (using card layout)
		bottomPanel.removeAll();
		bottomPanel.add(generateStatPanel(WAITINGTIME, list.get(0), globalSettingsData), WAITINGTIME);
		bottomPanel.add(generateStatPanel(SYNCHRONIZATIONTIME, list.get(1), globalSettingsData), SYNCHRONIZATIONTIME);
		bottomPanel.add(generateStatPanel(THROUGHPUTTIME, list.get(2), globalSettingsData), THROUGHPUTTIME);

		CardLayout cl = (CardLayout) bottomPanel.getLayout();
		cl.show(bottomPanel, (String) comboBox.getSelectedItem()); // set default view

		// populate middlePanel
		if (middlePanel.getComponentCount() > 0) {
			middlePanel.removeAll();
		}
		JTable table = generateEventClassFreqTable();
		table.setPreferredScrollableViewportSize(new Dimension(300, Math.round((float) table.getPreferredSize()
				.getHeight())));

		middlePanel.add(new JScrollPane(table));
		middlePanel.repaint();

		KPIPanel.repaint();
	}

	private JTable generateEventClassFreqTable() {

		// populate internal data
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Event Class");
		columnNames.add("Frequency");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>(); // vector which collects all
		List<XEventClass> listOfEventClasses = fpdNode.getEventClasses();
		List<Integer> listOfFrequencyOfEventClasses = fpdNode.getFreqEventClasses();
		for (int i = 0; i < fpdNode.getEventClasses().size(); i++) {
			Vector<Object> newVector = new Vector<Object>();
			newVector.add(listOfEventClasses.get(i));
			newVector.add(nf.format(listOfFrequencyOfEventClasses.get(i)));
			data.add(newVector);
		}
		// add the list, set it to non editable
		JTable table = new JTable(data, columnNames) {
			private static final long serialVersionUID = 4375770250193708533L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		DefaultTableCellRenderer tcrColumn = new DefaultTableCellRenderer();
		tcrColumn.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getColumnModel().getColumn(1).setCellRenderer(tcrColumn);
		return table;
	}

	private JPanel generateStatPanel(String property, double[] data, GlobalSettingsData globalSettingsData) {
		JPanel result = new JPanel();
		JTable table = StatisticTableGenerator.generateStatsTable(property, data, globalSettingsData);
		table.setPreferredScrollableViewportSize(new Dimension(300, Math.round((float) table.getPreferredSize()
				.getHeight())));

		JScrollPane scrollPane = new JScrollPane(table);
		result.add(scrollPane);
		result.setPreferredSize(new Dimension(410, 135));
		return result;
	}

	public void populateTextualInfoPanel() {
		if (textualInfoPanelTop.getComponentCount() > 0) {
			textualInfoPanelTop.removeAll();
		}

		// generate textual info panel
		TextualInfoPanelGenerator textualInfoPanelGenerator = new TextualInfoPanelGenerator();
		textualInfoPanelGenerator.addInfo("Selected Element*", fpdNode.getLabel());
		textualInfoPanelGenerator.addInfo("TIME UNIT", globalSettingsData.getTimeUnitLabel());

		textualInfoPanelGenerator.addInfo("AND-Join frequency", (fpdNode.getANDJOINrf() >= 0) ? nf.format(fpdNode
				.getANDJOINrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Activation Frequency", nf.format(fpdNode.getActivationFreq()));

		textualInfoPanelGenerator.addInfo("OR-Join frequency", (fpdNode.getORJOINrf() >= 0) ? nf.format(fpdNode
				.getORJOINrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Initialization Frequency", nf.format(fpdNode.getInitializationFreq()));

		textualInfoPanelGenerator.addInfo("XOR-Join frequency", (fpdNode.getXORJOINrf() >= 0) ? nf.format(fpdNode
				.getXORJOINrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Termination Frequency", nf.format(fpdNode.getTerminationFreq()));

		textualInfoPanelGenerator.addInfo("AND-Split frequency", (fpdNode.getANDSPLITrf() >= 0) ? nf.format(fpdNode
				.getANDSPLITrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Number of performer", nf.format(fpdNode.getNumberOfPerformer()));

		textualInfoPanelGenerator.addInfo("OR-Split frequency", (fpdNode.getORSPLITrf() >= 0) ? nf.format(fpdNode
				.getORSPLITrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Num. of cases with this node", nf.format(fpdNode.getFreqCaseHasThisNode()));

		textualInfoPanelGenerator.addInfo("XOR-Split frequency", (fpdNode.getXORSPLITrf() >= 0) ? nf.format(fpdNode
				.getXORSPLITrf()) : "Unavailable");
		textualInfoPanelGenerator.addInfo("Relative freq. in a case", nf.format((double) fpdNode
				.getAggregatedEventClassFreq()
				/ (double) fpdNode.getFreqCaseHasThisNode()));

		textualInfoPanelTop.add(textualInfoPanelGenerator.generatePanelInfo(7, 2, 10, 10, 400, 360, 90));
		textualInfoPanelTop.repaint();
	}

	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout) (bottomPanel.getLayout());
		cl.show(bottomPanel, (String) evt.getItem());
	}
}
