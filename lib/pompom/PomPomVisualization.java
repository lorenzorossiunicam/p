package org.processmining.plugins.pompom;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;

@Plugin(name = "@0 Visualize PomPom View", level = PluginLevel.PeerReviewed, returnLabels = { "Visualized PomPom View" }, returnTypes = { JComponent.class }, parameterLabels = { "PomPomView" }, userAccessible = true)
@Visualizer
public class PomPomVisualization implements ChangeListener, ItemListener {

	private int treshold = 0;
	JPanel mainPanel;
	JComponent display;
	PomPomView view;
	PomPomPanel panel;
	PluginContext context;

	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, PomPomView view) {
		this.view = view;
		this.context = context;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		display = view.generateView(context, treshold / 100.0f, 0);
		mainPanel.add(display, BorderLayout.CENTER);
		panel = new PomPomPanel(context, this);
		mainPanel.add(panel, BorderLayout.EAST);
		return mainPanel;
	}

	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		if (!source.getValueIsAdjusting()) {
			generateView();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		generateView();
	}

	private void generateView() {
		treshold = panel.nodeSignificanceSlider.getValue();
		mainPanel.remove(display);
		int viewType = 0;
		if (panel.epcRadioButton.isSelected()) {
			viewType = 1;
		}
		display = view.generateView(context, treshold / 100.0f, viewType);
		mainPanel.add(display, BorderLayout.CENTER);
		mainPanel.validate();
		mainPanel.repaint();
	}
}
