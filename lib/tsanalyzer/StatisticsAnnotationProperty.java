package org.processmining.plugins.tsanalyzer;

import org.processmining.plugins.tsanalyzer.annotation.AnnotationProperty;

public class StatisticsAnnotationProperty extends AnnotationProperty<Float> { //we use floats instead of doubles for better memory use

	private static final String AVERAGE = "average";
	private static final String STANDARD_DEVIATION = "st.dev.";
	private static final String MIN = "min";
	private static final String MAX = "max";
	private static final String SUM = "sum";
	private static final String VARIANCE = "var";
	private static final String FREQUENCY = "freq";
	private static final String MEDIAN = "median";

	public StatisticsAnnotationProperty() {
		super();

		setValue(new Float(0.0));
		setMeasurement(AVERAGE, null);
		setMeasurement(STANDARD_DEVIATION, null);
		setMeasurement(MIN, null);
		setMeasurement(MAX, null);
		setMeasurement(SUM, null);
		setMeasurement(VARIANCE, null);
		setMeasurement(FREQUENCY, null);
		setMeasurement(MEDIAN, null);
	}

	public float getMin() {
		
		return ((Float) getMeasurement(MIN)).floatValue();
	}

	public void setMin(float value) {
		setMeasurement(MIN, value);
	}

	public float getAverage() {
		return ((Float) getMeasurement(AVERAGE)).floatValue();
	}

	public void setAverage(float value) {
		setMeasurement(AVERAGE, value);
	}

	public float getStandardDeviation() {
		return ((Float) getMeasurement(STANDARD_DEVIATION)).floatValue();
	}

	public void setStandardDeviation(float value) {
		setMeasurement(STANDARD_DEVIATION, value);
	}

	public float getMax() {
		return ((Float) getMeasurement(MAX)).floatValue();
	}

	public void setMax(float value) {
		setMeasurement(MAX, value);
	}

	public float getSum() {
		return ((Float) getMeasurement(SUM)).floatValue();
	}

	public void setSum(float value) {
		setMeasurement(SUM, value);
	}

	public float getVariance() {
		return ((Float) getMeasurement(VARIANCE)).floatValue();
	}

	public void setVariance(float value) {
		setMeasurement(VARIANCE, value);
	}

	public long getFrequency() {
		return ((Float) getMeasurement(FREQUENCY)).longValue();
	}

	public void setFrequency(float value) {
		setMeasurement(FREQUENCY, value);
	}

	public float getMedian() {
		return ((Float) getMeasurement(MEDIAN)).floatValue();
	}

	public void setMedian(float value) {
		setMeasurement(MEDIAN, value);
	}
	public boolean isEmpty()
	{
		if(getMeasurement(AVERAGE) == null)
			return true;
		else
			return false;
	}
}
