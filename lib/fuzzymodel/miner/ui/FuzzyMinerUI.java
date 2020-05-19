package org.processmining.plugins.fuzzymodel.miner.ui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.models.connections.fuzzymodel.FuzzyModelConnection;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.Metric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.binary.BinaryMetric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.unary.UnaryMetric;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerTabbedPane;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

public class FuzzyMinerUI {
	//	private UIPluginContext context;

	protected static final String METRICS_ALL = "show all metrics";
	protected static final String METRICS_TRACE = "show trace metrics";
	protected static final String METRICS_UNARY = "show unary significance metrics";
	protected static final String METRICS_BINSIG = "show binary significance metrics";
	protected static final String METRICS_BINCOR = "show binary correlation metrics";

	/*
	 * protected static Color COLOR_OUTER_BG = new Color(130, 130, 130);
	 * protected static Color COLOR_BG = new Color(120, 120, 120);
	 */

	protected MetricsRepository metrics;
	protected Attenuation attenuation;

	protected JComboBox metricsFilterBox;
	protected JScrollPane metricsScrollPane;
	protected JPanel metricsList;

	protected JRadioButton attLinearRadioButton;
	protected JRadioButton attNRootRadioButton;
	protected JSlider maxRelDistSlider;
	protected JSlider attNRootNSlider;
	protected AttenuationDisplayPanel attDisplayPanel;
	protected UIPluginContext context;

	public FuzzyMinerUI(final UIPluginContext context) {
		this.context = context;
	}

