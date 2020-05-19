/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which are not
 * licensed under the terms of the GPL, given that they satisfy one or more of
 * the following conditions: 1) Explicit license is granted to the ProM and
 * ProMimport programs for usage, linking, and derivative work. 2) Carte blance
 * license is granted to all programs developed at Eindhoven Technical
 * University, The Netherlands, or under the umbrella of STW Technology
 * Foundation, The Netherlands. For further exemptions not covered by the above
 * conditions, please contact the author of this code.
 */
package org.processmining.plugins.fuzzymodel.miner.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XLog;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.BestEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.ConcurrencyEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FastTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.FuzzyEdgeTransformer;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMColors;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.fuzzymap.util.FuzzyMapPatternNode;
import org.processmining.plugins.fuzzymodel.miner.FuzzyMinerPlugin;
import org.processmining.plugins.fuzzymodel.miner.replay.FuzzyDetailAnalysis;
import org.processmining.plugins.fuzzymodel.miner.replay.FuzzyReplay;
import org.processmining.plugins.log.logabstraction.LogRelations;

import com.fluxicon.slickerbox.components.InspectorButton;
import com.fluxicon.slickerbox.components.StackedCardsTabbedPane;
import com.fluxicon.slickerbox.ui.SlickerCheckBoxUI;
import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * @author Jiafei Li (jiafei@jlu.edu.cn)
 */
public class FastTransformerPanel extends JPanel implements ChangeListener, ItemListener { //, Provider { if it is necessary, we implement Provider

	/**
	 * 
	 */
	private static final long serialVersionUID = 4221149364708440299L;
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String EDGE_TRANSFORMER_SELECTION = "EdgeTransformerSelection";
	public static final String EDGE_TRANSFORMER_SELECTION_BEST_EDGES = "EdgeTransformerSelectionBestEdges";
	public static final String EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES = "EdgeTransformerSelectionFuzzyEdges";
	public static final String CONCURRENCY_EDGE_TRANSFORMER_ACTIVE = "ConcurrencyEdgeTransformerActive";
	public static final String NODE_CUTOFF = "NodeCutoff";
	public static final String FUZZY_EDGE_RATIO = "FuzzyEdgeRatio";
	public static final String FUZZY_EDGE_CUTOFF = "FuzzyEdgeCutoff";
	public static final String CONCURRENCY_THRESHOLD = "ConcurrencyThreshold";
	public static final String CONCURRENCY_RATIO = "ConcurrencyRatio";
	protected static final String EDGES_FUZZY_IGNORE_LOOPS = "EdgesFuzzyIgnoreLoops";
	protected static final String EDGES_FUZZY_INTERPRET_ABSOLUTE = "EdgesFuzzyInterpretAbsolute";
	protected static final String FILE_SEPERATOR = File.separator;
	protected static final String FILE_LOCATION_LOG_ATT_KEY = "fileLocation";
	protected static final String PARENT_FILE_NAME_LOG_ATT_KEY = "parentFileName";

	protected XLog log = null;
	protected MutableFuzzyGraph graph;
	protected ProMJGraph jgraph;
	protected MutableFuzzyGraph resultGraph;
	protected ProMJGraph clusterGraph;
	protected ProMJGraph molecularGraph;
	protected PluginContext context;
	protected FastTransformer fastTransformer;
	protected BestEdgeTransformer bestEdgeTransformer;
	protected FuzzyEdgeTransformer fuzzyEdgeTransformer;
	protected ConcurrencyEdgeTransformer concurrencyEdgeTransformer;

	protected FuzzyReplay replay;
	protected FuzzyDetailAnalysis detail;
	protected ReplayView replayView;
	protected DetailView detailView;

	//	protected SidePanel sidePanel;
	//JF add for show the exported Fuzzy Graph object only
	protected JPanel rightPanel;
	protected boolean enableRedraw;

	protected JPanel rootPanel;
	protected ProMJGraphPanel graphPanel;
	protected ProMJGraphPanel clustGraphPanel;
	protected ProMJGraphPanel patternGraphPanel;
	//	protected ProMJGraphPanel molecularGraphPanel;
	//	protected ViewSpecificAttributeMap viewSpecificMap;
	protected JRadioButton edgesBestRadioButton;
	protected JRadioButton edgesFuzzyRadioButton;
	protected JSlider nodeSignificanceSlider;
	protected JSlider edgesFuzzyRatioSlider;
	protected JSlider edgesFuzzyPercentageSlider;
	protected JSlider edgesConcurrencyThresholdSlider;
	protected JSlider edgesConcurrencyRatioSlider;
	protected JLabel nodeSignificanceLabel;
	protected JLabel edgesFuzzyRatioLabel;
	protected JLabel edgesFuzzyPercentageLabel;
	protected JLabel edgesConcurrencyThresholdLabel;
	protected JLabel edgesConcurrencyRatioLabel;
	protected JCheckBox edgesFuzzyIgnoreLoopBox;
	protected JCheckBox edgesFuzzyInterpretAbsoluteBox;
	protected JCheckBox edgesConcurrencyActiveBox;
	protected LedGauge replayGauge;
	protected LedGauge detailGauge;
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	protected Integer timeToFormTranformerPanel = 0;
	protected HashSet<File> patternLogDirectorySet = new HashSet<File>();
	//	protected File patternLogDirectory;
	//to distinguish whether the log is a transformed log or not
	protected boolean isPatternBasedTransformedLog = false;

	// HV
	private Map<FMNode, JFrame> clusterFrames;
	//ljf
	private Map<FMNode, JFrame> molecularFrames;

	protected LogRelations molecularInnerRelations;

	/*
	 * JLi-Export Start public MutableFuzzyGraph getExportFuzzyGraphObjects()
	 * throws Exception { //add every cluster node and its corresponding graph
	 * MutableFuzzyGraph exportGraph = (MutableFuzzyGraph) graph.clone();
	 * 
	 * // when getting the cluster's inner graph and the molecular activity's
	 * graph, // the color of the nodes in the original whole graph are changed
	 * for (FMClusterNode clusternode : exportGraph.getClusterNodes()) {
	 * MutableFuzzyGraph clusterGraph = (MutableFuzzyGraph)
	 * (clusternode.getClusterGraphPanel(context, log)
	 * .getGraph().getProMGraph());
	 * exportGraph.getAbstractionNodeGraphMap().put(clusternode, clusterGraph);
	 * } //add every molecule node and its inner graph, no matter which
	 * hierarchical level this node is located // int size =
	 * exportGraph.getNumberOfInitialNodes(); // for(int i = 0; i<size;i++){ //
	 * FMNode node = exportGraph.getPrimitiveNode(i); // MutableFuzzyGraph
	 * innerGraph = null; // innerGraph = getMolecularGraph(node); //
	 * if(innerGraph!=null){ //
	 * exportGraph.getAbstractionNodeGraphMap().put(node, innerGraph); // } // }
	 * Vector<MutableFuzzyGraph> graphSet = new Vector<MutableFuzzyGraph>();
	 * graphSet.add(exportGraph); /* Iterator<MutableFuzzyGraph> graphIterator =
	 * graphSet.iterator(); while(graphIterator.hasNext()) {
	 */
	/*
	 * for (int j = 0; j < graphSet.size(); j++) { MutableFuzzyGraph curGraph =
	 * graphSet.get(j); int size = curGraph.getNumberOfInitialNodes(); for (int
	 * i = 0; i < size; i++) { FMNode node = curGraph.getPrimitiveNode(i);
	 * MutableFuzzyGraph innerGraph = null; innerGraph =
	 * getMolecularGraph(node); if (innerGraph != null) {
	 * exportGraph.getAbstractionNodeGraphMap().put(node, innerGraph);
	 * graphSet.add(innerGraph); } } }
	 * 
	 * Set<FMNode> originalNodes = exportGraph.getNodes(); //set the color of
	 * the nodes in the whole graph back for (FMNode node : originalNodes) { if
	 * (node instanceof FMClusterNode) {
	 * node.getAttributeMap().put(AttributeMap.FILLCOLOR,
	 * FMColors.getClusterBackgroundColor()); } else if
	 * (exportGraph.getAbstractionNodeGraphMap().containsKey(node)) { //if the
	 * node is an abstract node
	 * node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
	 * node.getAttributeMap().put(AttributeMap.FILLCOLOR,
	 * FMColors.getAbstractBackgroundColor()); } else {
	 * node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
	 * node.getAttributeMap().put(AttributeMap.FILLCOLOR,
	 * FMColors.getPrimitiveBackgroundColor()); } } //when visulize the
	 * exportGraph, there is no unary metrics panel and no binary metrics panel
	 * exportGraph.setMetrics(null); return exportGraph; }
	 */
	// JLi-Export End

