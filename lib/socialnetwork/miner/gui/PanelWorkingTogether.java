package org.processmining.plugins.socialnetwork.miner.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.plugins.socialnetwork.miner.SNMinerOptions;

public class PanelWorkingTogether extends JPanel {

	private static final long serialVersionUID = 1871882171075777406L;
	private final GridBagLayout gridBagLayout3 = new GridBagLayout();
	private final JRadioButton wtSimultaneousAppearance = new JRadioButton();
	private final JRadioButton wtDistanceWithoutCausality = new JRadioButton();

	public PanelWorkingTogether() {
		init();
	}

	private void init() {

		ButtonGroup workingTogetherGroup = new ButtonGroup();

		setLayout(gridBagLayout3);
		wtSimultaneousAppearance.setText("Simultaneous appearance ratio");
		wtDistanceWithoutCausality.setText("Consider distance without causality (beta=0.5)");
		wtSimultaneousAppearance.setSelected(true);
		this.add(wtSimultaneousAppearance, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(wtDistanceWithoutCausality, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		workingTogetherGroup.add(wtSimultaneousAppearance);
		workingTogetherGroup.add(wtDistanceWithoutCausality);

	}

	public boolean getSimultaneousAppearance() {
		return wtSimultaneousAppearance.isSelected();
	}

	public boolean getDistanceWithoutCausality() {
		return wtDistanceWithoutCausality.isSelected();
	}

	/**
	 * Returns the currently selected option for WORKING_TOGETHER. Can be one of
	 * the constants: SIMULTANEOUS_APPEARANCE_RATIO, DISTANCE_WITH_CAUSALITY or
	 * DISTANCE_WITHOUT_CAUSALITY.
	 * 
	 * @return the currently selected option for WORKING_TOGETHER
	 */
	public int getWorkingTogetherSetting() {
		if (getSimultaneousAppearance()) {
			return SNMinerOptions.SIMULTANEOUS_APPEARANCE_RATIO;
			//			} else if (getDistanceWithCausality()) {
			//					return SNMinerOptions.DISTANCE_WITH_CAUSALITY;
		} else { // panelWorkingTogether.getDistanceWithoutCausality()
			return SNMinerOptions.DISTANCE_WITHOUT_CAUSALITY;
		}
	}

}
