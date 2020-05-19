package org.processmining.plugins.ywl.replayer.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.yawl.YawlPerformanceResult;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPD;
import org.processmining.models.jgraph.ProMJGraphVisualizer;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.components.SlickerTabbedPane;

/**
 * @author David Piessens
 * @email d.a.m.piessens@student.tue.nl
 * @version May 29, 2010
 */
public class YawlReplayResultVisPanel extends JPanel {
	private static final long serialVersionUID = 3929165691864230402L;

	// general GUI components
	protected YPD net;
	protected Progress progress;

	protected JComponent graphVisPanel;
	protected SlickerTabbedPane southTabs;
	protected ViewSpecificAttributeMap viewSpecificMap;
	protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			true);

	public YawlReplayResultVisPanel(PluginContext context, YPD graph, Progress progress) {
		setLayout(new BorderLayout());

		// create panels
		this.net = graph;
		this.progress = progress;
		this.viewSpecificMap = new ViewSpecificAttributeMap();
		// this.graphVisPanel =
		// PromJGraphSlickerStyleVisualizer.getVisualizationPanelSlickerStyle(this.net,
		// viewSpecificMap, this.progress);
		this.graphVisPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, this.net,
				viewSpecificMap);

		// setup split pane
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		splitPane.setBackground(Color.BLACK);
		// this.southTabs = new SlickerTabbedPane("", SlickerColors.COLOR_BG_1,
		// SlickerColors.COLOR_FG, Color.WHITE);
		this.southTabs = new SlickerTabbedPane("", SlickerColors.COLOR_BG_1,
				Color.WHITE, Color.WHITE);
		splitPane.setLeftComponent(graphVisPanel);
		splitPane.setRightComponent(southTabs);

		// add it to be visualized
		this.add(splitPane, BorderLayout.CENTER);

		// signal the change and repaint graph
		repaint();
	}

	/**
	 * @param yawlPerformanceResult
	 */
	public void addPerformanceInfo(YawlPerformanceResult yawlPerformanceResult) {
		// write information in this panel
		JPanel panel = new JPanel();
		panel.setBackground(SlickerColors.COLOR_BG_1);
		// form vector of array of string
		Vector<Object> columnNames = new Vector<Object>();
		columnNames.add("Performance metrics");
		columnNames.add("Value");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();

		// format to 4 digit behind comma
		NumberFormat nf = new DecimalFormat("0.0000");

		for (Pair<String, Double> pair : yawlPerformanceResult
				.getPerformanceValues()) {
			Vector<Object> datum = new Vector<Object>();
			datum.add(pair.getFirst());
			datum.add(nf.format(pair.getSecond()));
			data.add(datum);
		}

		// add the list, set it to non editable
		JTable table = new JTable(data, columnNames) {
			private static final long serialVersionUID = -7116637591741809191L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		JScrollPane scrlPane = new JScrollPane(table);
		scrlPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		scrlPane.setBackground(SlickerColors.COLOR_BG_1);
		panel.add(scrlPane);
		table.setPreferredScrollableViewportSize(new Dimension(450, Math
				.round((float) table.getPreferredSize().getHeight())));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setAutoscrolls(true);

		// create scrollpane
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		southTabs.addTab("Performance Info", scrollPane);
	}

}
