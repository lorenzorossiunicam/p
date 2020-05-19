package org.processmining.plugins.ywl.importing;

import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;

import org.yawlfoundation.yawl.editor.actions.YAWLBaseAction;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YAWLPopupMenuItem extends JMenuItem {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private static final Insets margin = new Insets(0,0,0,0);

  public YAWLPopupMenuItem(YAWLBaseAction a) {
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
  
  public boolean shouldBeEnabled() {
    return ((YAWLBaseAction) getAction()).shouldBeEnabled();
  }
  
  public boolean shouldBeVisible() {
    return ((YAWLBaseAction) getAction()).shouldBeVisible();
  }
}
