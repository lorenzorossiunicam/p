package org.processmining.plugins.psd.model.sequence;

import java.util.Date;

import org.processmining.plugins.psd.model.PSDBlock;

public class PSDSequenceBlock extends PSDBlock {

	private Date beginTimestamp;
	private Date endTimestamp;

	/**
	 * Constructor that initializes the block
	 * 
	 * @param beginTimestamp
	 *            Date
	 * @param endTimestamp
	 *            Date
	 * @param dataElement
	 *            String
	 */
	public PSDSequenceBlock(Date beginTimestamp, Date endTimestamp, String dataElement) {
		super(dataElement);
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the duration of the period of activity
	 * 
	 * @return long
	 */
	public long getTimeIn() {
		if (beginTimestamp != null && endTimestamp != null) {
			return (endTimestamp.getTime() - beginTimestamp.getTime());
		} else {
			return 0;
		}
	}

	/**
	 * Returns the begin timestamp of this block
	 * 
	 * @return Date
	 */
	public Date getBeginTimestamp() {
		return beginTimestamp;
	}

	/**
	 * Sets the begin timestamp. Needed to be able to combine blocks/periods
	 * that overlap
	 * 
	 * @param beginTimestamp
	 *            Date
	 */
	public void setBeginTimestamp(Date beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end timestamp. Needed to be able to combine blocks/periods
	 * that overlap
	 * 
	 * @return Date
	 */
	public Date getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Sets the end timestamp
	 * 
	 * @param endTimestamp
	 *            Date
	 */
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}
}

