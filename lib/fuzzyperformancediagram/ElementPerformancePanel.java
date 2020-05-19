/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.plugins.performancemeasurement.util.BasicStatisticCalculator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 19, 2009
 */
public class ElementPerformancePanel extends JPanel {
	private static final long serialVersionUID = -5151470545361620808L;

	// static variable to refer panels
	public static final String NODEPERFORMANCE = "Node Performance Panel";
	public static final String EDGEPERFORMANCE = "Edge Performance Panel";

	// panel parents reference to update graphs
	private FPDInformationPanel fpdInformationPanel;

	// internal data
	private FPDElementPerformanceMeasurementData elementPerformanceMeasurementData;

	// "cache" internal data (both can be derived from elementPerformanceMeasurementData)
	private Map<FPDNode, List<double[]>> nodePerformanceStat;
	private Map<FPDEdge<? extends FPDNode, ? extends FPDNode>, double[]> edgePerformanceStat;

	// GUI element
	private NodePerformancePanel nodePerformancePanel;
	private EdgePerformancePanel edgePerformancePanel;

	public ElementPerformancePanel() {
	};

	public ElementPerformancePanel(FPDInformationPanel fpdInformationPanel,
			FPDElementPerformanceMeasurementData elementPerformanceMeasurementData,
			GlobalSettingsData globalSettingsData) {
		this.fpdInformationPanel = fpdInformationPanel;
		nodePerformancePanel = new NodePerformancePanel(this, globalSettingsData);
		edgePerformancePanel = new EdgePerformancePanel(this, globalSettingsData);
		this.elementPerformanceMeasurementData = elementPerformanceMeasurementData;

		// init internal data
		nodePerformanceStat = new HashMap<FPDNode, List<double[]>>();
		edgePerformanceStat = new HashMap<FPDEdge<? extends FPDNode, ? extends FPDNode>, double[]>();

		setLayout(new CardLayout());
		add(nodePerformancePanel, NODEPERFORMANCE);
		add(edgePerformancePanel, EDGEPERFORMANCE);

		// pre-calculate statistical performance
		updateStatisticWithBoundary(globalSettingsData);

		// for the beginning, show first node
		FPDNode initNode = elementPerformanceMeasurementData.getNodePerformance().keySet().iterator().next();
		showNode(initNode, globalSettingsData);
	}

	public void updateStatisticWithBoundary(GlobalSettingsData globalSettingsData) {
		// node
		for (FPDNode node : elementPerformanceMeasurementData.getNodePerformance().keySet()) {
			List<double[]> listOfPerformance = new LinkedList<double[]>();
			// calculate performance
			List<Double> tempWaitingStat = elementPerformanceMeasurementData.getNodePerformance().get(node).get(
					FPDElementPerformanceMeasurementData.WAITINGTIME);
			if (tempWaitingStat.size() > 0) {
				double[] waitingStat = BasicStatisticCalculator.calculateBasicStatisticsDouble(tempWaitingStat,
						globalSettingsData.getFastestBoundPercentage(), globalSettingsData.getSlowestBoundPercentage());
				listOfPerformance.add(waitingStat);
			} else {
				double waitingStat[] = { BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE };
				listOfPerformance.add(waitingStat);
			}

			List<Double> tempSynchronizationStat = elementPerformanceMeasurementData.getNodePerformance().get(node)
					.get(FPDElementPerformanceMeasurementData.SYNCHRONIZATIONTIME);
			if (tempSynchronizationStat.size() > 0) {
				double[] synchronizationStat = BasicStatisticCalculator.calculateBasicStatisticsDouble(
						tempSynchronizationStat, globalSettingsData.getFastestBoundPercentage(), globalSettingsData
								.getSlowestBoundPercentage());
				listOfPerformance.add(synchronizationStat);
			} else {
				double synchronizationStat[] = { BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE };
				listOfPerformance.add(synchronizationStat);
			}

			List<Double> tempThroughputStat = elementPerformanceMeasurementData.getNodePerformance().get(node).get(
					FPDElementPerformanceMeasurementData.THROUGHPUTTIME);
			if (tempThroughputStat.size() > 0) {
				double[] throughputStat = BasicStatisticCalculator.calculateBasicStatisticsDouble(tempThroughputStat,
						globalSettingsData.getFastestBoundPercentage(), globalSettingsData.getSlowestBoundPercentage());
				listOfPerformance.add(throughputStat);
			} else {
				double[] throughputStat = { BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE };
				listOfPerformance.add(throughputStat);
			}

			nodePerformanceStat.put(node, listOfPerformance);
		}
		// edge
		for (FPDEdge<? extends FPDNode, ? extends FPDNode> edge : elementPerformanceMeasurementData
				.getEdgePerformance().keySet()) {
			List<Double> tempEdgeStat = elementPerformanceMeasurementData.getEdgePerformance().get(edge);
			if (tempEdgeStat.size() > 0) {
				double[] edgeStat = BasicStatisticCalculator.calculateBasicStatisticsDouble(tempEdgeStat,
						globalSettingsData.getFastestBoundPercentage(), globalSettingsData.getSlowestBoundPercentage());
				edgePerformanceStat.put(edge, edgeStat);
			} else {
				double[] edgeStat = { BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE, BasicStatisticCalculator.UNAVAILABLE,
						BasicStatisticCalculator.UNAVAILABLE };
				edgePerformanceStat.put(edge, edgeStat);
			}
		}
	}

	public void showNode(FPDNode fpdNode, GlobalSettingsData globalSettingsData) {
		remove(nodePerformancePanel);

		// set so that it focuses on a node 
		nodePerformancePanel.populateData(fpdNode, nodePerformanceStat.get(fpdNode), globalSettingsData);
		add(nodePerformancePanel, NODEPERFORMANCE);

		CardLayout cl = (CardLayout) getLayout();
		cl.show(this, NODEPERFORMANCE);

		repaint();
	}

	public void showEdge(FPDEdge<FPDNode, FPDNode> fpdEdge, GlobalSettingsData globalSettingsData) {
		remove(edgePerformancePanel);

		// set so that it focuses on an edge
		edgePerformancePanel.populateData(fpdEdge, edgePerformanceStat.get(fpdEdge), globalSettingsData);
		add(edgePerformancePanel, EDGEPERFORMANCE);

		CardLayout cl = (CardLayout) getLayout();
		cl.show(this, EDGEPERFORMANCE);

		repaint();
	}

	public void refreshModel() {
		fpdInformationPanel.refreshModel();
	}
}
