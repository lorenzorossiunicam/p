package org.processmining.plugins.socialnetwork.miner.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.plugins.socialnetwork.miner.SNMinerOptions;

public class PanelReassignment extends JPanel {

	private static final long serialVersionUID = -8758269264589836543L;
	private final GridBagLayout gridBagLayout5 = new GridBagLayout();
	private final JRadioButton raDirectReassignment = new JRadioButton();
	//	private JRadioButton raConsiderDistance = new JRadioButton();
	private final JCheckBox raIgnoreMultipleTransfers = new JCheckBox();
	//	private JLabel raBetaLabel = new JLabel();
	//	private JTextField raBeta = new JTextField();

	private final JLabel raMultipleTransfersLabel2 = new JLabel();
	private final JLabel raMultipleTransfersLabel1 = new JLabel();

	public PanelReassignment() {
		init();
	}

	private void init() {
		//modifed 10.03
		raMultipleTransfersLabel1.setFont(new java.awt.Font("Dialog", 0, 9));
		raMultipleTransfersLabel1.setText("(checked: # of RA cases / # of all instances");
		raMultipleTransfersLabel2.setFont(new java.awt.Font("Dialog", 0, 9));
		raMultipleTransfersLabel2.setText(" unchecked: # of RA tasks / # of all tasks )");

		setLayout(gridBagLayout5);
		raIgnoreMultipleTransfers.setText("Ignore multiple transfers within one instance");
		raDirectReassignment.setText("Direct reassignment");
		this.add(raIgnoreMultipleTransfers, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(raMultipleTransfersLabel2, new GridBagConstraints(0, 3, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 25, 0, 0), 0, 0));
		this.add(raMultipleTransfersLabel1, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 25, 0, 0), 0, 0));

	}

	public boolean getIgnoreMultipleTransfers() {
		return raIgnoreMultipleTransfers.isSelected();
	}

	/*
	 * public boolean getDirectReassignment() { return
	 * raDirectReassignment.isSelected(); }
	 */

	/**
	 * Returns the currently selected option for REASSIGNMENT. Can be one of the
	 * constants: DIRECT_REASSIGNMENT or CONSIDER_DISTANCE.
	 * 
	 * @return the currently selected option for REASSIGNMENT
	 */
	public int getReassignmentSetting() {
		return getIgnoreMultipleTransfers() ? 0 : SNMinerOptions.MULTIPLE_REASSIGNMENT;
	}
}
