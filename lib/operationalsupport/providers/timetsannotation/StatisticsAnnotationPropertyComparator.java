package org.processmining.plugins.operationalsupport.providers.timetsannotation;

import java.util.Comparator;

import org.processmining.plugins.tsanalyzer.StatisticsAnnotationProperty;

public class StatisticsAnnotationPropertyComparator implements Comparator<StatisticsAnnotationProperty> {

	public int compare(StatisticsAnnotationProperty p1, StatisticsAnnotationProperty p2) {
		if ((p1 == null) || (p2 == null)) {
			return 0;
		} else {
			Float d1 = p1.getValue();
			Float d2 = p2.getValue();
			if ((d1 == null) || (d2 == null)) {
				return 0;
			} else {
				int result = (int) (d1 - d2);
				if (result == 0) {
					result = (int) (p1.getStandardDeviation() - p2.getStandardDeviation());
				}
				return result;
			}
		}
	}

}
