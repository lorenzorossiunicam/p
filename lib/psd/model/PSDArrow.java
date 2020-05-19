package org.processmining.plugins.psd.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.Map;

public class PSDArrow {

	/**
	 * Name of data-element that is the source of this arrow
	 */
	private String source;
	private PSDBlock sourceBlock;
	/**
	 * Name of data-element that is the source of this arrow
	 */
	private String destination;
	private PSDBlock destinationBlock;
	/**
	 * Start position at which this arrow is to be drawn
	 */
	private double startAt = -1;
	/**
	 * End position at which this arrow is to be drawn
	 */
	private double endAt = -1;

	/**
	 * constructor to initialize arrow
	 * 
	 * @param source
	 *            String
	 * @param destination
	 *            String
	 */
	public PSDArrow(String source, String destination) {
		this.source = source;
		this.destination = destination;
	}

	/////////////////////Get Methods//////////////////////////
	/**
	 * Returns the name of the data element instance from which the arrow
	 * originates.
	 * 
	 * @return String
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Returns the name of the data element instance in which the arrow ends.
	 * 
	 * @return String
	 */
	public String getDestination() {
		return destination;
	}

	/**
	 * Returns the starting position of the arrow
	 * 
	 * @return double
	 */
	public double getStartAt() {
		return startAt;
	}

	/**
	 * Returns the end position of the arrow
	 * 
	 * @return double
	 */
	public double getEndAt() {
		return endAt;
	}

	/**
	 * Sets the starting position of the arrow.
	 * 
	 * @param startAt
	 *            double
	 */
	public void setStartAt(double startAt) {
		this.startAt = startAt;
	}

	/**
	 * Sets the end position of the arrow.
	 * 
	 * @param endAt
	 *            double
	 */
	public void setEndAt(double endAt) {
		this.endAt = endAt;
	}

	/**
	 * Returns the block from which the arrow originates.
	 * 
	 * @return PSDBlock
	 */
	public PSDBlock getSourceBlock() {
		return sourceBlock;
	}

	/**
	 * Sets the block from which the arrow originates to sourceBlock
	 * 
	 * @param sourceBlock
	 *            PSDBlock
	 */
	public void setSourceBlock(PSDBlock sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	/**
	 * Returns the block in which the arrow ends.
	 * 
	 * @return PSDBlock
	 */
	public PSDBlock getDestinationBlock() {
		return destinationBlock;
	}

	/**
	 * Sets the block in which the arrow ends to destinationBlock
	 * 
	 * @param destinationBlock
	 *            PSDBlock
	 */
	public void setDestinationBlock(PSDBlock destinationBlock) {
		this.destinationBlock = destinationBlock;
	}

	/////////////////////Draw method///////////////////
	/**
	 * Draws the arrow
	 * 
	 * @param lifeLines
	 *            Map<String, PSDLifeLine>
	 * @param thisColor
	 *            Color
	 * @param g
	 *            Graphics2D
	 */
		public void drawArrow(Map<String, PSDLifeLine> lifeLines, Color thisColor, Graphics2D g) {
		PSDLifeLine sourceLine = lifeLines.get(source);
		PSDLifeLine destLine = lifeLines.get(destination);
		if (sourceLine != null && destLine != null) {
			double startX = sourceLine.getMiddle();
			double endX = destLine.getMiddle();
			if (startX < endX) {
				startX = startX + 10;
				endX = endX - 10;
			} else {
				startX = startX - 10;
				endX = endX + 10;
			}
			g.setColor(thisColor);
			Line2D l = new Line2D.Double(startX, startAt, endX, endAt);
			g.draw(l);
			//tangens of the angle is opposite/adjacent
			double tanAngle = Math.abs((endAt - startAt) / (endX - startX));
			double angle = Math.atan(tanAngle);
			angle += Math.PI / 4.0; //45 degrees between arrow and each 'arrow-end-line'
			double length = Math.sqrt(50);
			double xFirst = endX;
			double yFirst = endAt;
			if (endX > startX) {
				//arrow from left to right
				if (angle < Math.PI / 2.0) {
					xFirst -= Math.cos(angle) * length; //cosinus = adjacent/hypotenuse
					yFirst -= Math.sin(angle) * length; //sinus  =  opposite/hypotenuse
				} else {
					//angle larger than 90 degrees;
					angle = Math.PI - angle;
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
				angle = Math.atan(tanAngle) - Math.PI / 4.0; //45 degrees between arrow and each 'arrow-end-line'
				xFirst = endX;
				yFirst = endAt;
				if (angle >= 0) {
					xFirst -= Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = -angle;
					xFirst -= Math.cos(angle) * length;
					yFirst += Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
			} else {
				//startX > endX, arrow from right to left
				if (angle < Math.PI / 2.0) {
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = Math.PI - angle;
					xFirst -= Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
				angle = Math.atan(tanAngle) - Math.PI / 4.0; //45 degrees between arrow and each 'arrow-end-line'
				length = Math.sqrt(50);
				xFirst = endX;
				yFirst = endAt;
				if (angle >= 0) {
					xFirst += Math.cos(angle) * length;
					yFirst -= Math.sin(angle) * length;
				} else {
					angle = Math.abs(angle);
					xFirst += Math.cos(angle) * length;
					yFirst += Math.sin(angle) * length;
				}
				l = new Line2D.Double(xFirst, yFirst, endX, endAt);
				g.draw(l);
			}
		}
	}

	/////////////////////Comparison methods//////////////////////
	/**
	 * Returns true if point p is on the line of the arrow (or at most 2 pixels
	 * away)
	 * 
	 * @param p
	 *            Point
	 * @param lifeLines
	 *            Map<String, PSDLifeLine>
	 * @param scale
	 *            double
	 * @return boolean
	 */
	public boolean isOnLine(Point p, Map<String, PSDLifeLine> lifeLines, double scale) {
		PSDLifeLine sourceLine = lifeLines.get(source);
		PSDLifeLine destLine = lifeLines.get(destination);
		if (sourceLine != null && destLine != null) {
			double startX = sourceLine.getMiddle();
			double endX = destLine.getMiddle();
			if (startX < endX) {
				startX = startX + 10;
				endX = endX - 10;
			} else {
				startX = startX - 10;
				endX = endX + 10;
			}
			Line2D l = new Line2D.Double(Math.round(startX * scale), Math.round(startAt * scale), Math.round(endX
					* scale), Math.round(endAt * scale));
			if (Math.abs(l.ptSegDist(p)) <= 2) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

}

