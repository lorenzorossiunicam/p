package org.processmining.plugins.psd.model.pattern;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.processmining.plugins.psd.model.PSDArrow;
import org.processmining.plugins.psd.model.PSDBlock;
import org.processmining.plugins.psd.model.PSDLifeLine;
import org.processmining.plugins.psd.model.sequence.PSDSequence;
import org.processmining.plugins.psd.model.sequence.PSDSequenceArrow;
import org.processmining.plugins.psd.model.sequence.PSDSequenceBlock;

public class PSDPattern {

	/**
	 * Color in which the pattern is drawn
	 */
	private Color color = Color.WHITE;
	/**
	 * ArrayList containing the sequences that follow this pattern
	 */
	private List<PSDSequence> sequenceList;
	/**
	 * ArrayList containing the data-element blocks in the order they appear in
	 * the first sequence of the pattern. (sorted first on begin time, then on
	 * end time, then on data-element)
	 */
	private List<PSDPatternBlock> sortedDataEltBlocks;
	/**
	 * if strict-equivalent pattern type selected: ArrayList containing the
	 * data-element blocks in the order they appear in the first sequence of the
	 * pattern. (sorted first on end time, then on begin time, then on
	 * data-element)
	 */
	private List<PSDSequenceBlock> sortedOnEndDataEltBlocks;
	/**
	 * ArrayList containing the arrows of the first sequence of the pattern,
	 * sorted on source, destination, begin time and finally on end time
	 */
	private List<PSDPatternArrow> arrowList;
	/**
	 * statistics to calculate (avg, min, max, stdev etc) throughput times
	 */
	private DescriptiveStatistics timeStatistics = new DescriptiveStatistics();
	/**
	 * the number of the pattern (obviously)
	 */
	private int patternNumber = 0;

	/**
	 * Constructor to initialize pattern
	 * 
	 * @param sq
	 *            PSDSequence
	 */
	public PSDPattern(PSDSequence sq) {
		sequenceList = new ArrayList<PSDSequence>();
		sequenceList.add(sq);
		sortedDataEltBlocks = new ArrayList<PSDPatternBlock>();
		arrowList = new ArrayList<PSDPatternArrow>();
		List<PSDSequenceBlock> sequenceBlocks = sq.getSortedDataEltBlocks();
		for (int i = 0; i < sequenceBlocks.size(); i++) {
			PSDSequenceBlock block = sequenceBlocks.get(i);
			long beginTime = block.getBeginTimestamp().getTime() - sq.getBeginDate().getTime();
			long endTime = block.getEndTimestamp().getTime() - sq.getBeginDate().getTime();
			PSDPatternBlock patBlock = new PSDPatternBlock(beginTime, endTime, block.getDataElement());
			patBlock.setSimilarIndex(block.getSimilarIndex());
			sortedDataEltBlocks.add(patBlock);
		}
		List<PSDSequenceArrow> arrows = sq.getArrowList();
		for (int i = 0; i < arrows.size(); i++) {
			PSDSequenceArrow arrow = arrows.get(i);
			long beginTime = arrow.getBeginTimestamp().getTime() - sq.getBeginDate().getTime();
			long endTime = arrow.getEndTimestamp().getTime() - sq.getBeginDate().getTime();
			PSDPatternArrow patArrow = new PSDPatternArrow(beginTime, endTime, arrow.getSource(), arrow.getDestination());
			patArrow.setSourceBlock(arrow.getSourceBlock());
			patArrow.setDestinationBlock(arrow.getDestinationBlock());
			arrowList.add(patArrow);
		}
		sortedOnEndDataEltBlocks = sq.getSortedOnEndDataEltBlocks();
		color = sq.getColor();
	}

	/////////////////////////COMPARISON METHODS////////////////////////
	/**
	 * Compares the sequence to the pattern, returns true if they match
	 * 
	 * @param sq
	 *            PSDSequence
	 * @param isStrict
	 *            boolean
	 * @return boolean
	 */
	public boolean compareToSequence(PSDSequence sq, boolean isStrict) {
		//compare against the first sequence of this pattern
		if (!equalArrows(arrowList, sq.getArrowList(), isStrict)
				|| !equalParts(sortedDataEltBlocks, sq.getSortedDataEltBlocks(), isStrict)) {
			//the sequences do not have the same arrows or data-element blocks
			return false;
		} else {
			if (isStrict && !equalParts(sortedOnEndDataEltBlocks, sq.getSortedOnEndDataEltBlocks(), isStrict)) {
				//if strict selected, then data-element blocks sorted on end times should be equal as well
				return false;
			} else {
				//sequences have the same arrows and data-element blocks
				return true;
			}
		}
	}

