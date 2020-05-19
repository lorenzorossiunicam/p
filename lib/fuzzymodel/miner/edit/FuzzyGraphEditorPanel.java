/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.plugins.fuzzymodel.miner.edit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.fuzzymodel.FMClusterNode;
import org.processmining.models.graphbased.directed.fuzzymodel.FMNode;
import org.processmining.models.graphbased.directed.fuzzymodel.MutableFuzzyGraph;
import org.processmining.models.graphbased.directed.fuzzymodel.impl.FMEdgeImpl;
import org.processmining.models.graphbased.directed.fuzzymodel.transform.TransformerUtils;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;

import com.fluxicon.slickerbox.components.GradientPanel;
import com.fluxicon.slickerbox.components.HeaderBar;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.components.SlickerButton;

/**
 * @author Christian W. Guenther (christian@deckfour.org)
 * @author Li Jiafei(jiafei@jlu.edu.cn)
 */ 
public class FuzzyGraphEditorPanel extends JPanel {

	private static final long serialVersionUID = -1606635822234038080L;
	
	protected MutableFuzzyGraph graphForEdit;
	protected Color background = new Color(100, 100, 100);
	protected Color canvasBackground = Color.WHITE;
	protected int width = 180;
	protected int border = 10;
	
	protected JComponent sidePanel = null;
	protected JPanel innerPanel = null;
	
	//protected GGGraphView graphView;
	protected PluginContext context;
	protected ProMJGraphPanel graphPanel;
	
	public FuzzyGraphEditorPanel(PluginContext context,MutableFuzzyGraph graph) {
		this(context,graph, true);
	}
	
