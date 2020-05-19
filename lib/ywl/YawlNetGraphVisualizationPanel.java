package org.processmining.plugins.ywl;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jgraph.graph.BasicMarqueeHandler;
import org.jgraph.graph.GraphCell;
import org.processmining.framework.util.Cleanable;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.listeners.SelectionListener;
import org.processmining.plugins.ywl.importing.ElementDoubleClickListener;
import org.processmining.plugins.ywl.importing.YawlpipGraph;
import org.processmining.plugins.ywl.importing.YawlpipGraphModel;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import com.fluxicon.slickerbox.components.SlickerButton;
import com.fluxicon.slickerbox.ui.SlickerSliderUI;
import com.jgraph.layout.JGraphFacade;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YawlNetGraphVisualizationPanel extends JPanel implements Cleanable, ChangeListener, ActionListener {

	protected static final int UP = 1200;
	private static final long serialVersionUID = -3413469631030881661L;
	protected NetGraph graph;
	protected YawlpipGraph pipGraph;
	protected JSlider slider = new JSlider(1, UP);
	protected JButton scaleButton = new JButton("fit");
	protected JPanel previewPanel;
	protected JScrollPane scroll;
	private PIPPanel pipPanel;
	private List<SelectionListener<?, ?>> selectionListeners = new ArrayList<SelectionListener<?, ?>>(0);
	protected JLabel precentageLabel;
	protected Font largeFont;
	protected Font mediumFont;
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() != slider) {
			return;
		}
		graph.setScale(slider.getValue() / 100.0);
		this.precentageLabel.setText(slider.getValue() + " %");
		Object[] lCells = graph.getRoots();
		
		// reposition labels
		for (int i=0; i < lCells.length; i++) {
			graph.setElementLabelInsideUpdate((GraphCell) lCells[i], graph.getElementLabel((GraphCell) lCells[i])); 
		}
		
		scalePIP();
		repaintPIP(graph.getVisibleRect());
	}
	
	protected void centerHorizontally(JLabel label) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	}

	public YawlNetGraphVisualizationPanel(final NetGraph graph) {
		this.graph = graph;
			
		YawlpipGraphModel model = new YawlpipGraphModel(graph);
		
		this.pipGraph = new YawlpipGraph(model, true);
//		{
//			private static final long serialVersionUID = -4671278744184554287L;
//
//			@Override
//			public void changeHandled() {
//				scalePIP();
//				repaintPIP(graph.getVisibleRect());
//			}
//		};
		
		
		// add listener for CompositeTask, double click opens the subnet
		this.graph.addMouseListener(new ElementDoubleClickListener(this, graph));

		this.setLayout(new BorderLayout());
		scroll = new JScrollPane(graph);
		scroll.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// Message.add("Horizontal scrollbar value changed",
				// Message.DEBUG);
				repaintPIP(graph.getVisibleRect());
			}
		});
		scroll.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				// Message.add("Vertical scrollbar value changed",
				// Message.DEBUG);
				repaintPIP(graph.getVisibleRect());
			}
		});

		slider.setOrientation(SwingConstants.VERTICAL);
		slider.setValue(100);
		slider.setMajorTickSpacing(100);
		slider.setMinorTickSpacing(10);
		slider.addChangeListener(this);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);

		// Create the label table
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(new Integer(1), new JLabel("0 %"));
		for (int i = 100; i <= UP; i += 100) {
			labelTable.put(new Integer(i), new JLabel(i + " %"));
		}
		slider.setLabelTable(labelTable);
		
		slider.setUI(new SlickerSliderUI(slider));
		
		previewPanel = new JPanel(new BorderLayout());
		previewPanel.add(slider, BorderLayout.CENTER);
		scaleButton.addActionListener(this);
		previewPanel.add(scaleButton, BorderLayout.SOUTH);
		
		previewPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		previewPanel.setBackground(new Color(120, 120, 120));
		previewPanel.setOpaque(true);
		
		pipPanel = new PIPPanel(scroll, pipGraph, this);
		pipPanel.setRect(graph.getBounds());
		
		scalePIP();
		repaintPIP(graph.getVisibleRect());
		pipGraph.repaint();

		previewPanel.add(pipPanel, BorderLayout.NORTH);
		
		// middle panel
		JPanel midPanel = new JPanel(new BorderLayout());
		midPanel.setBorder(BorderFactory.createEmptyBorder());
		midPanel.setBackground(new Color(120, 120, 120));
		midPanel.setOpaque(true);
		
		JLabel zoomLevelLabel = new JLabel("Zoom level");
		zoomLevelLabel.setOpaque(false);
		zoomLevelLabel.setForeground(new Color(255, 255, 255));
		zoomLevelLabel.setFont(largeFont);
		centerHorizontally(zoomLevelLabel);
		
		precentageLabel = new JLabel("100 %");
		precentageLabel.setOpaque(false);
		precentageLabel.setForeground(new Color(255, 255, 255));
		precentageLabel.setFont(mediumFont);
		centerHorizontally(precentageLabel);
		
		midPanel.add(zoomLevelLabel, BorderLayout.NORTH);
		midPanel.add(slider, BorderLayout.CENTER);
		midPanel.add(precentageLabel, BorderLayout.SOUTH);
		
		// scaling button 
		scaleButton = new SlickerButton("Fit");
		scaleButton.addActionListener(this);

		// add all to previewPanel
		previewPanel.add(midPanel, BorderLayout.CENTER);
		previewPanel.add(scaleButton, BorderLayout.SOUTH);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.add(scroll, JSplitPane.LEFT);
		split.add(previewPanel, JSplitPane.RIGHT);
		split.setResizeWeight(1);
		split.setOneTouchExpandable(true);
		
		scalePIP();
		repaintPIP(graph.getVisibleRect());
		
		graph.setTolerance(4);

		graph.setMarqueeHandler(new BasicMarqueeHandler() {
			private boolean test(MouseEvent e) {
				return SwingUtilities.isRightMouseButton(e) && (e.getModifiers() & InputEvent.ALT_MASK) == 0;

			}

			public boolean isForceMarqueeEvent(MouseEvent event) {
				if (test(event)) {
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void mouseReleased(final MouseEvent e) {
				if (test(e)) {
					e.consume();
				} else {
					super.mouseReleased(e);
				}
			}

			@Override
			public void mousePressed(final MouseEvent e) {
				if (test(e)) {
					synchronized (graph) {
						// Check for selection.
						// If the cell that is being clicked is part of the
						// selection,
						// we use the current selection.
						// otherwise, we use a new selection
						Object cell = graph.getFirstCellForLocation(e.getX(), e.getY());

						if (cell == null) {
							graph.clearSelection();
						}
					}
				} else {
					super.mousePressed(e);
				}
				scalePIP();
				repaintPIP(graph.getVisibleRect());
			}
			

		});

		this.add(split, BorderLayout.CENTER);

	}
	
	public PIPPanel getPipPanel() {
		return pipPanel;
	}

	public void setPipPanel(PIPPanel pipPanel) {
		this.pipPanel = pipPanel;
	}

	public <N, E> void addSelectionListener(SelectionListener<N, E> listener) {
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener<?, ?> listener) {
		selectionListeners.remove(listener);
	}

	public void paint(Graphics g) {
		super.paint(g);
	}

	protected void scalePIP() {
		pipGraph.setScale(scaleToFit(graph, pipPanel, false));
		pipGraph.repaint();
	}

	protected void repaintPIP(Rectangle2D rect) {
		double s = factorMultiplyGraphToPIP();
		double x = Math.max(1, s * rect.getX());
		double y = Math.max(1, s * rect.getY());
		double w = Math.min(s * rect.getWidth(), pipPanel.getVisWidth() - 1);
		double h = Math.min(s * rect.getHeight(), pipPanel.getVisHeight() - 1);
		rect = new Rectangle2D.Double(x, y, w, h);
		pipPanel.setRect(rect);
		pipPanel.repaint();
	}

	public double factorMultiplyGraphToPIP() {
		return pipGraph.getScale() / graph.getScale();
	}

	public NetGraph getGraph() {
		return graph;
	}
	
	public void setGraph(NetGraph net) {
		graph = net;
	}

	public YawlpipGraph getPipGraph() {
		return pipGraph;
	}

	public void setPipGraph(YawlpipGraph pipGraph) {
		this.pipGraph = pipGraph;
		scalePIP();
		repaintPIP(graph.getVisibleRect());
	}

	public void actionPerformed(ActionEvent e) {
		assert (e.getSource() == scaleButton);
		double ratio = scaleToFit(graph, scroll.getViewport(), true);
		slider.setValue((int) Math.floor(ratio * 100));
		scalePIP();
		repaintPIP(graph.getVisibleRect());
	}

	protected double scaleToFit(NetGraph graph, Container container, boolean reposition) {
		Rectangle2D bounds = graph.getBounds();
		double x = bounds.getX();
		double y = bounds.getY();
		if (reposition) {
			JGraphFacade facade = new JGraphFacade(graph);
			double x2 = facade.getGraphOrigin().getX();
			double y2 = facade.getGraphOrigin().getY();
			facade.translateCells(facade.getVertices(), 2.0 - x2, 2.0 - y2);
			facade.translateCells(facade.getEdges(), 2.0 - x2, 2.0 - y2);
			graph.getGraphLayoutCache().edit(facade.createNestedMap(true, false));
			x = 0;
			y = 0;
		}
		
		Dimension size = container.getSize();
		
		// reposition labels
		Object[] lCells = graph.getRoots();
		for (int i=0; i < lCells.length; i++) {
			graph.setElementLabelInsideUpdate((GraphCell) lCells[i], graph.getElementLabel((GraphCell) lCells[i])); 
		}

		double ratio = Math.min(size.getWidth() / (bounds.getWidth() + x), size.getHeight() / (bounds.getHeight() + y));
		return ratio;
		
	}

	public void cleanUp() {

//		graph.cleanUp();
//		pipGraph.cleanUp();
	}

	@Override
	public void finalize() throws Throwable {
		try {
			cleanUp();
		} finally {
			super.finalize();
		}

	}

	public Collection<DirectedGraphNode> getSelectedNodes() {
		List<DirectedGraphNode> nodes = new ArrayList<DirectedGraphNode>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				nodes.add(((ProMGraphCell) o).getNode());
			}
		}
		return nodes;
	}

	public Collection<DirectedGraphEdge<?, ?>> getSelectedEdges() {
		List<DirectedGraphEdge<?, ?>> edges = new ArrayList<DirectedGraphEdge<?, ?>>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphEdge) {
				edges.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return edges;
	}

	public Collection<DirectedGraphElement> getSelectedElements() {
		List<DirectedGraphElement> elements = new ArrayList<DirectedGraphElement>();
		for (Object o : graph.getSelectionCells()) {
			if (o instanceof ProMGraphCell) {
				elements.add(((ProMGraphCell) o).getNode());
			} else if (o instanceof ProMGraphEdge) {
				elements.add(((ProMGraphEdge) o).getEdge());
			}
		}
		return elements;
	}

	public double getScale() {
		return graph.getScale();
	}

	public void setScale(double d) {
		int b = (int) (100.0 * d);
		b = Math.max(b, 1);
		b = Math.min(b, UP);
		slider.setValue(b);
	}
	
	public void updatePIPPanel(YawlpipGraph pipGraph) {
		
		PIPPanel newPIP = new PIPPanel(scroll, pipGraph, this);
		this.pipPanel = newPIP;
		scalePIP();
	}

}

