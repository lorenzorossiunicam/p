/**
 * 
 */
package org.processmining.plugins.aggregatedactivitiesperformancediagram;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPD;
import org.processmining.models.aggregatedactivitiesperformancediagram.AAPDElement;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.aapdgraph.AAPDBar;
import org.processmining.models.graphbased.directed.aapdgraph.AAPDGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version May 13, 2009
 */
public class AAPDInformationPanel extends JPanel {
	private static final long serialVersionUID = 801138747301782177L;

	// internal data structure
	private final AAPD aapd;
	private final GlobalSettingsData globalSettingData;
	private AAPDElement focusElements;

	// general GUI panel
	protected Progress progress;
	protected ProMJGraphPanel graphVisPanel;
	protected JPanel selectionSliderPanel;
	protected JTabbedPane southTabs;
	protected JComboBox barSelectionComboBox;
	protected JButton populateBarButton;
	protected JSlider sliderXScale;
	protected JSlider sliderElementXScale;
	protected JSlider sliderElementYScale;
	protected ViewSpecificAttributeMap viewSpecificMap;
	protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

	// GUI Components
	private AAPDStatisticPanel aapdStatisticPanel;

	// to adjust appearance
	public static int MAX_HEIGHT_OF_AN_ELEMENT = 150;
	public static int MIN_HEIGHT_OF_AN_ELEMENT = 1;
	public static int MIN_DISTANCE_BETWEEN_ELEMENT = 30;
	public static int INTERSECTION_BAR_HEIGHT = 5;
	public static int MIN_WIDTH_OF_AN_ELEMENT = 100;
	public static int MAX_X_DISTANCE_BETWEEN_ELEMENTS = 60;
	public static int MIN_X_SLIDER_SCALING_VALUE = -40;
	public static int MAX_X_SLIDER_SCALING_VALUE = 5;
	public static int MIN_WIDTH_SLIDER_SCALING_VALUE = -40;
	public static int MAX_WIDTH_SLIDER_SCALING_VALUE = 5;
	public static int MIN_HEIGHT_SLIDER_SCALING_VALUE = -3;
	public static int MAX_HEIGHT_SLIDER_SCALING_VALUE = 3;
	public static int MAJOR_TICK_SPACING = 5;
	public static int MINOR_TICK_SPACING = 1;

	public double xScaler = 0;
	public double xElementScaler = 0;
	public double yElementScaler = 0;

