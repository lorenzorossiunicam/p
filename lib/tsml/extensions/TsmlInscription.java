package org.processmining.plugins.tsml.extensions;

import org.processmining.models.graphbased.AbstractGraphElement;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlAnnotation;
import org.xmlpull.v1.XmlPullParser;

public class TsmlInscription extends TsmlAnnotation {

	/**
	 * PNML inscription tag.
	 */
	public final static String TAG = "inscription";

	/**
	 * Creates a fresh inscription.
	 */
	public TsmlInscription() {
		super(TAG);
	}

	/**
	 * Checks whether the current start tag is known. If known, it imports the
	 * corresponding child element and returns true. Otherwise, it returns
	 * false.
	 * 
	 * @return Whether the start tag was known.
	 */
	protected boolean importElements(XmlPullParser xpp, Tsml tsml) {
		if (super.importElements(xpp, tsml)) {
			/*
			 * Start tag corresponds to a known child element of a PNML
			 * annotation.
			 */
			return true;
		}
		/*
		 * Check whether text element present. If not, bail out.
		 */
		if (text == null) {
			return false;
		}
		return true;
	}

	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		/*
		 * Initial marking should be positive integer.
		 */
		int value = Integer.valueOf(text.getText());
		if (value <= 0) {
			tsml.log(tag, lineNumber, "Expected positive integer");
		}
	}

	/**
	 * Exports the inscription.
	 */
	protected String exportElements(Tsml tsml) {
		return super.exportElements(tsml);
	}

	/**
	 * Gets the inscription. Returns 1 if not specified.
	 * 
	 * @return
	 */
	public int getInscription() {
		try {
			return Integer.valueOf(text.getText());
		} catch (Exception ex) {
		}
		return 1;
	}

	/**
	 * Sets the weight of the given arc in the given net to this inscription.
	 * 
	 * @param arc
	 *            The given arc.
	 */
	public void unmarshall(Transition transition) {
		if (text != null) {
			super.unmarshall(transition);
		}
	}

	public TsmlInscription marshall(AbstractGraphElement element) {
		TsmlInscription result = null;
		if (super.marshall(element) != null) {
			result = this;
		}
		return result;
	}

}
