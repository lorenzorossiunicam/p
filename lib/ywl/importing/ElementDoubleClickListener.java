package org.processmining.plugins.ywl.importing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JViewport;

import org.processmining.plugins.ywl.YawlNetGraphVisualizationPanel;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class ElementDoubleClickListener extends MouseAdapter {
  private NetGraph main;
  private YawlNetGraphVisualizationPanel panel;

  public ElementDoubleClickListener(YawlNetGraphVisualizationPanel panel, NetGraph net) {
    this.main = net;
    this.panel = panel;
  }
  
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() != 2) {
      return;
    }
    if (this.main.getSelectionCount() != 1) {
      return;
    }
    Object selectedCell = this.main.getSelectionCell();

    if (selectedCell instanceof VertexContainer) {
      selectedCell = ((VertexContainer) selectedCell).getVertex();
    }
    
    if (selectedCell instanceof CompositeTask ||
    	selectedCell instanceof MultipleCompositeTask) {
    	
    	NetGraph subgraph = null;
    	
    	// first retrieve correct subnet for compositetask
    	String subnetName = ((CompositeTask) selectedCell).getUnfoldingNetName();	
    	int i = 0;
    	while (YAWLEditorDesktop.getInstance().getSelectedGraph() != null) {
    	     if (YAWLEditorDesktop.getInstance().getTitleAt(i) == subnetName) {
    	    	 YAWLEditorDesktop.getInstance().setSelectedIndex(i);
    	    	 subgraph = YAWLEditorDesktop.getInstance().getSelectedGraph();
    	    	 subgraph.addMouseListener(new RightClickListener(this.panel, subgraph));
    	    	 subgraph.addMouseListener(new ElementDoubleClickListener(this.panel, subgraph));
    	    	 break;
    	     } else {
    	    	 i++;
    	     }
    	}	
    	
    	// change the current net displayed with the subnet
    	JSplitPane splitpane = (JSplitPane) this.panel.getComponent(0);
    	this.panel.setGraph(subgraph);
    	JScrollPane scrollpane = (JScrollPane) splitpane.getComponent(1);
    	JViewport viewport = (JViewport) scrollpane.getComponent(0);
    	viewport.remove(0);
    	setGraphProperties(subgraph);
    	viewport.add(subgraph);	
    	// renew pipgraph
    	YawlpipGraphModel model = new YawlpipGraphModel(subgraph);
		YawlpipGraph pipGraph = new YawlpipGraph(model, true);
    	this.panel.setPipGraph(pipGraph);
    	this.panel.updatePIPPanel(pipGraph);
//    	JPanel pip = this.panel.getPipPanel();
//    	JPanel panel = (JPanel) splitpane.getComponent(2);
//    	for (int j = 0; j < 10; j++) {
//    		if (panel.getComponent(i) instanceof JPanel) {
//    			panel.remove(j);
//    		}
//    	}
//    	panel.add(pip);
    }
  }

  public void setGraphProperties(NetGraph net) {
		
		// set JGraph graph properties and remove YAWL net functionality
		
		net.setGridVisible(false);	
		net.setEditable(false);
		net.setMoveable(true);
		net.setConnectable(false);
		net.setDisconnectable(false);
		net.setSizeable(false);
		net.getGraphLayoutCache().setMovesChildrenOnExpand(true);
		net.getGraphLayoutCache().setResizesParentsOnCollapse(true);
		net.getGraphLayoutCache().setMovesParentsOnCollapse(true);
		net.getGraphLayoutCache().setAutoSizeOnValueChange(true);
		net.setSelectionEnabled(true);
	    net.repaint();   
		
		MouseListener[] listeners = net.getMouseListeners(); 
		
		// remove NetPopupListener and DoubleClickListener
		net.removeMouseListener(listeners[1]);
		net.removeMouseListener(listeners[2]);
		// TODO: remove ToolTipManager (only used for debugging purposes...)
		
	}  
}
