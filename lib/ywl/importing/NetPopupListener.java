package org.processmining.plugins.ywl.importing;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.SwingUtilities;

import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.utilities.NetCellUtilities;
import org.yawlfoundation.yawl.editor.swing.menu.PalettePopupMenu;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class NetPopupListener extends MouseAdapter {
  private static final PalettePopupMenu palettePopup = new PalettePopupMenu();
  @SuppressWarnings("unchecked")
private final Hashtable vertexPopupHash = new Hashtable();

  private NetGraph graph;

  public NetPopupListener(NetGraph graph) {
    this.graph = graph;
  }
  
  public void mousePressed(MouseEvent event) {
    if (!SwingUtilities.isRightMouseButton(event)) {
      return;
    }
    
    YAWLVertex vertex = NetCellUtilities.getVertexFromCell(
            graph.getFirstCellForLocation(
                event.getX(), 
                event.getY()
            )
        );
    
    if (!(vertex instanceof YAWLTask)) {
    	return;
    }
    
    if (vertex != null && vertex instanceof YAWLTask)  {
      getCellPopup(vertex).show(
          graph,
          event.getX(), 
          event.getY()
      );
      return;
    }

    palettePopup.show(graph,event.getX(), event.getY());

  }
  
  @SuppressWarnings("unchecked")
private VertexPopupMenu getCellPopup(Object cell) {
    if(!vertexPopupHash.containsKey(cell)) {
      vertexPopupHash.put(cell, new VertexPopupMenu((YAWLCell) cell, graph));
    }
    return (VertexPopupMenu) vertexPopupHash.get(cell);
  }
}
