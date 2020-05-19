package org.processmining.plugins.fuzzymap.util;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.processmining.models.patterntree.PatternTreeNode;

public class FuzzyMapUtil {
	/*
	 * According to the encoded label of the pattern node like
	 * "[a1, b1, c1, d1]", get each activity's name. like "a1,b1,c1,d1".
	 */
	public static Set<String> getActivityLabelsInPattern(PatternTreeNode pattern, int encodingLength) {
		String patternLabel = pattern.getLabel();
		return getOriginalActivityNameInPattern(patternLabel, encodingLength);
	}

	/*
	 * According to the encoded label of the pattern node like "[a,b,c,d]", get
	 * each activity's name. like "a,b,c,d"
	 */
	public static Set<String> getOriginalActivityNameInPattern(String patternLabel, int encodingLength) {
		//Set<String> curActLabels = new TreeSet<String>();
		Set<String> orgActLabels = new TreeSet<String>();
		String curActLabel;
		int step = encodingLength + 2;
		//the label of the activity in the pattern is seperated by", " 
		for (int i = 1; i < patternLabel.length(); i += step) {
			curActLabel = patternLabel.substring(i, i + encodingLength);
			orgActLabels.add(curActLabel);
		}
		return orgActLabels;

	}

	public static String getSingleActivityNodeLabel(String nodeLableString, String delimeter) {
		String label = "";
		int delimIdx = nodeLableString.indexOf(delimeter);
		String activityName = nodeLableString.substring(0, delimIdx);
		String activityType = nodeLableString.substring(delimIdx + 1, nodeLableString.length());
		label = "<html>" + activityName + "<br>" + activityType + "</html>";
		return label;

	}

	protected static NumberFormat numberFormat = NumberFormat.getInstance();
	{
		numberFormat.setMinimumFractionDigits(3);
		numberFormat.setMaximumFractionDigits(3);
	}

	public static String format(double number) {
		return numberFormat.format(number);
	}

	public static Set<String> findLongestLabel(Set<Set<String>> labels) {
		Set<String> longestStr = labels.iterator().next();
		int maxSize = 0;
		for (Set<String> label : labels) {
			int curSize = label.toString().length();
			if (curSize > maxSize) {
				maxSize = curSize;
				longestStr = label;
			}
		}
		return longestStr;

	}

	/*
	 * Get the longest common subsequence of two strings
	 */
	public static String LongestCommonSubsequence(String s1, String s2) {
		int[][] num = new int[s1.length() + 1][s2.length() + 1]; //2D array, initialized to 0

		//Actual algorithm
		for (int i = 1; i <= s1.length(); i++) {
			for (int j = 1; j <= s2.length(); j++) {
				if (s1.charAt(i - 1) == (s2.charAt(j - 1))) {
					num[i][j] = 1 + num[i - 1][j - 1];
				} else {
					num[i][j] = Math.max(num[i - 1][j], num[i][j - 1]);
				}
			}
		}

		System.out.println("length of LCS = " + num[s1.length()][s2.length()]);

		int s1position = s1.length(), s2position = s2.length();
		List<String> result = new LinkedList<String>();
		String commonSeuSequence = "";

		while ((s1position != 0) && (s2position != 0)) {
			if (s1.charAt(s1position - 1) == s2.charAt(s2position - 1)) {
				result.add(s1.substring(s1position - 1, s1position));
				s1position--;
				s2position--;
			} else if (num[s1position][s2position - 1] >= num[s1position - 1][s2position]) {
				s2position--;
			} else {
				s1position--;
			}
		}
		Collections.reverse(result);
		for (int i = 0; i < result.size(); i++) {
			commonSeuSequence += result.get(i);
		}
		return commonSeuSequence;
	}

	//find the intersection of two Sets
	public static <T> Set<T> intersection(Set<T> setA, Set<T> setB) {
		Set<T> tmp = new TreeSet<T>();
		for (T x : setA) {
			if (setB.contains(x)) {
				tmp.add(x);
			}
		}
		return tmp;
	}
}
