package org.processmining.plugins.tsml;

public class TsmlText extends TsmlElement {

	/**
	 * PNML text tag.
	 */
	public final static String TAG = "text";

	/**
	 * Text.
	 */
	private String text;

	/**
	 * Creates a fresh text object.
	 */
	public TsmlText() {
		super(TAG);
		text = "";
	}

	public TsmlText(String text) {
		super(TAG);
		this.text = text;
	}

	/**
	 * Imports the text.
	 */
	protected void importText(String text, Tsml tsml) {
		this.text = (this.text + text).trim().replaceAll("&gt;", ">").replaceAll("&lt;", "<");
	}

	/**
	 * Exports the text.
	 */
	protected String exportElements(Tsml tsml) {
		return text.replaceAll(">", "&gt;").replaceAll("<", "&lt;");
	}

	/**
	 * Gets the text.
	 * 
	 * @return
	 */
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
