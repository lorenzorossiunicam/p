package org.processmining.plugins.fuzzymodel.miner.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.in.XesXmlGZIPParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMColors;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FMLogEvents;
import org.processmining.models.graphbased.directed.fuzzymodel.util.FuzzyMinerLog;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.fuzzymap.util.FuzzyMapPatternNode;
import org.processmining.plugins.log.logabstraction.LogRelations;

public class FuzzyModelPanel extends JPanel {

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
	private static final String FILE_SEPERATOR = File.separator;
	private static final String FILE_LOCATION_LOG_ATT_KEY = "fileLocation";
	private static final String PARENT_FILE_NAME_LOG_ATT_KEY = "parentFileName";

	private XLog log = null;
	private MutableFuzzyGraph graph;
	private ProMJGraph jgraph;
	private PluginContext context;

	private JPanel rootPanel;
	private ProMJGraphPanel graphPanel;
	private ProMJGraphPanel clustGraphPanel;
	private ProMJGraphPanel patternGraphPanel;
	private ProMJGraphPanel molecularGraphPanel;
	private HashSet<File> patternLogDirectorySet = new HashSet<File>();
	private boolean isPatternBasedTransformedLog = false;
	private Map<FMNode, JFrame> clusterFrames;
	private Map<FMNode, JFrame> molecularFrames;

	protected LogRelations molecularInnerRelations;

