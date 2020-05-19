package org.processmining.plugins.pompom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.processmining.framework.plugin.PluginContext;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.StackedCardsTabbedPane;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerRadioButtonUI;

public class PomPomPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1576125631308495588L;

	private final PomPomVisualization vis;

	/*
	 * Copied from Fuzzy miner
	 */
	public NiceIntegerSlider nodeSignificanceSlider;
	public JRadioButton petriNetRadioButton;
	public JRadioButton epcRadioButton;
	private Font smallFont;
	private final Color COLOR_BG2 = new Color(120, 120, 120);
	private final Color COLOR_FG = new Color(30, 30, 30);

	public PomPomPanel(PluginContext context, PomPomVisualization vis) {
		this.vis = vis;
		setBackground(new Color(240, 240, 240));
		initializeGui();
	}

	private void initializeGui() {
		/*
		 * Copied from Fuzzy miner
		 */
		smallFont = getFont().deriveFont(11f);
		JPanel upperControlPanel = new JPanel();
		upperControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		upperControlPanel.setBackground(COLOR_BG2);
		upperControlPanel.setOpaque(true);
		upperControlPanel.setLayout(new BorderLayout());
//		JLabel nodeSigSliderLabel = new JLabel("Significance cutoff");
//		nodeSigSliderLabel.setFont(smallFont);
//		nodeSigSliderLabel.setOpaque(false);
//		nodeSigSliderLabel.setForeground(COLOR_FG);
//		centerHorizontally(nodeSigSliderLabel);
//		upperControlPanel.add(nodeSigSliderLabel, BorderLayout.NORTH);
//		nodeSignificanceLabel = new JLabel("0.000");
//		nodeSignificanceLabel.setOpaque(false);
//		nodeSignificanceLabel.setForeground(COLOR_FG);
//		nodeSignificanceLabel.setFont(smallFont);
//		centerHorizontally(nodeSignificanceLabel);
//		upperControlPanel.add(packVerticallyCentered(nodeSignificanceLabel, 50, 20), BorderLayout.SOUTH);
//		nodeSignificanceSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, 0);
//		nodeSignificanceSlider.setUI(new SlickerSliderUI(nodeSignificanceSlider));
		nodeSignificanceSlider = SlickerFactory.instance().createNiceIntegerSlider("Significance cutoff", 0, 100, 0,
				Orientation.VERTICAL);
		nodeSignificanceSlider.addChangeListener(vis);
		nodeSignificanceSlider.setOpaque(false);
		nodeSignificanceSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown as single activities,<br>" + "increasing the detail and complexity<br>"
				+ "of the model.</html>");
		upperControlPanel.add(nodeSignificanceSlider, BorderLayout.CENTER);

		JPanel lowerControlPanel = new JPanel(); // lowerControlPanel is the Edge filter panel
		lowerControlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		lowerControlPanel.setBackground(COLOR_BG2);
		lowerControlPanel.setOpaque(true);
		lowerControlPanel.setLayout(new BorderLayout());
		JPanel lowerHeaderPanel = new JPanel();
		lowerHeaderPanel.setOpaque(false);
		lowerHeaderPanel.setLayout(new BoxLayout(lowerHeaderPanel, BoxLayout.Y_AXIS));
		JLabel lowerHeaderLabel = new JLabel("Show result as");
		lowerHeaderLabel.setOpaque(false);
		lowerHeaderLabel.setForeground(COLOR_FG);
		lowerHeaderLabel.setFont(smallFont);
		//centerHorizontally(lowerHeaderLabel);
		petriNetRadioButton = new JRadioButton("Petri net");
		petriNetRadioButton.setSelected(true);
		petriNetRadioButton.setUI(new SlickerRadioButtonUI());
		petriNetRadioButton.setFont(smallFont);
		petriNetRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 12));
		petriNetRadioButton.setOpaque(false);
		petriNetRadioButton.setForeground(COLOR_FG);
		petriNetRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		petriNetRadioButton.setHorizontalAlignment(SwingConstants.LEFT);
		petriNetRadioButton.addItemListener(vis);
		petriNetRadioButton.setToolTipText("<html>Shows the result as a Petri net.</html>");
		epcRadioButton = new JRadioButton("EPC");
		epcRadioButton.setUI(new SlickerRadioButtonUI());
		epcRadioButton.setFont(smallFont);
		epcRadioButton.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 12));
		epcRadioButton.setOpaque(false);
		epcRadioButton.setForeground(COLOR_FG);
		epcRadioButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		epcRadioButton.setHorizontalAlignment(SwingConstants.LEFT);
		epcRadioButton.addItemListener(vis);
		epcRadioButton.setToolTipText("<html>Shows the result as an EPC.</html>");
		ButtonGroup radioEdgesGroup = new ButtonGroup();
		radioEdgesGroup.add(petriNetRadioButton);
		radioEdgesGroup.add(epcRadioButton);
		lowerHeaderPanel.add(lowerHeaderLabel);
		lowerHeaderPanel.add(Box.createVerticalStrut(2));
		lowerHeaderPanel.add(petriNetRadioButton);
		lowerHeaderPanel.add(epcRadioButton);
		lowerHeaderPanel.add(Box.createVerticalStrut(5));
		lowerControlPanel.add(lowerHeaderPanel, BorderLayout.NORTH);

		StackedCardsTabbedPane tabPane = new StackedCardsTabbedPane();
		tabPane.addTab("Node filter", upperControlPanel);
		tabPane.addTab("Show Result", lowerControlPanel);
		tabPane.setActive(0);
		tabPane.setMinimumSize(new Dimension(190, 220));
		tabPane.setMaximumSize(new Dimension(190, 10000));
		tabPane.setPreferredSize(new Dimension(190, 10000));
		tabPane.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BorderLayout());
		setOpaque(false);
		add(tabPane, BorderLayout.CENTER);

	}

	/*
	 * Copied from Fuzzy miner
	 */
	protected JPanel packVerticallyCentered(JComponent component, int width, int height) {
		JPanel boxed = new JPanel();
		boxed.setLayout(new BoxLayout(boxed, BoxLayout.X_AXIS));
		boxed.setBorder(BorderFactory.createEmptyBorder());
		boxed.setOpaque(false);
		Dimension dim = new Dimension(width, height);
		component.setMinimumSize(dim);
		component.setMaximumSize(dim);
		component.setPreferredSize(dim);
		component.setSize(dim);
		boxed.add(Box.createHorizontalGlue());
		boxed.add(component);
		boxed.add(Box.createHorizontalGlue());
		return boxed;
	}

}
