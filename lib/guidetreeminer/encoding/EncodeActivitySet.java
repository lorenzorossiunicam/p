// OM Ganesayanamaha
/**
 * 
 */
package org.processmining.plugins.guidetreeminer.encoding;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;

import org.processmining.plugins.guidetreeminer.swingx.ErrorDialog;

/**
 * @author R.P. Jagadeesh Chandra 'JC' Bose
 * @date 08 July 2010 
 * @since 01 June 2009
 * @version 1.0
 * @email j.c.b.rantham.prabhakara@tue.nl
 * @copyright R.P. Jagadeesh Chandra 'JC' Bose
 * 			  Architecture of Information Systems Group (AIS) 
 * 			  Department of Mathematics and Computer Science
 * 			  University of Technology, Eindhoven, The Netherlands
 */

public class EncodeActivitySet {
	/*
	 * Store the encoding corresponding to the activity/element
	 */
	private Map<String, String> activityCharMap;
	
	/*
	 * Store the decoded activity corresponding to a char encoding
	 */
	private Map<String, String> charActivityMap;
	
	/*
	 * The set of elements (activities and/or trace identifiers) to be encoded
	 */
	private Set<String> activitySet;
	
	/*
	 * The base length for encoding
	 */
	private int encodingLength;
	private int maximumActivityLength;

	/**
	 * This method encodes a set of elements passed to it; The elements can be
	 * the set of activities along with the trace identifier. Trace identifiers
	 * need to be encoded for use in the computation of repeats (used as a
	 * distinct delimiter separating the traces).
	 * 
	 * The encoding length of each element in the set is automatically
	 * estimated.
	 * 
	 * @param activitySet
	 * @throws ActivityOverFlowException (when the number of elements to be encoded is too large)
	 */
	public EncodeActivitySet(Set<String> activitySet) throws ActivityOverFlowException {
		this.activitySet = activitySet;

		this.activityCharMap = new HashMap<String, String>();
		this.charActivityMap = new HashMap<String, String>();

		encodeActivities();
	}

	/**
	 * This method does the encoding; First it decides how many characters would be required for encoding 
	 */
	private void encodeActivities() throws ActivityOverFlowException {
		String[] lowerCaseArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
				"q", "r", "s", "t", "u", "v", "w", "x", "y", "z" };
		String[] upperCaseArray = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P",
				"Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String[] alphaArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q",
				"r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
				"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		String[] lowerCaseIntArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
				"q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		String[] intArray = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
		String[] allArray = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
				"s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5",
				"6", "7", "8", "9" };

		int noActivities = activitySet.size();

		encodingLength = 2;
		/*
		 * If the number of activities to encode is less than 62 then use a
		 * single character encoding else check if the size is >62 and <= 260,
		 * then use lower case character followed by integer e.g., a0, b2, c9
		 * else check if the size is between 261 and 520 (both inclusive) else
		 * check if the size is between 521 and 936 (both inclusive) else check
		 * if the size is between 937 and 3844 (both inclusive) else check if
		 * the size is between 3845 and 6760 (both inclusive)
		 */
		if (noActivities <= allArray.length) {
			encodingLength = 1;
			encode(allArray);
		} else if (noActivities > allArray.length && noActivities <= lowerCaseArray.length * intArray.length) {
			encode(lowerCaseArray, intArray);
		} else if (noActivities > lowerCaseArray.length * intArray.length
				&& noActivities <= alphaArray.length * intArray.length) {
			encode(alphaArray, intArray);
		} else if (noActivities > alphaArray.length * intArray.length
				&& noActivities <= lowerCaseIntArray.length * upperCaseArray.length) {
			encode(lowerCaseIntArray, upperCaseArray);
		} else if (noActivities > lowerCaseIntArray.length * upperCaseArray.length
				&& noActivities <= allArray.length * allArray.length) {
			encode(allArray, allArray);
		} else if (noActivities <= lowerCaseArray.length * upperCaseArray.length * intArray.length) {
			encodingLength = 3;
			encode(lowerCaseArray, upperCaseArray, intArray);
		} else if (noActivities <= allArray.length * allArray.length * allArray.length) {
			encodingLength = 3;
			encode(allArray, allArray, allArray);
		} else {
			throw new ActivityOverFlowException();
		}

		//Free Memory
		lowerCaseArray = null;
		upperCaseArray = null;
		intArray = null;
		alphaArray = null;
		lowerCaseIntArray = null;
		allArray = null;
		System.gc();
	}

	protected void encode(String[] strArray) {
		int currentactivityIndex = 0;
		String charEncoding;
		
		maximumActivityLength = 0;
		for (String activity : activitySet) {
			if(activity.length() > maximumActivityLength)
				maximumActivityLength = activity.length();
			
			charEncoding = strArray[currentactivityIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				ErrorDialog.showErrorDialog(new JFrame(),"Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		//Free Memory
		charEncoding = null;
	}

	protected void encode(String[] strArray, String[] intArray) {
		int currentactivityIndex = 0;

		int firstCharIndex, secondCharIndex;
		String charEncoding;
		maximumActivityLength = 0;
		for (String activity : activitySet) {
			if(activity.length() > maximumActivityLength)
				maximumActivityLength = activity.length();
			
			firstCharIndex = currentactivityIndex / intArray.length;
			secondCharIndex = currentactivityIndex % intArray.length;

			charEncoding = strArray[firstCharIndex] + intArray[secondCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				ErrorDialog.showErrorDialog(new JFrame(),"Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		//Free Memory
		charEncoding = null;
	}

	protected void encode(String[] strArray1, String[] strArray2, String[] strArray3) {
		int currentactivityIndex = 0;

		int firstCharIndex, secondCharIndex, thirdCharIndex;
		String charEncoding;
		maximumActivityLength = 0;
		for (String activity : activitySet) {
			if(activity.length() > maximumActivityLength)
				maximumActivityLength = activity.length();
			
			thirdCharIndex = currentactivityIndex % strArray3.length;
			secondCharIndex = (currentactivityIndex / strArray3.length) % (strArray2.length);
			firstCharIndex = currentactivityIndex / (strArray3.length * strArray2.length);

			charEncoding = strArray1[firstCharIndex] + strArray2[secondCharIndex] + strArray3[thirdCharIndex];
			activityCharMap.put(activity, charEncoding);
			if (charActivityMap.containsKey(charEncoding)) {
				ErrorDialog.showErrorDialog(new JFrame(),"Something wrong with encoding: Already present charEncoding");
				System.exit(0);
			} else {
				charActivityMap.put(charEncoding, activity);
			}
			currentactivityIndex++;
		}

		//Free Memory
		charEncoding = null;
	}

	public Map<String, String> getActivityCharMap() {
		return activityCharMap;
	}

	public Map<String, String> getCharActivityMap() {
		return charActivityMap;
	}

	public int getEncodingLength() {
		return encodingLength;
	}

	public int getMaximumActivityLength() {
		return maximumActivityLength;
	}
}
