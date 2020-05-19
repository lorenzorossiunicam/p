/*
 * Created on July. 04, 2007
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

package org.processmining.plugins.dottedchartanalysis.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.plugins.dottedchartanalysis.DottedChartAnalysis;
import org.processmining.plugins.dottedchartanalysis.model.DottedChartModel;

public class DottedChartOptionPanel implements ChangeListener, ActionListener, ItemListener {

	public static Color ColorBg = new Color(120, 120, 120);
	public static Color ColorInnerBg = new Color(140, 140, 140);
	public static Color ColorFg = new Color(30, 30, 30);
	public static Color ColorTextAreaBg = new Color(160, 160, 160);

	protected DottedChartPanel dcPanel = null;
	private DottedChartAnalysis dcAnalysis = null;

	///////////// to do 
	protected ArrayList<String> eventTypeToKeep;
	// label
	private final JLabel optionLabel = new JLabel("Options");
	private final JLabel componentLabel = new JLabel("Component type:");
	private final JLabel colorByLabel = new JLabel("Color By:");
	private final JLabel shapeByLabel = new JLabel("Size/Shape By:");
	private final JLabel sortByLabel = new JLabel("Sort By:");
	private final JLabel mouseModeLabel = new JLabel("Mouse Mode:");
	private final JLabel timeOptionLabel = new JLabel("Time option:");
	private final String[] widthSorts = { "L-1", "L-10", "L-100", "L-500", "seconds", "minutes", "hours", // width option
			"days", "weeks", "months", "years" };
	private final long[] widthDividers = { 1, 10, 100, 500, 1000, 60000, 3600000L, 86400000L, 604800000L, 2592000000L,
			31536000000L };
	private final JComboBox widthBox = new JComboBox(widthSorts);
	private final JLabel widthSortLabel = new JLabel("Time sort (chart):");
	private final String[] timeOptions = { DottedChartModel.TIME_ACTUAL, DottedChartModel.TIME_RELATIVE_TIME,
			DottedChartModel.TIME_RELATIVE_RATIO, //DottedChartPanel.TIME_LOGICAL,
			DottedChartModel.TIME_LOGICAL_RELATIVE, }; // array of "time option" strings required to fill the timeOptionBox combobox with
	private final JComboBox timeOptionBox = new JComboBox(timeOptions);
	private final JComboBox componentBox = new JComboBox();
	private final JComboBox colorByBox = new JComboBox();
	private final JComboBox shapeByBox = new JComboBox();
	private final JComboBox sortByBox = new JComboBox();
	private final JCheckBox descCheckBox = new JCheckBox("descending");
	private final JCheckBox sizeCheckBox = new JCheckBox("Size shows # of events", true);
	private final JButton jbuttonZoom = new JButton("Zoom out");
	private final JButton jbuttonAction = new JButton("Update");
	private final JComboBox mouseMode = new JComboBox();
	protected JSlider zoomSliderX = null; // slider for zooming the view
	protected JSlider zoomSliderY = null; // slider for zooming the view
	protected String currentTimeOption = DottedChartModel.TIME_ACTUAL;

	public DottedChartOptionPanel(DottedChartAnalysis aDCA) {
		dcAnalysis = aDCA;
		initializeComponentBox();
	}

	public JPanel getOptionPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(ColorBg);
		//initialize westpanel
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.setPreferredSize(new Dimension(170, 400));
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		optionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		optionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		panel.add(optionLabel);
		// add component option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		componentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(componentLabel);
		componentBox.setMaximumSize(new Dimension(160, 20));
		componentBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(componentBox);
		// add time option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		panel.add(timeOptionLabel);
		timeOptionBox.setMaximumSize(new Dimension(160, 20));
		timeOptionBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(timeOptionBox);
		// add width sort option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		panel.add(widthSortLabel);
		widthBox.setSelectedIndex(9); //seconds selected in widthBox
		widthBox.setMaximumSize(new Dimension(160, 20));
		widthBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(widthBox);
		// add colorBy option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		colorByLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(colorByLabel);
		colorByBox.setMaximumSize(new Dimension(160, 20));
		colorByBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(colorByBox);
		// size check box
		panel.add(sizeCheckBox);
		// add shapeBy option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		shapeByLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(shapeByLabel);
		shapeByBox.setMaximumSize(new Dimension(160, 20));
		shapeByBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(shapeByBox);
		// add sortBy option
		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		sortByLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(sortByLabel);
		sortByBox.setMaximumSize(new Dimension(160, 20));
		sortByBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(sortByBox);
		panel.add(descCheckBox);
		// add mouseMode option
		//		panel.add(Box.createRigidArea(new Dimension(5, 2)));
		mouseModeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(mouseModeLabel);
		mouseMode.setMaximumSize(new Dimension(160, 20));
		mouseMode.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(Box.createRigidArea(new Dimension(5, 0)));
		panel.add(mouseMode);
		//		// add "show line box"
		//		panel.add(Box.createRigidArea(new Dimension(5, 10)));
		//		checkLineBox.setAlignmentX(LEFT_ALIGNMENT);
		//		checkLineBox.setSelected(false);
		//		panel.add(checkLineBox);
		// add zoom slide bar for X
		zoomSliderX = new JSlider(SwingConstants.VERTICAL, 0, 3000, 0); // zooms in view
		zoomSliderX.setMaximumSize(new Dimension(80, 120));
		Hashtable<Integer, JLabel> zoomLabelTableX = new Hashtable<Integer, JLabel>();
		zoomLabelTableX.put(new Integer(0), new JLabel("1x"));
		zoomLabelTableX.put(new Integer(1000), new JLabel("10x"));
		zoomLabelTableX.put(new Integer(2000), new JLabel("(10^2)x"));
		zoomLabelTableX.put(new Integer(3000), new JLabel("(10^3)x"));
		zoomSliderX.setLabelTable(zoomLabelTableX);
		zoomSliderX.setMajorTickSpacing(1000);
		zoomSliderX.setMinorTickSpacing(500);
		zoomSliderX.setPaintTicks(true);
		zoomSliderX.setPaintLabels(true);
		zoomSliderX.addChangeListener(this);
		zoomSliderX.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel zoomLabelX = new JLabel("zoom (X)");
		zoomLabelX.setAlignmentX(Component.CENTER_ALIGNMENT);
		zoomLabelX.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel zoomPanelX = new JPanel();
		zoomPanelX.setLayout(new BoxLayout(zoomPanelX, BoxLayout.Y_AXIS));
		zoomPanelX.setBorder(BorderFactory.createLoweredBevelBorder());
		zoomPanelX.setMinimumSize(new Dimension(80, 120));
		zoomPanelX.add(zoomSliderX);
		zoomPanelX.add(zoomLabelX);
		// add zoom slide bar for Y
		zoomSliderY = new JSlider(SwingConstants.VERTICAL, 0, 3000, 0); // zooms in view
		zoomSliderY.setMaximumSize(new Dimension(80, 120));
		Hashtable<Integer, JLabel> zoomLabelTableY = new Hashtable<Integer, JLabel>();
		zoomLabelTableY.put(new Integer(0), new JLabel("1x"));
		zoomLabelTableY.put(new Integer(1000), new JLabel("10x"));
		zoomLabelTableY.put(new Integer(2000), new JLabel("(10^2)x"));
		zoomLabelTableY.put(new Integer(3000), new JLabel("(10^3)x"));
		zoomSliderY.setLabelTable(zoomLabelTableY);
		zoomSliderY.setMajorTickSpacing(1000);
		zoomSliderY.setMinorTickSpacing(500);
		zoomSliderY.setPaintTicks(true);
		zoomSliderY.setPaintLabels(true);
		zoomSliderY.addChangeListener(this);
		zoomSliderY.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		JLabel zoomLabelY = new JLabel("zoom (Y)");
		zoomLabelY.setAlignmentX(Component.CENTER_ALIGNMENT);
		zoomLabelY.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		JPanel zoomPanelY = new JPanel();
		zoomPanelY.setLayout(new BoxLayout(zoomPanelY, BoxLayout.Y_AXIS));
		zoomPanelY.setBorder(BorderFactory.createLoweredBevelBorder());
		zoomPanelY.setMinimumSize(new Dimension(80, 120));
		zoomPanelY.add(zoomSliderY);
		zoomPanelY.add(zoomLabelY);

		JPanel zoomPanel = new JPanel();
		zoomPanel.setLayout(new BoxLayout(zoomPanel, BoxLayout.X_AXIS));
		zoomPanel.setMaximumSize(new Dimension(160, 120));
		zoomPanel.setPreferredSize(new Dimension(160, 120));
		zoomPanel.add(zoomPanelX);
		zoomPanel.add(zoomPanelY);

		panel.add(Box.createRigidArea(new Dimension(5, 15)));
		zoomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel.add(zoomPanel);
		// add "zoom out button"
		panel.add(jbuttonZoom);
		// add "action button"
		panel.add(jbuttonAction);
		panel.add(Box.createRigidArea(new Dimension(5, 10)));
		return panel;
	}

	public String getColorStandard() {
		return (String) colorByBox.getSelectedItem();
	}

	public String getShapeStandard() {
		return (String) shapeByBox.getSelectedItem();
	}

	public String getSortStandard() {
		return (String) sortByBox.getSelectedItem();
	}

	public String getMouseMode() {
		return (String) mouseMode.getSelectedItem();
	}

	public String getComponentType() {
		return (String) componentBox.getSelectedItem();
	}

	public long getWidthDivider() {
		return widthDividers[widthBox.getSelectedIndex()];
	}

	public JSlider getZoomSliderX() {
		return zoomSliderX;
	}

	public JSlider getZoomSliderY() {
		return zoomSliderY;
	}

	public String getTimeOption() {
		return (String) timeOptionBox.getSelectedItem();
	}

	public void changeWidthSort(int idx) {
		widthBox.setSelectedIndex(idx);
	}

	public boolean isDescCheckBoxSelected() {
		return descCheckBox.isSelected();
	}

	public boolean isSizeCheckBoxSelected() {
		return sizeCheckBox.isSelected();
	}

	private void initializeComponentBox() {
		colorByBox.addItem(DottedChartModel.ST_NONE);
		shapeByBox.addItem(DottedChartModel.ST_NONE);
		sortByBox.addItem(DottedChartModel.ST_NONE);

		for (String str : dcAnalysis.getDottedChartModel().getAvailableComponentList()) {
			componentBox.addItem(str);
			colorByBox.addItem(str);
			shapeByBox.addItem(str);
			if (str.equals(DottedChartModel.ST_INST)) {
				componentBox.setSelectedIndex(componentBox.getItemCount() - 1);
			}
			if (str.equals(DottedChartModel.ST_TASK)) {
				colorByBox.setSelectedIndex(colorByBox.getItemCount() - 1);
			}
		}

		if (dcAnalysis.getDottedChartModel().getAvailableComponentList().size() == 0) {
			componentBox.addItem(DottedChartModel.ST_NONE);
		}

		// initialize sort by box
		sortByBox.addItem(DottedChartPanel.ST_NAME);
		sortByBox.addItem(DottedChartPanel.ST_SIZE);
		sortByBox.addItem(DottedChartPanel.ST_SPAN);
		sortByBox.addItem(DottedChartPanel.ST_FIRST_EVENT);
		sortByBox.addItem(DottedChartPanel.ST_LAST_EVENT);
		sortByBox.addItem(DottedChartPanel.ST_DURATION);
		sortByBox.addItem(DottedChartPanel.ST_START_TIME);
		sortByBox.addItem(DottedChartPanel.ST_END_TIME);

		// setup mouse mode box
		mouseMode.addItem(DottedChartPanel.ST_SELECT);
		mouseMode.addItem(DottedChartPanel.ST_ZOOMIN);
		mouseMode.addItem(DottedChartPanel.ST_DRAG);

		// initialize shape box
		shapeByBox.setEnabled(false);
	}

	public void registerGUIListener(DottedChartPanel aDcPanel) {
		dcPanel = aDcPanel;
		jbuttonAction.addActionListener(this);
		jbuttonZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeSlider();
				dcPanel.initBufferedImage();

				dcPanel.repaint();
			}
		});
		//		colorByBox.addActionListener(this);
		//		shapeByBox.addActionListener(this);
		sizeCheckBox.addItemListener(this);
	}

	public void zoomOut() {
		changeSlider();
		dcPanel.initBufferedImage();
		dcPanel.repaint();
	}

	public void changeSlider() {
		zoomSliderX.setValue(0);
		zoomSliderX.repaint();
		zoomSliderY.setValue(0);
		zoomSliderY.repaint();
	}

	/**
	 * Walks through process instance pi, and returns the data-elements that
	 * appear in it as a sorted array
	 * 
	 * @param pi
	 *            ProcessInstance
	 * @return String[]
	 */
	//	private String[] getOtherDataElements(XTrace trace) {
	//		
	//		HashSet<String> elts = new HashSet<String>();
	//		for(XEvent event : trace) {
	//			
	//		}
	//		
	//		AuditTrailEntryList ates = pi.getAuditTrailEntryList();
	//		Iterator<AuditTrailEntry> it = ates.iterator();
	//		
	//		while (it.hasNext()) {
	//			AuditTrailEntry ate = (AuditTrailEntry) it.next();
	//			Iterator<String> it2 = ate.getAttributes().keySet().iterator();
	//			//run through attributes
	//			while (it2.hasNext()) {
	//				String tempString = it2.next();
	//				if (tempString != "") {
	//					//add tempString to elts if it is not equal to the empty String
	//					elts.add(tempString);
	//				}
	//			}
	//		}
	//		//put the data elements in an array
	//		String[] set = (String[]) elts.toArray(new String[0]);
	//		//sort the array
	//		Arrays.sort(set);
	//
	//		//return the sorted array
	//		return set;
	//	}

	// Listener for GUI
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		// handle zoom events
		if ((e.getSource() == zoomSliderX)) {
			if (!zoomSliderX.getValueIsAdjusting()) {
				double zoom = StrictMath.pow(10.0, (zoomSliderX.getValue() / 1000.0));
				dcPanel.setViewportZoomX(1.0 / zoom);
				dcPanel.adjustWidth();
				dcPanel.revalidate();
				dcAnalysis.reDrawBoxOnOverview();
			}
		} else if ((e.getSource() == zoomSliderY)) {
			if (!zoomSliderY.getValueIsAdjusting()) {
				double zoom = StrictMath.pow(10.0, (zoomSliderY.getValue() / 1000.0));
				dcPanel.setViewportZoomY(1.0 / zoom);
				dcPanel.revalidate();
				dcAnalysis.reDrawBoxOnOverview();
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		//		if (source == descCheckBox) {
		//			dcPanel.repaint();
		//		}
		if (source == sizeCheckBox) {
			if (sizeCheckBox.isSelected()) {
				shapeByBox.setEnabled(false);
			} else {
				shapeByBox.setEnabled(true);
			}
		}

	}

	public void actionPerformed(ActionEvent e) {
		dcPanel.changeDots();
		dcPanel.initBufferedImage();
		dcPanel.revalidate();
		dcPanel.repaint();
		dcAnalysis.actionPerformed(e);
	}

}
