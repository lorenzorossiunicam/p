package org.processmining.plugins.psd.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.plugins.psd.model.PSDModel;
import org.processmining.plugins.psd.model.pattern.PSDPattern;
import org.processmining.plugins.psd.model.sequence.PSDSequence;
import org.processmining.plugins.psd.utils.PSDLogUtils;

public class PSDVisualizer extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1204558302446485770L;

	private PSDSequencePanel sequencePanel = new PSDSequencePanel(this);
	private PSDPatternPanel patternPanel = new PSDPatternPanel(this);
	private PSDReplaceableScalableComponent scalableComponent = new PSDReplaceableScalableComponent(sequencePanel);
	private ScalableViewPanel mainPanel = new ScalableViewPanel(scalableComponent);

	private boolean isStrict = false;

	private double noiseRatio = 0.0;
	private String dataEltType = PSDLogUtils.TASKID;
	private int[] selectedInstanceIndices;

	/**
	 * variable that is true is patternDiagram has been selected, else (sequence
	 * diagram selected) false
	 */
	private boolean patternSelected = false;
	/**
	 * boolean, set to true when the patterndiagram has been drawn
	 */
	private boolean patternDrawn = false;
	/**
	 * variables to keep track of the selected time sort
	 */
	private String timeSort = "seconds";
	/**
	 * the time divider
	 */
	private long timeDivider = 1000;
	/**
	 * scale at which diagrams are displayed;
	 */
	private float scale = 1;

	private PSDModel model;
	private XEventClassifier classifier;
	private XLogInfo logInfo;
	private XLog log;
	private List<PSDSequence> sequences;
	private List<PSDPattern> patterns;
	private List<String> dataElts;
	private Date beginTotal;
	private Date endTotal;

	private PSDFilterInteractionPanel filterPanel;

	/**
	 * Map, where frequency of occurence is the key and a Set that contains the
	 * patterns that have that frequency of occurence is the value.
	 */
	private Map<Integer, Set<PSDPattern>> patternMap = new HashMap<Integer, Set<PSDPattern>>();

	@Plugin(name = "Show Sequences and Patterns", returnLabels = { "Visualization for Sequences and Patterns" }, returnTypes = { JComponent.class }, parameterLabels = { "Event Log" }, userAccessible = false)
	@Visualizer
	public JComponent show(UIPluginContext context, XLog log) {
		this.log = log;
		classifier = new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier());
		logInfo = XLogInfoFactory.createLogInfo(log, classifier);
		selectedInstanceIndices = new int[logInfo.getNumberOfTraces()];
		for (int i = 0; i < logInfo.getNumberOfTraces(); i++) {
			selectedInstanceIndices[i] = i;
		}
		model = new PSDModel(context, log);
		model.construct(log, logInfo, classifier, noiseRatio, dataEltType, isStrict, selectedInstanceIndices);
		sequences = model.getSequences();
		patterns = model.getPatterns();
		dataElts = model.getDataElts();
		beginTotal = model.getBeginTotal();
		endTotal = model.getEndTotal();

		jbInit();

		if (patternSelected) {
			showPatternDiagram();
		} else {
			showFullDiagram();
		}
		return this;
	}

	public void setExporting(boolean isExporting) {
		sequencePanel.setExporting(isExporting);
		patternPanel.setExporting(isExporting);
	}

	public void updateConfig(String dataEltType, String timeSort, long timeDivider, boolean isStrict) {
		this.timeSort = timeSort;
		this.timeDivider = timeDivider;
		this.dataEltType = dataEltType;
		this.isStrict = isStrict;
		model.construct(log, logInfo, classifier, noiseRatio, dataEltType, isStrict, selectedInstanceIndices);
		if (patternSelected) {
			showPatternDiagram();
		} else {
			showFullDiagram();
		}
	}

	/**
	 * Actually builds the GUI
	 */
	private void jbInit() {
		int height = this.getHeight() - this.getInsets().bottom - this.getInsets().top - 50;
		int width = this.getWidth() - this.getInsets().left - this.getInsets().right - 200;

		mainPanel.setBackground(Color.WHITE);
		mainPanel.addViewInteractionPanel(new PSDZoomInteractionPanel(mainPanel, this,
				ScalableViewPanel.MAX_ZOOM), SwingConstants.WEST);
		mainPanel.addViewInteractionPanel(new PSDExportInteractionPanel(mainPanel, this), SwingConstants.SOUTH);
		mainPanel.addViewInteractionPanel(new PSDConfigInteractionPanel(this, log), SwingConstants.SOUTH);
		filterPanel = new PSDFilterInteractionPanel(this, log);
		mainPanel.addViewInteractionPanel(filterPanel, SwingConstants.SOUTH);

		this.setBackground(Color.WHITE);
		this.setLayout(new BorderLayout());
		this.add(mainPanel, BorderLayout.CENTER);
		this.setMinimumSize(new Dimension(width, height));
		this.setAlignmentX(LEFT_ALIGNMENT);

		this.validate();
		this.repaint();
	}

	public void setFilterOptions() {
		filterPanel.set(logInfo, selectedInstanceIndices, sequences, patterns, timeSort, timeDivider, patternDrawn);
	}

	public void setDiagram(boolean patternSelected) {
		this.patternSelected = patternSelected;
		if (patternSelected) {
			showPatternDiagram();
			scalableComponent.replaceComponent(patternPanel);
			mainPanel.updated();
		} else {
			showFullDiagram();
			scalableComponent.replaceComponent(sequencePanel);
			mainPanel.updated();
		}
	}

	/**
	 * Zooms visible diagram to the position of the mouse. oldscale/scale
	 * determines in which direction (in or out) is zoomed
	 * 
	 * @param oldScale
	 *            float
	 */
	private void zoom(float oldScale) {
		if (patternSelected) {
			showPatternDiagram();
		} else {
			showFullDiagram();
		}
	}

	public void setScale(double scale) {
		float oldScale = this.scale;
		this.scale = (float) scale;
		zoom(oldScale);
	}

	public double getScale() {
		return scale;
	}

	/**
	 * Closes filter option screen (if opened), and sets selectedInstanceIndices
	 * to the indices of the instances that are selected in the option screen
	 * 
	 * @param selectedInstanceIndices
	 *            int[]
	 */
	public void closeFrame(int[] selectedInstanceIndices) {
		if (selectedInstanceIndices != null) {
			this.selectedInstanceIndices = selectedInstanceIndices;
			model.construct(log, logInfo, classifier, noiseRatio, dataEltType, isStrict, selectedInstanceIndices);
			if (patternSelected) {
				showPatternDiagram();
			} else {
				showFullDiagram();
			}
		}
	}

	/**
	 * Displays the full diagram
	 */
	private void showFullDiagram() {
		this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		Iterator<String> it = dataElts.iterator();
		int width = 50;
		double duration = 0;
		if (endTotal != null && beginTotal != null) {
			duration = endTotal.getTime() - beginTotal.getTime();
		}
		if (sequencePanel.getTimePerPixel() == 0) {
			double dps = duration / sequences.size();
			sequencePanel.setTimePerPixel(dps / 20.0);
		}
		while (it.hasNext()) {
			String elt = it.next();
			width += elt.length() * 8 + 50;
		}
		width = Math.round(width * scale);
		int height = Math.max(
				((Double) (((duration / sequencePanel.getTimePerPixel()) + 100) * scale)).intValue(),
				Math.round(600 * scale));
		sequencePanel.setSize(new Dimension(width, height));
		sequencePanel.setPreferredSize(new Dimension(width, height));
		sequencePanel.setMinimumSize(new Dimension(width, height));
		sequencePanel.initialize(sequences, dataElts, timeSort, timeDivider, scale, beginTotal, duration);
		sequencePanel.invalidate();
		sequencePanel.repaint();
		height = Math.round(height + 200 * scale);
		sequencePanel.setPreferredSize(new Dimension(width, height));
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

	}

	/**
	 * Displays the pattern diagram
	 */
	private void showPatternDiagram() {
		patternMap.clear();
		Iterator<PSDPattern> it = patterns.iterator();
		double totalthrp = 0;
		double timePerPixel = patternPanel.getTimePerPixel();
		while (it.hasNext()) {
			PSDPattern pt = it.next();
			//place each pattern in the hashMap patternMap, to be able to sort
			//on frequency
			Set<PSDPattern> tempSet = patternMap.get(pt.getFrequency());
			if (tempSet == null) {
				tempSet = new HashSet<PSDPattern>();
			}
			tempSet.add(pt);
			patternMap.put(pt.getFrequency(), tempSet);
			//calculate throughput time to determine the height of the panel on
			//which the patterns are to be drawn
			pt.calculateTimes();
			totalthrp += pt.getMeanThroughputTime();
		}
		Iterator<String> elts = dataElts.iterator();
		//determine width of the screen
		int width = 50;
		while (elts.hasNext()) {
			String elt = elts.next();
			//more than enough
			width += elt.length() * 8 + 50;
		}
		width = Math.round(width * scale);
		int height = 900;
		if (patterns.size() > 0 && timePerPixel == 0) {
			double avgTime = totalthrp / patterns.size();
			//on average 100 pixels per pattern
			timePerPixel = avgTime / 100;
			//			originalTppPattern = timePerPixel;
		}
		if (timePerPixel > 0) {
			Double screenHeight = totalthrp / timePerPixel;
			height = Math.round((screenHeight.floatValue() + 50 * patterns.size() + 60) * scale);
		}
		//set size of the pattern diagram (panel)
		patternPanel.setSize(new Dimension(width, height));
		patternPanel.setPreferredSize(new Dimension(width, height));
		patternPanel.setMinimumSize(new Dimension(width, height));
		//initialize the painting of the pattern diagram
		patternPanel.initializePaint(patternMap, dataElts, timeDivider, scale, timePerPixel);
		//paint the pattern diagram
		patternPanel.invalidate();
		patternPanel.repaint();
		height = Math.round(height + 200 * scale);
		patternPanel.setPreferredSize(new Dimension(width, height));
		patternDrawn = true;
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
}