	/**
	 * Checks whether same arrows appear and whether they appear in the same
	 * order. If this is the case, then: 'the arrows are equal' and true is
	 * returned.
	 * 
	 * @param firstArrows
	 *            List<PSDPatternArrow> firstArrows
	 * @param secondArrows
	 *            List<PSDSequenceArrow> secondArrows
	 * @param isStrict
	 *            boolean
	 * @return boolean
	 */
	private boolean equalArrows(List<PSDPatternArrow> firstArrows, List<PSDSequenceArrow> secondArrows, boolean isStrict) {
		if (firstArrows.size() != secondArrows.size()) {
			return false;
		} else {
			for (int i = 0; i < firstArrows.size(); i++) {
				PSDPatternArrow firstArrow = firstArrows.get(i);
				PSDSequenceArrow secondArrow = secondArrows.get(i);
				if (!firstArrow.getSource().equals(secondArrow.getSource())
						|| !firstArrow.getDestination().equals(secondArrow.getDestination())) {
					if (firstArrow.getSourceBlock().getSimilarIndex() != secondArrow.getSourceBlock().getSimilarIndex()
							|| firstArrow.getDestinationBlock().getSimilarIndex() != secondArrow.getDestinationBlock()
									.getSimilarIndex()) {
						//arrows with different source or destination data-element block
						return false;
					}
				}
			}
			return true;
		}
	}

	/**
	 * Checks whether the input arraylists contain the same data-element blocks
	 * 
	 * @param parts0
	 *            List<? extends PSDBlock> : sorted array with data-element blocks
	 * @param parts1
	 *            List<PSDSequenceBlock> : sorted array with data-element blocks
	 * @param isStrict
	 *            boolean : true if strict-equivalence is used
	 * @return boolean
	 */
	private boolean equalParts(List<? extends PSDBlock> parts0, List<PSDSequenceBlock> parts1, boolean isStrict) {
		if (parts0.size() != parts1.size()) {
			//different sizes, so unequal parts
			return false;
		} else {
			//run through sorted lists of data-element blocks
			for (int i = 0; i < parts0.size(); i++) {
				//obtain data-element block i of both lists
				PSDBlock firstBlock = parts0.get(i);
				PSDSequenceBlock secondBlock = parts1.get(i);
				if (!firstBlock.getDataElement().equals(secondBlock.getDataElement())) {
					//different data-elements, so different blocks
					return false;
				} else if (!isStrict) {
					//check if both data-element blocks i, have the same data-element blocks
					//following them
					for (int j = 0; j < parts0.size(); j++) {
						PSDBlock afterFirst = parts0.get(j);
						PSDSequenceBlock afterSecond = parts1.get(j);
						if (firstBlock instanceof PSDPatternBlock && afterFirst instanceof PSDPatternBlock) {
							if (((PSDPatternBlock) afterFirst).getAverageBeginTime() > ((PSDPatternBlock) firstBlock)
									.getAverageEndTime()
									&& !(afterSecond.getBeginTimestamp().getTime() >= secondBlock.getEndTimestamp()
											.getTime())) {
								//blocks don't appear in the same order
								return false;
							}
						} else if (firstBlock instanceof PSDSequenceBlock && afterFirst instanceof PSDSequenceBlock) {
							if (((PSDSequenceBlock) afterFirst).getBeginTimestamp().getTime() > ((PSDSequenceBlock) firstBlock)
									.getBeginTimestamp().getTime()
									&& !(afterSecond.getBeginTimestamp().getTime() >= secondBlock.getEndTimestamp()
											.getTime())) {
								//blocks don't appear in the same order
								return false;
							}
						} else {
							return false;
						}
					}
				}
			}
			return true;
		}
	}