class PIPPanel extends JPanel implements MouseListener, MouseMotionListener {

	private static final int PIPSIZE = 150;

	private static final long serialVersionUID = 5563202305263696868L;

	// new FlowLayout(FlowLayout.LEADING, 0, 0);
	private Rectangle2D rect;
	private Stroke stroke = new BasicStroke(2);
	private Color color = Color.RED;
	private final JScrollPane parentScroll;
	private final YawlNetGraphVisualizationPanel panel;

	private YawlpipGraph pipGraph;

	public PIPPanel(JScrollPane parentScroll, YawlpipGraph pipGraph, YawlNetGraphVisualizationPanel panel) {
		super(new BorderLayout());

		this.pipGraph = pipGraph;

		for (int i = 0; i < pipGraph.getMouseListeners().length; i++) {
			pipGraph.removeMouseListener(pipGraph.getMouseListeners()[0]);
		}
		for (int i = 0; i < pipGraph.getMouseMotionListeners().length; i++) {
			pipGraph.removeMouseMotionListener(pipGraph.getMouseMotionListeners()[0]);
		}
		pipGraph.addMouseMotionListener(this);
		pipGraph.addMouseListener(this);

		this.add(pipGraph);

		this.parentScroll = parentScroll;
		this.panel = panel;

		setPreferredSize(new Dimension(PIPSIZE, PIPSIZE));
		setMinimumSize(new Dimension(PIPSIZE, PIPSIZE));
		setMaximumSize(new Dimension(PIPSIZE, PIPSIZE));
		pipGraph.setMaximumSize(new Dimension(PIPSIZE, PIPSIZE));
		setSize(new Dimension(PIPSIZE, PIPSIZE));
	}

