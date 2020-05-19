/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfPanel.GraphHighlightObject;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author aadrians Mar 7, 2012
 * 
 */
public class ColorSettingPanel<N extends Manifest, C extends IPerfCounter<N>> extends JPanel {
	private static final long serialVersionUID = -154023208885889250L;

	/**
	 * Pointer to parent
	 */
	private JComboBox transitionColor;
	private JComboBox transitionSize;
	private JComboBox transitionBorderwidth;

	private JComboBox placeColor;
	private JComboBox placeSize;
	private JComboBox placeBorderwidth;

	private JComboBox arcColor;
	private JComboBox arcWidth;

	public ColorSettingPanel(final ManifestPerfPanel<N, C> manifestPerfPanel) {
		// transition stats
		Object[] transStats = new Object[] { ManifestPerfPanel.NONE, 
				ManifestPerfPanel.TRANS_THROUGHPUT_TIME_MIN,
				ManifestPerfPanel.TRANS_THROUGHPUT_TIME_MAX, 
				ManifestPerfPanel.TRANS_THROUGHPUT_TIME_AVG,
				//ManifestPerfPanel.TRANS_THROUGHPUT_TIME_STDDEV, 
				ManifestPerfPanel.TRANS_WAITING_TIME_MIN, 
				ManifestPerfPanel.TRANS_WAITING_TIME_MAX, 
				ManifestPerfPanel.TRANS_WAITING_TIME_AVG, 
				//ManifestPerfPanel.TRANS_WAITING_TIME_STDDEV,
				ManifestPerfPanel.TRANS_SOJOURN_TIME_MIN, 
				ManifestPerfPanel.TRANS_SOJOURN_TIME_MAX, 
				ManifestPerfPanel.TRANS_SOJOURN_TIME_AVG, 
				//ManifestPerfPanel.TRANS_SOJOURN_TIME_STDDEV, 
				ManifestPerfPanel.TRANS_FREQUENCY, 
				//ManifestPerfPanel.TRANS_UNIQUECASES 
				};

		// place related stats
		Object[] placeStats = new Object[] {ManifestPerfPanel.NONE, 
				ManifestPerfPanel.PLACE_WAITING_TIME_MIN,
				ManifestPerfPanel.PLACE_WAITING_TIME_MAX, 
				ManifestPerfPanel.PLACE_WAITING_TIME_AVG, 
				ManifestPerfPanel.PLACE_WAITING_TIME_STDDEV,
				ManifestPerfPanel.PLACE_SYNC_TIME_MIN, 
				ManifestPerfPanel.PLACE_SYNC_TIME_MAX, 
				ManifestPerfPanel.PLACE_SYNC_TIME_AVG, 
				ManifestPerfPanel.PLACE_SYNC_TIME_STDDEV, 
				ManifestPerfPanel.PLACE_SOJOURN_TIME_MIN, 
				ManifestPerfPanel.PLACE_SOJOURN_TIME_MAX,
				ManifestPerfPanel.PLACE_SOJOURN_TIME_AVG, 
				ManifestPerfPanel.PLACE_SOJOURN_TIME_STDDEV, 
				ManifestPerfPanel.PLACE_FREQUENCY 
				};

		// arc related stats
		Object[] arcStats = new Object[] { ManifestPerfPanel.NONE, ManifestPerfPanel.ARC_FREQUENCY };

		SlickerFactory factory = SlickerFactory.instance();
		
		transitionColor = factory.createComboBox(transStats);
		transitionColor.setSelectedItem(ManifestPerfPanel.TRANS_SOJOURN_TIME_AVG);
		transitionColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.TRANSITIONCOLOR, transitionColor.getSelectedItem().toString());
			}
		});
		
		transitionSize = factory.createComboBox(transStats);
		transitionSize.setSelectedItem(ManifestPerfPanel.NONE);
		transitionSize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.TRANSITIONSIZE, transitionSize.getSelectedItem().toString());
			}
		});
		
		transitionBorderwidth = factory.createComboBox(transStats);
		transitionBorderwidth.setSelectedItem(ManifestPerfPanel.TRANS_FREQUENCY);
		transitionBorderwidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.TRANSITIONWIDTH, transitionBorderwidth.getSelectedItem().toString());
			}
		});

		placeColor = factory.createComboBox(placeStats);
		placeColor.setSelectedItem(ManifestPerfPanel.PLACE_WAITING_TIME_AVG);
		placeColor.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.PLACECOLOR, placeColor.getSelectedItem().toString());
			}
		});
		
		placeSize = factory.createComboBox(placeStats);
		placeSize.setSelectedItem(ManifestPerfPanel.NONE);
		placeSize.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.PLACESIZE, placeSize.getSelectedItem().toString());
			}
		});
		
		placeBorderwidth = factory.createComboBox(placeStats);
		placeBorderwidth.setSelectedItem(ManifestPerfPanel.PLACE_FREQUENCY);
		placeBorderwidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.PLACEWIDTH, placeBorderwidth.getSelectedItem().toString());
			}
		});

		arcColor = factory.createComboBox(arcStats);
		arcColor.setSelectedItem(ManifestPerfPanel.ARC_FREQUENCY);
		arcColor.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.ARCCOLOR, arcColor.getSelectedItem().toString());
			}
		});
		
		arcWidth = factory.createComboBox(arcStats);
		arcWidth.setSelectedItem(ManifestPerfPanel.ARC_FREQUENCY);
		arcWidth.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				manifestPerfPanel.adjustGraphHighlight(GraphHighlightObject.ARCWIDTH, arcWidth.getSelectedItem().toString());
			}
		});

		double[][] size = new double[][] { { .4, .5 }, { 25, 25, 25, 25, 25, 25, 25, 25 } };
		setLayout(new TableLayout(size));
		add(factory.createLabel("Transition Color"), "0,0");
		add(transitionColor, "1,0");
		add(factory.createLabel("Transition Size"), "0,1");
		add(transitionSize, "1,1");
		add(factory.createLabel("Transition Border Width"), "0,2");
		add(transitionBorderwidth, "1,2");
		add(factory.createLabel("Place Color"), "0,3");
		add(placeColor, "1,3");
		add(factory.createLabel("Place Size"), "0,4");
		add(placeSize, "1,4");
		add(factory.createLabel("Place Border Width"), "0,5");
		add(placeBorderwidth, "1,5");
		add(factory.createLabel("Arc color"), "0,6");
		add(arcColor, "1,6");
		add(factory.createLabel("Arc Width"), "0,7");
		add(arcWidth, "1,7");
		
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}
}