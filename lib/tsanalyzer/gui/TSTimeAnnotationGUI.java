/*
 * Created on July. 02, 2007
 * 
 * Author: Minseok Song (c) 2006 Technische Universiteit Eindhoven, Minseok Song
 * all rights reserved
 * 
 * LICENSE WARNING: This code has been created within the realm of an STW
 * project. The question of under which license to publish this code, or whether
 * to have it published openly at all, is still unclear. Before this code can be
 * released in any form, be it binary or source code, this issue has to be
 * clarified with the STW. Please do not add this file to any build or source
 * export transferred to anybody outside the TM.IS group.
 */

package org.processmining.plugins.tsanalyzer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.payload.PayloadTransitionSystem;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;
import org.processmining.plugins.tsanalyzer.annotation.time.Duration;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeStateAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeTransitionAnnotation;
import org.processmining.plugins.tsanalyzer.annotation.time.TimeTransitionSystemAnnotation;

import com.fluxicon.slickerbox.components.AutoFocusButton;

public class TSTimeAnnotationGUI extends JPanel implements GuiNotificationTarget {

	private static final long serialVersionUID = -3036511492888928964L;

	public static String INTER = "inter";
	public static String OVER = "overall";

	private static final Color blueColor = new Color(56, 189, 255);;
	private static final Color yellowColor = new Color(250, 250, 157);
	private static final Color redColor = new Color(255, 102, 120);

	// Performance objects
	private JPanel splitPane;
	private final JPanel menuPanel = new JPanel();
	private JSplitPane chartPanel;
	//	protected GUIPropertyInteger bot = new GUIPropertyInteger("", 0, 0, 100);
	//	protected GUIPropertyInteger min = new GUIPropertyInteger("...Blue...", 60, 0, 100);
	//	protected GUIPropertyInteger max = new GUIPropertyInteger("...Yellow...", 80, 0, 100);
	//	protected GUIPropertyInteger top = new GUIPropertyInteger("...Red...", 100, 0, 100);

	protected GUIPropertyListEnumeration colorBySort;

	//	private JSplitPane minSplit = null;
	//	private JSplitPane maxSplit = null;
	//	private JLabel blueLabel;
	//	private JLabel yellowLabel;
	//	private JLabel redLabel;

	private TripleSlider slider;

	private StatisticsAnnotationPanel table;

	private final PayloadTransitionSystem<?> system;
	private final TimeTransitionSystemAnnotation annotation;
	private final ViewSpecificAttributeMap map;

	private final ProMJGraphPanel graphVisPanel;

	//	private final PluginContext context;

	public TSTimeAnnotationGUI(final PluginContext context, PayloadTransitionSystem<?> system,
			TimeTransitionSystemAnnotation annotation/*
													 * ,
													 * JGraphVisualizationPanel
													 * panel
													 */) {
		super();
		this.system = system;
		this.annotation = annotation;
		map = new ViewSpecificAttributeMap();
		//		this.context = context;
		graphVisPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, system, map);