	public FuzzyGraphEditorPanel(PluginContext context,MutableFuzzyGraph graph, boolean standalone) {
		// setup panel basics
		this.context = context;
		this.graphForEdit = graph;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setDoubleBuffered(true);
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		if(standalone == true) {
			// standalone, add some UI sugaring around us
			GradientPanel gradient = new GradientPanel(new Color(70, 70, 70), new Color(40, 40, 40));
			gradient.setLayout(new BorderLayout());
			gradient.setBorder(BorderFactory.createEmptyBorder());
			innerPanel = new RoundedPanel(10, 8, 1);
			innerPanel.setBackground(background);
			innerPanel.setLayout(new BorderLayout());
			gradient.add(innerPanel, BorderLayout.CENTER);
			HeaderBar header = new HeaderBar("Fuzzy Model Editor");
			header.setHeight(40);
			this.add(header, BorderLayout.NORTH);
			this.add(gradient, BorderLayout.CENTER);
		} else {
			// we are root!
			innerPanel = this;
		}
		// initialize view
		//LJF comment start
		//graphPanel = ProMJGraphVisualizer.getVisualizationPanel(graphForEdit, context.getProgress());
		//LJF comment end
		if (graphPanel == null) {
			graphPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, graphForEdit);
		}
		innerPanel.add(graphPanel,BorderLayout.CENTER);
		updateGraphLayout();
		showGeneralConfiguration();
		//ljf comment start
//		this.addAncestorListener(new AncestorListener() {
//			public void ancestorAdded(AncestorEvent event) {
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						updateGraphLayout();
//						showGeneralConfiguration();
//					}
//				});
//			}
//			public void ancestorMoved(AncestorEvent event) {
//				// ignore
//			}
//			public void ancestorRemoved(AncestorEvent event) {
//				// ignore
//			}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       
//		});
		//ljf comment end
	}
	
	public ProMJGraph updateGraphLayout() {
		ProMJGraph paintGraph = graphPanel.getGraph();
		installGraphListeners(paintGraph);
		revalidate();
		repaint();
		return paintGraph;
	}
	
	public void setGraph(MutableFuzzyGraph graph) {
		this.graphForEdit = graph;
		updateGraphLayout();
		showGeneralConfiguration();
	}
	
	public void showNodeConfiguration(ProMGraphCell node) {
		if(sidePanel != null) {
			innerPanel.remove(sidePanel);
		}
		ActionListener closeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGeneralConfiguration();
			}
		};
		sidePanel = new SidePanel(new FuzzyNodeEditor(context,this, node, closeListener));
		innerPanel.add(sidePanel, BorderLayout.WEST);
		this.revalidate();
		this.repaint();
	}
	
	public void showEdgeConfiguration(ProMGraphEdge edge) {
		if(sidePanel != null) {
			innerPanel.remove(sidePanel);
		}
		ActionListener closeListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showGeneralConfiguration();
			}
		};
		sidePanel = new SidePanel(new FuzzyEdgeEditor(context,this, edge, closeListener));
		innerPanel.add(sidePanel, BorderLayout.WEST);
		this.revalidate();
		this.repaint();
	}
	
	public void showGeneralConfiguration() {
		if(sidePanel != null) {
			innerPanel.remove(sidePanel);
		}
		// TODO!!
		sidePanel = new SidePanel(getOverviewPanel());//new SidePanel(new JLabel("empty!"));
		innerPanel.add(sidePanel, BorderLayout.WEST);
		this.revalidate();
		this.repaint();
	}
	
	protected void installGraphListeners(ProMJGraph graph) {
		
		// set the action whenever a node in the graph is selected
		GraphSelectionModel model = graph.getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {
			
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {							
							ProMGraphCell node = (ProMGraphCell) cell;
							showNodeConfiguration(node);							
						} else {
							if (cell instanceof ProMGraphEdge) {
								ProMGraphEdge edge = (ProMGraphEdge) cell;
								showEdgeConfiguration(edge);
							}
						}
					}
					else  //do not select the cell in the graph
					{
						showGeneralConfiguration();
					}
				}

			}
		});
		
		
	}
	
	public static JPanel layoutHorizontally(Component[] components, int height) {
		JPanel layout = new JPanel();
		layout.setBorder(BorderFactory.createEmptyBorder());
		layout.setOpaque(false);
		layout.setLayout(new BoxLayout(layout, BoxLayout.X_AXIS));
		if(height > 0) {
			layout.setMinimumSize(new Dimension(10, height));
			layout.setMaximumSize(new Dimension(50000, height));
			layout.setPreferredSize(new Dimension(200, height));
		}
		for(Component comp : components) {
			layout.add(comp);
		}
		return layout;
	}
	
	public static JPanel centerHorizontally(JComponent component, int height) {
		return layoutHorizontally(new Component[] {Box.createHorizontalGlue(), 
				component, Box.createHorizontalGlue()}, height);
	}
	
	protected class SidePanel extends JPanel {
		
		private static final long serialVersionUID = -5079687464761777691L;

		public SidePanel(JComponent payload) {
			this.setDoubleBuffered(true);
			this.setLayout(new BorderLayout());
			this.setBorder(BorderFactory.createEmptyBorder(border, border, border, border + 5));
			this.setBackground(background);
			this.setMinimumSize(new Dimension(width, 200));
			this.setMaximumSize(new Dimension(width, 2000));
			this.setPreferredSize(new Dimension(width, 800));
			this.add(payload, BorderLayout.CENTER);
		}
		
		protected void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			// fill background of sidebar panel
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(background);
			g2d.fillRect(0, 0, getWidth() + 1, getHeight() + 1);
			// paint rounded boundary of graph panel to the right
			g2d.setColor(canvasBackground);
			g2d.fill(new RoundRectangle2D.Float(getWidth() - 5, -0.5f, 15, getHeight(), 8, 8));
		}
	}
	
	protected JPanel getOverviewPanel() {
		JPanel overview = new JPanel();
		// setup panel basics
		overview.setLayout(new BoxLayout(overview, BoxLayout.Y_AXIS));
		overview.setOpaque(false);
		overview.setBorder(BorderFactory.createEmptyBorder());
		// assemble UI
		JLabel title = new JLabel("Welcome");
		title.setOpaque(false);
		title.setFont(title.getFont().deriveFont(15f));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setHorizontalTextPosition(JLabel.CENTER);
		title.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		String welcomeString = "<html>You can edit any node or edge in the Fuzzy Model displayed "
						+ "on the right. You can also add new nodes or edges to the model, "
						+ "or remove existing ones.<br>"
						+ "Clusters, and edges connected to them, can only be added or removed.</html>";
		JLabel welcome = createMessageLabel(welcomeString);
		// new node
		RoundedPanel nNodePanel = new RoundedPanel(8, 0, 3);
		nNodePanel.setLayout(new BoxLayout(nNodePanel, BoxLayout.Y_AXIS));
		nNodePanel.setBackground(new Color(130, 130, 130));
		JButton nNodeButton = new SlickerButton("add node");
		nNodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = "New event " + graphForEdit.getLogEvents().size();
				XEvent event = new XEventImpl();
				XAttributeMapImpl attMap = new XAttributeMapImpl();
				XAttributeLiteralImpl nameAttr = new XAttributeLiteralImpl("eventname",name);
				XAttributeLiteralImpl lifecycleAttr = new XAttributeLiteralImpl("eventtype","complete");
				attMap.put("concept:name", nameAttr);
				attMap.put("lifecycle:transition", lifecycleAttr);
				event.setAttributes(attMap);
                FMNode node = graphForEdit.addNode(event);
                graphForEdit.getNodeSignificanceMetric().setMeasure(node.getIndex(), 1.0);
        		String nodeLabel = TransformerUtils.getNodeLabel(node);
        		node.getAttributeMap().put(AttributeMap.LABEL,nodeLabel);
        		// node.getAttributeMap().put(AttributeMap.LABELCOLOR,Color.white);
        		//node.getAttributeMap().put(AttributeMap.FILLCOLOR, Color.RED);

                node.getAttributeMap().put(AttributeMap.SHOWLABEL,true);
                graphPanel.revalidate();
                graphPanel.repaint();
			/*	Thread updateThread = new Thread() {
					public void run() {
						
					}
				};
				updateThread.start();*/
			}
		});
		nNodePanel.add(Box.createVerticalStrut(5));
		nNodePanel.add(centerHorizontally(nNodeButton, 25));
		nNodePanel.add(Box.createVerticalStrut(5));
		// new edge
		RoundedPanel nEdgePanel = new RoundedPanel(8, 0, 3);
		nEdgePanel.setLayout(new BoxLayout(nEdgePanel, BoxLayout.Y_AXIS));
		nEdgePanel.setBackground(new Color(130, 130, 130));
		ArrayList<FMNode> primitives = new ArrayList<FMNode>();
		for(FMNode node : graphForEdit.getNodes()) {
			if((node instanceof FMClusterNode) == false) {
				primitives.add(node);
			}
		}
		final JComboBox sourceBox = new JComboBox(primitives.toArray());
		sourceBox.setMaximumSize(new Dimension(100, 25));
		sourceBox.setPreferredSize(new Dimension(100, 25));
		final JComboBox targetBox = new JComboBox(primitives.toArray());
		targetBox.setMaximumSize(new Dimension(100, 25));
		targetBox.setPreferredSize(new Dimension(100, 25));
		JLabel sourceLabel = new JLabel("From:");
		sourceLabel.setMaximumSize(new Dimension(35, 22));
		sourceLabel.setPreferredSize(new Dimension(35, 22));
		sourceLabel.setOpaque(false);
		sourceLabel.setFont(sourceLabel.getFont().deriveFont(11f));
		JLabel targetLabel = new JLabel("To:");
		targetLabel.setMaximumSize(new Dimension(35, 22));
		targetLabel.setPreferredSize(new Dimension(35, 22));
		targetLabel.setOpaque(false);
		targetLabel.setFont(sourceLabel.getFont().deriveFont(11f));
		JButton nEdgeButton = new SlickerButton("add edge");
		nEdgeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final int source = ((FMNode)sourceBox.getSelectedItem()).getIndex();
				final int target = ((FMNode)targetBox.getSelectedItem()).getIndex();
				graphForEdit.setBinarySignificance(source, target, 1.0);
				graphForEdit.setBinaryCorrelation(source, target, 1.0);
				graphForEdit.getEdgeCorrelationMetric().setMeasure(source, target, 1.0);
				graphForEdit.getEdgeSignificanceMetric().setMeasure(source, target, 1.0);
				FMNode sourceNode = graphForEdit.getNodeMappedTo(source);
				FMNode targetNode = graphForEdit.getNodeMappedTo(target);
				FMEdgeImpl edgeToAdd = graphForEdit.addEdge(sourceNode, targetNode, 1.0, 1.0);
				edgeToAdd.getAttributeMap().put(AttributeMap.EDGECOLOR, Color.RED);
				revalidate();
        		repaint();
			/*	Thread updateThread = new Thread() {
					public void run() {

					}
				};
				updateThread.start();*/
			}
		});
		JPanel eSourcePanel = layoutHorizontally(new Component[] {sourceLabel, Box.createHorizontalStrut(2), sourceBox}, 25);
		JPanel eTargetPanel = layoutHorizontally(new Component[] {targetLabel, Box.createHorizontalStrut(2), targetBox}, 25);
		nEdgePanel.add(Box.createVerticalStrut(7));
		nEdgePanel.add(eSourcePanel);
		nEdgePanel.add(Box.createVerticalStrut(7));
		nEdgePanel.add(eTargetPanel);
		nEdgePanel.add(Box.createVerticalStrut(7));
		nEdgePanel.add(centerHorizontally(nEdgeButton, 25));
		nEdgePanel.add(Box.createVerticalStrut(7));
		// assemble
		overview.add(centerHorizontally(title, 27));
		overview.add(Box.createVerticalStrut(7));
		overview.add(welcome);
		overview.add(Box.createVerticalStrut(12));
		overview.add(nNodePanel);
		overview.add(Box.createVerticalStrut(10));
		overview.add(nEdgePanel);
		overview.add(Box.createVerticalGlue());
		return overview;
	}
	
	public static JLabel createMessageLabel(String message) {
		JLabel messageLabel = new JLabel(message);
		messageLabel.setFont(messageLabel.getFont().deriveFont(10f));
		messageLabel.setHorizontalAlignment(JLabel.CENTER);
		messageLabel.setHorizontalTextPosition(JLabel.LEFT);
		messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		messageLabel.setMaximumSize(new Dimension(500, 200));
		messageLabel.setPreferredSize(new Dimension(200, 120));
		return messageLabel;
	}
}
