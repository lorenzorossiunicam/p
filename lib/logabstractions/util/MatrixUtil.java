package org.processmining.logabstractions.util;

import java.util.Arrays;

public class MatrixUtil {

	public static String prettyPrint(Object[][] arr) {
		String res = "";
		for (int row = 0; row < arr.length; row++) {
			res += Arrays.deepToString(arr[row]) + "\n";
		}
		return res;
	}

	public static String prettyPrint(double[][] arr) {
		String res = "";
		for (int row = 0; row < arr.length; row++) {
			res += Arrays.toString(arr[row]) + "\n";
		}
		return res;
	}

}
