package org.processmining.plugins.ywl.replayer.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/

// small changes to Arya's implementation for FPD's

public class WeightingStep extends ReplayStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4318663984761515562L;
	
	// GUI components
	private ButtonGroup remTokensGroup;
	private JRadioButton yesRemTokens;
	private JRadioButton noRemTokens;
	private NiceIntegerSlider remTokenSlider;
	
	private ButtonGroup misTokensGroup;
	private JRadioButton yesMisTokens;
	private JRadioButton noMisTokens;
	private NiceIntegerSlider misTokenSlider;
	
	private ButtonGroup heurDistanceGroup;
	private JRadioButton yesHeurDistance;
	private JRadioButton noHeurDistance;
	private NiceIntegerSlider heurDistanceSlider;
	
	private ButtonGroup cancellationGroup;
	private JRadioButton yesCancellation;
	private JRadioButton noCancellation;

	public WeightingStep(){
		initComponents();
	}
	
	/* (non-Javadoc)
	 * @see org.processmining.plugins.flex.replayer.ui.ReplayStep#precondition()
	 */
	public boolean precondition() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.processmining.plugins.flex.replayer.ui.ReplayStep#readSettings()
	 */
	public void readSettings() {
		// TODO Auto-generated method stub

	}

	private void initComponents() {
		// init instance
		SlickerFactory slickerFactory = SlickerFactory.instance();
		
    	double size[][] = {{TableLayout.FILL,TableLayout.FILL},{80, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30}};
    	setLayout(new TableLayout(size));
    	String body = "<p>Configure replay parameter values.</p>";
    	add(slickerFactory.createLabel("<html><h1>Configure parameter</h1>" + body), "0, 0, l, t");
    	
    	// add sliders
    	// missing tokens weight
    	misTokenSlider = slickerFactory.createNiceIntegerSlider("", 0, 100, 80, Orientation.HORIZONTAL); 
    	misTokenSlider.setPreferredSize(new Dimension(220,20));
    	misTokensGroup = new ButtonGroup();
    	yesMisTokens = slickerFactory.createRadioButton("Use missing tokens weighting");
    	noMisTokens = slickerFactory.createRadioButton("Don't use remaining tokens weighting");
    	misTokensGroup.add(yesMisTokens);
    	misTokensGroup.add(noMisTokens);
    	yesMisTokens.setSelected(true);
    	
    	add(slickerFactory.createLabel("<html><h3>Configure missing tokens weight</h2>"), "0, 1");
    	add(noMisTokens, "0, 2");
    	add(yesMisTokens, "0, 3");
    	add(misTokenSlider, "1, 3");

    	// remaining tokens weight
    	remTokenSlider = slickerFactory.createNiceIntegerSlider("", 0, 100, 10, Orientation.HORIZONTAL); 
    	remTokenSlider.setPreferredSize(new Dimension(220,20));
    	remTokensGroup = new ButtonGroup();
    	yesRemTokens = slickerFactory.createRadioButton("Use remaining tokens weighting");
    	noRemTokens = slickerFactory.createRadioButton("Don't use remaining tokens weighting");
    	remTokensGroup.add(yesRemTokens);
    	remTokensGroup.add(noRemTokens);
    	yesRemTokens.setSelected(true);
    	
    	add(slickerFactory.createLabel("<html><h3>Configure remaining tokens weight</h2>"), "0, 4");
    	add(noRemTokens, "0, 5");
    	add(yesRemTokens, "0, 6");
    	add(remTokenSlider, "1, 6");
    	
    	// heuristic distance weight 
    	heurDistanceSlider = slickerFactory.createNiceIntegerSlider("", 0, 100, 1, Orientation.HORIZONTAL); 
    	heurDistanceSlider.setPreferredSize(new Dimension(220,20));
    	heurDistanceGroup = new ButtonGroup();
    	yesHeurDistance = slickerFactory.createRadioButton("Use heuristic distance weighting");
    	noHeurDistance = slickerFactory.createRadioButton("Don't use remaining tokens weighting");
    	heurDistanceGroup.add(yesHeurDistance);
    	heurDistanceGroup.add(noHeurDistance);
    	yesHeurDistance.setSelected(true);
    	
    	add(slickerFactory.createLabel("<html><h3>Configure heuristic distance weight</h2>"), "0, 7");
    	add(noHeurDistance, "0, 8");
    	add(yesHeurDistance, "0, 9");
    	add(heurDistanceSlider, "1, 9");
    	
    	cancellationGroup = new ButtonGroup();
    	yesCancellation = slickerFactory.createRadioButton("Show cancellation performance metrics");
    	noCancellation = slickerFactory.createRadioButton("Don't show cancellation performance metrics");
    	cancellationGroup.add(yesCancellation);
    	cancellationGroup.add(noCancellation);
    	yesCancellation.setSelected(true);
    	
    	add(slickerFactory.createLabel("<html><h3>Cancellation regions</h2>"), "0, 10");
    	add(noCancellation, "0, 11");
    	add(yesCancellation, "0, 12");

	}

	public int[] getWeights() {
		int misTokenWeight = noMisTokens.isSelected() ? 0 : misTokenSlider.getValue();
		int remTokenWeight = noRemTokens.isSelected() ? 0 : remTokenSlider.getValue();
		int heurDistanceWeight = noHeurDistance.isSelected() ? 0 : heurDistanceSlider.getValue();
		int cancellation = noCancellation.isSelected() ? 0 : 1;
		return new int[] {misTokenWeight, remTokenWeight, heurDistanceWeight, cancellation};
	}
}
