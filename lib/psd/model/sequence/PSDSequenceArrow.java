package org.processmining.plugins.psd.model.sequence;

import java.util.Date;

import org.processmining.plugins.psd.model.PSDArrow;

public class PSDSequenceArrow extends PSDArrow {
	/**
	 * Timestamp at which the transfer of work begins
	 */
	private Date beginTimestamp;
	/**
	 * Timestamp at which the transfer of work ends
	 */
	private Date endTimestamp;

	/**
	 * Constructor
	 * 
	 * @param beginTimestamp
	 *            Date
	 * @param endTimestamp
	 *            Date
	 * @param source
	 *            String
	 * @param destination
	 *            String
	 */
	public PSDSequenceArrow(Date beginTimestamp, Date endTimestamp, String source, String destination) {
		super(source, destination);
		this.beginTimestamp = beginTimestamp;
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Returns the duration of the transfer of work
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
	 * @param beginTimestamp
	 *            Date
	 */
	public void setBeginTimestamp(Date beginTimestamp) {
		this.beginTimestamp = beginTimestamp;
	}

	/**
	 * Returns the end timestamp of this block
	 * 
	 * @return Date
	 */
	public Date getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @param endTimestamp
	 *            Date
	 */
	public void setEndTimestamp(Date endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Checks whether an arrow is equal to another one
	 * 
	 * @param obj
	 *            Object
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PSDSequenceArrow) {
			PSDSequenceArrow other = (PSDSequenceArrow) obj;
			try {
				if (other.getBeginTimestamp() == this.getBeginTimestamp()
						&& other.getEndTimestamp() == this.getEndTimestamp()
						&& other.getSource().equals(this.getSource())
						&& other.getDestination().equals(this.getDestination())) {
					return true;
				} else {
					return false;
				}
			} catch (NullPointerException ex) {
				return false;
			}
		} else {
			return false;
		}
	}
}

