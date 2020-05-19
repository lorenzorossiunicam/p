package org.processmining.plugins.manifestanalysis.visualization.performance;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.pnetprojection.PetrinetGraphP;
import org.processmining.models.pnetprojection.PlaceP;
import org.processmining.models.pnetprojection.TransitionP;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.pnalignanalysis.visualization.projection.ExportPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.FiveColorsLegendPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.GraphBuilder;
import org.processmining.plugins.utils.ProvidedObjectHelper;

import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ManifestPerfPanel<N extends Manifest, C extends IPerfCounter<N>> extends InspectorPanel {
	private static final long serialVersionUID = 4347950522579429502L;

	// GUI component
	protected final ScalableComponent scalable;
	protected JScrollPane scroll;
	private ManifestViewPanel<N, C> viewPanel;
	private JComponent exportPanel;
	private ManifestCaseStatPanel<N, C> caseStatPanel;
	private ManifestElementStatPanel<N, C> elStatPanel;
	private ColorSettingPanel<N, C> colSetPanel;
	private CaseFilterPanelPerf<N, C> caseFilterPanel;
	private PerformanceFilterPanel<N, C> perfFilterPanel;

	// slicker factory
	protected SlickerFactory factory;
	protected SlickerDecorator decorator;

	// for graph visualization
	private final ProMJGraph graph;
	private PetrinetGraphP newNet;
	private Marking mNewNet;

	// for export log
	private static int logExportCounter = 0;

	public enum GraphHighlightObject {
		TRANSITIONCOLOR, TRANSITIONSIZE, TRANSITIONWIDTH, PLACECOLOR, PLACESIZE, PLACEWIDTH, ARCWIDTH, ARCCOLOR
	}

	/**
	 * STATS CONSTANTS
	 */
	// trans stats
	public static final String TRANS_THROUGHPUT_TIME_MIN = "Throughput time (min)";
	public static final String TRANS_THROUGHPUT_TIME_MAX = "Throughput time (max)";
	public static final String TRANS_THROUGHPUT_TIME_AVG = "Throughput time (avg)";
	public static final String TRANS_THROUGHPUT_TIME_STDDEV = "Throughput time (std.dev)";
	public static final String TRANS_WAITING_TIME_MIN = "Waiting time (min)";
	public static final String TRANS_WAITING_TIME_MAX = "Waiting time (max)";
	public static final String TRANS_WAITING_TIME_AVG = "Waiting time (avg)";
	public static final String TRANS_WAITING_TIME_STDDEV = "Waiting time (std.dev)";
	public static final String TRANS_SOJOURN_TIME_MIN = "Sojourn time (min)";
	public static final String TRANS_SOJOURN_TIME_MAX = "Sojourn time (max)";
	public static final String TRANS_SOJOURN_TIME_AVG = "Sojourn time (avg)";
	public static final String TRANS_SOJOURN_TIME_STDDEV = "Sojourn time (std.dev)";
	public static final String TRANS_FREQUENCY = "Transition frequency";
	public static final String TRANS_UNIQUECASES = "Total unique case";

	// place stats
	public static final String PLACE_WAITING_TIME_MIN = "Waiting time (min)";
	public static final String PLACE_WAITING_TIME_MAX = "Waiting time (max)";
	public static final String PLACE_WAITING_TIME_AVG = "Waiting time (avg)";
	public static final String PLACE_WAITING_TIME_STDDEV = "Waiting time (std.dev)";
	public static final String PLACE_SYNC_TIME_MIN = "Synchronization time (min)";
	public static final String PLACE_SYNC_TIME_MAX = "Synchronization time (max)";
	public static final String PLACE_SYNC_TIME_AVG = "Synchronization time (avg)";
	public static final String PLACE_SYNC_TIME_STDDEV = "Synchronization time (std. dev)";
	public static final String PLACE_SOJOURN_TIME_MIN = "Sojourn time (min)";
	public static final String PLACE_SOJOURN_TIME_MAX = "Sojourn time (max)";
	public static final String PLACE_SOJOURN_TIME_AVG = "Sojourn time (avg)";
	public static final String PLACE_SOJOURN_TIME_STDDEV = "Sojourn time (std.dev)";
	public static final String PLACE_FREQUENCY = "Place frequency";

	// stats only for arcs
	public static final String ARC_FREQUENCY = "Arc frequency";

	public static final String NONE = "None";

	/**
	 * TRANSITION SIZE CONSTANTS
	 */
	public static final int DEFTRANSWIDTH = 50;
	public static final int DEFTRANSHEIGHT = 35;
	public static final int ELASTICITYWIDTH = 10;
	public static final int ELASTICITYHEIGHT = 10;

	/**
	 * PLACE SIZE CONSTANTS
	 */
	public static final int DEFPLACEDIAM = 20;
	public static final int ELASTICITYDIAM = 10;

	private Color transparentColor = new Color(255, 255, 255, 0);

	// zoom-related properties
	// The maximal zoom factor for the primary view on the transition system.
	public static final int MAX_ZOOM = 1200;

	// reference to original
	protected Map<PetrinetNode, PetrinetNode> mapOrig2ViewNode = new HashMap<PetrinetNode, PetrinetNode>();
	protected Map<PetrinetNode, PetrinetNode> mapView2OrigNode = new HashMap<PetrinetNode, PetrinetNode>();

	// reference to info
	private C provider;

	// reference to log replay result
	private N manifest;
	private NumberFormat nfDouble = NumberFormat.getInstance();

	/**
	 * @return the scalable
	 */
	public ScalableComponent getScalable() {
		return scalable;
	}

	public ManifestPerfPanel(PluginContext context, N manifest, C provider, String timeAtt, boolean showUnreliableCases) {
		super(context);
		this.manifest = manifest;

		nfDouble.setMaximumFractionDigits(2);
		nfDouble.setMinimumFractionDigits(2);

		/**
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();

		// net and marking to be modified
		PetrinetGraph origNet = manifest.getNet();
		newNet = new PetrinetGraphP("Projected " + origNet.getLabel());
		mNewNet = new Marking();

		GraphLayoutConnection oldLayoutConn = null;
		try {
			oldLayoutConn = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context,
					origNet);
		} catch (ConnectionCannotBeObtained exc) {
			// do nothing
		}
		GraphLayoutConnection newLayoutConn = GraphBuilder.constructPetrinetP(oldLayoutConn, origNet,
				manifest.getInitMarking(), newNet, mNewNet, mapOrig2ViewNode, mapView2OrigNode);

		for (Transition newTrans : newNet.getTransitions()) {
			TransPerfDecorator dec = new TransPerfDecorator();
			dec.setLabel(newTrans.getLabel());
			((TransitionP) newTrans).setDecorator(dec);
		}

		// create caseFilter
		boolean[] caseFilter = new boolean[manifest.getLog().size()];
		Arrays.fill(caseFilter, true);

		if (!showUnreliableCases) {
			for (int i = 0; i < caseFilter.length; i++) {
				caseFilter[i] = manifest.isCaseReliable(i);
			}
		}

		this.provider = provider;
		provider.init(manifest, timeAtt, getClassFor(timeAtt, manifest), caseFilter);

		/**
		 * TAB INFO
		 */
		// add info
		caseStatPanel = createStatPanel(provider);
		elStatPanel = createElStatPanel(provider);

		/**
		 * Main visualization (has to be after creating provider)
		 */
		scalable = GraphBuilder.buildJGraph(newNet, newLayoutConn);
		graph = (ProMJGraph) scalable;

		graph.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent e) {
				// selection of a transition would change the stats
				if (e.getCell() instanceof ProMGraphCell) {
					DirectedGraphNode cell = ((ProMGraphCell) e.getCell()).getNode();
					if (cell instanceof TransitionP) {
						elStatPanel.setTransition((Transition) mapView2OrigNode.get(cell));
					} else if (cell instanceof PlaceP) {
						elStatPanel.setPlace((Place) mapView2OrigNode.get(cell));
					}

					graph.getModel().beginUpdate();
					graph.getModel().endUpdate();
					graph.refresh();
				}
			}
		});

		for (Place p : mNewNet) {
			String label = "" + mNewNet.occurrences(p);
			p.getAttributeMap().put(AttributeMap.LABEL, label);
			p.getAttributeMap().put(AttributeMap.SHOWLABEL, !label.equals(""));
		}

		scroll = new JScrollPane(scalable.getComponent());
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		setLayout(new BorderLayout());
		add(scroll);

		// add additional tab for display settings
		JPanel displayMP = getInspector().addTab("Display");

		// add view panel (zoom in/out)
		viewPanel = createViewPanel(this, MAX_ZOOM);
		addInteractionViewports(viewPanel);

		// add legend panel
		FiveColorsLegendPanel legendPanel = new FiveColorsLegendPanel();
		JPanel lPanel = new JPanel();
		lPanel.setLayout(new BoxLayout(lPanel, BoxLayout.Y_AXIS));
		JLabel lblColoringScale = factory.createLabel("Coloring scale");
		lblColoringScale.setAlignmentX(Component.LEFT_ALIGNMENT);
		lPanel.add(lblColoringScale);
		lPanel.add(legendPanel);
		legendPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		lPanel.setPreferredSize(new Dimension(100, 50));
		lPanel.setMinimumSize(new Dimension(100, 50));

		// add it
		addInfo("Legend", lPanel);
		addInfo("View", viewPanel);
		addInfo("Element Statistics", elStatPanel);
		addInfo("Global Statistics", caseStatPanel);

		/**
		 * TAB DISPLAY SETTING
		 */
		// add colSetPanel
		colSetPanel = createColSetPanel();

		// add elements
		getInspector().addGroup(displayMP, "Color & Size Setting", colSetPanel);

		/**
		 * TAB FILTER
		 */
		// add additional tab for display settings
		JPanel filterP = getInspector().addTab("Filter");

		// add export image
		caseFilterPanel = createCaseFilterPanel(showUnreliableCases);

		perfFilterPanel = createPerfFilterPanel();

		// add elements
		getInspector().addGroup(filterP, "Case Filter", caseFilterPanel);
		getInspector().addGroup(filterP, "Performance Statistics (before any filter is applied)", perfFilterPanel);

		/**
		 * TAB EXPORT
		 */
		// add additional tab for display settings
		JPanel exportP = getInspector().addTab("Export");

		// add export image
		exportPanel = createExportPanel(context, scalable);

		// add elements
		getInspector().addGroup(exportP, "Export", exportPanel);

		constructVisualization();

		// attach zoom to 
		scroll.addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() > 0) {
					viewPanel.getZoom().zoomOut();
				} else if (e.getWheelRotation() < 0) {
					viewPanel.getZoom().zoomIn();
				}

			}
		});

		validate();
		repaint();
	}

	private PerformanceFilterPanel<N, C> createPerfFilterPanel() {
		return new PerformanceFilterPanel<N, C>(this, manifest, provider);
	}

	private void constructVisualization() {

		adjustGraphHighlight(GraphHighlightObject.TRANSITIONCOLOR, TRANS_SOJOURN_TIME_AVG);
		adjustGraphHighlight(GraphHighlightObject.TRANSITIONSIZE, NONE);
		adjustGraphHighlight(GraphHighlightObject.TRANSITIONWIDTH, NONE);
		adjustGraphHighlight(GraphHighlightObject.PLACECOLOR, PLACE_WAITING_TIME_AVG);
		adjustGraphHighlight(GraphHighlightObject.PLACESIZE, NONE);
		adjustGraphHighlight(GraphHighlightObject.PLACEWIDTH, NONE);
		adjustGraphHighlight(GraphHighlightObject.ARCWIDTH, ARC_FREQUENCY);
		adjustGraphHighlight(GraphHighlightObject.ARCCOLOR, ARC_FREQUENCY);
	}

	private CaseFilterPanelPerf<N, C> createCaseFilterPanel(boolean showUnreliableCases) {
		// get labels of cases
		XConceptExtension conceptExt = XConceptExtension.instance();
		XLog log = manifest.getLog();
		List<String> caseLabels = new ArrayList<String>(log.size());
		for (XTrace trace : log) {
			caseLabels.add(conceptExt.extractName(trace));
		}

		return new CaseFilterPanelPerf<N, C>(this, caseLabels, manifest.getCaseReliability(), showUnreliableCases);
	}

	private Class<?> getClassFor(String timeAtt, Manifest m) {
		XAttribute xattr = m.getLog().iterator().next().iterator().next().getAttributes().get(timeAtt);
		if (xattr instanceof XAttributeTimestamp) {
			return java.util.Date.class;
		} else if (xattr instanceof XAttributeContinuous) {
			return Double.class;
		} else if (xattr instanceof XAttributeDiscrete) {
			return Integer.class;
		}
		throw new IllegalArgumentException("Not supported data type");
	}

	private ManifestElementStatPanel<N, C> createElStatPanel(C provider) {
		return new ManifestElementStatPanel<N, C>(manifest, provider);
	}

	private JComponent createExportPanel(final PluginContext context, ScalableComponent graph) {
		ExportPanel panel = new ExportPanel(graph);

		// export filtered log to framework
		JButton exportCases = new SlickerButton("Export shown cases as new Log");
		final XLog log = manifest.getLog();
		exportCases.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (!caseFilterPanel.getSelectedIndex().isEmpty()) {
					String input = JOptionPane.showInputDialog("Write the name of exported log", "Filtered "
							+ XConceptExtension.instance().extractName(log) + "-" + logExportCounter++);
					if (input != null) {
						XFactory f = XFactoryRegistry.instance().currentDefault();
						XLog newLog = f.createLog(log.getAttributes());
						for (int idx : caseFilterPanel.getSelectedIndex()) {
							newLog.add(log.get(idx));
						}
						newLog.getClassifiers().addAll(log.getClassifiers());

						// push it to the framework
						ProvidedObjectHelper.publish(context, input, newLog, XLog.class, true);

						JOptionPane.showMessageDialog(new JPanel(), "Sucessfully exported Filtered log-"
								+ (logExportCounter - 1));
					}
				} else {
					JOptionPane.showMessageDialog(new JPanel(),
							"No cases is selected. Choose at least one case in the \"Filter\" panel.");
				}
			}
		});
		panel.add(exportCases);

		// TODO: export filtered log as manifest for performance view

		return panel;
	}

	private ManifestViewPanel<N, C> createViewPanel(ManifestPerfPanel<N, C> mainPanel, int maxZoom) {
		return new ManifestViewPanel<N, C>(this, maxZoom);
	}

	private ManifestCaseStatPanel<N, C> createStatPanel(C provider) {
		return new ManifestCaseStatPanel<N, C>(provider);
	}

	private ColorSettingPanel<N, C> createColSetPanel() {
		return new ColorSettingPanel<N, C>(this);
	}

	public JComponent getComponent() {
		return scalable.getComponent();
	}

	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	public double getScale() {
		return scalable.getScale();
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public Component getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	public Component getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	public void addInteractionViewports(final ManifestViewPanel<N, C> viewPanel) {
		this.scroll.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentResized(ComponentEvent arg0) {

				if (arg0.getComponent().isValid()) {

					Dimension size = arg0.getComponent().getSize();

					int width = 250, height = 250;

					if (size.getWidth() > size.getHeight())
						height *= size.getHeight() / size.getWidth();
					else
						width *= size.getWidth() / size.getHeight();

					viewPanel.getPIP().setPreferredSize(new Dimension(width, height));
					viewPanel.getPIP().initializeImage();

					viewPanel.getZoom().computeFitScale();
				}
			}

			public void componentShown(ComponentEvent arg0) {
			}

		});
	}

	public void adjustGraphHighlight(GraphHighlightObject selectedObject, String statsType) {
		graph.getModel().beginUpdate();
		// change transition/place/arc color into a certain value
		switch (selectedObject) {
			case TRANSITIONCOLOR :
				changeTransitionColor(statsType);
				break;
			case TRANSITIONSIZE :
				changeTransitionSize(statsType);
				break;
			case TRANSITIONWIDTH :
				changeTransitionWidth(statsType);
				break;
			case PLACECOLOR :
				changePlaceColor(statsType);
				break;
			case PLACESIZE :
				changePlaceSize(statsType);
				break;
			case PLACEWIDTH :
				changePlaceWidth(statsType);
				break;
			case ARCCOLOR :
				changeArcColor(statsType);
				break;
			case ARCWIDTH :
				changeArcWidth(statsType);
				break;
		}
		graph.getModel().endUpdate();
		graph.refresh();
		graph.repaint();
		revalidate();
		repaint();

	}

	private void changeArcWidth(String statsType) {
		if (statsType.equals(NONE)) {
			for (Transition t : newNet.getTransitions()) {
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = newNet.getInEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.LINEWIDTH, 0.5f);
					edge.getAttributeMap().put(AttributeMap.TOOLTIP, "<no value>");
				}
				edges = newNet.getOutEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.LINEWIDTH, 0.5f);
					edge.getAttributeMap().put(AttributeMap.TOOLTIP, "<no value>");
				}
			}
		} else {
			double[] values = getAllValOfTrans(ManifestPerfPanel.TRANS_FREQUENCY, true);

			Transition[] origTrans = provider.getTransArray();
			for (int i = 0; i < origTrans.length; i++) {
				float suggestedArcWidth = getAppropriateStrokeWidth(values[i]);
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = newNet
						.getInEdges(mapOrig2ViewNode.get(origTrans[i]));
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.LINEWIDTH, suggestedArcWidth);
					edge.getAttributeMap().put(AttributeMap.TOOLTIP, "" + values[i]);
				}
				edges = newNet.getOutEdges(mapOrig2ViewNode.get(origTrans[i]));
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.LINEWIDTH, suggestedArcWidth);
					edge.getAttributeMap().put(AttributeMap.TOOLTIP, "" + values[i]);
				}
			}
		}
	}

	private int getAppropriateStrokeWidth(double value) {
		// update width of incoming and outgoing arcs
		float suggestedArcWidth = 1f;

		if (Double.compare(value, 0) > 0) {
			suggestedArcWidth += new Float(Math.log(Math.E) * Math.log10(value));
		}

		return (int) suggestedArcWidth;
	}

	private void changeArcColor(String statsType) {
		if (statsType.equals(NONE)) {
			for (Transition t : newNet.getTransitions()) {
				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = newNet.getInEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.BLACK);
				}
				edges = newNet.getOutEdges(t);
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.BLACK);
				}
			}
		} else {
			double[] values = getAllValOfTrans(ManifestPerfPanel.TRANS_FREQUENCY, true);
			double[] minmax = getMinMax(values);

			Transition[] origTrans = provider.getTransArray();
			for (int i = 0; i < origTrans.length; i++) {
				int rgb = minmax[1] > 0 ? 150 - (int) ((values[i] - minmax[0]) / (minmax[1] - minmax[0]) * 150) : 150;
				Color suggestedColor = new Color(rgb, rgb, rgb);

				Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = newNet
						.getInEdges(mapOrig2ViewNode.get(origTrans[i]));
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.EDGECOLOR, suggestedColor);
				}
				edges = newNet.getOutEdges(mapOrig2ViewNode.get(origTrans[i]));
				for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
					edge.getAttributeMap().put(AttributeMap.EDGECOLOR, suggestedColor);
				}
			}
		}
	}

	private void changePlaceWidth(String statsType) {
		if (statsType.equals(NONE)) {
			for (Place p : newNet.getPlaces()) {
				p.getAttributeMap().put(AttributeMap.BORDERWIDTH, 1);
			}
		} else {
			Place[] pArr = provider.getPlaceArray();
			for (int i = 0; i < pArr.length; i++) {
				double[] stats = provider.getPlaceStats(i);
				mapOrig2ViewNode
						.get(pArr[i])
						.getAttributeMap()
						.put(AttributeMap.BORDERWIDTH,
								getAppropriateStrokeWidth(getValFromPlaceStats(statsType, stats)));
			}
		}
	}

	private void changePlaceSize(String statsType) {
		if (statsType.equals(NONE)) {
			for (Place p : newNet.getPlaces()) {
				p.getAttributeMap().put(AttributeMap.SIZE, new Dimension(DEFPLACEDIAM, DEFPLACEDIAM));
			}
		} else {
			Place[] pArr = provider.getPlaceArray();
			double[] values = new double[pArr.length];
			for (int i = 0; i < values.length; i++) {
				double[] stats = provider.getPlaceStats(i);
				values[i] = getValFromPlaceStats(statsType, stats);
			}

			double[] minmax = getMinMax(values);

			double median = (minmax[1] + minmax[0]) / 2;
			double distance = (minmax[1] - minmax[0]) / 2;

			for (int i = 0; i < pArr.length; i++) {
				int suggestedDiam = DEFPLACEDIAM + (int) Math.floor(ELASTICITYDIAM * (values[i] - median) / distance);

				mapOrig2ViewNode.get(pArr[i]).getAttributeMap()
						.put(AttributeMap.SIZE, new Dimension(suggestedDiam, suggestedDiam));
			}
		}
	}

	private void changePlaceColor(String statsType) {
		if (statsType.equals(NONE)) {
			for (Place p : newNet.getPlaces()) {
				p.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.WHITE);
			}
		} else {
			// identify minimum and maximum value
			double[] temp = new double[provider.getNumPlaces()];
			Arrays.fill(temp, Double.MAX_VALUE);
			int pointer = 0;

			// find limits
			Place[] placeArr = provider.getPlaceArray();
			for (int i = 0; i < placeArr.length; i++) {
				double[] stats = provider.getPlaceStats(i);
				double val = getValFromPlaceStats(statsType, stats);
				if (!Double.isNaN(val)) {
					temp[pointer++] = val;
				}
			}

			Arrays.sort(temp);

			if (pointer > 0) {
				for (int i = 0; i < placeArr.length; i++) {
					mapOrig2ViewNode
							.get(placeArr[i])
							.getAttributeMap()
							.put(AttributeMap.FILLCOLOR,
									getAppropriateFillColoring(temp[0], temp[pointer - 1],
											getValFromPlaceStats(statsType, provider.getPlaceStats(i))));
				}
			} else {
				for (int i = 0; i < placeArr.length; i++) {
					mapOrig2ViewNode.get(placeArr[i]).getAttributeMap().put(AttributeMap.FILLCOLOR, transparentColor);
				}
			}
		}
	}

	private double getValFromPlaceStats(String statsType, double[] stats) {
		if (statsType.equals(PLACE_WAITING_TIME_MIN)) {
			return stats[0];
		} else if (statsType.equals(PLACE_WAITING_TIME_MAX)) {
			return stats[1];
		} else if (statsType.equals(PLACE_WAITING_TIME_AVG)) {
			return stats[2];
		} else if (statsType.equals(PLACE_WAITING_TIME_STDDEV)) {
			return stats[3];
		} else if (statsType.equals(PLACE_SYNC_TIME_MIN)) {
			return stats[5];
		} else if (statsType.equals(PLACE_SYNC_TIME_MAX)) {
			return stats[6];
		} else if (statsType.equals(PLACE_SYNC_TIME_AVG)) {
			return stats[7];
		} else if (statsType.equals(PLACE_SYNC_TIME_STDDEV)) {
			return stats[8];
		} else if (statsType.equals(PLACE_SOJOURN_TIME_MIN)) {
			return stats[10];
		} else if (statsType.equals(PLACE_SOJOURN_TIME_MAX)) {
			return stats[11];
		} else if (statsType.equals(PLACE_SOJOURN_TIME_AVG)) {
			return stats[12];
		} else if (statsType.equals(PLACE_SOJOURN_TIME_STDDEV)) {
			return stats[13];
		} else if (statsType.equals(PLACE_FREQUENCY)) {
			return stats[4]; // from freq waiting time
		}
		throw new IllegalArgumentException("No index for place stats, [" + statsType + "] in placeStats");
	}

	private void changeTransitionWidth(String statsType) {
		if (statsType.equals(NONE)) {
			for (Transition t : newNet.getTransitions()) {
				t.getAttributeMap().put(AttributeMap.BORDERWIDTH, 1);
			}
		} else {
			Transition[] origTrans = provider.getTransArray();
			for (int i = 0; i < origTrans.length; i++) {
				double[] stats = provider.getPlaceStats(i);
				mapOrig2ViewNode
						.get(origTrans[i])
						.getAttributeMap()
						.put(AttributeMap.BORDERWIDTH,
								getAppropriateStrokeWidth(getValFromTransStats(statsType, stats)));
			}
		}
	}

	private void changeTransitionColor(String statsType) {
		if (statsType.equals(NONE)) {
			for (Transition t : newNet.getTransitions()) {
				if (!t.isInvisible()) {
					t.getAttributeMap().put(AttributeMap.FILLCOLOR, transparentColor);
				}
			}
		} else {
			// identify minimum and maximum value
			double[] temp = new double[provider.getNumTrans()];
			Arrays.fill(temp, Double.MAX_VALUE);
			int pointer = 0;

			// find limits
			for (Transition t : newNet.getTransitions()) {
				if (!t.isInvisible()) {
					double[] stats = provider.getTransStats(manifest,
							provider.getEncOfTrans((Transition) mapView2OrigNode.get(t)));
					if (stats != null) {
						double val = getValFromTransStats(statsType, stats);
						if (!Double.isNaN(val)) {
							temp[pointer++] = val;
						}
					}
				}
			}

			Arrays.sort(temp);

			for (Transition t : newNet.getTransitions()) {
				if (!t.isInvisible()) {
					double[] allStats = provider.getTransStats(manifest,
							provider.getEncOfTrans((Transition) mapView2OrigNode.get(t)));
					if (allStats != null) {
						double stats = getValFromTransStats(statsType, allStats);
						Color color = getAppropriateFillColoring(temp[0], !Double.isNaN(stats) ? temp[pointer - 1]
								: temp[0], stats);
						t.getAttributeMap().put(AttributeMap.FILLCOLOR, color);

						((TransPerfDecorator) ((TransitionP) t).getDecorator())
								.setLightColorLabel(adjustLightDarkTextColor(color));

						// format visualization
						if (statsType.equals(TRANS_FREQUENCY)) {
							((TransPerfDecorator) ((TransitionP) t).getDecorator()).setStats(String
									.valueOf((int) stats));
						} else {
							// time related, then format it if not NaN
							if (Double.isNaN(stats)) {
								((TransPerfDecorator) ((TransitionP) t).getDecorator()).setStats("NaN");
							} else {
								((TransPerfDecorator) ((TransitionP) t).getDecorator()).setStats(TimeFormatter
										.formatTime(stats, nfDouble));
							}
						}
					}

				}
			}
		}
	}

	/**
	 * Return true if set to light
	 * 
	 * @param t
	 * @param color
	 * @return
	 */
	private boolean adjustLightDarkTextColor(Color color) {
		return (color.equals(PerformanceConstants.BAD) || color.equals(PerformanceConstants.BADTOMEDIUM));
	}

	private void changeTransitionSize(String statsType) {
		if (statsType.equals(NONE)) {
			for (Transition t : newNet.getTransitions()) {
				t.getAttributeMap().put(AttributeMap.SIZE, new Dimension(DEFTRANSWIDTH, DEFTRANSHEIGHT));
			}
		} else {
			double[] values = getAllValOfTrans(statsType, true);
			double[] minmax = getMinMax(values);

			double median = (minmax[1] + minmax[0]) / 2;
			double distance = (minmax[1] - minmax[0]) / 2;

			Transition[] origTrans = provider.getTransArray();
			for (int i = 0; i < origTrans.length; i++) {
				int suggestedWidth = DEFTRANSWIDTH
						+ (int) Math.floor(ELASTICITYWIDTH * (values[i] - median) / distance);
				int suggestedHeight = DEFTRANSHEIGHT
						+ (int) Math.floor(ELASTICITYHEIGHT * (values[i] - median) / distance);

				mapOrig2ViewNode.get(origTrans[i]).getAttributeMap()
						.put(AttributeMap.SIZE, new Dimension(suggestedWidth, suggestedHeight));
			}
		}
	}

	/**
	 * @param limits
	 *            bottom and upper limit for medium value
	 * @param value
	 * @return
	 */
	private Color getAppropriateFillColoring(double min, double max, double value) {
		if (Double.isNaN(value)) {
			return transparentColor;
		} else
		// get color in between
		if ((Double.compare(max, 0.0) > 0) && (!Double.isNaN(max))) {
			int res = (int) ((value - min) * 5 / (max - min));
			if (res == 0) {
				return PerformanceConstants.GOOD;
			} else if (res == 1) {
				return PerformanceConstants.MEDTOGOOD;
			} else if (res == 2) {
				return PerformanceConstants.MEDIUM;
			} else if (res == 3) {
				return PerformanceConstants.BADTOMEDIUM;
			} else {
				return PerformanceConstants.BAD;
			}

			// this is for seamless coloring - green to red
			//			double res = (value - min) / (max - min);
			//			int red, green;
			//			if (Double.compare(res, 0.5) > 0) {
			//				red = 255;
			//				green = (int) Math.floor((1 - (res - 0.5) / 0.5) * 255);
			//			} else {
			//				red = (int) Math.floor((res / 0.5) * 255);
			//				green = 255;
			//			}
			//			return new Color(red, green, 0);
		} else {
			return PerformanceConstants.GOOD;
		}
	}

	/**
	 * return the desired value
	 * 
	 * @param statsType
	 * @param stats
	 *            from provider.getTransStats() method
	 * @return
	 */
	private double getValFromTransStats(String statsType, double[] stats) {
		if (statsType.equals(TRANS_THROUGHPUT_TIME_MIN)) {
			return stats[0];
		} else if (statsType.equals(TRANS_THROUGHPUT_TIME_MAX)) {
			return stats[1];
		} else if (statsType.equals(TRANS_THROUGHPUT_TIME_AVG)) {
			return stats[2];
		} else if (statsType.equals(TRANS_WAITING_TIME_MIN)) {
			return stats[4];
		} else if (statsType.equals(TRANS_WAITING_TIME_MAX)) {
			return stats[5];
		} else if (statsType.equals(TRANS_WAITING_TIME_AVG)) {
			return stats[6];
		} else if (statsType.equals(TRANS_SOJOURN_TIME_MIN)) {
			return stats[8];
		} else if (statsType.equals(TRANS_SOJOURN_TIME_MAX)) {
			return stats[9];
		} else if (statsType.equals(TRANS_SOJOURN_TIME_AVG)) {
			return stats[10];
		} else if (statsType.equals(TRANS_FREQUENCY)) {
			return stats[12];
		}
		throw new IllegalArgumentException("No index for [" + statsType + "] in transStats");
	}

	private double[] getAllValOfTrans(String statsType, boolean includeInvi) {
		// find limits
		Transition[] origTrans = provider.getTransArray();
		double[] values = new double[origTrans.length];

		for (int i = 0; i < origTrans.length; i++) {
			if ((origTrans[i].isInvisible()) && (!includeInvi)) {
				continue;
			} else {
				// special for move invi, only have stats when statsType is frequency
				if ((includeInvi) && (origTrans[i].isInvisible())) {
					if (statsType.equals(TRANS_FREQUENCY)) {
						values[i] = provider.getMoveModelOfTrans(i);
					} else {
						values[i] = 0.00;
					}
				} else {
					double[] stats = provider.getTransStats(manifest, i);
					if (stats != null) {
						double val = getValFromTransStats(statsType, stats);
						values[i] = val;
					} else {
						if (statsType.equals(TRANS_FREQUENCY)) {
							values[i] = provider.getMoveModelOfTrans(i);
						} else {
							values[i] = 0.00;
						}
					}
				}
			}
		}

		return values;
	}

	private double[] getMinMax(double[] data) {
		// identify minimum and maximum value
		double min = Double.NaN;
		double max = Double.NaN;

		for (double d : data) {
			if (!Double.isNaN(d)) {
				if (Double.isNaN(min)) {
					min = d;
				} else {
					if (Double.compare(min, d) > 0) {
						min = d;
					}
				}
				if (Double.isNaN(max)) {
					max = d;
				} else {
					if (Double.compare(max, d) < 0) {
						max = d;
					}
				}
			}
		}

		return new double[] { min, max };
	}

	public void filterAlignmentPreserveIndex(Set<Integer> setSelectedIndices) {
		// if index of a trace exists in the preservedIndex array, the trace is visualized
		boolean[] caseFilter = new boolean[manifest.getLog().size()];
		Arrays.fill(caseFilter, false);
		for (int i : setSelectedIndices) {
			caseFilter[i] = true;
		}
		this.provider.calculatePerformance(manifest, caseFilter); // update GUI performance
		this.caseFilterPanel.selectCases(caseFilter); // select cases in GUI
		this.caseStatPanel.setInfoProvider(provider); // update cases GUI
		this.caseStatPanel.repaint();

		constructVisualization();

		validate();
		repaint();
	}
}