	public MutableFuzzyGraph getExportFuzzyGraphObjects() throws Exception {
		//add every cluster node and its corresponding graph 
		MutableFuzzyGraph exportGraph = (MutableFuzzyGraph) graph.clone();

		// when getting the cluster's inner graph and the molecular activity's graph, 
		// the color of the nodes in the original whole graph are changed 
		for (FMClusterNode clusternode : exportGraph.getClusterNodes()) {
			MutableFuzzyGraph clusterGraph = (MutableFuzzyGraph) (clusternode.getClusterGraphPanel(context, log)
					.getGraph().getProMGraph());
			exportGraph.getAbstractionNodeGraphMap().put(clusternode, clusterGraph);
		}
		Vector<MutableFuzzyGraph> graphSet = new Vector<MutableFuzzyGraph>();
		graphSet.add(exportGraph);
		/*
		 * Iterator<MutableFuzzyGraph> graphIterator = graphSet.iterator();
		 * while(graphIterator.hasNext()) {
		 */
		for (int j = 0; j < graphSet.size(); j++) {
			MutableFuzzyGraph curGraph = graphSet.get(j);
			int size = curGraph.getNumberOfInitialNodes();
			for (int i = 0; i < size; i++) {
				FMNode node = curGraph.getPrimitiveNode(i);
				MutableFuzzyGraph innerGraph = null;
				innerGraph = getMolecularGraph(node);
				if (innerGraph != null) {
					exportGraph.getAbstractionNodeGraphMap().put(node, innerGraph);
					graphSet.add(innerGraph);
				}
			}
		}

		Set<FMNode> originalNodes = exportGraph.getNodes();
		//set the color of the nodes in the whole graph back
		for (FMNode node : originalNodes) {
			if (node instanceof FMClusterNode) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getClusterBackgroundColor());
			} else if (exportGraph.getAbstractionNodeGraphMap().containsKey(node)) {
				//if the node is an abstract node
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getAbstractBackgroundColor());
			} else {
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
			}
		}
		//when visulize the exportGraph, there is no unary metrics panel and no binary metrics panel
		exportGraph.setMetrics(null);
		return exportGraph;
	}

	public FuzzyModelPanel(PluginContext context, MetricsRepository metrics) {
		this(context, new MutableFuzzyGraph(metrics), metrics.getLogReader(), metrics.getNumberOfLogEvents());
	}

	public FuzzyModelPanel(PluginContext context, MetricsRepository metrics, XLog log, int showNumberOfNodes) {
		this(context, new MutableFuzzyGraph(metrics), log, showNumberOfNodes);
	}

	public FuzzyModelPanel(PluginContext context, MutableFuzzyGraph graph) {
		this(context, graph, graph.getLog(), graph.getLogEvents().size());
	}

	public FuzzyModelPanel(PluginContext context, MutableFuzzyGraph graph, XLog log, int showNumberOfNodes) {
		this.log = log;
		this.graph = graph;
		this.context = context;
		this.setBackground(new Color(240, 240, 240));
		initializeGui();
		redrawGraph(); //Set the Fuzzy Graph
		clusterFrames = new HashMap<FMNode, JFrame>();
		molecularFrames = new HashMap<FMNode, JFrame>();
	}

	public FuzzyGraph getGraph() {
		return graph;
	}

	private void initializeGui() {
		rootPanel = new JPanel();
		rootPanel.setBorder(BorderFactory.createEmptyBorder());
		rootPanel.setBackground(new Color(100, 100, 100));
		rootPanel.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.add(rootPanel, BorderLayout.CENTER);
	}

	public void redrawGraph() {
//		System.out.println("[FuzzyModelPanel] Graph size = " + graph.getNodes().size() + "," + graph.getClusterNodes().size() + "," + graph.getEdgeImpls().size());

		setColorOfAbstractActivityInGraph(graph);

		if (graphPanel == null) {
//			System.out.println("Creating new graph panel");
			graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
//			System.out.println("Replacing graph panel");
			rootPanel.add(graphPanel, BorderLayout.CENTER);
		}
		
		jgraph = graphPanel.getGraph();
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

	private void setColorOfAbstractActivityInGraph(MutableFuzzyGraph mGraph) {
		for (FMNode node : mGraph.getNodes()) {
			if (isMolecularNode(node))
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.CYAN);
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
						clustGraphPanel = ((FMClusterNode) fmNode).getClusterGraphPanel(context, log);
						//set the color of the abstract activities in the clusterGraph
						setColorOfAbstractActivityInGraph((MutableFuzzyGraph) clustGraphPanel.getGraph().getProMGraph());
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

				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						HashSet<FMNode> adjacentNodes = new HashSet<FMNode>();
						adjacentNodes.addAll(((FMClusterNode) fmNode).getPredecessors());
						adjacentNodes.addAll(((FMClusterNode) fmNode).getSuccessors());
						setAdjacentNodesColorBack(adjacentNodes);
					}
				});
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

	private void setAdjacentNodesColorBack(HashSet<FMNode> adjacentNodes) {
		for (FMNode node : adjacentNodes) {
			if (node instanceof FMClusterNode) {
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getClusterBackgroundColor());
			} else {
				node.getAttributeMap().put(AttributeMap.SQUAREBB, true);
				node.getAttributeMap().put(AttributeMap.FILLCOLOR, FMColors.getPrimitiveBackgroundColor());
			}
		}
	}

	public boolean isMolecularNode(FMNode fmNode) {
		//	molecularGraphPanel = null;
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

	/**
	 * show the detail sub-process graph of the abstract/molecule activity
	 * 
	 * @param fmNode
	 * @return
	 */
	public MutableFuzzyGraph getMolecularGraph(FMNode fmNode) {
		molecularGraphPanel = null;
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
										molecularGraphPanel = getMolecularGraphPanel(context, patternSubLog, false,
												fmNode);
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
		//for export the inner graph of the abstract activity
		if (molecularGraphPanel != null) {
			MutableFuzzyGraph innerGraph = (MutableFuzzyGraph) (molecularGraphPanel.getGraph().getProMGraph());
			return innerGraph;
		} else {
			return null;
		}
	}

	/**
	 * show the detail sub-process graph of the abstract/molecule activity
	 * 
	 * @param fmNode
	 * @return
	 */
	public MutableFuzzyGraph showMolecularGraph(FMNode fmNode) {
		molecularGraphPanel = null;
		File patternLogDirectory;
		if (isPatternBasedTransformedLog) {
			if (patternLogDirectorySet.isEmpty()) {
				setTransformedLogDirectory();
			}
			String legalPatternName = getLegalFileName(fmNode.getElementName());
			String legalPatternLogFileName = legalPatternName + ".xes.gz";
			boolean haveFoundSubLog = false;
			//		System.out.println("----the legal pattern log file name is : " + legalPatternLogFileName);
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
								haveFoundSubLog = true;
								// ljf
								/*
								 * Test whether we already have a frame open for
								 * this node.
								 */
								if (!molecularFrames.containsKey(fmNode)) {
									/*
									 * No, we do not. Create a frame for this
									 * node.
									 */
									try {
										List<XLog> subLogs = parser.parse(file);
										XLog patternSubLog = subLogs.get(0);
										//create molecular graph
										molecularGraphPanel = getMolecularGraphPanel(context, patternSubLog, false,
												fmNode);
										//set the color of the abstract activities in the molecular Graph
										setColorOfAbstractActivityInGraph((MutableFuzzyGraph) molecularGraphPanel
												.getGraph().getProMGraph());
										JFrame frame1 = new JFrame("Detailed Graph For Subprocess ' "
												+ fmNode.getElementName() + "+" + fmNode.getEventType() + " '");
										frame1.setContentPane(molecularGraphPanel);
										frame1.setLocation(90, 90);
										frame1.setSize(750, 400);
										/*
										 * Hide the frame when closed. If we
										 * also would dispose of it, then we
										 * might not be able to make it visible
										 * the next time. Therefore, we hide but
										 * do not dispose.
										 */
										/*
										 * Register the frame for this node.
										 */
										molecularFrames.put(fmNode, frame1);
										installMolecularNodeGraphPanelListener();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								/*
								 * Now there should be a frame for this node.
								 * Get it and make it visible.
								 */
								JFrame frame = molecularFrames.get(fmNode);
								frame.setVisible(true);
							}
							break;
						}
					}
				}
			}
		}
		//for export the inner graph of the abstract activity
		if (molecularGraphPanel != null) {
			MutableFuzzyGraph innerGraph = (MutableFuzzyGraph) (molecularGraphPanel.getGraph().getProMGraph());
			return innerGraph;
		} else {
			return null;
		}
	}

	/*
	 * After pre-processing the log with patterns,there will be many molecular
	 * node in the new log. The molecular node contains many atomic nodes, this
	 * method is to get the inner graph inside the molecular node.
	 */
	public ProMJGraphPanel getMolecularGraphPanel(PluginContext context, XLog log, boolean isAtomicNode, FMNode node) {
		ProMJGraphPanel graphPanel = null;
		Set<FMNode> startNodes = new HashSet<FMNode>();
		Set<FMNode> endNodes = new HashSet<FMNode>();
		MutableFuzzyGraph molecularInnerGraph = new MutableFuzzyGraph(log);
		//molecularInnerGraph.getAttributeMap().put(AttributeMap.PREF_ORIENTATION, SwingConstants.NORTH);
		FMLogEvents logEvents = FuzzyMinerLog.getLogEvents(log);
		Set<XEvent> startEvents = FuzzyMinerLog.getStartEvents(log);
		Set<XEvent> endEvents = FuzzyMinerLog.getEndEvents(log);
		int eventsCount = logEvents.getEventsCount();
		Map<String, FMNode> eventClassIDFMNodeMap = new HashMap<String, FMNode>();
		//set the significance of the inner node in the molecular graph to be the significance of the molecular node
		//	Double nodeSignificance = node.getSignificance();
		Double nodeSignificance = 1.0;
		try {
			//create nodes
			for (int i = 0; i < eventsCount; i++) {
				XEvent evt = logEvents.get(i);
				String evtName = FuzzyMinerLog.getEventName(evt);
				String evtType = FuzzyMinerLog.getEventType(evt);
				String evtLabel = "<html>" + evtName + "<br>" + evtType + "<br>"
						+ MutableFuzzyGraph.format(nodeSignificance) + "</html>";
				FMNode evtNode = new FMNode(molecularInnerGraph, i, evtLabel);
				if (FuzzyMinerLog.isEventAdded(evt, startEvents)) {
					startNodes.add(evtNode);
				}
				if (FuzzyMinerLog.isEventAdded(evt, endEvents)) {
					endNodes.add(evtNode);
				}
				molecularInnerGraph.addNode(evtNode, i);
				String eventClassID = evtName + "+" + evtType;
				eventClassIDFMNodeMap.put(eventClassID, evtNode);
			}
			//create arcs
			molecularInnerRelations = getLogRelations(context, log);

			/*
			 * Map<Pair<XEventClass,XEventClass>,Double> causalRelations =
			 * molecularInnerRelations.getCausalDependencies();
			 * for(Pair<XEventClass,XEventClass> evtPair:
			 * causalRelations.keySet()){ Double relationDegree =
			 * causalRelations.get(evtPair);
			 */
			Map<Pair<XEventClass, XEventClass>, Integer> directFollowRelations = molecularInnerRelations
					.getDirectFollowsDependencies();
			for (Pair<XEventClass, XEventClass> evtPair : directFollowRelations.keySet()) {
				Integer relationDegree = directFollowRelations.get(evtPair);
				if (relationDegree > 0) {
					XEventClass first = evtPair.getFirst();
					XEventClass second = evtPair.getSecond();

					String firstEvtId = first.getId();
					String secondEvtId = second.getId();
					if (eventClassIDFMNodeMap.containsKey(firstEvtId) && eventClassIDFMNodeMap.containsKey(firstEvtId)) {
						FMNode firstNode, secondNode;
						firstNode = eventClassIDFMNodeMap.get(firstEvtId);
						secondNode = eventClassIDFMNodeMap.get(secondEvtId);
						molecularInnerGraph.addEdge(firstNode, secondNode, 0.5, 0.5);
					}
				}
			}
			//LJF add start
			//add start nodes and end nodes of the inner graph of this molecule node
			molecularInnerGraph.setStartNodes(startNodes);
			molecularInnerGraph.setEndNodes(endNodes);
			//LJF add end
			graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, molecularInnerGraph);
			return graphPanel;

		} catch (CancellationException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return graphPanel;
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
	public void installMolecularNodeGraphPanelListener() {
		GraphSelectionModel model = molecularGraphPanel.getGraph().getSelectionModel();
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
		XAttributeMap subAttrs = parentFileAbsolutePath.getAttributes();
		Iterator<XAttribute> ancesetorFiles = subAttrs.values().iterator();
		while (ancesetorFiles.hasNext()) {
			String ancesterLogFile = ((XAttributeLiteral) ancesetorFiles.next()).getValue();
			if (ancesterLogFile.contains(FILE_SEPERATOR)) {
				delimit = ancesterLogFile.indexOf(".");
				String ancestorLogFileDir = ancesterLogFile.substring(0, delimit) + FILE_SEPERATOR;
				patternLogDirectorySet.add(new File(ancestorLogFileDir));
			}
		}
	}

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
