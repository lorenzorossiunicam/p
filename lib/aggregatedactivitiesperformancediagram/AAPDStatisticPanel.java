/**
 * 
 */
package org.processmining.plugins.aggregatedactivitiesperformancediagram;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.framework.util.Pair;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
public class AAPDStatisticPanel extends JPanel implements ItemListener {
	/*
	 * [HV] The serializable class AAPDStatisticPanel does not declare a static
	 * final serialVersionUID field of type long
	 */
	private static final long serialVersionUID = -7879305446474629599L;
	// constants
	public static final String THROUGHPUT_TIME = "Throughput time";
	public static final String START_TIME = "Start time";
	public static final String SERVICE_TIME = "Service time";
	public static final String WAITING_TIME = "Queuing time";
	public static final String INTERSECTION_TIME = "Intersection time";

	// parents panel
	private final AAPDInformationPanel parent;

	// for FrequencyPanel
	private JPanel relativeFrequencyMainPanel;
	private JLabel focusElementLbl;
	private JPanel relativeFrequencyPanel;
	private JLabel activeTimeUnit;

	// for KPI Panel
	private JPanel KPIPanel;
	private JPanel comboBoxPanel;
	private JPanel tablePanel;
	private JComboBox comboBox;

	// for MinBoundPerformanceIndicatorPanel
	private JPanel minBoundPerformanceIndicatorPanel;

	// buttons
	private JButton throughputButtonLow;
	private JButton throughputButtonHigh;

	// internal data
	private final AAPD aapd;
	private final GlobalSettingsData globalSettingsData;
	private AAPDElement aapdFocusElements;

	// utility
	private final NumberFormat nf;
	private final Font boldFont = new Font("Arial", Font.BOLD, 12);
	private final Font normalFont = new Font("Arial", Font.PLAIN, 12);

	public AAPDStatisticPanel(AAPDInformationPanel parent, AAPD aapd, GlobalSettingsData globalSettingsData,
			AAPDElement aapdFocusElements) {
		super();

		// set internal data
		this.aapd = aapd;
		this.globalSettingsData = globalSettingsData;
		this.parent = parent;

		// init utility
		nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(2);
		nf.setMaximumFractionDigits(2);

		// init panel
		initiateRelativeFrequencyPanel();
		initiateKPIPanel();
		initiateMinBoundPerformanceIndicatorPanel();

		// show the first aapdFocusElements
		this.aapdFocusElements = aapdFocusElements;
		showAAPDFocusElement(this.aapdFocusElements);
	}

	public void showAAPDFocusElement(AAPDElement aapdFocusElements) {
		this.aapdFocusElements = aapdFocusElements;

		// populate relativeFrequencyMainPanel
		populateRelativeFrequencyPanel();

		// populate statistics table
		populateKPIPanel();

		// populate minbound
		populateMinBoundPerformanceIndicatorPanel();
	}

	public void initiateRelativeFrequencyPanel() {
		relativeFrequencyMainPanel = new JPanel();
		relativeFrequencyMainPanel.setLayout(new BorderLayout());

		// initiate text
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());

		JLabel selectedAAPDFocusElementLbl = new JLabel("Focus Element");
		selectedAAPDFocusElementLbl.setFont(boldFont);
		textPanel.add(selectedAAPDFocusElementLbl, BorderLayout.NORTH);

		focusElementLbl = new JLabel();
		focusElementLbl.setFont(normalFont);
		textPanel.add(focusElementLbl, BorderLayout.CENTER);

		JPanel timeUnitPanel = new JPanel();
		timeUnitPanel.setLayout(new BorderLayout());

		JLabel activeTimeUnitDescLbl = new JLabel("TIME UNIT");
		activeTimeUnitDescLbl.setFont(boldFont);
		timeUnitPanel.add(activeTimeUnitDescLbl, BorderLayout.NORTH);

		activeTimeUnit = new JLabel(globalSettingsData.getTimeUnitLabel());
		activeTimeUnit.setFont(normalFont);
		timeUnitPanel.add(activeTimeUnit, BorderLayout.CENTER);

		JPanel selectedBarTextPanel = new JPanel();
		selectedBarTextPanel.setLayout(new BorderLayout());
		selectedBarTextPanel.add(textPanel, BorderLayout.WEST);
		selectedBarTextPanel.add(timeUnitPanel, BorderLayout.EAST);

		// initiate relativeFrequencyMainPanel
		relativeFrequencyPanel = new JPanel();

		// add to main Panel
		relativeFrequencyMainPanel.add(selectedBarTextPanel, BorderLayout.NORTH);
		relativeFrequencyMainPanel.add(relativeFrequencyPanel, BorderLayout.CENTER);

