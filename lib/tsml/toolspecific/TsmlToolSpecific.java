package org.processmining.plugins.tsml.toolspecific;

import org.processmining.plugins.tsml.Tsml;
import org.processmining.plugins.tsml.TsmlElement;
import org.xmlpull.v1.XmlPullParser;

public class TsmlToolSpecific extends TsmlElement {

	public final static String TAG = "toolspecific";

	private String tool;
	private String version;

	public TsmlToolSpecific() {
		super(TAG);
		tool = null;
		version = null;
	}

	protected void importAttributes(XmlPullParser xpp, Tsml tsml) {
		super.importAttributes(xpp, tsml);
		importTool(xpp);
		importVersion(xpp);
	}

	protected String exportAttributes(Tsml tsml) {
		return super.exportAttributes(tsml) + exportTool(tsml) + exportVersion(tsml);
	}

	private void importTool(XmlPullParser xpp) {
		String value = xpp.getAttributeValue(null, "tool");
		if (value != null) {
			tool = value;
		}
	}

	private String exportTool(Tsml tsml) {
		if (tool != null) {
			return exportAttribute("tool", tool, tsml);
		}
		return "";
	}

	private void importVersion(XmlPullParser xpp) {
		String value = xpp.getAttributeValue(null, "version");
		if (value != null) {
			version = value;
		}
	}

	private String exportVersion(Tsml tsml) {
		if (version != null) {
			return exportAttribute("version", version, tsml);
		}
		return "";
	}

	protected void checkValidity(Tsml tsml) {
		super.checkValidity(tsml);
		if ((tool == null) || (version == null)) {
			tsml.log(tag, lineNumber, "Expected tool and version");
		}
	}
}
