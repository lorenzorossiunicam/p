/**
 * 
 */
package org.processmining.plugins.performancematrix;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.NumberFormat;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.framework.util.ui.widgets.WidgetColors;
import org.processmining.plugins.manifestanalysis.visualization.performance.PerformanceConstants;
import org.processmining.plugins.manifestanalysis.visualization.performance.TimeFormatter;
import org.processmining.plugins.pnalignanalysis.visualization.projection.LegendPanelConf;

/**
 * @author aadrians Jul 19, 2012
 * 
 */
public class PerformanceMatrixPanel extends JPanel {
	private static final long serialVersionUID = -1202689891997947205L;

	// real information 
	private String[] axisLbl;
	private String[] ordinateLbl;
	private double[] data; // size need to be axisLbl times ordinateLbl

	private double minVal = Double.NaN;
	private double maxVal = Double.NaN;
	private double threshold = 0.00;

	// attribute of the matrix
	private boolean showValues = true;
	private boolean showMarks = true;
	private int blockWidth = 0;
	private int blockHeight = 0;
	private Font labelFont;
	private Font valueFont;
	private double divisor = 1.00;
	private final NumberFormat nf = NumberFormat.getInstance();

	private int longestStrOrdinate = Integer.MIN_VALUE;
	private int longestStrAxis = Integer.MIN_VALUE;
	private int padding = 2;

	private boolean infoBoxShown = false;
	private boolean isTime = true;

