package org.processmining.plugins.ywl.importing;

import javax.swing.JPopupMenu;

import org.yawlfoundation.yawl.editor.actions.element.ViewCancellationSetAction;
import org.yawlfoundation.yawl.editor.elements.model.YAWLCell;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.menu.YAWLPopupMenuCheckBoxItem;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class VertexPopupMenu extends JPopupMenu {

  private static final long serialVersionUID = 1L;
  private NetGraph graph;
  private YAWLCell cell;

  private YAWLPopupMenuItem multipleInstanceDetailItem;
  
  public VertexPopupMenu(YAWLCell cell, NetGraph graph) {
    super();
    this.cell = cell;
    this.graph = graph;
    addMenuItems();
  }
  
  private void addMenuItems() {
    YAWLVertex vertex = (YAWLVertex) cell;
    addControlFlowPerspectiveMenuItems(vertex);
  } 
  
  private void addControlFlowPerspectiveMenuItems(YAWLVertex vertex) {
    
    if (vertex instanceof YAWLTask) {
      add(buildViewCancellationSetItem());
    }
    
  }

  private YAWLPopupMenuCheckBoxItem buildViewCancellationSetItem() {
    ViewCancellationSetAction action = 
      new ViewCancellationSetAction((YAWLTask) cell, graph);
    YAWLPopupMenuCheckBoxItem checkBoxItem = 
      new YAWLPopupMenuCheckBoxItem(action);
    action.setCheckBox(checkBoxItem);
    return checkBoxItem;
  }

  public YAWLCell getCell() {
    return cell;
  }
  
  public void setVisible(boolean state) {
    if (state) {
       
      if (multipleInstanceDetailItem != null) {
          multipleInstanceDetailItem.setEnabled(
              multipleInstanceDetailItem.shouldBeEnabled()
          );
      }

    }
    super.setVisible(state);
  }
}