		add(relativeFrequencyMainPanel);
	}

	public void initiateKPIPanel() {
		KPIPanel = new JPanel();
		KPIPanel.setLayout(new BorderLayout());

		// add combobox
		String comboBoxItems[] = { THROUGHPUT_TIME, START_TIME, SERVICE_TIME, WAITING_TIME, INTERSECTION_TIME };
		comboBox = new JComboBox(comboBoxItems);
		comboBox.setEditable(false);
		comboBox.setSelectedIndex(0);
		comboBox.addItemListener(this);

		JLabel selectKPIlbl = new JLabel("Select KPI");

		comboBoxPanel = new JPanel();
		comboBoxPanel.add(selectKPIlbl);
		comboBoxPanel.add(comboBox);

		// table panel
		tablePanel = new JPanel();
		tablePanel.setLayout(new CardLayout());

		// add all to KPI panel
		KPIPanel.add(comboBoxPanel, BorderLayout.NORTH);
		KPIPanel.add(tablePanel, BorderLayout.CENTER);

		add(KPIPanel);
	}

	public void populateKPIPanel() {
		// changeable panel (using card layout)
		tablePanel.removeAll();
		tablePanel.add(generateStatPanel(AAPD.BAR_THROUGHPUT_TIME), THROUGHPUT_TIME);
		tablePanel.add(generateStatPanel(AAPD.BAR_START_TIME), START_TIME);
		tablePanel.add(generateStatPanel(AAPD.BAR_WAITING_TIME), WAITING_TIME);
		tablePanel.add(generateStatPanel(AAPD.BAR_SERVICE_TIME), SERVICE_TIME);
		tablePanel.add(generateStatPanel(AAPD.BAR_INTERSECTION_TIME), INTERSECTION_TIME);

		CardLayout cl = (CardLayout) tablePanel.getLayout();
		cl.show(tablePanel, (String) comboBox.getSelectedItem()); // set default view

		KPIPanel.setPreferredSize(new Dimension((int) tablePanel.getPreferredSize().getWidth(), (int) tablePanel
				.getPreferredSize().getHeight() + 50));
		KPIPanel.repaint();
	}

	private Component generateStatPanel(int statType) {
		JPanel result = new JPanel();
		JTable table = AAPDStatisticTableGenerator.generateStatsTable(statType, aapd.getRelationTime().get(
				aapdFocusElements), globalSettingsData, aapdFocusElements, aapd);
		table.setPreferredScrollableViewportSize(new Dimension(600, Math.round((float) table.getPreferredSize()
				.getHeight())));

		JScrollPane scrollPane = new JScrollPane(table);
		result.add(scrollPane);
		return result;
	}

	private void initiateMinBoundPerformanceIndicatorPanel() {
		minBoundPerformanceIndicatorPanel = new JPanel();
		minBoundPerformanceIndicatorPanel.setLayout(new GridLayout(3, 1));
		minBoundPerformanceIndicatorPanel.setPreferredSize(new Dimension(290, 110));

		// TOP PANEL
		JLabel minBoundPerformanceIndicatorPanelLabelTitle = new JLabel();
		minBoundPerformanceIndicatorPanelLabelTitle.setText("Max. bound for performance indicator**");

		// MIDDLE PANEL
		JPanel middlePanelMinBoundPerformanceIndicatorPanel = new JPanel();
		middlePanelMinBoundPerformanceIndicatorPanel.setLayout(new GridLayout(1, 3));

		// labels
		JLabel throughputLabel = new JLabel();
		throughputLabel.setText("Throughput time");

		throughputButtonLow = new JButton();
		throughputButtonLow.setBackground(Color.GREEN);

		throughputButtonHigh = new JButton();
		throughputButtonHigh.setBackground(Color.YELLOW);

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
		throughputButtonLow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object result = JOptionPane.showInputDialog(
						"Insert maximum bound for throughput time (good performance)", aapd.getBoundaries(
								aapdFocusElements).getFirst()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() > aapd.getBoundaries(aapdFocusElements)
								.getSecond()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for good performance cannot be higher than boundary for medium performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							aapd.setBoundaries(aapdFocusElements, new Pair<Double, Double>(newValue
									* globalSettingsData.getDividerValue(), aapd.getBoundaries(aapdFocusElements)
									.getSecond()));

							throughputButtonLow.setText(nf.format(newValue));
							throughputButtonLow.repaint();

							// refresh the figure
							parent.refreshModel();
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
						"Insert maximum bound for throughput time (medium performance)", aapd.getBoundaries(
								aapdFocusElements).getSecond()
								/ globalSettingsData.getDividerValue());
				if (result != null) {
					try {
						Double newValue = Double.parseDouble((String) result);
						if (newValue * globalSettingsData.getDividerValue() < aapd.getBoundaries(aapdFocusElements)
								.getFirst()) {
							JOptionPane
									.showMessageDialog(
											new JPanel(),
											"Boundary for medium performance cannot be lower than boundary for good performance",
											"Error", JOptionPane.ERROR_MESSAGE);
						} else {
							aapd.setBoundaries(aapdFocusElements, new Pair<Double, Double>(aapd.getBoundaries(
									aapdFocusElements).getFirst(), newValue * globalSettingsData.getDividerValue()));

							throughputButtonHigh.setText(nf.format(newValue));
							throughputButtonHigh.repaint();

							// refresh the figure
							parent.refreshModel();
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

	public void populateRelativeFrequencyPanel() {
		// set Text
		focusElementLbl.setText(aapdFocusElements.getLabel());

		// set table
		relativeFrequencyPanel.removeAll();
		JTable table = AAPDStatisticTableGenerator.generateRelativeFreqStatsTable(aapd.getFocusElementsCaseFrequency()
				.get(aapdFocusElements), aapd.getRelationTime().get(aapdFocusElements), globalSettingsData);
		table.setPreferredScrollableViewportSize(new Dimension(300, Math.round((float) table.getPreferredSize()
				.getHeight())));

		JScrollPane scrollPane = new JScrollPane(table);

		// add table
		relativeFrequencyPanel.add(scrollPane);

		relativeFrequencyMainPanel.repaint();
	}

	public void populateMinBoundPerformanceIndicatorPanel() {
		Pair<Double, Double> boundaries = aapd.getBoundaries(aapdFocusElements);
		throughputButtonLow.setText(nf.format(boundaries.getFirst() / globalSettingsData.getDividerValue()));
		throughputButtonHigh.setText(nf.format(boundaries.getSecond() / globalSettingsData.getDividerValue()));
		minBoundPerformanceIndicatorPanel.repaint();
	}

	public void itemStateChanged(ItemEvent e) {
		CardLayout cl = (CardLayout) tablePanel.getLayout();
		cl.show(tablePanel, (String) comboBox.getSelectedItem());
		KPIPanel.repaint();
	}

}
