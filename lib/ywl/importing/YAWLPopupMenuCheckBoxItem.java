package org.processmining.plugins.ywl.importing;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YAWLPopupMenuCheckBoxItem extends JCheckBoxMenuItem {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Insets margin = new Insets(0,0,0,0);

  public YAWLPopupMenuCheckBoxItem(Action a) {
    super(a);    
    setMargin(margin);
  }
  
  public Point getToolTipLocation(MouseEvent e) {
    return new Point(0 + 2,getSize().height + 2);
  }
  
  public void setEnabled(boolean enabled) {
    if (getAction() instanceof TooltipTogglingWidget) {
      TooltipTogglingWidget action = (TooltipTogglingWidget) this.getAction();
      if (enabled) {
        setToolTipText(action.getEnabledTooltipText());
      } else {
        setToolTipText(action.getDisabledTooltipText());
      }
    }
    super.setEnabled(enabled);
  }
}