	// JC/SR Export Start
	//Added by JC/SR
	public MutableFuzzyGraph getExportFuzzyGraphObjects() throws Exception {
		//add every cluster node and its corresponding graph 
		System.out.println("graph edges " + graph.getEdges());
		MutableFuzzyGraph exportGraph = (MutableFuzzyGraph) graph.clone();

		// when getting the cluster's inner graph and the molecular activity's graph, 
		// the color of the nodes in the original whole graph are changed 
		for (FMClusterNode clusternode : exportGraph.getClusterNodes()) {
			MutableFuzzyGraph clusterGraph = (MutableFuzzyGraph) (clusternode.getClusterGraphPanel(context, log)
					.getGraph().getProMGraph());
			exportGraph.getAbstractionNodeGraphMap().put(clusternode, clusterGraph);
			getExportedClusterFuzzyGraphObject(clusterGraph, clusternode);
		}
		//add every molecule node and its inner graph, no matter which hierarchical level this node is located 
		for (FMNode node : exportGraph.getNodes()) {
			MutableFuzzyGraph innerGraph = null;
			innerGraph = getMolecularGraph(node);
			if (innerGraph != null) {
				exportGraph.getAbstractionNodeGraphMap().put(node, innerGraph);
				getExportedFuzzyGraphObject(innerGraph);
			}
		}

		Set<FMNode> originalNodes = exportGraph.getNodes();
		//set the color of the nodes in the whole graph back
		for (FMNode node : originalNodes) {
			if (node instanceof FMClusterNode) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, new Color(102, 153, 255));
			} else if (exportGraph.getAbstractionNodeGraphMap().containsKey(node)) {
				//if the node is an abstract node
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, new Color(102, 153, 255));
			} else {
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
			}
		}
		//when visulize the exportGraph, there is no unary metrics panel and no binary metrics panel
		exportGraph.setMetrics(null);
		return exportGraph;
	}

	//end JC/SR 

	//Added by JC/SR
	public void getExportedClusterFuzzyGraphObject(MutableFuzzyGraph innerGraph, FMClusterNode clusterNode) {
		boolean hasSubGraph = false;
		Set<FMNode> clusterPrimitiveNodesSet = clusterNode.getPrimitives();
		for (FMNode node : innerGraph.getNodes()) {
			if (clusterPrimitiveNodesSet.contains(node)) {
				MutableFuzzyGraph graph = getMolecularGraph(node);
				if (graph != null) {
					innerGraph.getAbstractionNodeGraphMap().put(node, graph);
					if (node instanceof FMClusterNode)
						getExportedClusterFuzzyGraphObject(graph, (FMClusterNode) node);
					else
						getExportedFuzzyGraphObject(graph);
					hasSubGraph = true;
				}
			}
		}
		if (!hasSubGraph)
			return;
	}

	//end JC/SR

	//Added by JC/SR
	public void getExportedFuzzyGraphObject(MutableFuzzyGraph innerGraph) {
		boolean hasSubGraph = false;
		for (int i = 0; i < innerGraph.getNumberOfInitialNodes(); i++) {
			FMNode node = innerGraph.getPrimitiveNode(i);
			MutableFuzzyGraph graph = getMolecularGraph(node);
			if (graph != null) {
				innerGraph.getAbstractionNodeGraphMap().put(node, graph);
				getExportedFuzzyGraphObject(graph);
				hasSubGraph = true;
			}
		}
		if (!hasSubGraph)
			return;
	}

	//end JC/SR

	// JC/SR Export End

	public FastTransformerPanel(PluginContext context, MetricsRepository metrics) {
		this(context, new MutableFuzzyGraph(metrics), metrics.getLogReader(), metrics.getNumberOfLogEvents(),
				SwingConstants.NORTH);
	}

	public FastTransformerPanel(PluginContext context, MetricsRepository metrics, int orientation) {
		this(context, new MutableFuzzyGraph(metrics), metrics.getLogReader(), metrics.getNumberOfLogEvents(),
				orientation);
	}

	public FastTransformerPanel(PluginContext context, MetricsRepository metrics, XLog log, int showNumberOfNodes) {
		this(context, new MutableFuzzyGraph(metrics), log, showNumberOfNodes, SwingConstants.NORTH);
	}

	public FastTransformerPanel(PluginContext context, MutableFuzzyGraph graph) {
		this(context, graph, graph.getLog(), graph.getLogEvents().size(), SwingConstants.NORTH);
	}

	public FastTransformerPanel(PluginContext context, MutableFuzzyGraph graph, XLog log, int showNumberOfNodes,
			int orientation) {
		this.log = log;
		this.graph = graph;
		this.context = context;
		this.setBackground(new Color(240, 240, 240));
		graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, orientation);
		fastTransformer = new FastTransformer(context);
		bestEdgeTransformer = new BestEdgeTransformer(context);
		fuzzyEdgeTransformer = new FuzzyEdgeTransformer(context);
		concurrencyEdgeTransformer = new ConcurrencyEdgeTransformer(context);
		enableRedraw = false;
		//if this log has the attribute of "location", then it is a pattern-based transformed log or not
		if (log.getAttributes().containsKey(FILE_LOCATION_LOG_ATT_KEY)
				&& log.getAttributes().containsKey(PARENT_FILE_NAME_LOG_ATT_KEY)) {
			isPatternBasedTransformedLog = true;
		}
		//To use the JGraph  to show the FuzzyGraph
		initializeGui();
		setGuiToInitialValues(showNumberOfNodes);
		readGraphAttributes();
		enableRedraw = true;
		timeToFormTranformerPanel++;
		redrawGraph(false); //Set the Fuzzy Graph
		// HV
		clusterFrames = new HashMap<FMNode, JFrame>();
		//ljf
		molecularFrames = new HashMap<FMNode, JFrame>();

	}

	public FuzzyGraph getGraph() {
		return graph;
	}

	protected boolean readGraphAttributes() {
		String edgeTransformerSelection = graph.getAttribute(EDGE_TRANSFORMER_SELECTION);
		if (edgeTransformerSelection != null) {
			if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_BEST_EDGES)) {
				edgesBestRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(bestEdgeTransformer);
			} else if (edgeTransformerSelection.equalsIgnoreCase(EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES)) {
				edgesFuzzyRadioButton.setSelected(true);
				fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			}
		}
		String concurrencyTransformerActive = graph.getAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE);
		if (concurrencyTransformerActive != null) {
			if (concurrencyTransformerActive.equals(TRUE)) {
				edgesConcurrencyActiveBox.setSelected(true);
				fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			} else if (concurrencyTransformerActive.equals(FALSE)) {
				edgesConcurrencyActiveBox.setSelected(false);
				fastTransformer.removePreTransformer(concurrencyEdgeTransformer);
			}
		}
		String nodeCutoff = graph.getAttribute(NODE_CUTOFF);
		if (nodeCutoff != null) {
			double nodeThreshold = Double.parseDouble(nodeCutoff);
			nodeSignificanceSlider.setValue((int) (nodeThreshold * 1000.0));
			nodeSignificanceLabel.setText(MutableFuzzyGraph.format(nodeThreshold));
			fastTransformer.setThreshold(nodeThreshold);
		}
		String fuzzyEdgeRatio = graph.getAttribute(FUZZY_EDGE_RATIO);
		if (fuzzyEdgeRatio != null) {
			double fuzzyERatio = Double.parseDouble(fuzzyEdgeRatio);
			edgesFuzzyRatioSlider.setValue((int) (fuzzyERatio * 1000.0));
			edgesFuzzyRatioLabel.setText(MutableFuzzyGraph.format(fuzzyERatio));
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(fuzzyERatio);
		}
		String fuzzyEdgeCutoff = graph.getAttribute(FUZZY_EDGE_CUTOFF);
		if (fuzzyEdgeCutoff != null) {
			double fuzzyECutoff = Double.parseDouble(fuzzyEdgeCutoff);
			edgesFuzzyPercentageSlider.setValue((int) (fuzzyECutoff * 1000.0));
			edgesFuzzyPercentageLabel.setText(MutableFuzzyGraph.format(fuzzyECutoff));
			fuzzyEdgeTransformer.setPreservePercentage(fuzzyECutoff);
		}
		String concurrencyThreshold = graph.getAttribute(CONCURRENCY_THRESHOLD);
		if (concurrencyThreshold != null) {
			double threshold = Double.parseDouble(concurrencyThreshold);
			edgesConcurrencyThresholdSlider.setValue((int) (threshold * 1000.0));
			edgesConcurrencyThresholdLabel.setText(MutableFuzzyGraph.format(threshold));
			concurrencyEdgeTransformer.setPreserveThreshold(threshold);
		}
		String concurrencyRatio = graph.getAttribute(CONCURRENCY_RATIO);
		if (concurrencyRatio != null) {
			double ratio = Double.parseDouble(concurrencyRatio);
			edgesConcurrencyRatioSlider.setValue((int) (ratio * 1000.0));
			edgesConcurrencyRatioLabel.setText(MutableFuzzyGraph.format(ratio));
			concurrencyEdgeTransformer.setRatioThreshold(ratio);
		}
		String ignoreLoops = graph.getAttribute(EDGES_FUZZY_IGNORE_LOOPS);
		if (ignoreLoops != null) {
			if (ignoreLoops.equals(TRUE)) {
				edgesFuzzyIgnoreLoopBox.setSelected(true);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			} else if (ignoreLoops.equals(FALSE)) {
				edgesFuzzyIgnoreLoopBox.setSelected(false);
				fuzzyEdgeTransformer.setIgnoreSelfLoops(false);
			}
		}
		String interpretAbsolute = graph.getAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE);
		if (interpretAbsolute != null) {
			if (interpretAbsolute.equals(TRUE)) {
				edgesFuzzyInterpretAbsoluteBox.setSelected(true);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(true);
			} else if (interpretAbsolute.equals(FALSE)) {
				edgesFuzzyInterpretAbsoluteBox.setSelected(false);
				fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			}
		}
		return false;
	}

	protected void initializeGui() {
		// derive standard control element font
		this.smallFont = this.getFont().deriveFont(11f);
		// root panel
		rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(new Color(100, 100, 100));
		rootPanel.setLayout(new BorderLayout());
		// upper node filter panel
		JPanel upperControlPanel = new JPanel();
		upperControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		upperControlPanel.setBackground(COLOR_BG2);
		upperControlPanel.setOpaque(true);
		upperControlPanel.setLayout(new BorderLayout());
		JLabel nodeSigSliderLabel = new JLabel("Significance cutoff");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		upperControlPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
		nodeSignificanceLabel = new JLabel("0.000");
		nodeSignificanceLabel.setOpaque(false);
		nodeSignificanceLabel.setForeground(COLOR_FG);
		nodeSignificanceLabel.setFont(this.smallFont);
		centerHorizontally(nodeSignificanceLabel);
		upperControlPanel.add(packVerticallyCentered(nodeSignificanceLabel, 50, 20), BorderLayout.SOUTH);
		nodeSignificanceSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		nodeSignificanceSlider.setUI(new SlickerSliderUI(nodeSignificanceSlider));
		nodeSignificanceSlider.addChangeListener(this);
		nodeSignificanceSlider.setOpaque(false);
		nodeSignificanceSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown as single activities,<br>" + "increasing the detail and complexity<br>"
				+ "of the model.</html>");
		upperControlPanel.add(nodeSignificanceSlider, BorderLayout.CENTER);
		//start  of the "Edge filter" panel
		// lower edge transformer panel
		JPanel lowerControlPanel = new JPanel(); // lowerControlPanel is the Edge filter panel
		lowerControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		lowerControlPanel.setBackground(COLOR_BG2);
		lowerControlPanel.setOpaque(true);
		lowerControlPanel.setLayout(new BorderLayout());
		// lower header panel (radio buttons etc.)
		JPanel lowerHeaderPanel = new JPanel();
		lowerHeaderPanel.setOpaque(false);
		lowerHeaderPanel.setLayout(new BoxLayout(lowerHeaderPanel, BoxLayout.Y_AXIS));
		JLabel lowerHeaderLabel = new JLabel("Edge transformer");
		lowerHeaderLabel.setOpaque(false);
		lowerHeaderLabel.setForeground(COLOR_FG);
		lowerHeaderLabel.setFont(this.smallFont);
		//centerHorizontally(lowerHeaderLabel);
		edgesBestRadioButton = new JRadioButton("Best edges");
		edgesBestRadioButton.setUI(new SlickerRadioButtonUI());
		edgesBestRadioButton.setFont(this.smallFont);
		edgesBestRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
		edgesBestRadioButton.setOpaque(false);
		edgesBestRadioButton.setForeground(COLOR_FG);
		edgesBestRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		edgesBestRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		edgesBestRadioButton.addItemListener(this);
		edgesBestRadioButton.setToolTipText("<html>Activates the 'Best edges'<br>"
				+ "edge filtering strategy, which<br>" + "preserves for each node the two most<br>"
				+ "significant connections.</html>");
		edgesFuzzyRadioButton = new JRadioButton("Fuzzy edges");
		edgesFuzzyRadioButton.setUI(new SlickerRadioButtonUI());
		edgesFuzzyRadioButton.setFont(this.smallFont);
		edgesFuzzyRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 2));
		edgesFuzzyRadioButton.setOpaque(false);
		edgesFuzzyRadioButton.setForeground(COLOR_FG);
		edgesFuzzyRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
		edgesFuzzyRadioButton.setHorizontalAlignment(JRadioButton.LEFT);
		edgesFuzzyRadioButton.addItemListener(this);
		edgesFuzzyRadioButton.setToolTipText("<html>Activates the 'Fuzzy edges'<br>"
				+ "edge filtering strategy, which is<br>" + "based on the utility value of each<br>"
				+ "edge for any node.</html>");
		ButtonGroup radioEdgesGroup = new ButtonGroup();
		radioEdgesGroup.add(edgesBestRadioButton);
		radioEdgesGroup.add(edgesFuzzyRadioButton);
		lowerHeaderPanel.add(lowerHeaderLabel);
		lowerHeaderPanel.add(Box.createVerticalStrut(2));
		lowerHeaderPanel.add(edgesBestRadioButton);
		lowerHeaderPanel.add(edgesFuzzyRadioButton);
		lowerHeaderPanel.add(Box.createVerticalStrut(5));
		// lower slider parent panel
		JPanel lowerSliderPanel = new JPanel();
		lowerSliderPanel.setOpaque(false);
		lowerSliderPanel.setLayout(new BoxLayout(lowerSliderPanel, BoxLayout.X_AXIS));
		// lower ratio slider panel
		JPanel fuzzyRatioPanel = new JPanel();
		fuzzyRatioPanel.setOpaque(false);
		fuzzyRatioPanel.setLayout(new BorderLayout());
		JLabel fuzzyRatioHeader = new JLabel("Utility rt.");
		fuzzyRatioHeader.setFont(this.smallFont);
		fuzzyRatioHeader.setOpaque(false);
		fuzzyRatioHeader.setForeground(COLOR_FG);
		centerHorizontally(fuzzyRatioHeader);
		edgesFuzzyRatioSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesFuzzyRatioSlider.setUI(new SlickerSliderUI(edgesFuzzyRatioSlider));
		edgesFuzzyRatioSlider.setOpaque(false);
		edgesFuzzyRatioSlider.addChangeListener(this);
		edgesFuzzyRatioSlider.setToolTipText("<html>Controls the utility ratio used<br>"
				+ "for edge filtering. A higher value will<br>" + "give more preference to edges' significance,<br>"
				+ "lower value prefers correlation.</html>");
		edgesFuzzyRatioLabel = new JLabel("0.000");
		centerHorizontally(edgesFuzzyRatioLabel);
		edgesFuzzyRatioLabel.setSize(new Dimension(100, 25));
		edgesFuzzyRatioLabel.setForeground(COLOR_FG);
		edgesFuzzyRatioLabel.setFont(this.smallFont);
		fuzzyRatioPanel.add(packVerticallyCentered(fuzzyRatioHeader, 60, 20), BorderLayout.NORTH);
		fuzzyRatioPanel.add(edgesFuzzyRatioSlider, BorderLayout.CENTER);
		fuzzyRatioPanel.add(packVerticallyCentered(edgesFuzzyRatioLabel, 40, 20), BorderLayout.SOUTH);
		// lower percentage slider panel
		JPanel fuzzyPercentagePanel = new JPanel();
		fuzzyPercentagePanel.setOpaque(false);
		fuzzyPercentagePanel.setLayout(new BorderLayout());
		JLabel fuzzyPercentageHeader = new JLabel("Cutoff");
		fuzzyPercentageHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		fuzzyPercentageHeader.setOpaque(false);
		fuzzyPercentageHeader.setForeground(COLOR_FG);
		fuzzyPercentageHeader.setFont(this.smallFont);
		centerHorizontally(fuzzyPercentageHeader);
		edgesFuzzyPercentageSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesFuzzyPercentageSlider.setUI(new SlickerSliderUI(edgesFuzzyPercentageSlider));
		edgesFuzzyPercentageSlider.setOpaque(false);
		edgesFuzzyPercentageSlider.addChangeListener(this);
		edgesFuzzyPercentageSlider.setToolTipText("<html>Determines the minimal utility for<br>"
				+ "an edge to be included, with a larger value<br>" + "allowing more edges to be displayed, thus<br>"
				+ "increasing the detail of the model.</html>");
		edgesFuzzyPercentageLabel = new JLabel("0.000");
		edgesFuzzyPercentageLabel.setForeground(COLOR_FG);
		edgesFuzzyPercentageLabel.setSize(new Dimension(100, 25));
		edgesFuzzyPercentageLabel.setFont(this.smallFont);
		centerHorizontally(edgesFuzzyPercentageLabel);
		fuzzyPercentagePanel.add(packVerticallyCentered(fuzzyPercentageHeader, 40, 20), BorderLayout.NORTH);
		fuzzyPercentagePanel.add(edgesFuzzyPercentageSlider, BorderLayout.CENTER);
		fuzzyPercentagePanel.add(packVerticallyCentered(edgesFuzzyPercentageLabel, 40, 20), BorderLayout.SOUTH);
		// assemble lower slider panel
		lowerSliderPanel.add(fuzzyPercentagePanel);
		lowerSliderPanel.add(fuzzyRatioPanel);
		// assemble check box panel
		JPanel lowerSettingsPanel = new JPanel();
		lowerSettingsPanel.setOpaque(false);
		lowerSettingsPanel.setLayout(new BoxLayout(lowerSettingsPanel, BoxLayout.Y_AXIS));
		edgesFuzzyIgnoreLoopBox = new JCheckBox("ignore self-loops");
		edgesFuzzyIgnoreLoopBox.setUI(new SlickerCheckBoxUI());
		edgesFuzzyIgnoreLoopBox.setOpaque(false);
		edgesFuzzyIgnoreLoopBox.setForeground(COLOR_FG);
		edgesFuzzyIgnoreLoopBox.setFont(this.smallFont);
		edgesFuzzyIgnoreLoopBox.addItemListener(this);
		edgesFuzzyIgnoreLoopBox.setToolTipText("<html>If active, length-1-loops (i.e.,<br>"
				+ "repeptitions of one event) will not be,<br>" + "taken into account when filtering edges.</html>");
		edgesFuzzyInterpretAbsoluteBox = new JCheckBox("interpret absolute");
		edgesFuzzyInterpretAbsoluteBox.setUI(new SlickerCheckBoxUI());
		edgesFuzzyInterpretAbsoluteBox.setOpaque(false);
		edgesFuzzyInterpretAbsoluteBox.setForeground(COLOR_FG);
		edgesFuzzyInterpretAbsoluteBox.setFont(this.smallFont);
		edgesFuzzyInterpretAbsoluteBox.addItemListener(this);
		edgesFuzzyInterpretAbsoluteBox.setToolTipText("<html>If active, all edges' utility value<br>"
				+ "must exceed the cutoff globally, i.e. in an<br>"
				+ "absolute way, rather than locally, i.e. in a<br>" + "relative way.</html>");
		lowerSettingsPanel.add(edgesFuzzyIgnoreLoopBox);
		lowerSettingsPanel.add(edgesFuzzyInterpretAbsoluteBox);
		// assemble lower control panel
		lowerControlPanel.add(lowerHeaderPanel, BorderLayout.NORTH);
		lowerControlPanel.add(lowerSliderPanel, BorderLayout.CENTER);
		lowerControlPanel.add(lowerSettingsPanel, BorderLayout.SOUTH);
		//end of the "Edge filter" panel

		// concurrency edge transformer slider panel
		JPanel concurrencySliderPanel = new JPanel();
		concurrencySliderPanel.setOpaque(false);
		concurrencySliderPanel.setLayout(new BoxLayout(concurrencySliderPanel, BoxLayout.X_AXIS));
		// concurrency edge preserve threshold slider panel
		JPanel concurrencyPreservePanel = new JPanel();
		concurrencyPreservePanel.setOpaque(false);
		concurrencyPreservePanel.setLayout(new BorderLayout());
		JLabel concurrencyPreserveHeader = new JLabel("Preserve");
		concurrencyPreserveHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		concurrencyPreserveHeader.setOpaque(false);
		concurrencyPreserveHeader.setForeground(COLOR_FG);
		concurrencyPreserveHeader.setFont(this.smallFont);
		centerHorizontally(concurrencyPreserveHeader);
		edgesConcurrencyThresholdSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesConcurrencyThresholdSlider.setUI(new SlickerSliderUI(edgesConcurrencyThresholdSlider));
		edgesConcurrencyThresholdSlider.setOpaque(false);
		edgesConcurrencyThresholdSlider.addChangeListener(this);
		edgesConcurrencyThresholdSlider.setToolTipText("<html>For conflicting relations, this sets the<br>"
				+ "threshold for their relative significance which,<br>"
				+ "if not exceeded, causes the conflicting edges to<br>"
				+ "fall victim to simplification. A smaller value<br>"
				+ "allows more conflicting relations in the model.</html>");
		edgesConcurrencyThresholdLabel = new JLabel("0.000");
		edgesConcurrencyThresholdLabel.setSize(new Dimension(100, 25));
		edgesConcurrencyThresholdLabel.setForeground(COLOR_FG);
		edgesConcurrencyThresholdLabel.setFont(this.smallFont);
		centerHorizontally(edgesConcurrencyThresholdLabel);
		concurrencyPreservePanel.add(packVerticallyCentered(concurrencyPreserveHeader, 60, 20), BorderLayout.NORTH);
		concurrencyPreservePanel.add(edgesConcurrencyThresholdSlider, BorderLayout.CENTER);
		concurrencyPreservePanel
				.add(packVerticallyCentered(edgesConcurrencyThresholdLabel, 40, 20), BorderLayout.SOUTH);
		// concurrency edge ratio threshold slider panel
		JPanel concurrencyRatioPanel = new JPanel();
		concurrencyRatioPanel.setOpaque(false);
		concurrencyRatioPanel.setLayout(new BorderLayout());
		JLabel concurrencyRatioHeader = new JLabel("Ratio");
		concurrencyRatioHeader.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		concurrencyRatioHeader.setOpaque(false);
		concurrencyRatioHeader.setForeground(COLOR_FG);
		concurrencyRatioHeader.setFont(this.smallFont);
		centerHorizontally(concurrencyRatioHeader);
		edgesConcurrencyRatioSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		edgesConcurrencyRatioSlider.setUI(new SlickerSliderUI(edgesConcurrencyRatioSlider));
		edgesConcurrencyRatioSlider.setOpaque(false);
		edgesConcurrencyRatioSlider.addChangeListener(this);
		edgesConcurrencyRatioSlider.setToolTipText("<html>For conflicting relations which have fallen<br>"
				+ "victim to simplification, this determines ratio<br>"
				+ "threshold. A lower value prefers sequentialization of<br>"
				+ "conflicting relations, a higher value tends to<br>"
				+ "interpret them as being scheduled concurrently.</html>");
		edgesConcurrencyRatioLabel = new JLabel("0.000");
		edgesConcurrencyRatioLabel.setSize(new Dimension(100, 25));
		edgesConcurrencyRatioLabel.setForeground(COLOR_FG);
		edgesConcurrencyRatioLabel.setFont(this.smallFont);
		centerHorizontally(edgesConcurrencyRatioLabel);
		concurrencyRatioPanel.add(packVerticallyCentered(concurrencyRatioHeader, 60, 20), BorderLayout.NORTH);
		concurrencyRatioPanel.add(edgesConcurrencyRatioSlider, BorderLayout.CENTER);
		concurrencyRatioPanel.add(packVerticallyCentered(edgesConcurrencyRatioLabel, 40, 20), BorderLayout.SOUTH);
		// assemble concurrency slider panel
		concurrencySliderPanel.add(concurrencyPreservePanel);
		concurrencySliderPanel.add(concurrencyRatioPanel);
		// setup concurrency parent panel
		JPanel concurrencyParentPanel = new JPanel();
		concurrencyParentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		concurrencyParentPanel.setBackground(COLOR_BG2);
		concurrencyParentPanel.setOpaque(true);
		concurrencyParentPanel.setLayout(new BorderLayout());
		edgesConcurrencyActiveBox = new JCheckBox("Filter concurrency");
		edgesConcurrencyActiveBox.setUI(new SlickerCheckBoxUI());
		edgesConcurrencyActiveBox.setOpaque(false);
		edgesConcurrencyActiveBox.setForeground(COLOR_FG);
		edgesConcurrencyActiveBox.setFont(this.smallFont);
		edgesConcurrencyActiveBox.addItemListener(this);
		edgesConcurrencyActiveBox.setToolTipText("<html>This control can be used to switch off<br>"
				+ "concurrency filtering in the model.</html>");
		JPanel edgesConcurrencyHeaderPanel = new JPanel();
		edgesConcurrencyHeaderPanel.setLayout(new BoxLayout(edgesConcurrencyHeaderPanel, BoxLayout.Y_AXIS));
		edgesConcurrencyHeaderPanel.setOpaque(false);
		edgesConcurrencyHeaderPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 20, 10));
		edgesConcurrencyHeaderPanel.add(edgesConcurrencyActiveBox);
		edgesConcurrencyHeaderPanel.add(Box.createVerticalGlue());
		concurrencyParentPanel.add(edgesConcurrencyHeaderPanel, BorderLayout.NORTH);
		concurrencyParentPanel.add(concurrencySliderPanel, BorderLayout.CENTER);
		// assemble slick tab pane
		StackedCardsTabbedPane tabPane = new StackedCardsTabbedPane();
		tabPane.addTab("Concurrency filter", concurrencyParentPanel);
		tabPane.addTab("Edge filter", lowerControlPanel);
		tabPane.addTab("Node filter", upperControlPanel);
		tabPane.setActive(2);
		tabPane.setMinimumSize(new Dimension(190, 220));
		tabPane.setMaximumSize(new Dimension(190, 10000));
		tabPane.setPreferredSize(new Dimension(190, 10000));
		tabPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		//Log Replay Inspector  i.e. replay Gauge
		replayGauge = new LedGauge("Log conformance",
				"This amount of events in the log could\nbe replayed successfully.\n(click for details)", 2);
		replayGauge.setMinimumSize(new Dimension(26, 20));
		replayGauge.setMaximumSize(new Dimension(26, 10000));
		replayGauge.setPreferredSize(new Dimension(26, 10000));
		MouseListener replayListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (replayView != null || enableRedraw == false) {
					return; // ignore
				}
				replayView = new ReplayView(replay);
				ActionListener closeListener = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						rootPanel.remove(replayView);
						rootPanel.add(graphPanel, BorderLayout.CENTER);
						//						sidePanel.setCanvasBg(Color.WHITE);
						rootPanel.revalidate();
						rootPanel.repaint();
						replayView = null;
					}
				};
				replayView.setCloseActionListener(closeListener);
				if (detailView != null) {
					rootPanel.remove(detailView);
					detailView = null;
				} else {
					rootPanel.remove(graphPanel);
				}
				rootPanel.add(replayView, BorderLayout.CENTER);
				//				sidePanel.setCanvasBg(new Color(30, 30, 30));
				rootPanel.revalidate();
				rootPanel.repaint();
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		};
		replayGauge.addMouseListener(replayListener);

		//Model detail Guage
		detailGauge = new LedGauge("Model detail",
				"All visible nodes in the model represent this\namount of overall significance.\n(click for details)",
				1);
		detailGauge.setMinimumSize(new Dimension(26, 20));
		detailGauge.setMaximumSize(new Dimension(26, 10000));
		detailGauge.setPreferredSize(new Dimension(26, 10000));
		MouseListener detailListener = new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				if (detailView != null || enableRedraw == false) {
					return; // ignore
				}
				detailView = new DetailView(detail);
				ActionListener closeListener = new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						rootPanel.remove(detailView);
						rootPanel.add(graphPanel, BorderLayout.CENTER);
						//						sidePanel.setCanvasBg(Color.WHITE);
						rootPanel.revalidate();
						rootPanel.repaint();
						detailView = null;
					}
				};
				detailView.setCloseActionListener(closeListener);
				if (replayView != null) {
					rootPanel.remove(replayView);
					replayView = null;
				} else {
					rootPanel.remove(graphPanel);
				}
				replayView = null;
				rootPanel.add(detailView, BorderLayout.CENTER);
				//				sidePanel.setCanvasBg(new Color(30, 30, 30));
				rootPanel.revalidate();
				rootPanel.repaint();
			}

			public void mouseEntered(MouseEvent arg0) { /* ignore */
			}

			public void mouseExited(MouseEvent arg0) { /* ignore */
			}

			public void mousePressed(MouseEvent arg0) { /* ignore */
			}

			public void mouseReleased(MouseEvent arg0) { /* ignore */
			}
		};
		detailGauge.addMouseListener(detailListener);
		//Detail Button
		InspectorButton detailButton = new InspectorButton();
		detailButton.setToolTipText("click to show model detail inspector");
		detailButton.addMouseListener(detailListener);
		detailButton.setAlignmentX(CENTER_ALIGNMENT);
		detailButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		detailButton.setMinimumSize(new Dimension(20, 20));
		//log Conformance Replay button
		InspectorButton replayButton = new InspectorButton();
		replayButton.setToolTipText("click to show log replay inspector");
		replayButton.addMouseListener(replayListener);
		replayButton.setAlignmentX(CENTER_ALIGNMENT);
		replayButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
		replayButton.setMinimumSize(new Dimension(20, 20));
		//Set up Replay Panel 
		JPanel replayPanel = new JPanel();
		replayPanel.setLayout(new BoxLayout(replayPanel, BoxLayout.Y_AXIS));
		replayPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 2));
		replayPanel.setOpaque(false);
		replayPanel.add(detailGauge);
		replayPanel.add(detailButton);
		replayPanel.add(Box.createVerticalStrut(5));
		replayPanel.add(replayGauge);
		replayPanel.add(replayButton);
		//JF comment
		//	JPanel rightPanel = new JPanel();
		rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder());
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.add(tabPane, BorderLayout.CENTER);
		if (log != null) {
			rightPanel.add(replayPanel, BorderLayout.WEST);
		}
		//		sidePanel = new SidePanel();
		//		rootPanel.add(sidePanel, BorderLayout.WEST);
		rootPanel.add(rightPanel, BorderLayout.EAST);
		//not put the graphPanel in the rootPanel yet
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(rootPanel, BorderLayout.CENTER);
	}

	protected JPanel packVerticallyCentered(JComponent component, int width, int height) {
		JPanel boxed = new JPanel();
		boxed.setLayout(new BoxLayout(boxed, BoxLayout.X_AXIS));
		boxed.setBorder(BorderFactory.createEmptyBorder());
		boxed.setOpaque(false);
		Dimension dim = new Dimension(width, height);
		component.setMinimumSize(dim);
		component.setMaximumSize(dim);
		component.setPreferredSize(dim);
		component.setSize(dim);
		boxed.add(Box.createHorizontalGlue());
		boxed.add(component);
		boxed.add(Box.createHorizontalGlue());
		return boxed;
	}

	protected void setGuiToInitialValues(int showingNumberOfNodes) {

		if (showingNumberOfNodes > 0) {
			double nodeThreshold = graph.getThresholdShowingPrimitives(showingNumberOfNodes)
					- graph.getMinimalNodeSignificance();
			nodeThreshold = nodeThreshold / (1.0 - graph.getMinimalNodeSignificance());
			nodeSignificanceSlider.setValue((int) (nodeThreshold * 1000.0));
			fastTransformer.setThreshold(nodeThreshold);
			edgesFuzzyRadioButton.setSelected(true);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			edgesFuzzyRatioSlider.setValue(750);
			edgesFuzzyRatioLabel.setText("0.750");
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			edgesFuzzyPercentageSlider.setValue(200);
			edgesFuzzyPercentageLabel.setText("0.200");
			fuzzyEdgeTransformer.setPreservePercentage(0.2);
			edgesFuzzyIgnoreLoopBox.setSelected(true);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			edgesFuzzyInterpretAbsoluteBox.setSelected(false);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			edgesConcurrencyActiveBox.setSelected(true);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			edgesConcurrencyThresholdSlider.setValue(600);
			edgesConcurrencyThresholdLabel.setText("0.600");
			concurrencyEdgeTransformer.setPreserveThreshold(0.6);
			edgesConcurrencyRatioSlider.setValue(700);
			edgesConcurrencyRatioLabel.setText("0.700");
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		} else {
			nodeSignificanceSlider.setValue(nodeSignificanceSlider.getMinimum());
			fastTransformer.setThreshold(0.0);
			edgesFuzzyRadioButton.setSelected(true);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			edgesFuzzyRatioSlider.setValue(750);
			edgesFuzzyRatioLabel.setText("0.750");
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(0.75);
			edgesFuzzyPercentageSlider.setValue(1000);
			edgesFuzzyPercentageLabel.setText("1.0");
			fuzzyEdgeTransformer.setPreservePercentage(1.0);
			edgesFuzzyIgnoreLoopBox.setSelected(true);
			fuzzyEdgeTransformer.setIgnoreSelfLoops(true);
			edgesFuzzyInterpretAbsoluteBox.setSelected(false);
			fuzzyEdgeTransformer.setInterpretPercentageAbsolute(false);
			edgesConcurrencyActiveBox.setSelected(true);
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
			edgesConcurrencyThresholdSlider.setValue(1000);
			edgesConcurrencyThresholdLabel.setText("1.0");
			concurrencyEdgeTransformer.setPreserveThreshold(1.0);
			edgesConcurrencyRatioSlider.setValue(700);
			edgesConcurrencyRatioLabel.setText("0.700");
			concurrencyEdgeTransformer.setRatioThreshold(0.7);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource() == nodeSignificanceSlider) {
			updateNodeSignificanceSlider();
		} else if (evt.getSource() == edgesFuzzyRatioSlider) {
			updateFuzzyEdgeRatioSlider();
		} else if (evt.getSource() == edgesFuzzyPercentageSlider) {
			updateFuzzyEdgePercentageSlider();
		} else if (evt.getSource() == edgesBestRadioButton || evt.getSource() == edgesFuzzyRadioButton) {
			updateEdgeTransformerSelection();
		} else if (evt.getSource() == edgesConcurrencyThresholdSlider) {
			updateConcurrencyThresholdSlider();
		} else if (evt.getSource() == edgesConcurrencyRatioSlider) {
			updateConcurrencyRatioSlider();
		}
	}

	public void redrawGraph(boolean isUpdateForChangedParameters) {
		//		System.err.println("[FastTransformerPanel] Orientation for graph " + graph.hashCode() + " is " + graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION));
		if (enableRedraw == false) {
			return; // ignore
		}

		if (graph.getMetrics() != null) {
			Object orientation = graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION);
			graph = new MutableFuzzyGraph(graph.getMetrics());
			graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, orientation);
		}

		updateGraphAttributesFromUI();
		setGuiEnabled(false);
		// setting progress view
		if (replayView != null) {
			rootPanel.remove(replayView);
			replayView = null;
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}
		if (detailView != null) {
			rootPanel.remove(detailView);
			detailView = null;
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}
		//		sidePanel.setVisible(false);
		rootPanel.revalidate();
		boolean noEdges = graph.getEdgeImpls().isEmpty();
		if (isUpdateForChangedParameters) {
			graph.initializeGraph();
			setColorOfAbstractActivityInGraph(graph);
			fastTransformer.transform(graph);
			//Set edges for Fuzzy Graph
			graph.setEdgeImpls();
		}
		//else if(timeToFormTranformerPanel == 1){
		else if (timeToFormTranformerPanel == 1 && noEdges) {
			setColorOfAbstractActivityInGraph(graph);
			fastTransformer.transform(graph);

			//Set edges for Fuzzy Graph
			graph.setEdgeImpls();
			//	graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);
			//	timeToFormTranformerPanel++;
			//graph.signalViews();
		}

		//		System.out.println("[FastTransformerPanel] Graph size = " + graph.getNodes().size() + ","
		//				+ graph.getClusterNodes().size() + "," + graph.getEdgeImpls().size());
		if (graphPanel != null) {
			rootPanel.remove(graphPanel);
			graphPanel = null;
		}
		if (graphPanel == null) {
			//			System.out.println("Creating new graph panel");
			graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
			//			System.out.println("Replacing graph panel");
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}

		//If the exported fuzzy graph object is shown, then only the graph will be shown
		//	if(noEdges ||isUpdateForChangedParameters){
		MetricsRepository metrics = graph.getMetrics();
		if (metrics != null) {
			//			sidePanel.setVisible(true);
			if (log != null) {
				repaint();
				// replay log in new graph to determine metrics values
				context.log("Starting fuzzy graph replay...", MessageLevel.NORMAL);
				long start = System.currentTimeMillis();
				try {
					replayGauge.setValid(false);
					detailGauge.setValid(false);
					detail = new FuzzyDetailAnalysis(graph, log);
					detailGauge.setCoverage(detail.getDetail());
					replay = new FuzzyReplay(graph, log, replayGauge);
					context.log("...replayed fuzzy graph in " + ((System.currentTimeMillis() - start) / 1000)
							+ " seconds.", MessageLevel.NORMAL);
					context.log("Measured fuzzy conformance is " + replay.getValue(), MessageLevel.NORMAL);
					replayGauge.setCoverage(replay.getValue());
				} catch (IOException ioe) {
					// no way to fix this here...
					context.log("Fatal error in Fuzzy Miner: please check your STDERR output for stack trace!",
							MessageLevel.ERROR);
					ioe.printStackTrace();
				} finally {
					setGuiEnabled(true);
				}
			} else {
				// no log to replay
				setGuiEnabled(true);
			}
		} else {
			//			sidePanel.setVisible(false);
			rightPanel.setVisible(false);
		}
		//release 	
		jgraph = graphPanel.getGraph();
		jgraph.setMarqueeHandler(new BasicMarqueeHandler() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if ((e.getModifiersEx() & InputEvent.BUTTON3_DOWN_MASK) != 0) {
					JPopupMenu popupMenu1 = new JPopupMenu();
					JMenuItem menuItem1 = new JMenuItem("Export Fuzzy Model");
					menuItem1.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							//System.out.println("###BEGIN EXPORT FUZZY MODEL");
							MutableFuzzyGraph exptFMObjs;
							try {
								exptFMObjs = getExportFuzzyGraphObjects();
								//   System.out.println("Pushing Start");
								context.getProvidedObjectManager().createProvidedObject(
										"ExportedFuzzyModel (mined from "
												+ XConceptExtension.instance().extractName(log) + ")", exptFMObjs,
										MutableFuzzyGraph.class, context);
								// Make exported Fuzzy instance favorite, if possible.
								if (context instanceof UIPluginContext) {
									((UIPluginContext) context).getGlobalContext().getResourceManager()
											.getResourceForInstance(exptFMObjs).setFavorite(true);
								}
								// System.out.println("Pushing End");
							} catch (Exception exp) {
								exp.printStackTrace();
							}
						}
					});
					popupMenu1.add(menuItem1);
					if (popupMenu1 != null) {
						popupMenu1.show(graphPanel, e.getX(), e.getY());
					}
				} else {
					super.mousePressed(e);
				}
			}
		});

		//LJF 0615
		jgraph.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) { //double-click the node
					synchronized (jgraph.getProMGraph()) {
						// Check for selection.
						// If the cell that is being clicked is part of the selection,
						// we use the current selection.
						// otherwise, we use a new selection
						Object cell = jgraph.getFirstCellForLocation(e.getX(), e.getY());
						Collection<DirectedGraphElement> sel;
						if (cell == null) {
							// Nothing selected
							jgraph.clearSelection();
							sel = new ArrayList<DirectedGraphElement>(0);
						} else if (jgraph.getSelectionModel().isCellSelected(cell)) {
							// the current selection contains cell
							// use that selection
							sel = graphPanel.getSelectedElements();
							showDetailGraphOfNode(cell);
						} else {
							// the current selection does not contain cell.
							// reset the selection to [cell]
							sel = new ArrayList<DirectedGraphElement>(1);
							sel.add(graphPanel.getElementForLocation(e.getX(), e.getY()));
							jgraph.setSelectionCell(cell);
						}
					}
				}
			}

			public void mouseEntered(MouseEvent e) {

			}

			public void mouseReleased(MouseEvent e) {

			}

			public void mousePressed(MouseEvent e) {

			}

			public void mouseExited(MouseEvent e) {

			}
		});

	}

	public JComponent getGraphPanel() {
		return graphPanel;
	}

	private void setColorOfAbstractActivityInGraph(MutableFuzzyGraph mGraph, ViewSpecificAttributeMap map) {
		for (FMNode node : mGraph.getNodes()) {
			if (isMolecularNode(node)) {
				Color color = (Color) map.get(node, AttributeMap.FILLCOLOR);
				/*
				 * Test whether adjacent node. Test is on color, which is not
				 * really what it should be. However, how else to tell whether
				 * the node is adjacent?
				 */
				if (color.equals(FMColors.getAdjacentPrimitiveBackgroundColor())) {
					color = FMColors.getAdjacentAbstractBackgroundColor();
				} else {
					color = FMColors.getAbstractBackgroundColor();
				}
				map.putViewSpecific(node, AttributeMap.FILLCOLOR, color);
			}
		}
	}

	private void setColorOfAbstractActivityInGraph(MutableFuzzyGraph mGraph) {
		for (FMNode node : mGraph.getNodes()) {
			if (isMolecularNode(node)) {
				Color color = (Color) node.getAttributeMap().get(AttributeMap.FILLCOLOR);
				/*
				 * Test whether adjacent node. Test is on color, which is not
				 * really what it should be. However, how else to tell whether
				 * the node is adjacent?
				 */
				if (color.equals(FMColors.getAdjacentPrimitiveBackgroundColor())) {
					color = FMColors.getAdjacentAbstractBackgroundColor();
				} else {
					color = FMColors.getAbstractBackgroundColor();
				}
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, color);
			}
		}
	}

	/*
	 * show the detail graph when double clicking the node in the graph handle
	 * all kinds of node including the cluster node and the molecule/abstract
	 * node
	 */
	private void showDetailGraphOfNode(Object cell) {
		if (cell instanceof ProMGraphCell) {
			ProMGraphCell node = (ProMGraphCell) cell;
			final FMNode fmNode = (FMNode) (node.getNode());
			if (fmNode instanceof FMClusterNode) {
				//System.out.println("Clicked on cluster: " + fmNode.getLabel());				
				// HV
				/*
				 * Test whether we already have a frame open for this node.
				 */
				if (!clusterFrames.containsKey(fmNode)) {
					/*
					 * No, we do not. Create a frame for this node.
					 */
					//	System.out.println("Creating a frame for cluster: " + fmNode.getLabel());
					try {
						ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
						clustGraphPanel = ((FMClusterNode) fmNode).getClusterGraphPanel(context, log, map);
						//set the color of the abstract activities in the clusterGraph
						setColorOfAbstractActivityInGraph(
								(MutableFuzzyGraph) clustGraphPanel.getGraph().getProMGraph(), map);
						JFrame frame = new JFrame("Detailed graph for ' " + ((FMClusterNode) fmNode).id() + " '");
						frame.setContentPane(clustGraphPanel);
						frame.setLocation(50, 50);
						frame.setSize(1000, 600);
						/*
						 * Hide the frame when closed. If we also would dispose
						 * of it, then we might not be able to make it visible
						 * the next time. Therefore, we hide but do not dispose.
						 */
						frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
						/*
						 * Register the frame for this node.
						 */
						clusterFrames.put(fmNode, frame);
						installClusterGraphPanelListener();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				/*
				 * Now there should be a frame for this node. Get it and make it
				 * visible.
				 */
				JFrame frame = clusterFrames.get(fmNode);
				frame.setVisible(true);
				//
				//				frame.addWindowListener(new WindowAdapter() {
				//					public void windowClosing(WindowEvent e) {
				//						HashSet<FMNode> adjacentNodes = new HashSet<FMNode>();
				//						adjacentNodes.addAll(((FMClusterNode) fmNode).getPredecessors());
				//						adjacentNodes.addAll(((FMClusterNode) fmNode).getSuccessors());
				//						setAdjacentNodesColorBack(adjacentNodes);
				//					}
				//				});
			} else if (fmNode instanceof FuzzyMapPatternNode) {
				//System.out.println("Creating a frame for pattern node: " + fmNode.getLabel());
				try {
					patternGraphPanel = ((FuzzyMapPatternNode) fmNode).getPatternInnerGraphPanel(context, log);
					JFrame frame1 = new JFrame("Detailed graph for ' " + ((FuzzyMapPatternNode) fmNode).id() + " '");
					frame1.setContentPane(patternGraphPanel);
					frame1.setLocation(50, 50);
					frame1.setSize(1000, 600);
					frame1.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					//frame1.setVisible(true);

					//clusterFrames.put(fmNode, frame);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//}			
				//JFrame frame = clusterFrames.get(fmNode);
				//frame1.setVisible(true);
			} else {
				//if the FMNode is a molecular node,there will be a inner structure graph for the molecular node.
				//else there will be a message box to hint the user it is an atomic node.
				//showMolecularGraph(rootPanel,fmNode);
				showMolecularGraph(fmNode);
			}
		} else {
			if (cell instanceof ProMGraphEdge) {
				// show sig and cor of the edge
				Map<Object, Object> nested = new HashMap<Object, Object>();
				Map<Object, Object> attributeMap1 = new HashMap<Object, Object>();
				// Set the label of the selected edge  
				GraphConstants.setLabelAlongEdge(attributeMap1, true);
				nested.put(cell, attributeMap1);
				graphPanel.getGraph().getGraphLayoutCache().edit(nested, null, null, null);
			}
		}
	}

	//	private void setAdjacentNodesColorBack(HashSet<FMNode> adjacentNodes) {
	//		for (FMNode node : adjacentNodes) {
	//			if (node instanceof FMClusterNode) {
	//				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getClusterBackgroundColor());
	//			} else {
	//				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
	//				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
	//			}
	//		}
	//	}

	public boolean isMolecularNode(FMNode fmNode) {
		boolean haveFoundSubLog = false;
		File patternLogDirectory;
		if (isPatternBasedTransformedLog) {
			if (patternLogDirectorySet.isEmpty()) {
				setTransformedLogDirectory();
			}
			String legalPatternName = getLegalFileName(fmNode.getElementName());
			String legalPatternLogFileName = legalPatternName + ".xes.gz";
			Iterator<File> fileDirs = patternLogDirectorySet.iterator();
			while (fileDirs.hasNext() && (!haveFoundSubLog)) {
				patternLogDirectory = fileDirs.next();
				if (patternLogDirectory.exists()) {
					File[] patternLogFiles = patternLogDirectory.listFiles();
					for (File file : patternLogFiles) {
						if (file.getName().equals(legalPatternLogFileName)) {
							//transform the file into a XLog object
							XesXmlGZIPParser parser = new XesXmlGZIPParser();
							if (parser.canParse(file)) {

								/*
								 * Set the Fill Color of this Node to PINK
								 */
								//fmNode.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.PINK);
								haveFoundSubLog = true;
							}
							break;
						}
					}
				}
			}
		}
		//graph.signalViews();
		return haveFoundSubLog;
	}

	private ProMJGraphPanel getMolecularGraphPanel(FMNode fmNode) {
		ProMJGraphPanel panel = null;
		File patternLogDirectory;
		if (isPatternBasedTransformedLog) {
			if (patternLogDirectorySet.isEmpty()) {
				setTransformedLogDirectory();
			}
			String legalPatternName = getLegalFileName(fmNode.getElementName());
			String legalPatternLogFileName = legalPatternName + ".xes.gz";
			boolean haveFoundSubLog = false;
			Iterator<File> fileDirs = patternLogDirectorySet.iterator();
			while (fileDirs.hasNext() && (!haveFoundSubLog)) {
				patternLogDirectory = fileDirs.next();
				if (patternLogDirectory.exists()) {
					File[] patternLogFiles = patternLogDirectory.listFiles();
					for (File file : patternLogFiles) {
						if (file.getName().equals(legalPatternLogFileName)) {
							//transform the file into a XLog object
							XesXmlGZIPParser parser = new XesXmlGZIPParser();
							if (parser.canParse(file)) {
								// ljf
								/*
								 * Test whether we already have a frame open for
								 * this node.
								 */
								haveFoundSubLog = true;
								if (!molecularFrames.containsKey(fmNode)) {
									/*
									 * No, we do not. Create a frame for this
									 * node.
									 */
									try {
										List<XLog> subLogs = parser.parse(file);
										XLog patternSubLog = subLogs.get(0);
										//create molecular graph
										panel = getMolecularGraphPanel(context, patternSubLog, false, fmNode);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
							break;
						}
					}
				}
			}
		}
		return panel;
	}

	/**
	 * show the detail sub-process graph of the abstract/molecule activity
	 * 
	 * @param fmNode
	 * @return
	 */
	public MutableFuzzyGraph getMolecularGraph(FMNode fmNode) {
		ProMJGraphPanel panel = getMolecularGraphPanel(fmNode);
		return (MutableFuzzyGraph) (panel != null ? panel.getGraph().getProMGraph() : null);
	}

	/**
	 * show the detail sub-process graph of the abstract/molecule activity
	 * 
	 * @param fmNode
	 * @return
	 */
	public void showMolecularGraph(FMNode fmNode) {
		ProMJGraphPanel panel = null;
		if (!molecularFrames.containsKey(fmNode)) {
			panel = getMolecularGraphPanel(fmNode);
			if (panel != null) {
				MutableFuzzyGraph graph = (MutableFuzzyGraph) panel.getGraph().getProMGraph();
				//			graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
				//			System.err.println("[FastTransfomerPanel] Orientation for graph " + graph.hashCode() + " set to " + graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION));
				setColorOfAbstractActivityInGraph(graph);
				JFrame frame1 = new JFrame("Detailed Graph For Subprocess ' " + fmNode.getElementName() + "+"
						+ fmNode.getEventType() + " '");
				frame1.setContentPane(panel);
				frame1.setLocation(90, 90);
				frame1.setSize(750, 400);
				frame1.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				molecularFrames.put(fmNode, frame1);
			}
		}
		if (molecularFrames.containsKey(fmNode)) {
			JFrame frame = molecularFrames.get(fmNode);
			frame.setVisible(true);
		}
	}

	/*
	 * After pre-processing the log with patterns,there will be many molecular
	 * node in the new log. The molecular node contains many atomic nodes, this
	 * method is to get the inner graph inside the molecular node.
	 */
	public ProMJGraphPanel getMolecularGraphPanel(PluginContext context, XLog log, boolean isAtomicNode, FMNode node) {
		FuzzyMinerPlugin fuzzyMiner = new FuzzyMinerPlugin();
		MetricsRepository repository = fuzzyMiner.mineDefault(context, log);
		FastTransformerPanel fastTransformerPanel = new FastTransformerPanel(context, repository, SwingConstants.WEST);
		//		fastTransformerPanel.graph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.WEST);
		//		System.err.println("[FastTransfomerPanel] Orientation for graph " + fastTransformerPanel.graph.hashCode() + " set to " + fastTransformerPanel.graph.getAttributeMap().get(AttributeMap.PREF_ORIENTATION));
		return fastTransformerPanel.graphPanel;
	}

	/*
	 * Install the listener to the cluster panel, When the molecular node is
	 * clicked, the inner structure graph will be shown. Otherwise, the message
	 * box is popped up.
	 */
	public void installClusterGraphPanelListener() {
		GraphSelectionModel model = clustGraphPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							ProMGraphCell node = (ProMGraphCell) cell;
							FMNode fmNode = (FMNode) (node.getNode());
							if ((!(fmNode instanceof FMClusterNode)) && (!(fmNode instanceof FuzzyMapPatternNode))) {
								showMolecularGraph(fmNode);
							}
						}
					}
				}
			}
		});
	}

	/*
	 * Install the listener to the cluster panel, When the molecular node is
	 * clicked, the inner structure graph will be shown. Otherwise, the message
	 * box is popped up.
	 */
	//	public void installMolecularNodeGraphPanelListener() {
	//		GraphSelectionModel model = molecularGraphPanel.getGraph().getSelectionModel();
	//		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
	//		model.addGraphSelectionListener(new GraphSelectionListener() {
	//			public void valueChanged(GraphSelectionEvent evt) {
	//				for (Object cell : evt.getCells()) {
	//					if (evt.isAddedCell(cell)) {
	//						if (cell instanceof ProMGraphCell) {
	//							ProMGraphCell node = (ProMGraphCell) cell;
	//							FMNode fmNode = (FMNode) (node.getNode());
	//							if ((!(fmNode instanceof FMClusterNode)) && (!(fmNode instanceof FuzzyMapPatternNode))) {
	//								showMolecularGraph(fmNode);
	//							}
	//						}
	//					}
	//				}
	//			}
	//		});
	//	}

	/*
	 * If this log has been transformed , the method will set the directory of
	 * the transformed log
	 */
	private void setTransformedLogDirectory() {
		//get the directory that saved the pattern sub logs
		File patternLogDirectory;
		String logName = ((XAttributeLiteral) log.getAttributes().get("concept:name")).getValue();
		String logLocation = ((XAttributeLiteral) log.getAttributes().get(FILE_LOCATION_LOG_ATT_KEY)).getValue();
		//	System.out.println("$$$$$$$$$$ The location of the log is : " + logLocation +"$$$$$$$$$$$");	
		//get the directory of the sublog in this level
		int delimit = logName.indexOf(".");
		logName = logName.substring(0, delimit);
		String patternLogDir = logLocation + logName + FILE_SEPERATOR;
		patternLogDirectory = new File(patternLogDir);
		patternLogDirectorySet.add(patternLogDirectory);
		//If there are several phase of transformation iterations, track back to find all the directories of sub logs in each level
		XAttributeLiteral parentFileAbsolutePath = ((XAttributeLiteral) log.getAttributes().get(
				PARENT_FILE_NAME_LOG_ATT_KEY));
		String prtFlAbsPathStr = parentFileAbsolutePath.getValue();
		if (prtFlAbsPathStr.contains(FILE_SEPERATOR)) {
			delimit = prtFlAbsPathStr.indexOf(".");
			String parentLogFileDir = prtFlAbsPathStr.substring(0, delimit) + FILE_SEPERATOR;
			patternLogDirectorySet.add(new File(parentLogFileDir));
		}

		/*
		 * JC Addition Start - Get all the attributes with parentLogFileLocation
		 * to explore for abstract sub-logs This recursive scanning of
		 * attributes is done using a stack
		 */
		Stack<XAttribute> childAttributeStack = new Stack<XAttribute>();
		childAttributeStack.add(parentFileAbsolutePath);
		List<XAttribute> ancestorFileAttributeList = new ArrayList<XAttribute>();
		while (!childAttributeStack.isEmpty()) {
			XAttribute temp = childAttributeStack.pop();
			ancestorFileAttributeList.add(temp);
			XAttributeMap childAttributes = temp.getAttributes();
			Iterator<XAttribute> it = childAttributes.values().iterator();
			while (it.hasNext())
				childAttributeStack.add(it.next());
		}

		//		System.out.println("Ancestor File Attribute List: "+ancestorFileAttributeList.size());
		for (XAttribute ancestorFileAttribute : ancestorFileAttributeList) {
			String ancestorLogFile = ((XAttributeLiteral) ancestorFileAttribute).getValue();
			if (ancestorLogFile.contains(FILE_SEPERATOR)) {
				delimit = ancestorLogFile.indexOf(".");
				String ancestorLogFileDir = ancestorLogFile.substring(0, delimit) + FILE_SEPERATOR;
				patternLogDirectorySet.add(new File(ancestorLogFileDir));
			}
		}
		//		System.out.println("Pattern Log Directory Set: "+patternLogDirectorySet);
		// JC Addition End

		/*
		 * JC Commented
		 * 
		 * XAttributeMap subAttrs = parentFileAbsolutePath.getAttributes();
		 * Iterator<XAttribute> ancestorFiles = subAttrs.values().iterator();
		 * 
		 * while (ancestorFiles.hasNext()) { String ancestorLogFile =
		 * ((XAttributeLiteral) ancestorFiles.next()).getValue(); if
		 * (ancestorLogFile.contains(FILE_SEPERATOR)) { delimit =
		 * ancestorLogFile.indexOf("."); String ancestorLogFileDir =
		 * ancestorLogFile.substring(0, delimit) + FILE_SEPERATOR;
		 * patternLogDirectorySet.add(new File(ancestorLogFileDir)); } }
		 */
	}

	//LJF comment for multiple level views
	//	private void setTransformedLogDirectory()
	//	{
	//		//get the directory that saved the pattern sub logs
	//		String logName = ((XAttributeLiteral)log.getAttributes().get("concept:name")).getValue();
	//		String logLocation =  ((XAttributeLiteral)log.getAttributes().get("location")).getValue();
	//		System.out.println("$$$$$$$$$$ The location of the log is : " + logLocation +"$$$$$$$$$$$");		
	//		int delimit = logName.indexOf(".");
	//		logName = logName.substring(0, delimit);
	//		String patternLogDir = logLocation + logName+"\\";
	//		patternLogDirectory = new File(patternLogDir);
	//	}

	public void setGuiEnabled(boolean enabled) {
		enableRedraw = enabled;
		edgesBestRadioButton.setEnabled(enabled);
		edgesFuzzyRadioButton.setEnabled(enabled);
		nodeSignificanceSlider.setEnabled(enabled);
		edgesFuzzyRatioSlider.setEnabled(enabled);
		edgesFuzzyPercentageSlider.setEnabled(enabled);
		edgesConcurrencyThresholdSlider.setEnabled(enabled);
		edgesConcurrencyRatioSlider.setEnabled(enabled);
		edgesFuzzyIgnoreLoopBox.setEnabled(enabled);
		edgesFuzzyInterpretAbsoluteBox.setEnabled(enabled);
		edgesConcurrencyActiveBox.setEnabled(enabled);
	}

	protected void updateEdgeTransformerSelection() {
		if (edgesBestRadioButton.isSelected() == true) {
			setFuzzyEdgeControlsEnabled(false);
			fastTransformer.removeInterimTransformer(fuzzyEdgeTransformer);
			fastTransformer.addInterimTransformer(bestEdgeTransformer);
			redrawGraph(true);
		} else if (edgesFuzzyRadioButton.isSelected() == true) {
			setFuzzyEdgeControlsEnabled(true);
			fastTransformer.removeInterimTransformer(bestEdgeTransformer);
			fastTransformer.addInterimTransformer(fuzzyEdgeTransformer);
			redrawGraph(true);
		}
	}

	protected void setFuzzyEdgeControlsEnabled(boolean enabled) {
		edgesFuzzyRatioSlider.setEnabled(enabled);
		edgesFuzzyPercentageSlider.setEnabled(enabled);
		edgesFuzzyIgnoreLoopBox.setEnabled(enabled);
		edgesFuzzyInterpretAbsoluteBox.setEnabled(enabled);
	}

	protected void setConcurrencyEdgeTransformerActive(boolean active) {
		if (active == true) {
			fastTransformer.addPreTransformer(concurrencyEdgeTransformer);
		} else {
			fastTransformer.removePreTransformer(concurrencyEdgeTransformer);
		}
		edgesConcurrencyRatioSlider.setEnabled(active);
		edgesConcurrencyThresholdSlider.setEnabled(active);
		redrawGraph(true);
	}

	protected void updateNodeSignificanceSlider() {
		double value = getNodeThresholdFromSlider();
		nodeSignificanceLabel.setText(MutableFuzzyGraph.format(value));
		if (nodeSignificanceSlider.getValueIsAdjusting() == false) {
			fastTransformer.setThreshold(value);
			redrawGraph(true);
		}
	}

	protected void updateFuzzyEdgeRatioSlider() {
		double value = getFuzzyEdgeRatioFromSlider();
		edgesFuzzyRatioLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesFuzzyRatioSlider.getValueIsAdjusting() == false) {
			fuzzyEdgeTransformer.setSignificanceCorrelationRatio(value);
			redrawGraph(true);
		}
	}

	protected void updateFuzzyEdgePercentageSlider() {
		double value = getFuzzyEdgePercentageFromSlider();
		edgesFuzzyPercentageLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesFuzzyPercentageSlider.getValueIsAdjusting() == false) {
			fuzzyEdgeTransformer.setPreservePercentage(value);
			redrawGraph(true);
		}
	}

	protected void updateConcurrencyThresholdSlider() {
		double value = getConcurrencyThresholdFromSlider();
		edgesConcurrencyThresholdLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesConcurrencyThresholdSlider.getValueIsAdjusting() == false) {
			concurrencyEdgeTransformer.setPreserveThreshold(value);
			redrawGraph(true);
		}
	}

	protected void updateConcurrencyRatioSlider() {
		double value = getConcurrencyRatioFromSlider();
		edgesConcurrencyRatioLabel.setText(MutableFuzzyGraph.format(value));
		if (edgesConcurrencyRatioSlider.getValueIsAdjusting() == false) {
			concurrencyEdgeTransformer.setRatioThreshold(value);
			redrawGraph(true);
		}
	}

	protected double getNodeThresholdFromSlider() {
		double threshold = nodeSignificanceSlider.getValue() / 1000.0;
		// normalize threshold to minimal node frequency
		threshold = ((1.0 - graph.getMinimalNodeSignificance()) * threshold) + graph.getMinimalNodeSignificance();
		return threshold;
	}

	protected double getFuzzyEdgeRatioFromSlider() {
		return edgesFuzzyRatioSlider.getValue() / 1000.0;
	}

	protected double getFuzzyEdgePercentageFromSlider() {
		return edgesFuzzyPercentageSlider.getValue() / 1000.0;
	}

	protected double getConcurrencyThresholdFromSlider() {
		double value = Math.pow((edgesConcurrencyThresholdSlider.getValue() / 1000.0), 4.0);
		return value;
	}

	protected double getConcurrencyRatioFromSlider() {
		return edgesConcurrencyRatioSlider.getValue() / 1000.0;
	}

	protected void updateGraphAttributesFromUI() {
		if (edgesFuzzyIgnoreLoopBox.isSelected()) {
			graph.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, TRUE);
		} else {
			graph.setAttribute(EDGES_FUZZY_IGNORE_LOOPS, FALSE);
		}
		if (edgesFuzzyInterpretAbsoluteBox.isSelected()) {
			graph.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, TRUE);
		} else {
			graph.setAttribute(EDGES_FUZZY_INTERPRET_ABSOLUTE, FALSE);
		}
		if (edgesConcurrencyActiveBox.isSelected()) {
			graph.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, TRUE);
		} else {
			graph.setAttribute(CONCURRENCY_EDGE_TRANSFORMER_ACTIVE, FALSE);
		}
		graph.setAttribute(CONCURRENCY_RATIO, Double.toString(getConcurrencyRatioFromSlider()));
		graph.setAttribute(CONCURRENCY_THRESHOLD, Double.toString(getConcurrencyThresholdFromSlider()));
		graph.setAttribute(FUZZY_EDGE_CUTOFF, Double.toString(getFuzzyEdgePercentageFromSlider()));
		graph.setAttribute(FUZZY_EDGE_RATIO, Double.toString(getFuzzyEdgeRatioFromSlider()));
		graph.setAttribute(NODE_CUTOFF, Double.toString(getNodeThresholdFromSlider()));
		if (edgesBestRadioButton.isSelected() == true) {
			graph.setAttribute(EDGE_TRANSFORMER_SELECTION, EDGE_TRANSFORMER_SELECTION_BEST_EDGES);
		} else if (edgesFuzzyRadioButton.isSelected() == true) {
			graph.setAttribute(EDGE_TRANSFORMER_SELECTION, EDGE_TRANSFORMER_SELECTION_FUZZY_EDGES);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() == edgesFuzzyIgnoreLoopBox) {
			boolean enabled = edgesFuzzyIgnoreLoopBox.isSelected();
			this.fuzzyEdgeTransformer.setIgnoreSelfLoops(enabled);
			redrawGraph(true);
		} else if (evt.getSource() == edgesFuzzyInterpretAbsoluteBox) {
			boolean enabled = edgesFuzzyInterpretAbsoluteBox.isSelected();
			this.fuzzyEdgeTransformer.setInterpretPercentageAbsolute(enabled);
			redrawGraph(true);
		} else if (evt.getSource() == edgesBestRadioButton || evt.getSource() == edgesFuzzyRadioButton) {
			updateEdgeTransformerSelection();
		} else if (evt.getSource() == edgesConcurrencyActiveBox) {
			setConcurrencyEdgeTransformerActive(edgesConcurrencyActiveBox.isSelected());
		}
	}

	protected void centerHorizontally(JLabel label) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	}

	//	protected class SidePanel extends JPanel {
	//
	//		private static final long serialVersionUID = -5079687464761777691L;
	//		protected Color canvasBg = Color.WHITE;
	//
	//		public SidePanel() {
	//			this.setDoubleBuffered(true);
	//			this.setLayout(new BorderLayout());
	//			this.setBorder(BorderFactory.createEmptyBorder());
	//			this.setOpaque(false);
	//			this.setMinimumSize(new Dimension(5, 200));
	//			this.setMaximumSize(new Dimension(5, 2000));
	//			this.setPreferredSize(new Dimension(5, 1000));
	//		}
	//
	//		public void setCanvasBg(Color color) {
	//			canvasBg = color;
	//		}
	//
	//		protected void paintComponent(Graphics g) {
	//			Graphics2D g2d = (Graphics2D) g;
	//			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	//			// paint rounded boundary of graph panel to the right
	//			g2d.setColor(canvasBg);
	//			g2d.fill(new RoundRectangle2D.Float(0, -0.5f, 15, getHeight(), 8, 8));
	//		}
	//	}

	private String getLegalFileName(String illegalFileName) {
		String[] illegalCharSetsInFileName = { "\\", "/", ":", "?", "\"", "<", ">", "|" };
		int numOfIllegalChars = illegalCharSetsInFileName.length;

		//	System.out.println("!!!The input file name is " + illegalFileName);
		for (int i = 0; i < numOfIllegalChars; i++) {

			illegalFileName = illegalFileName.replace(illegalCharSetsInFileName[i], " ");
		}

		illegalFileName = illegalFileName.replaceAll("-complete", "");
		String leagalFileName = illegalFileName;
		//			System.out.println("!!!!The out  file name is " + illegalFileName);
		return leagalFileName;
	}

	/*
	 * get the causal relations in the specified log
	 */
	public LogRelations getLogRelations(PluginContext context, XLog log) throws CancellationException,
			InterruptedException, ExecutionException {
		XLogInfo summary = XLogInfoFactory.createLogInfo(log);
		Collection<Pair<Integer, PluginParameterBinding>> plugins = context.getPluginManager().find(Plugin.class,
				LogRelations.class, context.getPluginContextType(), true, false, false, XLog.class, summary.getClass());

		if (plugins.isEmpty()) {
			context.log("No plugin found to create log relations, please specify relations manually",
					MessageLevel.ERROR);
			return null;
		}
		// Let's just take the first available plugin for the job of constructing log abstractions
		Pair<Integer, PluginParameterBinding> plugin = plugins.iterator().next();

		// Now, the binding can be executed on the log and the summary
		// FIrst, we instantiate a new context for this plugin, which is a child context of the current context. 
		PluginContext c2 = context.createChildContext("Log Relation Constructor");

		// Let's notify our lifecyclelisteners about the fact that we created a new context. this is
		// optional, but if this is not done, then the user interface doesn't show it (if there is a UI).
		context.getPluginLifeCycleEventListeners().firePluginCreated(c2);

		// At this point, we execute the binding to get the LogRelations. For this, we call the invoke method
		// on the PluginParameterBinding stored in the plugin variable. The return type is LogRelations.class and
		// as input we give the new context c2, the log and the summary. Note that the plugin might return mulitple
		// objects, hence we extract the object with number x, where x is stored as the first element of the plugin
		// variable.

		PluginExecutionResult pluginResult = plugin.getSecond().invoke(c2, log, summary);
		pluginResult.synchronize();
		LogRelations relations = pluginResult.<LogRelations>getResult(plugin.getFirst());
		return relations;
	}

}
