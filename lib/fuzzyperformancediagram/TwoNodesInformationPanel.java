/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.framework.util.Pair;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.StatisticTableGenerator;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.TextualInfoPanelGenerator;
import org.processmining.plugins.performancemeasurement.util.BasicStatisticCalculator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 7, 2009
 */
public class TwoNodesInformationPanel extends JPanel {
	private static final long serialVersionUID = 2721879440778343095L;

	// panel parents reference to update graphs
	private final FPDInformationPanel fpdInformationPanel;

	// internal data
	private final TwoFPDNodesPerformanceData twoNodesPerformanceData;

	/**
	 * GUI elements
	 */
	// panel
	private final JPanel leftPanel;
	private final JPanel middlePanel;
	private final JPanel rightPanel;

	private final JButton calculateKPIButton;
	private final JRadioButton sourceNodeButton;
	private final JRadioButton targetNodeButton;
	private final JComboBox sourceNodeComboBox;
	private final JComboBox targetNodeComboBox;

	// utility
	private final GlobalSettingsData globalSettingsData;

	/**
	 * Constructor
	 * 
	 * @param informationPanel
	 * @param twoNodesPerformanceData
	 * @param globalSettingData
	 */
	public TwoNodesInformationPanel(FPDInformationPanel informationPanel,
			TwoFPDNodesPerformanceData twoNodesPerformanceData, GlobalSettingsData globalSettingsData) {
		fpdInformationPanel = informationPanel;

		// internal data/utility
		this.twoNodesPerformanceData = twoNodesPerformanceData;
		this.globalSettingsData = globalSettingsData;

		/**
		 * Left panel
		 */
		JLabel selectNodesLbl = new JLabel("Select source and target node*");
		sourceNodeButton = new JRadioButton("Select source node");
		targetNodeButton = new JRadioButton("Select target node");

		sourceNodeComboBox = new JComboBox();
		sourceNodeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				sourceNodeButton.setSelected(true);
			}
		});

		targetNodeComboBox = new JComboBox();
		targetNodeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				targetNodeButton.setSelected(true);
			}
		});

		for (FPDNode node : twoNodesPerformanceData.getPerformances().keySet()) {
			sourceNodeComboBox.addItem(node);
			targetNodeComboBox.addItem(node);
		}

		ButtonGroup nodeSelectionGroup = new ButtonGroup();
		nodeSelectionGroup.add(sourceNodeButton);
		nodeSelectionGroup.add(targetNodeButton);

		calculateKPIButton = new JButton("Calculate KPI");
		calculateKPIButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// update KPI performance
				showTwoNodesPerformance((FPDNode) sourceNodeComboBox.getSelectedItem(), (FPDNode) targetNodeComboBox
						.getSelectedItem());
			}
		});

		JLabel infoText = new JLabel(
				"<html>*First, click radio button to select source/target<br/> node.Then, click a node on the shown  model.  Or,<br/>use provided combo boxes</html>");

		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftPanel.add(selectNodesLbl, BorderLayout.NORTH);

		JPanel middleLeftPanel = new JPanel();
		middleLeftPanel.setLayout(new GridLayout(2, 2));
		middleLeftPanel.add(sourceNodeButton);
		middleLeftPanel.add(sourceNodeComboBox);
		middleLeftPanel.add(targetNodeButton);
		middleLeftPanel.add(targetNodeComboBox);

		leftPanel.add(middleLeftPanel, BorderLayout.CENTER);

		JPanel bottomLeftPanel = new JPanel();
		bottomLeftPanel.add(infoText);
		bottomLeftPanel.add(calculateKPIButton);

		leftPanel.add(bottomLeftPanel, BorderLayout.SOUTH);

		JScrollPane scrollPane = new JScrollPane(leftPanel);
		scrollPane.setPreferredSize(new Dimension(410, 150));
		add(scrollPane);

		/**
		 * middle panel
		 */
		middlePanel = new JPanel();
		add(middlePanel);

		/**
		 * right panel
		 */
		rightPanel = new JPanel();
		add(rightPanel);
	}

	/**
	 * Method to populate text information
	 * 
	 * @param pairFrequencyAndFittingCase
	 */
	private void populateTextualInfoPanel(Pair<Integer, Integer> pairFrequencyAndFittingCase) {
		if (middlePanel.getComponentCount() > 0) {
			middlePanel.removeAll();
		}

		// generate textual info panel
		TextualInfoPanelGenerator textualInfoPanelGenerator = new TextualInfoPanelGenerator();
		textualInfoPanelGenerator.addInfo("TIME UNIT", globalSettingsData.getTimeUnitLabel());
		textualInfoPanelGenerator.addInfo("Source-target pair frequency", String.valueOf(pairFrequencyAndFittingCase
				.getFirst()));
		textualInfoPanelGenerator.addInfo("Number of fitting case", String.valueOf(pairFrequencyAndFittingCase
				.getSecond()));

		middlePanel.add(textualInfoPanelGenerator.generatePanelInfo(3, 1, 10, 10, 250, 150, 90));
	}

	/**
	 * Method to populate KPI table
	 * 
	 * @param dataList
	 * @param globalSettingsData
	 */
	private void populateKPIPanel(List<Long> dataList, GlobalSettingsData globalSettingsData) {
		rightPanel.removeAll();

		double[] data = BasicStatisticCalculator.calculateBasicStatisticsLong(dataList, globalSettingsData
				.getFastestBoundPercentage(), globalSettingsData.getSlowestBoundPercentage());

		// populate KPI panel
		JTable table = StatisticTableGenerator.generateStatsTable("Sojourn time", data, globalSettingsData, true);
		table.setPreferredScrollableViewportSize(new Dimension(300, Math.round((float) table.getPreferredSize()
				.getHeight())));

		rightPanel.add(new JScrollPane(table));
	}

	/**
	 * Method to show elapsed time between the first occurrence of sourceNode
	 * instance and targetNode instance in all cases
	 * 
	 * @param sourceNode
	 * @param targetNode
	 */
	public void showTwoNodesPerformance(FPDNode sourceNode, FPDNode targetNode) {
		if (sourceNode.equals(targetNode)) {
			JOptionPane.showMessageDialog(this, "Please select two different nodes", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			// populate pairFrequency and fitting case for textual info
			Pair<Integer, Integer> pairFrequencyAndFittingCases = twoNodesPerformanceData
					.getPairFrequencyAndFittingCases(sourceNode, targetNode);

			if (pairFrequencyAndFittingCases != null) {
				populateTextualInfoPanel(pairFrequencyAndFittingCases);

				// populate KPI panel data
				populateKPIPanel(twoNodesPerformanceData.getTwoNodesPerformance(sourceNode, targetNode),
						globalSettingsData);

				repaint();
				fpdInformationPanel.repaint();
			} else {
				JOptionPane.showMessageDialog(this, "No case consist of both nodes together", "Information",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/**
	 * method to select a node using provided FPD
	 * 
	 * @param fpdNode
	 */
	public void selectNode(FPDNode fpdNode) {
		if (sourceNodeButton.isSelected()) {
			sourceNodeComboBox.setSelectedItem(fpdNode);
		} else {
			targetNodeComboBox.setSelectedItem(fpdNode);
		}
	}
}