	public double getVisWidth() {
		return pipGraph.getPreferredSize().getWidth() - 1;
	}

	public double getVisHeight() {
		return pipGraph.getPreferredSize().getHeight() - 1;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (rect != null) {
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(color);
			g2d.setStroke(stroke);
			g2d.draw(rect);
		}
	}

	public Rectangle2D getRect() {
		return rect;
	}

	public void setRect(Rectangle2D rect) {
		this.rect = rect;
	}

	public void mouseDragged(MouseEvent evt) {
		if (SwingUtilities.isLeftMouseButton(evt)) {
			// a is the point in the graph where I dragged to
			if (pressPoint == null) {
				// I didn't start dragging inside rectangle.
				return;
			}
			double offsetX = pressPoint.getX() - rect.getX();
			double offsetY = pressPoint.getY() - rect.getY();
			pressPoint = evt.getPoint();
			double x = evt.getPoint().getX() - offsetX;
			double y = evt.getPoint().getY() - offsetY;
			drawMain(x, y);
		} else if (SwingUtilities.isRightMouseButton(evt)) {
			Point endDragPoint = evt.getPoint();

			Rectangle2D visRect = new Rectangle2D.Double(0, 0, getVisWidth(), getVisHeight());
			if (visRect.contains(endDragPoint) && startDragPoint != null) {

				rect = new Rectangle2D.Double(startDragPoint.getX(), startDragPoint.getY(), 0, 0);
				rect.add(endDragPoint);
				repaint();
			}
		}
	}