	////////////////////TIME CALCULATION METHOD/////////////////////
	/**
	 * Calculates throughput times
	 */
	public void calculateTimes() {
		timeStatistics.clear();
		Iterator<PSDSequence> lit = sequenceList.listIterator();
		while (lit.hasNext()) {
			PSDSequence current = lit.next();
			double temp = current.getThroughputTime();
			if (temp >= 0) {
				timeStatistics.addValue(temp);
			}
		}
	}

	////////////////////////////GET, SET AND ADD METHODS///////////////////
	/**
	 * Adds sequence sq to the sequenceList, which contains all sequences that
	 * follow this pattern
	 * 
	 * @param sq
	 *            PSDSequence
	 */
	public void addSequence(PSDSequence sq) {
		try {
			sequenceList.add(sq);
			for (int i = 0; i < sortedDataEltBlocks.size(); i++) {
				PSDPatternBlock block = sortedDataEltBlocks.get(i);
				PSDSequenceBlock bl = sq.getSortedDataEltBlocks().get(i);
				long beginTime = bl.getBeginTimestamp().getTime() - sq.getBeginDate().getTime();
				long endTime = bl.getEndTimestamp().getTime() - sq.getBeginDate().getTime();
				block.addBeginTime(beginTime);
				block.addEndTime(endTime);
			}
			for (int i = 0; i < arrowList.size(); i++) {
				PSDPatternArrow arrow = arrowList.get(i);
				PSDSequenceArrow ar = sq.getArrowList().get(i);
				long beginTime = ar.getBeginTimestamp().getTime() - sq.getBeginDate().getTime();
				long endTime = ar.getEndTimestamp().getTime() - sq.getBeginDate().getTime();
				arrow.addBeginTime(beginTime);
				arrow.addEndTime(endTime);
			}
		} catch (NullPointerException npe) {
			//should not occur
		}
	}

	/**
	 * Returns the number of sequences that follow this pattern
	 * 
	 * @return int
	 */
	public int getFrequency() {
		return sequenceList.size();
	}

	/**
	 * Returns the average time spend in data-element part with number 'number'
	 * 
	 * @param number
	 *            int
	 * @return double
	 */
	public double getTimePart(int number) {
		PSDPatternBlock block = sortedDataEltBlocks.get(number);
		return block.getTimeIn();
	}

	/**
	 * Returns the average time between the beginning of a sequence of this
	 * pattern and the Timestamp at which data-element part with number 'number'
	 * starts
	 * 
	 * @param number
	 *            int
	 * @return double
	 */
	public double getAvgTimeToBegin(int number) {
		PSDPatternBlock block = sortedDataEltBlocks.get(number);
		return block.getAverageBeginTime();
	}

	/**
	 * Returns the average time between the beginning of a sequence of this
	 * pattern and the Timestamp at which arrow number 'number' began and ended
	 * 
	 * @param number
	 *            int
	 * @return double
	 */
	public double[] getArrowPosition(int number) {
		PSDPatternArrow arrow = arrowList.get(number);
		double[] returnArray = new double[2];
		returnArray[0] = arrow.getAverageBeginTime();
		returnArray[1] = arrow.getAverageEndTime();
		return returnArray;
	}

	/**
	 * Returns the sorted list of data-element blocks
	 * 
	 * @return List<PSDPatternBlock>
	 */
	public List<PSDPatternBlock> getSortedDataElementBlocks() {
		return sortedDataEltBlocks;
	}

	/**
	 * Returns the sorted arrows of this pattern
	 * 
	 * @return List<PSDPatternArrow>
	 */
	public List<PSDPatternArrow> getArrowList() {
		return arrowList;
	}

	/**
	 * Returns the color of the pattern
	 * 
	 * @return Color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Returns the number of the pattern
	 * 
	 * @return int
	 */
	public int getPatternNumber() {
		return patternNumber;
	}

	/**
	 * Sets the number of the pattern
	 * 
	 * @param patternNumber
	 *            int
	 */
	public void setPatternNumber(int patternNumber) {
		this.patternNumber = patternNumber;
	}

	/**
	 * Returns the set of (names of) process instances, which follow this
	 * pattern
	 * 
	 * @return Set<String>
	 */
	public Set<String> getPiNames() {
		Set<String> piNames = new HashSet<String>();
		for (int i = 0; i < sequenceList.size(); i++) {
			PSDSequence sequence = sequenceList.get(i);
			piNames.add(sequence.getPiName());
		}
		return piNames;
	}

