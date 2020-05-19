package org.processmining.plugins.transitionsystem;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.TransitionSystem;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.plugins.transitionsystem.miner.TSMiner;
import org.processmining.plugins.transitionsystem.miner.TSMinerInput;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.SlickerButton;

public class MinedTSLogFilter extends JPanel implements ActionListener, ViewInteractionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3710929196431016518L;

	private TSMinerInput settings;
	private PluginContext context;
	private ProMJGraphPanel parentPanel;
	private NiceIntegerSlider slider;
	private SlickerButton filterButton;
	private TransitionSystem transitionSystem;
	private JPanel componentPanel;
	private JLabel label;
	private int maxValue;

	public MinedTSLogFilter(PluginContext context, TransitionSystem ts, TSMinerInput settings, ProMJGraphPanel panel) {
		
		this.context = context;
		transitionSystem = ts;
		this.settings = settings;
		parentPanel = panel;
				
		double size[][] = { { 400, 100 }, { 25, 10, 25 } };
		componentPanel = new JPanel();
		componentPanel.setLayout(new TableLayout(size));
		componentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		componentPanel.setOpaque(true);
		
		maxValue = 1;
		slider = new NiceIntegerSlider("Coverage", 0, Math.max(maxValue, 1), 1);
		componentPanel.add(slider, "0, 0, 1, 0");
		slider.setVisible(false);
		
		filterButton = new SlickerButton("Filter log");
		filterButton.setFont(filterButton.getFont().deriveFont(11f));
		filterButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 12));
		filterButton.setOpaque(false);
		filterButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		filterButton.setHorizontalAlignment(SwingConstants.LEFT);
		filterButton.addActionListener(this);
		filterButton.setToolTipText("<html>Create a log containing those traces that reach the threshold.</html>");
		componentPanel.add(filterButton, "1, 2");
		filterButton.setVisible(false);

		label = new JLabel("Please, first select some elements.");
		componentPanel.add(label, "0, 0, 1, 2");
		label.setVisible(true);
	}
	
	@Override
	public JComponent getComponent() {
		// TODO Auto-generated method stub
		return componentPanel;
	}

	@Override
	public double getHeightInView() {
		// TODO Auto-generated method stub
		return 60;
	}

	@Override
	public String getPanelName() {
		// TODO Auto-generated method stub
		return "Filter log";
	}

	@Override
	public double getWidthInView() {
		// TODO Auto-generated method stub
		return 500;
	}

	@Override
	public void setParent(ScalableViewPanel viewPanel) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setScalableComponent(ScalableComponent scalable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void willChangeVisibility(boolean to) {
		// TODO Auto-generated method stub
		maxValue = parentPanel.getSelectedElements().size();
		/*
		 * NiceIntegerSlider does not provide a SetMaximum.
		 * As a result, we need to replace the NiceIntegerSlider with a brand new one.
		 */
		componentPanel.remove(slider);
		slider = new NiceIntegerSlider("Threshold for number of selected elements", 0, Math.max(maxValue, 1), 1);
		componentPanel.add(slider, "0, 0, 1, 0");
		/*
		 * Set the proper visibilities.
		 */
		slider.setVisible(maxValue > 0);
		filterButton.setVisible(maxValue > 0);
		label.setVisible(maxValue <= 0);
	}

	@Override
	public void updated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/*
		 * User selected a filter button.
		 * 
		 * Get the selected nodes and selected edges. Should be states and
		 * transitions.
		 */
		Collection<DirectedGraphNode> selectedNodes = parentPanel.getSelectedNodes();
		Collection<State> selectedStates = new HashSet<State>();
		for (DirectedGraphNode node : selectedNodes) {
			if (node instanceof State) {
				selectedStates.add((State) node);
			}
		}
		Collection<DirectedGraphEdge<?, ?>> selectedEdges = parentPanel.getSelectedEdges();
		Collection<Transition> selectedTransitions = new HashSet<Transition>();
		for (DirectedGraphEdge<?, ?> edge : selectedEdges) {
			if (edge instanceof Transition) {
				selectedTransitions.add((Transition) edge);
			}
		}
		/*
		 * Filter the log as stored in the settings object based on the selected
		 * states and transitions. Only traces that cover all (if All button was
		 * selected) or that cover any (if Any button was selected) selected
		 * states and selected transitions will be present in the resulting log.
		 */
		TSMiner miner = new TSMiner(null);
		XLog log = miner.filter(settings, transitionSystem, selectedStates, selectedTransitions,
				slider.getValue());
		/*
		 * Create a provided object for the new log.
		 */
		context.getProvidedObjectManager().createProvidedObject("Log filtered by Transition System", log, XLog.class,
				context);
		/*
		 * Make the resulting log a favorite, if possible.
		 */
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(log)
					.setFavorite(true);
		}
	}

}