	private final static float dash1[] = { 5.0f };
	private final static BasicStroke dashed = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
			10.0f, dash1, 0.0f);
	private final static BasicStroke defaultStroke = new BasicStroke(1.0f);

	private InfoBox infoBox = new InfoBox();

	public void setDivisor(double divisor) {
		this.divisor = divisor;
	}

	public double getDivisor() {
		return divisor;
	}

	public void setShowValues(boolean showValues) {
		this.showValues = showValues;
	}

	public boolean isShowValues() {
		return this.showValues;
	}

	public void setShowMarks(boolean showMarks) {
		this.showMarks = showMarks;
	}

	public boolean isShowMarks() {
		return this.showMarks;
	}

	public PerformanceMatrixPanel(String[] axis, final String[] ordinate, double[] data) {
		initializeData(axis, ordinate, data);

		addMouseMotionListener(new MouseMotionListener() {

			public void mouseMoved(MouseEvent e) {
				// check if a box is hovered
				if ((e.getX() > longestStrOrdinate + padding) && (e.getY() < (blockHeight * ordinateLbl.length))
						&& (e.getX() < (axisLbl.length * blockWidth) + longestStrOrdinate + padding)) {
					infoBoxShown = true;
					int x = getSelectedX(e.getX());
					int y = getSelectedY(e.getY());
					infoBox.setMetaMessage("From " + axisLbl[y] + " to " + ordinateLbl[x]);

					double val = getSelectedData(x, y);
					if (!Double.isNaN(val)) {
						infoBox.setMessage("val:" + (isTime ? TimeFormatter.formatTime(val, nf) : nf.format(val)));
					} else {
						infoBox.setMessage("val: -");
					}

					moveInfo(e.getX(), e.getY());
				} else if (infoBoxShown) {
					// repaint the infoBox area to clear it 
					infoBox.setMessage(null);
					infoBox.setMetaMessage(null);
					repaintInfoBox();
					infoBoxShown = false;
				}
			}

			public void mouseDragged(MouseEvent e) {
				// none
			}
		});

		setPreferredSize(new Dimension(30 * ordinate.length + padding + 40, 30 * axis.length + padding + 40));
		setBackground(WidgetColors.PROPERTIES_BACKGROUND);
	}

	private void initializeData(String[] axis, String[] ordinate, double[] data) {
		assert ((axis != null) && (ordinate != null) && (data != null) && (axis.length > 0) && (ordinate.length > 0) && (data.length > 0));

		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(2);

		this.axisLbl = axis;
		this.ordinateLbl = ordinate;
		setData(data);

	}

	private int getSelectedY(int y) {
		return (int) Math.floor(y / blockHeight);
	}

	private int getSelectedX(int x) {
		return (int) Math.floor((x - longestStrOrdinate - padding) / blockWidth);
	}

	private void repaintInfoBox() {
		repaint(infoBox.getX() + 2 - infoBox.getWidth(), infoBox.getY() - 2, infoBox.getWidth(), infoBox.getHeight());
	}

	private void moveInfo(int x, int y) {
		repaintInfoBox();

		// Update coordinates.
		infoBox.setXY(x, y);

		// Repaint the square at the new location.
		repaintInfoBox();
	}

	protected void paintComponent(Graphics g) {
		// get the length of longest string in ordinate
		Graphics2D g2d = (Graphics2D) g;

		// estimate font size
		labelFont = new Font("Arial", Font.PLAIN, (this.getHeight() / (2 * (ordinateLbl.length + 5))));
		valueFont = new Font("Arial", Font.PLAIN, (int) (this.getHeight() / (2.5 * (ordinateLbl.length + 5))));

		
		FontMetrics fmLabelFont = g2d.getFontMetrics(labelFont);
		longestStrOrdinate = Integer.MIN_VALUE;
		longestStrAxis = Integer.MIN_VALUE;
		padding = 2;
		for (String ordinate : ordinateLbl) {
			if (longestStrOrdinate < fmLabelFont.stringWidth(ordinate)) {
				longestStrOrdinate = fmLabelFont.stringWidth(ordinate);
			}
			;
		}
		for (String axis : axisLbl) {
			if (longestStrAxis < fmLabelFont.stringWidth(axis)) {
				longestStrAxis = fmLabelFont.stringWidth(axis);
			}
			;
		}

		// paint background
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// paint rectangle
		this.blockWidth = (getWidth() - longestStrOrdinate - padding) / axisLbl.length;
		this.blockHeight = (getHeight() - longestStrAxis - padding) / ordinateLbl.length;

		FontMetrics fmValueFont = g2d.getFontMetrics(valueFont);

		for (int y = 0; y < ordinateLbl.length; y++) {
			for (int x = 0; x < axisLbl.length; x++) {
				// paint rectangle
				paintRectangle(g2d, x, y, fmValueFont);
			}
		}

		// paint text 
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
		g2d.setFont(labelFont);
		g2d.setColor(Color.WHITE);

		int verticalD = ((this.blockHeight - fmLabelFont.getHeight()) / 2) + fmLabelFont.getHeight() + padding;

		for (int i = 0; i < ordinateLbl.length; i++) {
			// get height of the text
			g2d.drawString(ordinateLbl[i], 0, (blockHeight * i) + verticalD);
		}

		Graphics2D gcopybase = (Graphics2D) g2d.create();
		gcopybase.translate(this.longestStrOrdinate + padding + (this.blockWidth / 4),
				(this.blockHeight * ordinateLbl.length));
		int squareTwo = (int) Math.sqrt(2.00);
		for (int i = 0; i < axisLbl.length; i++) {
			Graphics2D gcopy = (Graphics2D) gcopybase.create(i * this.blockWidth, 0, squareTwo * longestStrAxis
					+ padding, squareTwo * longestStrOrdinate + padding);
			gcopy.rotate(Math.toRadians(45.0));
			gcopy.drawString(axisLbl[i], 5, 5);
		}

		// paint the object if a box is hovered
		infoBox.paintInfo(g2d, labelFont);
	}

	private double getSelectedData(int x, int y) {
		return data[(y * axisLbl.length) + x];
	}

	/**
	 * Paint rectangles that shows performance value
	 * 
	 * @param g
	 * @param x
	 * @param y
	 */
	private void paintRectangle(Graphics2D g, int x, int y, FontMetrics fm) {
		double val = getSelectedData(x, y);
		Color color = getColor(val);
		g.setColor(color);
		g.fillRect(this.longestStrOrdinate + this.padding + (x * blockWidth), (y * blockHeight), blockWidth - 1,
				blockHeight - 1);

		if (this.showValues) {
			if (color.equals(PerformanceConstants.BAD) || color.equals(PerformanceConstants.BADTOMEDIUM)) {
				g.setColor(Color.WHITE);
			} else {
				g.setColor(Color.BLACK);
			}
			g.setFont(valueFont);
			String valStr = nf.format(val / divisor);
			g.drawString(valStr,
					this.longestStrOrdinate + this.padding + (x * blockWidth) + (blockWidth - fm.stringWidth(valStr)) / 2,
					(y * blockHeight) + fm.getHeight() + ((blockHeight - fm.getHeight()) / 2));
		}
		if (this.showMarks) {
			if (Double.compare(val / divisor, threshold) <= 0) {
				// under threshold
				g.setColor(Color.BLACK);
				g.setStroke(dashed);

				// get width
				g.drawRect(this.longestStrOrdinate + this.padding + (x * blockWidth) + 4, (y * blockHeight) + 4,
						blockWidth - 1 - 8, blockHeight - 1 - 8);
			}
		}
		g.setStroke(defaultStroke);
	}

	private Color getColor(double val) {
		if (Double.isNaN(val)) {
			return Color.BLACK;
		}

		int res = (int) ((val - this.minVal) * 5 / (this.maxVal - this.minVal));
		if (res == 0) {
			return PerformanceConstants.GOOD;
		} else if (res == 1) {
			return PerformanceConstants.MEDTOGOOD;
		} else if (res == 2) {
			return PerformanceConstants.MEDIUM;
		} else if (res == 3) {
			return PerformanceConstants.BADTOMEDIUM;
		} else {
			return PerformanceConstants.BAD;
		}
	}
	public JComponent getLegend() {
		return new LegendPanelConf();
	}

	/**
	 * @return the axisLbl
	 */
	public String[] getAxisLbl() {
		return axisLbl;
	}

	/**
	 * @param axisLbl
	 *            the axisLbl to set
	 */
	public void setAxisLbl(String[] axisLbl) {
		this.axisLbl = axisLbl;
	}

	/**
	 * @return the ordinateLbl
	 */
	public String[] getOrdinateLbl() {
		return ordinateLbl;
	}

	/**
	 * @param ordinateLbl
	 *            the ordinateLbl to set
	 */
	public void setOrdinateLbl(String[] ordinateLbl) {
		this.ordinateLbl = ordinateLbl;
	}

	public void setData(double[] data) {
		setData(data, true, 0.00);
	}

	public void setData(double[] data, boolean isTime, double threshold) {
		this.data = data;
		this.isTime = isTime;
		this.threshold = threshold;

		// get min and max
		minVal = Double.NaN;
		maxVal = Double.NaN;
		for (double val : data) {
			if (Double.isNaN(minVal)) {
				minVal = val;
			} else {
				if ((!Double.isNaN(val)) && (Double.compare(minVal, val) > 0)) {
					minVal = val;
				}
			}
			if (Double.isNaN(maxVal)) {
				maxVal = val;
			} else {
				if ((!Double.isNaN(val)) && (Double.compare(maxVal, val) < 0)) {
					maxVal = val;
				}
			}
		}

	}

	public double[] getData() {
		return data;
	}

	public void setThreshold(double t) {
		this.threshold = t;
	}

}