	private Point2D pressPoint = null;

	public void mouseClicked(MouseEvent e) {
		// don't care
	}

	public void mouseEntered(MouseEvent e) {
		// don't care
	}

	public void mouseExited(MouseEvent e) {
		// don't care
	}

	private Point startDragPoint;

	private Rectangle2D lastRect;

	public void mousePressed(MouseEvent e) {
		// store the point where I clicked the mouse
		if (rect != null) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				pressPoint = null;
				Point2D a = e.getPoint();
				if (rect.contains(a)) {
					pressPoint = a;
				}
				stroke = new BasicStroke(2);
				color = Color.RED;
			} else if (SwingUtilities.isRightMouseButton(e)) {
				pressPoint = null;
				startDragPoint = null;
				Rectangle2D visRect = new Rectangle2D.Double(0, 0, getVisWidth(), getVisHeight());
				if (visRect.contains(e.getPoint())) {
					startDragPoint = e.getPoint();
					lastRect = rect;
					rect = null;
					stroke = new BasicStroke(1);
					color = Color.GRAY;
					repaint();
				}
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			stroke = new BasicStroke(2);
			color = Color.RED;
			if (rect == null || lastRect == null) {
				return;
			}

			double f = panel.factorMultiplyGraphToPIP();

			double w = Math.max(parentScroll.getViewport().getExtentSize().getWidth() * f, lastRect.getWidth())
					/ lastRect.getWidth();
			double h = Math.max(parentScroll.getViewport().getExtentSize().getHeight() * f, lastRect.getHeight())
					/ lastRect.getHeight();

			double scaleFactor = rect.getWidth() / lastRect.getWidth() / w;
			scaleFactor = Math.max(scaleFactor, rect.getHeight() / lastRect.getHeight() / h);
			double x = rect.getMinX();
			double y = rect.getMinY();

			panel.setScale(panel.getScale() / scaleFactor);
			drawMain(x, y);
			// repaint();

		}
	}

	public void drawMain(double x, double y) {
		// The point I have now should be translated back
		// to a point in the main graph.

		x = Math.min(x, getVisWidth() - rect.getWidth());
		y = Math.min(y, getVisHeight() - rect.getHeight());
		x = Math.max(x, 0);
		y = Math.max(y, 0);

		double f = panel.factorMultiplyGraphToPIP();

		parentScroll.getViewport().setViewPosition(new Point((int) Math.round(x / f), (int) Math.round(y / f)));
		pipGraph.repaint();
	}

	public void mouseMoved(MouseEvent e) {
	}
	
	
}