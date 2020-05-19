/**
 * 
 */
package org.processmining.plugins.manifestanalysis.visualization.performance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

import org.processmining.models.pnetprojection.ITransitionPDecorator;

/**
 * @author aadrians Jun 8, 2012
 * 
 */
public class TransPerfDecorator implements ITransitionPDecorator {

	private static Font defFont = new Font(Font.SANS_SERIF, Font.PLAIN, 5);
	private static int MARGIN = 3;

	private String label = "<no label>";
	private String stats = "<none>";
	private boolean lightColorLabel = false;

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		JLabel nodeName;

		StringBuilder sb = new StringBuilder();
		sb.append("<html><div style=\"align:center;width:" + (width - (6 * MARGIN)) +
				"px;\">");
		sb.append(label);
		sb.append("<br><b>(");
		sb.append(stats);
		sb.append(")</b>");
		sb.append("</div></html>");
		
		nodeName = new JLabel(sb.toString());
		sb.setLength(0);

		// draw transition label
		// get metrics from the graphics
		FontMetrics metrics = g2d.getFontMetrics(defFont);
		// get the height of a line of text in this
		// font and render context
		int hgt = (int) height - (3 * MARGIN);
		// get the advance of my text in this font
		// and render context
		int adv = metrics.stringWidth(nodeName.getText());

		final int labelX = (int) x + MARGIN;
		final int labelY = (int) y + 1;
		final int labelW = adv;
		final int labelH = hgt;

		nodeName.setPreferredSize(new Dimension(labelW, labelH));
		nodeName.setSize(new Dimension(labelW, labelH));

		nodeName.setFont(defFont);
		nodeName.validate();
		if (lightColorLabel) {
			nodeName.setForeground(Color.WHITE);
		} else {
			nodeName.setForeground(Color.BLACK);
		}
		nodeName.paint(g2d.create(labelX, labelY, labelW, labelH));

	}

	public boolean isLightColorLabel() {
		return lightColorLabel;
	}
	
	public void setLightColorLabel(boolean isLight) {
		this.lightColorLabel = isLight;
	}

	
	public void setStats(String stats) {
		this.stats = stats;
	}

	public String getStats() {
		return stats;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getLabel(){
		return label;
	}

}
