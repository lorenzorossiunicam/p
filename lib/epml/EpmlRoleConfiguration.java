package org.processmining.plugins.epml;

import java.util.Arrays;

import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeCRole"> <xs:choice minOccurs="0">
 *         <xs:element name="configuration"> <xs:complexType> <xs:attribute
 *         name="optionality"> <xs:simpleType> <xs:restriction base="xs:string">
 *         <xs:enumeration value="mnd"/> <xs:enumeration value="off"/>
 *         <xs:enumeration value="opt"/> </xs:restriction> </xs:simpleType>
 *         </xs:attribute> <xs:attribute name="specialization"
 *         type="xs:positiveInteger"/> </xs:complexType> </xs:element>
 *         </xs:choice> </xs:complexType>
 */

public class EpmlRoleConfiguration extends EpmlElement {

	public final static String TAG = "configuration";

	/*
	 * Attributes
	 */
	String optionality;
	String specialization;

	public EpmlRoleConfiguration() {
		super(TAG);

		optionality = null;
		specialization = null;
	}

	protected void importAttributes(XmlPullParser xpp, Epml epml) {
		super.importAttributes(xpp, epml);
		String value = xpp.getAttributeValue(null, "optionality");
		if (value != null) {
			optionality = value;
		}
		value = xpp.getAttributeValue(null, "specialization");
		if (value != null) {
			specialization = value;
		}
	}

	protected String exportAttributes() {
		String s = super.exportAttributes();
		if (optionality != null) {
			s += exportAttribute("optionality", optionality);
		}
		if (specialization != null) {
			s += exportAttribute("specialization", specialization);
		}
		return s;
	}

	protected void checkValidity(Epml epml) {
		super.checkValidity(epml);
		checkRestriction(epml, "optionality", optionality, Arrays.asList("mnd", "off", "opt"), false);
		checkPositiveInteger(epml, "specialization", specialization, false);
	}
}
