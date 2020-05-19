package org.processmining.plugins.tsml;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

public class TsmlElement {

	/**
	 * The PNML tag for this element.
	 */
	public String tag;

	public int lineNumber;

	/**
	 * Creates a fresh PNML element.
	 * 
	 * @param tag
	 */
	public TsmlElement(String tag) {
		this.tag = tag;
	}

	/**
	 * Imports the given element.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	public void importElement(XmlPullParser xpp, Tsml tsml) {
		lineNumber = xpp.getLineNumber();
		/*
		 * Import all attributes of this element.
		 */
		importAttributes(xpp, tsml);
		/*
		 * Create afresh stack to keep track of start tags to match.
		 */
		ArrayList<String> stack = new ArrayList<String>();
		/*
		 * Add the current tag to this stack, as we still have to find the
		 * matching end tag.
		 */
		stack.add(tag);
		/*
		 * As long as the stack is not empty, we're still working on this
		 * object.
		 */
		while (!stack.isEmpty()) {
			/*
			 * Get next event.
			 */
			try {
				int eventType = xpp.next();
				if (eventType == XmlPullParser.END_DOCUMENT) {
					/*
					 * End of document. Should not happen.
					 */
					tsml.log(tag, xpp.getLineNumber(), "Found end of document");
					//System.err.println("Line " + xpp.getLineNumber() + ": Malformed TSML document: No </"+ tag + "> found.");
					//throw new Exception("Malformed TSML document: No </"+ tag + "> found.");
					return;
				} else if (eventType == XmlPullParser.START_TAG) {
					//tsml.logInfo("Tag " + tag, XLifecycleExtension.StandardModel.START, "Line " + xpp.getLineNumber());
					/*
					 * Start tag. Push it on the stack.
					 */
					stack.add(xpp.getName());
					/*
					 * If this tag is the second on the stack, then it is a
					 * direct child.
					 */
					if (stack.size() == 2) {
						/*
						 * For a direct child, check whether the tag is known.
						 * If so, take proper action. Note that this needs not
						 * to be done for other offspring.
						 */
						if (importElements(xpp, tsml)) {
							/*
							 * Known start tag. The end tag has been matched and
							 * can be popped from the stack.
							 */
							stack.remove(stack.size() - 1);
						}
					}
				} else if ((eventType == XmlPullParser.END_TAG)) {
					//tsml.logInfo("Tag " + tag, XLifecycleExtension.StandardModel.COMPLETE, "Line " + xpp.getLineNumber());
					/*
					 * End tag. Should be identical to top of the stack.
					 */
					if (xpp.getName().equals(stack.get(stack.size() - 1))) {
						/*
						 * Yes it is. Pop the stack.
						 */
						stack.remove(stack.size() - 1);
					} else {
						/*
						 * No it is not. XML violation.
						 */
						tsml.log(tag, xpp.getLineNumber(), "Found " + xpp.getName() + ", expected "
								+ stack.get(stack.size() - 1));
						return;
					}
				} else if (eventType == XmlPullParser.TEXT) {
					/*
					 * Plain text. Import it.
					 */
					//tsml.logInfo("Text", XLifecycleExtension.StandardModel.UNKNOWN, "Line " + xpp.getLineNumber(), xpp.getText());
					importText(xpp.getText(), tsml);
				}
			} catch (Exception ex) {
				tsml.log(tag, xpp.getLineNumber(), ex.getMessage());
				return;
			}
		}
		/*
		 * The element has been imported. Now is a good time to check its
		 * validity.
		 */
		checkValidity(tsml);
	}

	/**
	 * Exports the element.
	 * 
	 * @return
	 */
	public String exportElement(Tsml tsml) {
		/*
		 * Export all attributes of this element.
		 */
		String s = "<" + tag;
		s += exportAttributes(tsml);
		/*
		 * Export all child elements.
		 */
		String t = exportElements(tsml);
		if (t.equals("")) {
			/*
			 * No child elements, use combined start-end tag.
			 */
			s += "/>\n";
		} else {
			/*
			 * Child elements, use separated start and end tags.
			 */
			s += ">\n" + t + "</" + tag + ">\n";
		}
		return s;
	}

	/**
	 * Imports all standard attributes: None. If some subclass has attributes,
	 * this method needs to be overruled by it.
	 * 
	 * @param xpp
	 * @param tsml
	 */
	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
	}

	/**
	 * Exports all standard attributes: None. If some subclass has attributes,
	 * this method needs to be overruled by it.
	 * 
	 * @return
	 */
	protected String exportAttributes(Tsml tsml) {
		return "";
	}

	/**
	 * Imports all standard child elements: None. If some subclass has child
	 * elements, this method needs to be overruled by it.
	 * 
	 * @param xpp
	 * @param tsml
	 * @return
	 */
	protected boolean importElements(XmlPullParser xpp, Tsml tsml) {
		return false;
	}

	/**
	 * Exports all standard elements: None. If some subclass has child elements,
	 * this method needs to be overruled by it.
	 * 
	 * @return
	 */
	protected String exportElements(Tsml tsml) {
		return "";
	}

	/**
	 * Imports standard text: No action. If some subclass needs to import text,
	 * this method needs to be overruled by it.
	 * 
	 * @param text
	 * @param tsml
	 */
	protected void importText(String text, Tsml tsml) {
	}

	/**
	 * Default way to export some attribute.
	 * 
	 * @param tag
	 *            The attribute tag.
	 * @param value
	 *            The attribute value.
	 * @return
	 */
	protected String exportAttribute(String tag, String value, Tsml tsml) {
		return " " + tag + "=\"" + protectSpecialCharacters(value) + "\"";
	}

	/**
	 * Default check for validity: No action. If some subclass needs to check
	 * validity, this method needs to be overruled by it.
	 * 
	 * @param tsml
	 */
	protected void checkValidity(Tsml tsml) {
	}

	private static String protectSpecialCharacters(String originalUnprotectedString) {
	    if (originalUnprotectedString == null) {
	        return null;
	    }
	    boolean anyCharactersProtected = false;

	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < originalUnprotectedString.length(); i++) {
	        char ch = originalUnprotectedString.charAt(i);

	        boolean controlCharacter = ch < 32;
	        boolean unicodeButNotAscii = ch > 126;
	        boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

	        if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
	            stringBuffer.append("&#" + (int) ch + ";");
	            anyCharactersProtected = true;
	        } else {
	            stringBuffer.append(ch);
	        }
	    }
	    if (anyCharactersProtected == false) {
	        return originalUnprotectedString;
	    }

	    return stringBuffer.toString();
	}

}
