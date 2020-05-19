package org.processmining.logabstractions.util;

import java.util.Arrays;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.processmining.framework.util.Pair;
import org.processmining.logabstractions.models.LengthOneLoopAbstraction;

public class XEventClassUtils {

	public static XEventClass[] toArray(XEventClasses ec) {
		XEventClass[] res = new XEventClass[ec.size()];
		for (int i = 0; i < ec.size(); i++) {
			res[i] = ec.getByIndex(i);
		}
		return res;
	}

	public static Pair<XEventClass[], int[]> stripLengthOneLoops(XEventClass[] original, LengthOneLoopAbstraction<XEventClass> lola) {
		int[] classMap = new int[original.length];
		XEventClass[] classesLoLFree = new XEventClass[0];
		for (int i = 0; i < original.length; i++) {
			if (!lola.holds(i)) {
				classesLoLFree = Arrays.copyOf(classesLoLFree, classesLoLFree.length + 1);
				classesLoLFree[classesLoLFree.length - 1] = lola.getEventClass(i);
				classMap[i] = classesLoLFree.length - 1;
			} else {
				classMap[i] = -1;
			}
		}
		return new Pair<XEventClass[], int[]>(classesLoLFree, classMap);
	}

}
