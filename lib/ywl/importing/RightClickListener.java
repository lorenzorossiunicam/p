package org.processmining.plugins.ywl.importing;

import java.awt.event.InputEvent;
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
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class RightClickListener extends MouseAdapter {
  private NetGraph main;
  private YawlNetGraphVisualizationPanel panel;

  public RightClickListener(YawlNetGraphVisualizationPanel panel, NetGraph net) {
    this.main = net;
    this.panel = panel;
  }
  
  public void mouseClicked(MouseEvent e) {
    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
    	Object selectedCell = this.main.getSelectionCell();
    	
    	// determine the parent net of the current net
    	NetGraph maingraph = null;
    	String name = this.main.getName();
    	
    	NetGraph graph = YAWLEditorDesktop.getInstance().getSelectedGraph();
    	
        YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
                graph.getFirstCellForLocation(
                    e.getX(), 
                    e.getY()
                )
            );
           			
    	if (vertex instanceof YAWLTask) {
    		MouseListener[] listeners = graph.getMouseListeners(); 
			for (int x =0; x < listeners.length;x++) {
				if (listeners[x] instanceof org.yawlfoundation.yawl.editor.net.NetPopupListener) {
					graph.removeMouseListener(listeners[x]);
					NetPopupListener newPopup = new NetPopupListener(graph);
					graph.addMouseListener(newPopup);
				}
				if (x == 2) {
					graph.removeMouseListener(listeners[x]);	
				}
			}
			NetPopupListener newPopup = new NetPopupListener(graph);
			graph.addMouseListener(newPopup);
        }
		
    	if (selectedCell == null) {
    		int i=0;
    		boolean stop = false;
    		YAWLEditorDesktop.getInstance().setSelectedIndex(i);
    		while (!stop) {
    		    YAWLEditorDesktop.getInstance().setSelectedIndex(i);
    			NetGraph tGraph = YAWLEditorDesktop.getInstance().getSelectedGraph();
    			Object[] cells = tGraph.getRoots();
    		    for (int j=0; j < cells.length; j++) {
    			      if (cells[j] instanceof VertexContainer) {
    			         cells[j] = ((VertexContainer) cells[j]).getVertex(); 
    			      }
    			      if (cells[j] instanceof CompositeTask ||
    				      cells[j] instanceof MultipleCompositeTask ) {
    			    	  if (((CompositeTask) cells[j]).getUnfoldingNetName() == name) {
    			    		  maingraph = tGraph;
    			    		  
    			    		  stop = true;
    			    		  break;
    			    	  }
    			      }
    		    }
    		    i++;
    		}
    		
	    	 MouseListener[] listeners = maingraph.getMouseListeners(); 
		  		for (int x =0; x < listeners.length;x++) {
					if (listeners[x] instanceof org.yawlfoundation.yawl.editor.net.NetPopupListener) {
						maingraph.removeMouseListener(listeners[x]);
						NetPopupListener newPopup = new NetPopupListener(maingraph);
						maingraph.addMouseListener(newPopup);
					}
					if (x == 2) {
						maingraph.removeMouseListener(listeners[x]);	
					}
				}
				NetPopupListener newPopup = new NetPopupListener(maingraph);
				maingraph.addMouseListener(newPopup);
	  	    	maingraph.addMouseListener(new ElementDoubleClickListener(this.panel, maingraph));
    		
    		// display the parent net again in the YawlNetGraphVisualizationPanel
        	JSplitPane splitpane = (JSplitPane) this.panel.getComponent(0);
        	this.panel.setGraph(maingraph);
        	JScrollPane scrollpane = (JScrollPane) splitpane.getComponent(1);
        	JViewport viewport = (JViewport) scrollpane.getComponent(0);
        	viewport.remove(0);
        	viewport.add(maingraph);
        	// renew pipgraph
        	YawlpipGraphModel model = new YawlpipGraphModel(maingraph);
    		YawlpipGraph pipGraph = new YawlpipGraph(model, true);
        	this.panel.setPipGraph(pipGraph);
        	//this.panel.updatePIPPanel(pipGraph);
    	}
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
