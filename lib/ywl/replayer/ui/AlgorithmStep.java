package org.processmining.plugins.ywl.replayer.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

import org.processmining.plugins.ywl.replayer.IYwlLogReplayAlgorithm;
import org.processmining.plugins.ywl.replayer.YawlLogReplayAlgorithm;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerComboBoxUI;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class AlgorithmStep extends ReplayStep {

	private static final long serialVersionUID = -4015645475781862031L;

	private JComboBox combo;
	
	private ButtonGroup cancellationGroup;
	private JRadioButton yesCancellation;
	private JRadioButton noCancellation;
	
	public AlgorithmStep(){
		initComponents();
	}

	public boolean precondition() {
		return true;
	}

	public void readSettings() {}

	private void initComponents() {
		// init instance
		SlickerFactory slickerFactory = SlickerFactory.instance();
		
    	double size[][] = {{TableLayout.FILL},{80, 30}};
    	setLayout(new TableLayout(size));
    	String body = "<p>Select your replay algorithm.</p>";
    	add(slickerFactory.createLabel("<html><h1>Select Algorithm</h1>" + body), "0, 0, l, t");
    	
    	// add combobox
    	IYwlLogReplayAlgorithm[] availAlgorithms = new IYwlLogReplayAlgorithm[1];
    	availAlgorithms[0] = new YawlLogReplayAlgorithm();
    	
		combo = new JComboBox(availAlgorithms);
		combo.setPreferredSize(new Dimension(150,25));
		combo.setSize(new Dimension(150,25));
		combo.setMinimumSize(new Dimension(150,25));
		combo.setSelectedItem(0);
		combo.setUI(new SlickerComboBoxUI());
		add(combo, "0, 1");
		
    	cancellationGroup = new ButtonGroup();
    	yesCancellation = slickerFactory.createRadioButton("Show cancellation performance metrics");
    	noCancellation = slickerFactory.createRadioButton("Don't show cancellation performance metrics");
    	cancellationGroup.add(yesCancellation);
    	cancellationGroup.add(noCancellation);
    	yesCancellation.setSelected(true);
    	
    	add(slickerFactory.createLabel("<html><h3>Cancellation regions</h2>"), "0, 2");
    	add(noCancellation, "0, 3");
    	add(yesCancellation, "0, 4");
	}

	public IYwlLogReplayAlgorithm getAlgorithm() {
		return (IYwlLogReplayAlgorithm) combo.getSelectedItem();
	}
	
	public int getCancellation() {
		int cancellation = noCancellation.isSelected() ? 0 : 1;
		return cancellation;
	}
	
}