	public JPanel getOptionsPanel(XLogInfo summary) {
		// setup
		JPanel wholePanel = new JPanel();
		wholePanel.removeAll();
		wholePanel.setLayout(new BorderLayout());
		metrics = MetricsRepository.createRepository(summary);
		// GUI setup
		metricsFilterBox = SlickerFactory.instance().createComboBox(
				new String[] { FuzzyMinerUI.METRICS_ALL, FuzzyMinerUI.METRICS_TRACE, FuzzyMinerUI.METRICS_UNARY,
						FuzzyMinerUI.METRICS_BINSIG, FuzzyMinerUI.METRICS_BINCOR });
		metricsFilterBox.setOpaque(false);
		metricsFilterBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// adjust metric list's contents to combo box filter selection value
				metricsList = new JPanel();
				metricsList.setBackground(new Color(40, 40, 40));
				metricsList.setBorder(BorderFactory.createEmptyBorder());
				metricsList.setLayout(new BoxLayout(metricsList, BoxLayout.Y_AXIS));
				ArrayList<Metric> upData = new ArrayList<Metric>();
				String filter = (String) metricsFilterBox.getSelectedItem();
				if (filter.equals(FuzzyMinerUI.METRICS_ALL)) {
					upData.addAll(metrics.getTraceMetrics());
					upData.addAll(metrics.getUnaryMetrics());
					upData.addAll(metrics.getSignificanceBinaryMetrics());
					upData.addAll(metrics.getCorrelationBinaryMetrics());
				} else if (filter.equals(FuzzyMinerUI.METRICS_TRACE)) {
					upData.addAll(metrics.getTraceMetrics());
				} else if (filter.equals(FuzzyMinerUI.METRICS_UNARY)) {
					upData.addAll(metrics.getUnaryMetrics());
				} else if (filter.equals(FuzzyMinerUI.METRICS_BINSIG)) {
					upData.addAll(metrics.getSignificanceBinaryMetrics());
				} else if (filter.equals(FuzzyMinerUI.METRICS_BINCOR)) {
					upData.addAll(metrics.getCorrelationBinaryMetrics());
				}
				for (Metric metric : upData) {
					metricsList.add(new MetricConfigurationComponent(metric));
				}
				metricsScrollPane.getViewport().setView(metricsList);
			}
		});
		metricsList = new JPanel();
		metricsList.setBorder(BorderFactory.createEmptyBorder());
		metricsList.setBackground(new Color(40, 40, 40));
		metricsList.setLayout(new BoxLayout(metricsList, BoxLayout.Y_AXIS));
		ArrayList<Metric> upData = new ArrayList<Metric>();
		upData.addAll(metrics.getTraceMetrics());
		upData.addAll(metrics.getUnaryMetrics());
		upData.addAll(metrics.getSignificanceBinaryMetrics());
		upData.addAll(metrics.getCorrelationBinaryMetrics());
		for (Metric metric : upData) {
			metricsList.add(new MetricConfigurationComponent(metric));
		}
		metricsScrollPane = new JScrollPane(metricsList);
		metricsScrollPane.setBorder(BorderFactory.createEmptyBorder());
		metricsScrollPane.setOpaque(false);
		metricsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		metricsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//the metrics Panel for tab Panel
		RoundedPanel metricsPanel = new RoundedPanel(10, 5, 0);
		//metricsPanel.setBackground(new Color(80, 80, 80));
		metricsPanel.setBackground(SlickerColors.COLOR_BG_2);
		metricsPanel.setLayout(new BorderLayout());
		metricsPanel.setMinimumSize(new Dimension(480, 200));
		metricsPanel.setMaximumSize(new Dimension(1000, 2000));
		metricsPanel.setPreferredSize(new Dimension(480, 300));
		JPanel filterPanel = new JPanel();
		filterPanel.setOpaque(false);
		filterPanel.setLayout(new BorderLayout());
		filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		JLabel filterLabel = new JLabel("Customize view: ");
		//filterLabel.setForeground(new Color(160, 160, 160));
		filterLabel.setForeground(SlickerColors.COLOR_FG);
		filterLabel.setOpaque(false);
		filterPanel.add(filterLabel, BorderLayout.WEST);
		filterPanel.add(metricsFilterBox, BorderLayout.CENTER);
		metricsPanel.add(filterPanel, BorderLayout.NORTH);
		metricsPanel.add(metricsScrollPane, BorderLayout.CENTER);
		// right panel
		ChangeListener attUpdateChangeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				updateAttenuationPanel();
			}
		};
		attenuation = new NRootAttenuation(2.7, 5);
		attLinearRadioButton = new JRadioButton("Linear attenuation", false);
		attLinearRadioButton.setUI(new SlickerRadioButtonUI());
		attLinearRadioButton.setOpaque(false);
		attLinearRadioButton.addChangeListener(attUpdateChangeListener);
		attNRootRadioButton = new JRadioButton("Nth root with radical", true);
		attNRootRadioButton.setUI(new SlickerRadioButtonUI());
		attNRootRadioButton.setOpaque(false);
		attNRootRadioButton.addChangeListener(attUpdateChangeListener);
		ButtonGroup attButtonGroup = new ButtonGroup();
		attButtonGroup.add(attLinearRadioButton);
		attButtonGroup.add(attNRootRadioButton);
		attNRootNSlider = new JSlider(JSlider.HORIZONTAL, 1000, 4000, 2700);
		//attNRootNSlider = new JSlider(JSlider.HORIZONTAL, 100, 400, 270);
		attNRootNSlider.setUI(new SlickerSliderUI(attNRootNSlider));
		attNRootNSlider.addChangeListener(attUpdateChangeListener);
		maxRelDistSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 4);
		maxRelDistSlider.setUI(new SlickerSliderUI(maxRelDistSlider));
		maxRelDistSlider.addChangeListener(attUpdateChangeListener);
		//attenuationDisplayPanel
		attDisplayPanel = new AttenuationDisplayPanel(attenuation, 5);
		attDisplayPanel.setBackground(Color.BLACK);
		RoundedPanel attDisplayRoundedPanel = RoundedPanel.enclose(attDisplayPanel, 10, 5, 0);
		attDisplayRoundedPanel.setMinimumSize(new Dimension(200, 150));
		attDisplayRoundedPanel.setPreferredSize(new Dimension(300, 200));
		attDisplayRoundedPanel.setMaximumSize(new Dimension(400, 300));
		JPanel confPanel = new JPanel();
		confPanel.setOpaque(false);
		confPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		confPanel.setMaximumSize(new Dimension(800, 500));
		confPanel.setMinimumSize(new Dimension(230, 180));
		//confPanel.setPreferredSize(new Dimension(250, 180));
		confPanel.setPreferredSize(new Dimension(350, 180));
		//Maximal event distance Panel
		JPanel maxDistPanel = new JPanel();
		maxDistPanel.setMaximumSize(new Dimension(1200, 40));
		//maxDistPanel.setMaximumSize(new Dimension(250, 40));
		maxDistPanel.setOpaque(false);
		maxDistPanel.setLayout(new BoxLayout(maxDistPanel, BoxLayout.X_AXIS));
		JLabel maxDistLabel = new JLabel("Maximal event distance:");
		maxDistLabel.setOpaque(false);
		maxDistPanel.add(maxDistLabel);
		maxDistPanel.add(Box.createHorizontalStrut(5));
		maxDistPanel.add(maxRelDistSlider);
		//Select attenuation Header Panel
		JPanel attHeaderPanel = new JPanel();
		attHeaderPanel.setMaximumSize(new Dimension(1000, 30));
		//attHeaderPanel.setMaximumSize(new Dimension(250, 30));
		attHeaderPanel.setOpaque(false);
		attHeaderPanel.setLayout(new BoxLayout(attHeaderPanel, BoxLayout.X_AXIS));
		JLabel attHeaderLabel = new JLabel("Select attenuation to use:");
		attHeaderLabel.setOpaque(false);
		attHeaderPanel.add(attHeaderLabel);
		attHeaderPanel.add(Box.createHorizontalGlue());
		//NRoot Panel
		JPanel attNRootPanel = new JPanel();
		attNRootPanel.setMaximumSize(new Dimension(1000, 40));
		attNRootPanel.setOpaque(false);
		attNRootPanel.setLayout(new BoxLayout(attNRootPanel, BoxLayout.X_AXIS));
		//attNRootPanel.setBorder(BorderFactory.createEmptyBorder(30, 15, 15, 15));
		attNRootPanel.add(Box.createHorizontalStrut(30));
		attNRootPanel.add(attNRootRadioButton);
		attNRootPanel.add(Box.createHorizontalStrut(5));
		attNRootPanel.add(attNRootNSlider);
		//Linear attenuation Panel
		JPanel attLinearPanel = new JPanel();
		attLinearPanel.setMaximumSize(new Dimension(1000, 40));
		//attLinearPanel.setMaximumSize(new Dimension(250, 40));
		attLinearPanel.setOpaque(false);
		attLinearPanel.setLayout(new BoxLayout(attLinearPanel, BoxLayout.X_AXIS));
		attLinearPanel.add(Box.createHorizontalStrut(30));
		attLinearPanel.add(attLinearRadioButton);
		attLinearPanel.add(Box.createHorizontalGlue());
		// assemble conf. panel
		confPanel.add(maxDistPanel);
		confPanel.add(Box.createVerticalStrut(20));
		confPanel.add(attHeaderPanel);
		confPanel.add(Box.createVerticalStrut(15));
		confPanel.add(attNRootPanel);
		confPanel.add(Box.createVerticalStrut(10));
		confPanel.add(attLinearPanel);
		// assemble right hand panel
		//SmoothPanel rightSplit = new SmoothPanel();
		JPanel rightSplit = new JPanel();
		rightSplit.setOpaque(false);
		rightSplit.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		/*
		 * rightSplit.setBackground(COLOR_BG); rightSplit.setLayout(new
		 * BorderLayout()); rightSplit.add(attDisplayRoundedPanel,
		 * BorderLayout.WEST); rightSplit.add(confPanel, BorderLayout.CENTER);
		 */
		//ljf change the layout method of the measurement tab panel
		double size1[][] = { {0.4,0.6},{ TableLayout.FILL} };
		//double size1[][] = { { 300, 300 }, { TableLayout.FILL } };
		rightSplit.setLayout(new TableLayout(size1));
		rightSplit.add(attDisplayRoundedPanel, "0,0");
		rightSplit.add(confPanel, "1,0");
		// add to root
		//GradientPanel back = new GradientPanel(new Color(80, 80, 80), new Color(40, 40, 40));
		//GradientPanel back = new GradientPanel(SlickerColors.COLOR_BG_1, SlickerColors.COLOR_BG_2);
		JPanel back = new JPanel();
		//	back.setLayout(new BorderLayout());
		//back.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		double size[][] = { { TableLayout.FILL }, { TableLayout.FILL } };
		back.setLayout(new TableLayout(size));
		//SlickerTabbedPane tabs = new SlickerTabbedPane("Configuration", new Color(20, 20, 20, 230), new Color(160, 160, 160, 180), new Color(220, 220, 220, 180));
		SlickerTabbedPane tabs = new SlickerTabbedPane("Configuration", SlickerColors.COLOR_BG_1,
				SlickerColors.COLOR_FG, SlickerColors.COLOR_FG);
		tabs.addTab("Measurement", rightSplit);
		tabs.addTab("Metrics", metricsPanel);
		//back.add(tabs, BorderLayout.CENTER);
		back.add(tabs, "0,0");
		back.setOpaque(true);
		//	HeaderBar header = new HeaderBar("Fuzzy Miner");
		//header.setHeight(40);

		//set the whole configuration panel
		//wholePanel.add(header, BorderLayout.NORTH);
		/*
		 * wholePanel.add(back, BorderLayout.CENTER);
		 * wholePanel.setBackground(new Color(40, 40, 40));
		 * wholePanel.setOpaque(true);
		 */
		return back;

	}

	protected void updateAttenuationPanel() {
		int maxDistance = maxRelDistSlider.getValue();
		if (attLinearRadioButton.isSelected()) {
			attenuation = new LinearAttenuation(maxDistance, maxDistance);
		} else if (attNRootRadioButton.isSelected()) {
			double n = attNRootNSlider.getValue() / 1000.0;
			attenuation = new NRootAttenuation(n, maxDistance);
		}
		attDisplayPanel.setAttenuation(attenuation);
		attDisplayPanel.setMaxDistance(maxDistance);
		attDisplayPanel.repaint();
	}

	public MutableFuzzyGraph mine(XLog log) {

		//set Configuration UI
		XLogInfo logsummary = XLogInfoFactory.createLogInfo(log);
		JPanel configPanel = this.getOptionsPanel(logsummary);
		InteractionResult result = context.showConfiguration("Fuzzy Miner", configPanel);

		/*
		 * if(isCorrectlyConfigured() == false) { String message =
		 * "The Fuzzy Miner needs at least one active log-based metric\n" +
		 * "with a positive weight, from all major types, i.e.:\n" +
		 * " - at least one log-based unary metric\n" +
		 * " - at least one log-based binary significance metric\n" +
		 * " - at least one log-based binary correlation metric\n" +
		 * "Adjust your metrics' weight accordingly, please!";
		 * System.out.println(message); return null; }
		 */
		//Compute the Metrics
		switch (result) {
			case CANCEL :
				context.log("The user has cancelled the Fuzzy miner!");
				context.getFutureResult(0).cancel(true);
			//	context.getFutureResult(1).cancel(true);
			//	context.getFutureResult(2).cancel(true);
				//return new Object[] {null, null };
				//return new Object[] {null};
				return null;
			case CONTINUE :

				long time = System.currentTimeMillis();
				metrics.apply(log, attenuation, maxRelDistSlider.getValue(), context);
				time = System.currentTimeMillis() - time;
				String logStr = new String("Fuzzy Miner: Building repository took " + time + " ms.");
				context.log(logStr, MessageLevel.NORMAL);
				MutableFuzzyGraph resultFMGraph = new MutableFuzzyGraph(metrics);
				//context.addConnection(new FuzzyModelConnection(metrics,log));
				context.addConnection(new FuzzyModelConnection(resultFMGraph));
				return resultFMGraph;
			default :
				context.getFutureResult(0).cancel(true);
			//	context.getFutureResult(1).cancel(true);
				//return new Object[] { null, null };
				//return new Object[] {null};
				return null;
		}
	}

	protected boolean isCorrectlyConfigured() {
		boolean minOneUnary = false;
		boolean minOneBinarySig = false;
		for (UnaryMetric metric : metrics.getUnaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				minOneUnary = true;
				break;
			}
		}
		if (minOneUnary == false) {
			return false;
		}
		for (BinaryMetric metric : metrics.getSignificanceBinaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				minOneBinarySig = true;
				break;
			}
		}
		if (minOneBinarySig == false) {
			return false;
		}
		for (BinaryMetric metric : metrics.getCorrelationBinaryLogMetrics()) {
			if (metric.getNormalizationMaximum() > 0.0) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getHtmlDescription()
	 */
	public String getHtmlDescription() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Plugin#getName()
	 */
	public String getName() {
		return "Fuzzy Miner";
	}

}
