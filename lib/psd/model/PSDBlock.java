package org.processmining.plugins.psd.model;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PSDBlock {
	/**
	 * data element instance to which this block belongs
	 */
	private String dataElement;
	/**
	 * The index of the block
	 */
	private int similarIndex = 0;
	/**
	 * Start Y-coordinate where data element block should be drawn
	 */
	private double startAt = -1;
	/**
	 * End Y-coordinate where data element block should be drawn
	 */
	private double endAt = -1;
	/**
	 *
	 */
	private Set<PSDArrow> inArrows = new HashSet<PSDArrow>();
	private Set<PSDArrow> outArrows = new HashSet<PSDArrow>();

	/**
	 * constructor to initialize the block
	 * 
	 * @param dataElement
	 *            String
	 */
	public PSDBlock(String dataElement) {
		this.dataElement = dataElement;
	}

	///////////////////////GET AND SET METHODS///////////////////////////
	/**
	 * Returns the name of the data-element of this block
	 * 
	 * @return String
	 */
	public String getDataElement() {
		return dataElement;
	}

	/**
	 * Sets the starting position of this block
	 * 
	 * @param startAt
	 *            double
	 */
	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	/**
	 * Sets the end position of this block
	 * 
	 * @param endAt
	 *            double
	 */
	public void setEndAt(double endAt) {
		this.endAt = endAt;
	}

	/**
	 * sets similarIndex
	 * 
	 * @param similarIndex
	 *            int
	 */
	public void setSimilarIndex(int similarIndex) {
		this.similarIndex = similarIndex;
	}

	/**
	 * returns the similarIndex
	 * 
	 * @return int
	 */
	public int getSimilarIndex() {
		return similarIndex;
	}

	/**
	 * Returns the arrows that end in this block
	 * 
	 * @return Set<PSDArrow>
	 */
	public Set<PSDArrow> getInArrows() {
		return inArrows;
	}

	/**
	 * Sets the arrows that end in this block
	 * 
	 * @param inArrows
	 *            Set<PSDArrow>
	 */
	public void setInArrows(Set<PSDArrow> inArrows) {
		this.inArrows = inArrows;
	}

	/**
	 * Returns the arrows that originate from this block
	 * 
	 * @return Set<PSDArrow>
	 */
	public Set<PSDArrow> getOutArrows() {
		return outArrows;
	}

	/**
	 * Sets the arrows that originate from this block
	 * 
	 * @param outArrows
	 *            Set<PSDArrow>
	 */
	public void setOutArrows(Set<PSDArrow> outArrows) {
		this.outArrows = outArrows;
	}

	////////////////////////Comparison methods//////////////////////
	/**
	 * Returns true if point p is in this block
	 * 
	 * @param p
	 *            Point
	 * @param lifeLines
	 *            Map<String, PSDLifeLine>
	 * @param scale
	 *            double
	 * @return boolean
	 */
	public boolean isInBlock(Point p, Map<String, PSDLifeLine> lifeLines, double scale) {
		try {
			PSDLifeLine eltLine = lifeLines.get(dataElement);
			double xMiddle = eltLine.getMiddle();
			if (p.getX() >= Math.round((xMiddle - 10) * scale) && p.getX() <= Math.round((xMiddle + 10) * scale)
					&& p.getY() >= Math.round(startAt * scale) && p.getY() <= Math.round(endAt * scale)) {
				return true;
			} else {
				return false;
			}
		} catch (NullPointerException ex) {
			return false;
		}
	}

	/////////////////////Draw Method/////////////////////////
	/**
	 * Draws the data-element block on top of the the lifeline
	 * 
	 * @param startX
	 *            double
	 * @param thisColor
	 *            Color
	 * @param g
	 *            Graphics2D
	 */
	public void drawBlock(double startX, Color thisColor, Graphics2D g) {
		Rectangle2D r = new Rectangle2D.Double(startX, startAt, 20, endAt - startAt);
		Paint initialPaint = g.getPaint();
		GradientPaint towhite = new GradientPaint(((Double) startX).floatValue(), ((Double) startAt).floatValue(),
				thisColor, ((Double) startX).floatValue() + 20, ((Double) startAt).floatValue(), Color.WHITE);
		g.setPaint(towhite);
		g.fill(r);
		g.setPaint(initialPaint);
		g.setColor(Color.BLACK);
		g.draw(r);
	}

}