	/**
	 * Returns mean throughput time
	 * 
	 * @return double
	 */
	public double getMeanThroughputTime() {
		return timeStatistics.getMean();
	}

	/**
	 * Returns minimum throughput time
	 * 
	 * @return double
	 */
	public double getMinThroughputTime() {
		return timeStatistics.getMin();
	}

	/**
	 * Returns maximum throughput time
	 * 
	 * @return double
	 */
	public double getMaxThroughputTime() {
		return timeStatistics.getMax();
	}

	/**
	 * Returns standard deviation in throughput time
	 * 
	 * @return double
	 */
	public double getStdevThroughputTime() {
		return timeStatistics.getStandardDeviation();
	}

	/////////////////////////////DRAW-RELATED METHODS/////////////////////////
	/**
	 * 
	 * @param patternNumber
	 *            int
	 * @param startY
	 *            int
	 * @param timePerPixel
	 *            double
	 */
	public void initializeDrawPattern(int patternNumber, int startY, double timePerPixel) {
		this.patternNumber = patternNumber;
		for (int i = 0; i < sortedDataEltBlocks.size(); i++) {
			double timeLength = getTimePart(i) / timePerPixel;
			PSDPatternBlock block = sortedDataEltBlocks.get(i);
			double startAt = startY + getAvgTimeToBegin(i) / timePerPixel;
			block.setStartAt(startAt);
			block.setEndAt(startAt + timeLength);
		}
		Iterator<PSDPatternArrow> arrows = arrowList.listIterator();
		int num = 0;
		while (arrows.hasNext()) {
			PSDArrow arrow = arrows.next();
			double[] arrowPosition = getArrowPosition(num++);
			double beginPosition = arrowPosition[0] / timePerPixel;
			double endPosition = arrowPosition[1] / timePerPixel;
			if (beginPosition >= 0 && endPosition >= 0) {
				beginPosition += startY;
				endPosition += startY;
				arrow.setStartAt(beginPosition);
				arrow.setEndAt(endPosition);
			}
		}
	}

	/**
	 * Draws the pattern in the pattern diagram
	 * 
	 * @param lifeLines
	 *            Map<String, PSDLifeLine>
	 * @param startY
	 *            int
	 * @param g
	 *            Graphics2D
	 */
	public void drawPattern(Map<String, PSDLifeLine> lifeLines, int startY, Graphics2D g) {
		g.setColor(Color.BLACK);
		g.drawString("Pattern " + patternNumber + ":", 10, startY + 5);
		g.setColor(color);
		//draw data-element blocks
		for (int i = 0; i < sortedDataEltBlocks.size(); i++) {
			PSDPatternBlock block = sortedDataEltBlocks.get(i);
			try {
				block.drawBlock(lifeLines.get(block.getDataElement()).getMiddle() - 10, color, g);
			} catch (NullPointerException ex) {
			}
		}
		//draw arrows
		Iterator<PSDPatternArrow> arrows = arrowList.listIterator();
		while (arrows.hasNext()) {
			PSDArrow arrow = arrows.next();
			arrow.drawArrow(lifeLines, color, g);
		}
	}

	/**
	 * Draws a rectangle of width 20, height length and starting point
	 * (startX,startY) in the northwest corner of the rectangle. In case
	 * logicSteps is true, the height is 10.
	 * 
	 * @param startX
	 *            double
	 * @param startY
	 *            double
	 * @param length
	 *            double
	 * @param logicSteps
	 *            boolean
	 * @param g
	 *            Graphics2D
	 */
	public void drawRectangle(double startX, double startY, double length, boolean logicSteps, Graphics2D g) {
		Rectangle2D r = new Rectangle2D.Double(startX, startY, 20, length);
		if (logicSteps) {
			r = new Rectangle2D.Double(startX, startY, 20, 10);
		}
		Color initialColor = g.getColor();
		Paint initialPaint = g.getPaint();
		GradientPaint towhite = new GradientPaint(((Double) startX).floatValue(), ((Double) startY).floatValue(),
				initialColor, ((Double) startX).floatValue() + 20, ((Double) (startY)).floatValue(), Color.WHITE);
		g.setPaint(towhite);
		g.fill(r);
		g.setPaint(initialPaint);
		g.setColor(Color.BLACK);
		g.draw(r);
		g.setColor(initialColor);
	}
}

