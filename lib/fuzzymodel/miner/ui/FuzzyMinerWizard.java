package org.processmining.plugins.fuzzymodel.miner.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.Attenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.LinearAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.attenuation.NRootAttenuation;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.Metric;
import org.processmining.models.graphbased.directed.fuzzymodel.metrics.MetricsRepository;

import com.fluxicon.slickerbox.colors.SlickerColors;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;

public class FuzzyMinerWizard {

	private XLog log;
	private XLogInfo logInfo;
	private UIPluginContext context;
	
	private XEventClassifier classifier;
	private MetricsRepository metrics;
	private Attenuation attenuation;
	private int maxDistance;
	
	private WizardStep[] wizardSteps;
//	int classifierStep;
	int metricsStep;
	int attenuationStep;
	int currentStep;
	int nofSteps;
	
	public FuzzyMinerWizard(UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;

		nofSteps = 0;
//		classifierStep = nofSteps++;
		metricsStep = nofSteps++;
		attenuationStep = nofSteps++;
		wizardSteps = new WizardStep[nofSteps];
//		wizardSteps[classifierStep] = new ClassifierStep();
		wizardSteps[metricsStep] = new MetricsStep();
		wizardSteps[attenuationStep] = new AttenuationStep();
//		currentStep = classifierStep;
		currentStep = metricsStep;
	}
	
	public InteractionResult show() {
		InteractionResult result = InteractionResult.NEXT;
		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= nofSteps) {
				currentStep = nofSteps - 1;
			}
			context.log("Current step: " + currentStep);
			result = context
					.showWizard("Fuzzy Miner", currentStep == 0, currentStep == nofSteps - 1, wizardSteps[currentStep]);
			switch (result) {
				case NEXT :
					wizardSteps[currentStep].readSettings();
					go(1);
					break;
				case PREV :
					go(-1);
					break;
				case FINISHED :
					wizardSteps[currentStep].readSettings();
					return result;
				default :
					return result;
			}
		}
	}
	
	private int go(int direction) {
		currentStep += direction;
		if ((currentStep >= 0) && (currentStep < nofSteps)) {
			if (wizardSteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}
	
	public MetricsRepository getMetrics() {
		return metrics;
	}
	
	public Attenuation getAttenuation() {
		return attenuation;
	}
	
	public int getMaxDistance() {
		return maxDistance;
	}

	public XLogInfo getLogInfo() {
		return logInfo;
	}
	
	private abstract class WizardStep extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6892655601953727616L;

		public abstract boolean precondition();

		public abstract void readSettings();
	}

	@SuppressWarnings("unused")
	private class ClassifierStep extends WizardStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5629896729801647063L;

		public ClassifierStep() {
			initComponents();
		}

		public boolean precondition() {
			if (log.getClassifiers().size() > 0) {
				return true;
			} 
			/*
			 * No classifiers found in log. Create standard classifier and skip this step.
			 */
			classifier = new XEventNameClassifier();
			return false;
		}

		private void initComponents() {
			double size[][] = { { TableLayoutConstants.FILL },
					{ 50, 30, TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			add(SlickerFactory.instance().createLabel("<html><h2>Select classifier</h2>"), "0, 0");

			JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
			SlickerDecorator.instance().decorate(jScrollPane2, SlickerColors.COLOR_BG_3, SlickerColors.COLOR_FG,
					SlickerColors.COLOR_BG_1);
			jList1 = new javax.swing.JList(log.getClassifiers().toArray());
			jList1.setSelectedIndex(0);
			jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jScrollPane2.setPreferredSize(new Dimension(500, 300));
			jScrollPane2.setViewportView(jList1);
			add(jScrollPane2, "0, 2");

		}

		private javax.swing.JList jList1;

		public void readSettings() {
			classifier = (XEventClassifier) jList1.getSelectedValue();
		}
	}

	private class MetricsStep extends WizardStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5629896729801647063L;

		public MetricsStep() {
			classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
			logInfo = XLogInfoFactory.createLogInfo(log, classifier);
			initComponents();
		}

		public boolean precondition() {
//			initComponents();
			return true;
		}

		private JComboBox metricsFilterBox;
		private JPanel metricsList;
		private JScrollPane metricsScrollPane;
		
		private void initComponents() {
			this.removeAll();
			double size[][] = { { TableLayoutConstants.FILL },
					{ 50, 30, TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			add(SlickerFactory.instance().createLabel("<html><h2>Select metrics</h2>"), "0, 0");

			metrics = MetricsRepository.createRepository(logInfo);
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
			add(metricsPanel, "0, 2");
		}

		public void readSettings() {
		}
	}

	private class AttenuationStep extends WizardStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5629896729801647063L;

		private JRadioButton attLinearRadioButton;
		private JRadioButton attNRootRadioButton;
		private JSlider maxRelDistSlider;
		private JSlider attNRootNSlider;
		private AttenuationDisplayPanel attDisplayPanel;

		public AttenuationStep() {
			initComponents();
		}

		public boolean precondition() {
			return true;
		}

		private void initComponents() {
			double size[][] = { { TableLayoutConstants.FILL },
					{ 50, 30, TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			add(SlickerFactory.instance().createLabel("<html><h2>Select attenuation</h2>"), "0, 0");

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
			double size1[][] = { {0.4,0.6},{ TableLayout.FILL} };
			//double size1[][] = { { 300, 300 }, { TableLayout.FILL } };
			rightSplit.setLayout(new TableLayout(size1));
			rightSplit.add(attDisplayRoundedPanel, "0,0");
			rightSplit.add(confPanel, "1,0");
			add(rightSplit, "0, 2");
		}

		public void readSettings() {
			maxDistance = maxRelDistSlider.getValue();
			if (attLinearRadioButton.isSelected()) {
				attenuation = new LinearAttenuation(maxDistance, maxDistance);
			} else if (attNRootRadioButton.isSelected()) {
				double n = attNRootNSlider.getValue() / 1000.0;
				attenuation = new NRootAttenuation(n, maxDistance);
			}
		}

		protected void updateAttenuationPanel() {
			attDisplayPanel.setAttenuation(attenuation);
			attDisplayPanel.setMaxDistance(maxDistance);
			attDisplayPanel.repaint();
		}
	}

}