class InfoBox {

	// pointer coordinate
	private int xPos = 10;
	private int yPos = 10;

	// used for repainting
	private int width = 10;
	private int height = 10;

	private String message = null;
	private String metaMessage = null;

	public void setXY(int x, int y) {
		this.xPos = x;
		this.yPos = y;
	}

	public int getX() {
		return xPos;
	}

	public int getY() {
		return yPos;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMetaMessage(String metaMessage) {
		this.metaMessage = metaMessage;
	}

	public String getMetaMessage() {
		return metaMessage;
	}

	public void paintInfo(Graphics2D g, Font labelFont) {
		if ((message != null) && (metaMessage != null)) {
			// adjust width and height
			FontMetrics fm = g.getFontMetrics(labelFont);
			int msgWidth = fm.stringWidth(message);
			int strWidth = Math.max(msgWidth, fm.stringWidth(metaMessage));

			g.setColor(Color.WHITE);
			g.fillRoundRect(xPos - 15 - strWidth - 2, yPos + 15, strWidth + 4, (2 * fm.getHeight()) + 4, 2, 2);
			g.setColor(Color.BLUE);
			g.fillOval(xPos - 2, yPos - 2, 4, 4);
			g.drawLine(xPos, yPos, xPos - 15, yPos + 15);
			g.drawRoundRect(xPos - 15 - strWidth - 2, yPos + 15, strWidth + 4, (2 * fm.getHeight()) + 4, 2, 2);
			g.drawString(metaMessage, xPos - 15 - strWidth, yPos + 15 + fm.getHeight());
			g.drawString(message, xPos - 15 - msgWidth, yPos + 15 + (2 * fm.getHeight()));

			this.width = 8 + strWidth + 15 + 4;
			this.height = 4 + (2 * fm.getHeight()) + 15 + 4;

		}
	}
}
