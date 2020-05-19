package org.processmining.plugins.psd.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.freehep.graphicsbase.util.export.ExportDialog;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.scalableview.ScalableViewPanel;
import org.processmining.framework.util.ui.scalableview.interaction.ViewInteractionPanel;

import com.fluxicon.slickerbox.components.SlickerButton;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

public class PSDExportInteractionPanel extends JPanel implements ViewInteractionPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1036741994786060955L;
	protected final ScalableViewPanel panel;
	private ScalableComponent scalable;
	private SlickerButton exportButton;
	private PSDVisualizer root;

	public PSDExportInteractionPanel(ScalableViewPanel panel, PSDVisualizer root) {
		this.panel = panel;
		this.root = root;
		double size[][] = { { 10, TableLayoutConstants.FILL, 10 }, { 10, TableLayoutConstants.FILL, 10 } };
		setLayout(new TableLayout(size));
		exportButton = new SlickerButton("Export view...");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		this.add(exportButton, "1, 1");
	}

	private void export() {
		root.setExporting(true);
		ExportDialog export = new ExportDialog();
		export.showExportDialog(this, "Export view as ...", scalable.getComponent(), "View");
		root.setExporting(false);
	}

	public void updated() {
		// TODO Auto-generated method stub

	}

	public String getPanelName() {
		return "Export";
	}

	public JComponent getComponent() {
		return this;
	}

	public void setScalableComponent(ScalableComponent scalable) {
		this.scalable = scalable;
	}

	public void setParent(ScalableViewPanel viewPanel) {
	}

	public double getHeightInView() {
		return 50;
	}

	public double getWidthInView() {
		return 100;
	}

	public void willChangeVisibility(boolean to) {
	}
}