		setLayout(new BorderLayout());
		removeAll();
		initGraphMenu();
		buildMainMenuGui();
		updateGUI();
	}

	public void initGraphMenu() {
		table = new StatisticsAnnotationPanel();
		splitPane = new JPanel(new BorderLayout());
		chartPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// Initially, the divider will split the 'real estate' 80/20. 
		chartPanel.setResizeWeight(0.8);
		initColorBySort();
		menuPanel.setLayout(new BorderLayout());
		menuPanel.add(colorBySort.getPropertyPanel(), BorderLayout.WEST);
		//		menuPanel.add(bot.getPropertyPanel());
		//		bot.disable();
		//		menuPanel.add(min.getPropertyPanel());
		//		JLabel label = new JLabel("Yellow");
		//		label.setBackground(yellowColor);
		//		label.setOpaque(true);
		//		menuPanel.add(label);
		//		menuPanel.add(max.getPropertyPanel());
		//		menuPanel.add(top.getPropertyPanel());
		//		top.disable();

		//		maxSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//		minSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		//		blueLabel = new JLabel("Blue (0% - 60%)");
		//		blueLabel.setBackground(blueColor);
		//		blueLabel.setOpaque(true);
		//		yellowLabel = new JLabel("Yellow (60% - 80%)");
		//		yellowLabel.setBackground(yellowColor);
		//		yellowLabel.setOpaque(true);
		//		redLabel = new JLabel("Red (80% - 100%)");
		//		redLabel.setBackground(redColor);
		//		redLabel.setOpaque(true);
		//		minSplit.setLeftComponent(blueLabel);
		//		minSplit.setRightComponent(yellowLabel);
		//		minSplit.setResizeWeight(0.75);
		//		minSplit.setBorder(null);
		//		maxSplit.setLeftComponent(minSplit);
		//		maxSplit.setRightComponent(redLabel);
		//		maxSplit.setResizeWeight(0.8);
		//		maxSplit.setBorder(null);
		//		int maxValue = (100*maxSplit.getDividerLocation())/maxSplit.getMaximumDividerLocation(); //max.getValue();
		//		int minValue = (maxValue*minSplit.getDividerLocation())/minSplit.getMaximumDividerLocation(); //min.getValue();
		//
		//		if (maxValue <= 0) {
		//			maxValue = 80;
		//		}
		//		if (minValue <= 0) {
		//			minValue = 60;
		//		}
		//		blueLabel.setText("Blue (0% - " + minValue + "%)");
		//		yellowLabel.setText("Yellow (" + minValue + "% - " + maxValue + "%)");
		//		redLabel.setText("Red (" + maxValue + "% - 100%)");

		slider = new TripleSlider();
		slider.setValues(0.6, 0.2);
		slider.setColor(0, blueColor);
		slider.setColor(1, yellowColor);
		slider.setColor(2, redColor);

		menuPanel.add(slider, BorderLayout.CENTER);

		JButton updateButton = new AutoFocusButton("Update");
		updateButton.setOpaque(false);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateGUI();
			}
		});
		menuPanel.add(updateButton, BorderLayout.EAST);
		splitPane.add(menuPanel, BorderLayout.NORTH);
		adjustTimeScale();
	}

	private double getOverallTransitionMax() {
		double overallEdgeMax = 0.0;
		for (TimeTransitionAnnotation tran : annotation.getAllTransitionAnnotations()) {

			StatisticsAnnotationProperty prop = tran.getDuration();
			if (overallEdgeMax < getData(prop)) {
				overallEdgeMax = getData(prop);
			}
		}
		return overallEdgeMax;
	}

	private double getOverallStateMax() {
		double overallMax = 0.0;
		for (TimeStateAnnotation state : annotation.getAllStateAnnotations()) {

			StatisticsAnnotationProperty prop = getTimeMap(state);
			if (overallMax < getData(prop)) {
				overallMax = getData(prop);
			}
		}

		return overallMax;
	}

	public void buildMainMenuGui() {
		removeAll();
		this.add(splitPane, BorderLayout.CENTER);
		revalidate();
		this.repaint();
	}

	public void updateGUI() {
		adjustTimeScale();
		splitPane.remove(chartPanel);

		// BVD: No need to do an update here as the system did not change
		//		ProMJGraphPanel graphVisPanel = ProMJGraphVisualizer.visualizeGraph(system, map);
		
		// HV: The following three lines look stupid, but they serve a purpose: the refresh the graph.
		double scale = graphVisPanel.getScale();
		graphVisPanel.setScale(0.5 * scale);
		graphVisPanel.setScale(scale);
		
		chartPanel.setLeftComponent(graphVisPanel);
		chartPanel.setRightComponent(table);
		int divider = chartPanel.getDividerLocation();
		chartPanel.validate();

		splitPane.add(chartPanel, BorderLayout.CENTER);

		GraphSelectionModel model = graphVisPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {

			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							DirectedGraphNode node = ((ProMGraphCell) cell).getNode();
							if (node instanceof State) {
								State state = (State) node;
								TimeStateAnnotation stateAnnotation = annotation.getStateAnnotation(state);
								table.showStateAnnotation(stateAnnotation);
							}
						} else {
							if (cell instanceof ProMGraphEdge) {
								DirectedGraphEdge<?, ?> edge = ((ProMGraphEdge) cell).getEdge();
								if (edge instanceof Transition) {
									Transition transition = (Transition) edge;
									TimeTransitionAnnotation transitionAnnotation = annotation
											.getTransitionAnnotation(transition);
									table.showTransitionAnnotation(transitionAnnotation);
								}
							}
						}
					}
				}

			}
		});
		validate();
		chartPanel.setDividerLocation(divider);
	}

	protected void initColorBySort() {
		ArrayList<String> colorByList = new ArrayList<String>();
		for (String property : TimeStateAnnotation.getNamesOfProperties()) {
			colorByList.add(property);
		}
		/*
		 * colorByList.add("Sojourn"); colorByList.add("Remaining");
		 * colorByList.add("Elapsed");
		 */
		colorBySort = new GUIPropertyListEnumeration("Color By:", "", colorByList, this, 150);
	}

	protected double getData(StatisticsAnnotationProperty stat) {
		return stat.getValue().doubleValue();
	}

	private StatisticsAnnotationProperty getTimeMap(TimeStateAnnotation sa) {
		/*
		 * if (colorBySort.getValue().equals("Sojourn")) { return
		 * sa.getSoujourn(); } else if
		 * (colorBySort.getValue().equals("Elapsed")) { return sa.getElapsed();
		 * } else if (colorBySort.getValue().equals("Remaining")) { return
		 * sa.getRemaining(); } return null;
		 */
		return (StatisticsAnnotationProperty) sa.getProperty(colorBySort.getValue().toString());
	}

	private void adjustTimeScale() {
		double overallStateMax = getOverallStateMax();
		double overallEdgeMax = getOverallTransitionMax();

		double minValue = slider.getValue(0);
		double maxValue = minValue + slider.getValue(1);

		double stateMinValue = overallStateMax * minValue;
		double stateMaxValue = overallStateMax * maxValue;

		double transMinValue = overallEdgeMax * minValue;
		double transMaxValue = overallEdgeMax * maxValue;

		for (State state : system.getNodes()) {
			String old = "<html><p align=\"center\">" + state.getAttributeMap().get(AttributeMap.LABEL).toString();
			String value = "unknown";
			TimeStateAnnotation san = annotation.getStateAnnotation(state);
			if (san != null) {

				/*
				 * StatisticsAnnotationProperty soujourn = san.getSoujourn();
				 * StatisticsAnnotationProperty remaining = san.getRemaining();
				 * StatisticsAnnotationProperty elapsed = san.getElapsed();
				 * String ss = (soujourn != null)?
				 * getDuration(getData(soujourn)):"unknown"; String se =
				 * (elapsed != null)? getDuration(getData(elapsed)):"unknown";
				 * String sr = (remaining != null)?
				 * getDuration(getData(remaining)):"unknown"; value = "s = " +
				 * ss + "<br> e = " + se + "<br> r = "+ sr;
				 */
				StatisticsAnnotationProperty property = getTimeMap(san);
				value = (property != null) ? "[" + getDuration(getData(property)) + "]" : "unknown";

				map.putViewSpecific(state, AttributeMap.AUTOSIZE, true);
				if (getData(getTimeMap(san)) >= 0.0) {
					double tempValue = getData(getTimeMap(san));
					map.putViewSpecific(state, AttributeMap.FILLCOLOR,
							getColor(tempValue, stateMinValue, stateMaxValue));
				}
			}
			map.putViewSpecific(state, AttributeMap.LABEL, old + "<br>" + value + "</p></html>");
		}

		for (Transition trans : system.getEdges()) {
			TimeTransitionAnnotation trann = annotation.getTransitionAnnotation(trans);
			String value = "unknown";
			if (trann != null) {
				double tempValue = getData(trann.getDuration());
				value = getDuration(tempValue);
				map.putViewSpecific(trans, AttributeMap.EDGECOLOR, getColor(tempValue, transMinValue, transMaxValue));
			}
			map.putViewSpecific(trans, AttributeMap.TOOLTIP, "interval = " + value);
		}
	}

	private Color getColor(double value, double min, double max) {
		Color color = redColor;
		if (value < min) {
			color = blueColor;
		} else if (value < max) {
			color = yellowColor;
		}
		return color;
	}

	public HashSet<Transition> getAllEdgesTo(State state) {
		HashSet<Transition> s = new HashSet<Transition>();
		for (Transition t : system.getEdges()) {
			if (isInPath(t, state)) {
				s.add(t);
			}
		}
		return s;
	}

	public boolean isInPath(State v1, State v2, HashSet<State> vs) {
		Iterator<Transition> it = getOutEdges(v1).iterator();
		while (it.hasNext()) {
			Transition e = it.next();
			if (e.getTarget() == v1) {
				continue;
			}
			if (vs.contains(e.getTarget())) {
				return false;
			}
			if (e.getTarget() == v2) {
				return true;
			} else {
				vs.add(v1);
				if ((getOutEdges(e.getTarget()) != null) && (getOutEdges(e.getTarget()).size() > 0)) {
					if (isInPath(e.getTarget(), v2, vs)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isInPath(Transition e1, State v2) {
		if (e1.getTarget() == v2) {
			return true;
		} else {
			if ((e1.getSource() != e1.getTarget()) && (getOutEdges(e1.getTarget()) != null)
					&& (getOutEdges(e1.getTarget()).size() > 0)) {
				HashSet<State> vs = new HashSet<State>();
				return isInPath(e1.getTarget(), v2, vs);
			}
		}
		return false;
	}

	private Collection<Transition> getOutEdges(State s) {
		ArrayList<Transition> result = new ArrayList<Transition>();
		for (Transition t : system.getEdges()) {
			if (t.getSource() == s) {
				result.add(t);
			}
		}
		return result;
	}

	private String getDuration(double miliseconds) {
		Double d = new Double(miliseconds);
		Duration duration = new Duration(d.longValue());
		return duration.toString();
	}
}
