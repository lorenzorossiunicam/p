package org.processmining.plugins.psd.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;
import org.processmining.plugins.psd.utils.PSDLogUtils;

import com.fluxicon.slickerbox.components.SlickerButton;

public class PSDConfigInteractionPanel extends JPanel implements ViewInteractionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8349551417892841532L;

	private JLabel optionLabel = new JLabel("Options");
	private JLabel patternTypeLabel = new JLabel("Pattern type:");
	private JLabel timeSortLabel = new JLabel("Time sort:");
	private JLabel componentLabel = new JLabel("Component type:");

	private JComboBox componentBox = new JComboBox();
	
	//array of "time" strings required to fill the timeBox combobox with
	private String[] timeSorts = { "milliseconds", "seconds", "minutes", "hours", "days", "weeks", "months", "years" };
	private long[] dividers = { 1, 1000, 60000, 3600000L, 86400000L, 604800000L, 2592000000L, 31536000000L };
	private JComboBox timeBox = new JComboBox(timeSorts);
	
	//radio buttons + group to allow the user to select auto or manual settings
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JRadioButton flexibleButton = new JRadioButton("Flexible equivalent", true);
	private JRadioButton strictButton = new JRadioButton("Strict equivalent");
	
	//buttons
	private SlickerButton seqButton;
	private SlickerButton patButton;

	private PSDVisualizer root;
	private XLog log;

	public PSDConfigInteractionPanel(PSDVisualizer root, XLog log) {
		this.root = root;
		this.log = log;

		jbInit();
		initializeComponentBox();
		registerGUIListener();
	}

	private void jbInit() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setPreferredSize(new Dimension(160, 400));
		
		this.add(Box.createRigidArea(new Dimension(5, 5)));
		optionLabel.setAlignmentX(LEFT_ALIGNMENT);
		optionLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		this.add(optionLabel);
		
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		componentLabel.setAlignmentX(LEFT_ALIGNMENT);
		this.add(componentLabel);
		
		componentBox.setMaximumSize(new Dimension(150, 20));
		componentBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(componentBox);
		
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		this.add(timeSortLabel);
		
		//seconds selected in timeBox
		timeBox.setSelectedIndex(1);
		timeBox.setMaximumSize(new Dimension(150, 20));
		timeBox.setAlignmentX(LEFT_ALIGNMENT);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(timeBox);
		
		this.add(Box.createRigidArea(new Dimension(5, 10)));
		this.add(patternTypeLabel);
		buttonGroup.add(flexibleButton);
		buttonGroup.add(strictButton);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(flexibleButton);
		this.add(Box.createRigidArea(new Dimension(5, 0)));
		this.add(strictButton);
		
		this.add(Box.createRigidArea(new Dimension(5, 15)));
		seqButton = new SlickerButton("View sequences");
		seqButton.setEnabled(true);
		this.add(seqButton);
		patButton = new SlickerButton("View patterns");
		patButton.setEnabled(true);
		this.add(patButton);
	}

	public void updated() {
	}

	public String getPanelName() {
		return "Configure";
	}

	public JComponent getComponent() {
		return this;
	}

	public void setScalableComponent(ScalableComponent scalable) {
	}

	public void setParent(ScalableViewPanel viewPanel) {
	}

	public double getHeightInView() {
		return 250;
	}

	public double getWidthInView() {
		return 160;
	}

	public void willChangeVisibility(boolean to) {
	}

	/**
	 * variables to keep track of the selected time sort
	 */
	private String timeSort = "seconds";
	/**
	 * the time divider
	 */
	private long timeDivider = 1000;
	private String dataEltType = PSDLogUtils.TASKID;

	private void registerGUIListener() {
		//componentBox listener: set dataElt to the component that is selected
		//in the box
		componentBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataEltType = (String) componentBox.getSelectedItem();
			}
		});
		//timeBox listener: set timeSort to the sort that is selected in the box
		//and set timeDivider to the corresponding divider.
		timeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeSort = (String) timeBox.getSelectedItem();
				timeDivider = dividers[timeBox.getSelectedIndex()];
			}
		});
		//showButton listener: perform the actual analysis and display the
		//selected diagram
		seqButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				root.updateConfig(dataEltType, timeSort, timeDivider, strictButton.isSelected());
				root.setDiagram(false);
			}
		});
		patButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				root.updateConfig(dataEltType, timeSort, timeDivider, strictButton.isSelected());
				root.setDiagram(true);
			}
		});
	}

	private void initializeComponentBox() {
		//still have to reset, even though instanceIterator() is used
		//		inputLog.reset();
		Iterator<XTrace> lit = log.iterator();
		if (lit.hasNext()) {
			XTrace pi = lit.next();
			try {
				int index = 0;
				XEvent ate = pi.get(index++);
				while (PSDLogUtils.getTimestamp(ate) == null && index != pi.size()) {
					//get the first audit trail entry that has a timestamp attached to it
					ate = pi.get(index++);
				}
				if (PSDLogUtils.getTimestamp(ate) == null) {
					//There is no audit trail etry that has a timeStamp attached to it
					componentBox.addItem("None");
				} else {
					//There is an audit trail etry that has a timeStamp attached to it
					componentBox.addItem("Task ID");
					if (PSDLogUtils.getOriginator(ate) != null) {
						//the audit trail entry has an originator attached to it
						componentBox.addItem("Originator");
					}
					//get the other data elements in the audit trail entry
					String[] otherElts = getOtherDataElements(pi);
					for (int i = 0; i < otherElts.length; i++) {
						//add the other data elements to the componentBox
						componentBox.addItem(otherElts[i]);
					}
				}
			} catch (Exception e) {
			}

		}
	}

	/**
	 * Walks through process instance pi, and returns the data-elements that
	 * appear in it as a sorted array
	 * 
	 * @param pi
	 *            ProcessInstance
	 * @return String[]
	 */
	private String[] getOtherDataElements(XTrace pi) {
		XTrace ates = pi;
		Iterator<XEvent> it = ates.iterator();
		Set<String> elts = new HashSet<String>();
		while (it.hasNext()) {
			XEvent ate = it.next();
			Iterator<String> it2 = ate.getAttributes().keySet().iterator();
			//run through attributes
			while (it2.hasNext()) {
				String tempString = it2.next();
				if (tempString != "") {
					//add tempString to elts if it is not equal to the empty String
					elts.add(tempString);
				}
			}
		}
		//put the data elements in an array
		String[] set = elts.toArray(new String[0]);
		//sort the array
		Arrays.sort(set);
		//return the sorted array
		return set;
	}
}