	public AAPDInformationPanel(PluginContext context, Progress progress, AAPD aapd,
			GlobalSettingsData globalSettingsData) {
		// General layout
		setLayout(new BorderLayout());

		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		viewSpecificMap = new ViewSpecificAttributeMap();
		this.progress = progress;
		this.aapd = aapd;
		globalSettingData = globalSettingsData;

		// visualize graph
		focusElements = aapd.getAapdFocusElements().iterator().next();

		// populate the first shown AAPD bar
		populateVisualizationPanel(focusElements);

		// populate selectionSliderPanel
		JPanel topLeftPanel = new JPanel();
		JLabel selectionLabel = new JLabel("Select element");
		barSelectionComboBox = new JComboBox(aapd.getAapdFocusElements().toArray());
		barSelectionComboBox.setEditable(false);
		barSelectionComboBox.setSelectedItem(focusElements);
		barSelectionComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				populateVisualizationPanel((AAPDElement) e.getItem());
				aapdStatisticPanel.showAAPDFocusElement((AAPDElement) e.getItem());
			}
		});

		topLeftPanel.add(selectionLabel);
		topLeftPanel.add(barSelectionComboBox);

		JLabel xScaleLabel = new JLabel("X-distance scaling");
		sliderXScale = new JSlider(SwingConstants.HORIZONTAL, MIN_X_SLIDER_SCALING_VALUE, MAX_X_SLIDER_SCALING_VALUE, 0);
		sliderXScale.setMinorTickSpacing(MINOR_TICK_SPACING);
		sliderXScale.setPaintLabels(false);
		sliderXScale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setXScaler(sliderXScale.getValue());
				populateVisualizationPanel(focusElements);
			}
		});
		sliderXScale.setPaintTicks(true);
		JPanel topRightPanel = new JPanel();
		topRightPanel.add(xScaleLabel);
		topRightPanel.add(sliderXScale);

		JLabel xElementLengthLabel = new JLabel("Element width scaling");
		sliderElementXScale = new JSlider(SwingConstants.HORIZONTAL, MIN_WIDTH_SLIDER_SCALING_VALUE,
				MAX_WIDTH_SLIDER_SCALING_VALUE, 0);
		sliderElementXScale.setMinorTickSpacing(MINOR_TICK_SPACING);
		sliderElementXScale.setPaintLabels(false);
		sliderElementXScale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setElementXScaler(sliderElementXScale.getValue());
				populateVisualizationPanel(focusElements);
			}
		});
		sliderElementXScale.setPaintTicks(true);
		topRightPanel.add(xElementLengthLabel);
		topRightPanel.add(sliderElementXScale);

		JLabel yElementLengthLabel = new JLabel("Element height scaling");
		sliderElementYScale = new JSlider(SwingConstants.HORIZONTAL, MIN_HEIGHT_SLIDER_SCALING_VALUE,
				MAX_HEIGHT_SLIDER_SCALING_VALUE, 0);
		sliderElementYScale.setMinorTickSpacing(MINOR_TICK_SPACING);
		sliderElementYScale.setPaintLabels(false);
		sliderElementYScale.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setElementYScaler(sliderElementYScale.getValue());
				populateVisualizationPanel(focusElements);
			}
		});
		sliderElementYScale.setPaintTicks(true);
		topRightPanel.add(yElementLengthLabel);
		topRightPanel.add(sliderElementYScale);

		// finishing 
		topRightPanel.setPreferredSize(new Dimension(1000, 60));

		// top panel
		JPanel topPanel = new JPanel();
		topPanel.add(topLeftPanel);
		topPanel.add(topRightPanel);

		// bottom panel
		southTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

		selectionSliderPanel = new JPanel();
		selectionSliderPanel.setLayout(new BorderLayout());
		selectionSliderPanel.add(new JScrollPane(topPanel), BorderLayout.NORTH);
		selectionSliderPanel.add(southTabs, BorderLayout.CENTER);

		splitPane.setRightComponent(selectionSliderPanel);

		this.add(splitPane, BorderLayout.CENTER);

		// generate other panel
		generateAllPanel(focusElements);
	}

	private void populateVisualizationPanel(AAPDElement aapdFocusElements) {
		focusElements = aapdFocusElements;

		double currentScale = -1;
		if (graphVisPanel != null) {
			currentScale = graphVisPanel.getScale();
		}

		AAPDGraph graph = AAPDGraphGenerator.generateAAPDGraph(aapdFocusElements, aapd, MAX_HEIGHT_OF_AN_ELEMENT,
				MIN_HEIGHT_OF_AN_ELEMENT, MIN_DISTANCE_BETWEEN_ELEMENT, xScaler, xElementScaler, yElementScaler,
				INTERSECTION_BAR_HEIGHT, MAX_X_DISTANCE_BETWEEN_ELEMENTS, MIN_WIDTH_OF_AN_ELEMENT);
		graphVisPanel = ProMJGraphVisualizer.instance().visualizeGraphWithoutRememberingLayout(graph);
		if (currentScale > 0.0) {
			graphVisPanel.setScale(currentScale);
		}
		graphVisPanel.repaint();

		// set the action whenever a node in the graph is selected
		GraphSelectionModel model = graphVisPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							ProMGraphCell node = (ProMGraphCell) cell;
							AAPDBar aapdBar = (AAPDBar) node.getNode();
							barSelectionComboBox.setSelectedItem(aapdBar.getAapdFocusElements());
						}
					}
				}

			}
		});

		splitPane.setLeftComponent(graphVisPanel);
		repaint();
	}

	public void generateAllPanel(AAPDElement focusElements) {
		aapdStatisticPanel = new AAPDStatisticPanel(this, aapd, globalSettingData, focusElements);
		southTabs.addTab("<html>AAPD Statistic</html>", new JScrollPane(aapdStatisticPanel));
	}

	public void refreshModel() {
		populateVisualizationPanel(focusElements);
		aapdStatisticPanel.showAAPDFocusElement(focusElements);
	}

	public double getXScaler() {
		return xScaler;
	}

	public void setXScaler(double xScaler) {
		this.xScaler = xScaler;
	}

	public double getElementXScaler() {
		return xElementScaler;
	}

	public void setElementXScaler(double xElementScaler) {
		this.xElementScaler = xElementScaler;
	}

	public double getElementYScaler() {
		return yElementScaler;
	}

	public void setElementYScaler(double yElementScaler) {
		this.yElementScaler = yElementScaler;
	}
}
