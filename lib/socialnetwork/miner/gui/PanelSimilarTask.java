package org.processmining.plugins.socialnetwork.miner.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.processmining.plugins.socialnetwork.miner.SNMinerOptions;

public class PanelSimilarTask extends JPanel {

	private static final long serialVersionUID = -7420305525990184005L;

	private final GridBagLayout gridBagLayout4 = new GridBagLayout();
	private final JRadioButton stEuclidianDistance = new JRadioButton();
	private final JRadioButton stCorrelationCoefficient = new JRadioButton();
	private final JRadioButton stSimilarityCoefficient = new JRadioButton();
	private final JRadioButton stHammingDistance = new JRadioButton();

	public PanelSimilarTask() {
		init();
	}

	private void init() {
		ButtonGroup similarTaskGroup = new ButtonGroup();

		// ----------- Similar task -----------------------------------

		setLayout(gridBagLayout4);
		stEuclidianDistance.setText("Euclidian distance");
		stCorrelationCoefficient.setText("Correlation coefficient");
		stSimilarityCoefficient.setText("Similarity coefficient");
		stHammingDistance.setText("Hamming distance");
		stEuclidianDistance.setSelected(true);
		this.add(stEuclidianDistance, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(stCorrelationCoefficient, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(stSimilarityCoefficient, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(stHammingDistance, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		similarTaskGroup.add(stEuclidianDistance);
		similarTaskGroup.add(stCorrelationCoefficient);
		similarTaskGroup.add(stSimilarityCoefficient);
		similarTaskGroup.add(stHammingDistance);
	}

	public boolean getEuclidianDistance() {
		return stEuclidianDistance.isSelected();
	}

	public boolean getCorrelationCoefficient() {
		return stCorrelationCoefficient.isSelected();
	}

	public boolean getSimilarityCoefficient() {
		return stSimilarityCoefficient.isSelected();
	}

	/**
	 * Returns the currently selected option for SIMILAR_TASK. Can be one of the
	 * constants: EUCLIDIAN_DISTANCE, CORRELATION_COEFFICIENT,
	 * SIMILARITY_COEFFICIENT or HAMMING_DISTANCE.
	 * 
	 * @return the currently selected option for SIMILAR_TASK
	 */
	public int getSimilarTaskSetting() {
		if (getEuclidianDistance()) {
			return SNMinerOptions.EUCLIDIAN_DISTANCE;
		} else if (getCorrelationCoefficient()) {
			return SNMinerOptions.CORRELATION_COEFFICIENT;
		} else if (getSimilarityCoefficient()) {
			return SNMinerOptions.SIMILARITY_COEFFICIENT;
		} else { // stHammingDistance.isSelected()
			return SNMinerOptions.HAMMING_DISTANCE;
		}
	}
}
