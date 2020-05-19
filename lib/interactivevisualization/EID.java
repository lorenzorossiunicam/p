package org.processmining.plugins.interactivevisualization;

/**
 * Unique identifier for XEvents.
 * 
 * This identifier is designed to be plain and simple, light, and to provide you with direct
 * access to the corresponding event. It consists of only two integer values, the index
 * number of the trace and the index number for the event. This identifier is designed with
 * the assumption that the XLog instance will not change during the lifetime of the
 * identifiers. The identifiers are not designed to be saved and restored, only for
 * communication.
 * 
 * @author Danny van Heumen
 */
public class EID implements Comparable<EID> {
	/**
	 * The index of the trace within the log.
	 */
	public final int traceIdx;
	/**
	 * The index of the event within the trace.
	 */
	public final int eventIdx;
	
	/**
	 * The constructor for the Event ID.
	 * 
	 * @param traceIndex Index number for the trace in the log.
	 * @param eventIndex Index number for the event in the trace.
	 */
	public EID(final int traceIndex, final int eventIndex) {
		traceIdx = traceIndex;
		eventIdx = eventIndex;
	}
	
	/**
	 * Friendly representation of the Event ID for the cases where code is being debugged.
	 * @return Returns debug-friendly representation of the EID.
	 */
	public String toString() {
		return "(EID:"+traceIdx+","+eventIdx+")";
	}
	
	/**
	 * Test for equality of an object with the Event ID instance.
	 * 
	 * An EID is considered equal to another object if this object is an EID instance,
	 * and the traceIdx and eventIdx are equal.
	 * 
	 * @param other The other object with which to test for equality.
	 * @return Returns 'true' if the object is equal, or 'false' otherwise.
	 */
	public boolean equals(Object other) {
		
		if(other == this) {
			return true;
		}
		
		if(!(other instanceof EID)) {
			return false;
		}
		
		return (traceIdx == ((EID)other).traceIdx && eventIdx == ((EID)other).eventIdx);
	}

	/**
	 * Method providing the natural ordering for comparing one EID with another.
	 * Ordering by traceIdx and eventIdx respectively.
	 * This ordering is important since we will assume this ordering within certain
	 * Interactivity Manager methods for reasons of efficiency.
	 * 
	 * @param other The other EID with which to compare itself.
	 * @return Returns -1 if traceIdx is lower or traceIdx is equal and eventIdx is lower.
	 * Returns 0 if traceIdx is equal and eventIdx is equal. Returns 1 if traceIdx is equal
	 * and eventIdx is greater, or if traceIdx is greater.
	 */
	@Override
	public int compareTo(EID other) {
		
		if(other == this) {
			return 0;
		}
		
		if(this.traceIdx < other.traceIdx || (this.traceIdx == other.traceIdx && this.eventIdx < other.eventIdx)) {
			return -1;
		}
		else if(this.traceIdx == other.traceIdx && this.eventIdx == other.eventIdx) {
			return 0;
		}
		else {
			return 1;
		}
	}
}
