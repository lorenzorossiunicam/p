/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.fuzzyperformancediagram.FPD;
import org.processmining.models.fuzzyperformancediagram.FPDEdge;
import org.processmining.models.fuzzyperformancediagram.FPDNode;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.AccumulatedFPDNodeInstanceBars;
import org.processmining.models.performancemeasurement.dataelements.CaseKPIData;
import org.processmining.models.performancemeasurement.dataelements.FPDElementPerformanceMeasurementData;
import org.processmining.models.performancemeasurement.dataelements.TwoFPDNodesPerformanceData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Mar 23, 2009
 */
public class FPDInformationPanel extends JPanel {
	private static final long serialVersionUID = -5835963422452017013L;

	// general GUI components
	protected FPD net;
	protected Progress progress;

	protected ProMJGraphPanel graphVisPanel;
	protected JTabbedPane southTabs;
	protected ViewSpecificAttributeMap viewSpecificMap;
	protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

	// Internal Data Structure
	private GlobalSettingsData globalSettingData;
	private CaseKPIData caseKPIData;
	private FPDElementPerformanceMeasurementData elementPerformanceMeasurementData;
	private AccumulatedFPDNodeInstanceBars accumulatedActivityInstances;
	private TwoFPDNodesPerformanceData twoNodesPerformanceData;

	// GUI Components
	private CaseKPIInformationPanel caseKPIInformationPanel;
	private ElementPerformancePanel elementPerformancePanel;
	private TwoNodesInformationPanel twoNodeInformationPanel;

	/**
	 * Constructor of both Editor panel and mapping panel
	 * 
	 * @param graph
	 * @param progress
	 * @param availableMappingsSet
	 */
	public FPDInformationPanel(PluginContext context, FPD graph, Progress progress, GlobalSettingsData globalSettingsData) {
		// General layout
		setLayout(new BorderLayout());

		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		viewSpecificMap = new ViewSpecificAttributeMap();
		this.progress = progress;
		net = graph;
		globalSettingData = globalSettingsData;

		// update element's divider according to globalSettingsData
		for (FPDNode node : net.getNodes()) {
			node.setDivider(globalSettingsData.getDividerValue());
		}
		for (FPDEdge<? extends FPDNode, ? extends FPDNode> edge : net.getEdges()) {
			edge.setDivider(globalSettingsData.getDividerValue());
		}

		// visualize graph
		graphVisPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, net, viewSpecificMap);
		splitPane.setLeftComponent(graphVisPanel);

		southTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		splitPane.setRightComponent(southTabs);

		this.add(splitPane, BorderLayout.CENTER);

		// set the action whenever a node in the graph is selected
		GraphSelectionModel model = graphVisPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {

			@SuppressWarnings("unchecked")
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							ProMGraphCell node = (ProMGraphCell) cell;
							FPDNode fpdNode = (FPDNode) node.getNode();
							// System.out.println(fpdNode.getInfo());
							elementPerformancePanel.showNode(fpdNode, globalSettingData);

							// select element in twoNodeInformationPanel based
							// on currently checked button
							twoNodeInformationPanel.selectNode(fpdNode);
						} else {
							if (cell instanceof ProMGraphEdge) {
								ProMGraphEdge edge = (ProMGraphEdge) cell;
								FPDEdge<FPDNode, FPDNode> fpdEdge = (FPDEdge<FPDNode, FPDNode>) edge.getEdge();
								// System.out.println(fpdEdge.getInfo());
								if (!twoNodeInformationPanel.isShowing()) {
									elementPerformancePanel.showEdge(fpdEdge, globalSettingData);
								}
							}
						}
					}
				}

			}
		});
	}

	/**
	 * @return the twoNodesPerformanceData
	 */
	public TwoFPDNodesPerformanceData getTwoNodesPerformanceData() {
		return twoNodesPerformanceData;
	}

	/**
	 * @return the globalSettingData
	 */
	public GlobalSettingsData getGlobalSettingData() {
		return globalSettingData;
	}

	/**
	 * @param globalSettingData
	 *            the globalSettingData to set
	 */
	public void setGlobalSettingData(GlobalSettingsData globalSettingData) {
		this.globalSettingData = globalSettingData;
	}

	/**
	 * @return the caseKPIData
	 */
	public CaseKPIData getCaseKPIData() {
		return caseKPIData;
	}

	/**
	 * @param caseKPIData
	 *            the caseKPIData to set
	 */
	public void setCaseKPIData(CaseKPIData caseKPIData) {
		this.caseKPIData = caseKPIData;
	}

	/**
	 * @return the elementPerformanceMeasurementData
	 */
	public FPDElementPerformanceMeasurementData getElementPerformanceMeasurementData() {
		return elementPerformanceMeasurementData;
	}

	/**
	 * @param elementPerformanceMeasurementData
	 *            the elementPerformanceMeasurementData to set
	 */
	public void setElementPerformanceMeasurementData(
			FPDElementPerformanceMeasurementData elementPerformanceMeasurementData) {
		this.elementPerformanceMeasurementData = elementPerformanceMeasurementData;
	}

	/**
	 * @param twoNodesPerformanceData
	 *            the twoNodesPerformanceData to set
	 */
	public void setTwoNodesPerformanceData(TwoFPDNodesPerformanceData twoNodesPerformanceData) {
		this.twoNodesPerformanceData = twoNodesPerformanceData;
	}

	/**
	 * @return the accumulatedActivityInstances
	 */
	public AccumulatedFPDNodeInstanceBars getAccumulatedActivityInstances() {
		return accumulatedActivityInstances;
	}

	/**
	 * @param accumulatedActivityInstances
	 *            the accumulatedActivityInstances to set
	 */
	public void setAccumulatedActivityInstances(AccumulatedFPDNodeInstanceBars accumulatedActivityInstances) {
		this.accumulatedActivityInstances = accumulatedActivityInstances;
	}

	public void generateAllPanel() {
		caseKPIInformationPanel = new CaseKPIInformationPanel(caseKPIData, globalSettingData);
		southTabs.addTab("<html>Case KPI</html>", new JScrollPane(caseKPIInformationPanel));

		elementPerformancePanel = new ElementPerformancePanel(this, elementPerformanceMeasurementData,
				globalSettingData);
		southTabs.addTab("<html>Element Performance</html>", new JScrollPane(elementPerformancePanel));

		twoNodeInformationPanel = new TwoNodesInformationPanel(this, twoNodesPerformanceData, globalSettingData);
		southTabs.addTab("<html>Two Nodes Performance</html>", new JScrollPane(twoNodeInformationPanel));

	}

	public void refreshModel() {
		// BVD: Not sure why this line is there. The net didn't change, so why update the visualization thereof?
		//graphVisPanel = ProMJGraphVisualizer.visualizeGraph(net, viewSpecificMap);
		splitPane.setLeftComponent(graphVisPanel);
		// set the action whenever a node in the graph is selected
		GraphSelectionModel model = graphVisPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {

			@SuppressWarnings("unchecked")
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							ProMGraphCell node = (ProMGraphCell) cell;
							FPDNode fpdNode = (FPDNode) node.getNode();
							elementPerformancePanel.showNode(fpdNode, globalSettingData);
						} else {
							if (cell instanceof ProMGraphEdge) {
								ProMGraphEdge edge = (ProMGraphEdge) cell;
								FPDEdge<FPDNode, FPDNode> fpdEdge = (FPDEdge<FPDNode, FPDNode>) edge.getEdge();
								elementPerformancePanel.showEdge(fpdEdge, globalSettingData);
							}
						}
					}
				}

			}
		});

		// signal the change and repaint graph
		repaint();
	}
}
