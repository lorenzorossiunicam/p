package org.processmining.plugins.tsml;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.AttributeMap;

public class TsmlName extends TsmlAnnotation {

	/**
	 * TSML name tag.
	 */
	public final static String TAG = "name";

	/**
	 * Creates a fresh PNML name.
	 */
	public TsmlName() {
		super(TAG);
	}

	public TsmlName(String text) {
		super(text, TAG);
	}

	public String getName(String defaultName) {
		if (text != null) {
			return text.getText();
		}
		return defaultName;
	}

	public TsmlName marshall(AbstractGraphElement element) {
		TsmlName result = null;
		if (element.getAttributeMap().containsKey(AttributeMap.LABEL)) {
			text = new TsmlText(element.getAttributeMap().get(AttributeMap.LABEL, ""));
			result = this;
		}
		return result;
	}
}
