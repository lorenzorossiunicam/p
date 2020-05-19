package org.processmining.plugins.epml;

import java.util.Map;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCNode;
import org.processmining.models.graphbased.directed.epc.elements.Connector;
import org.xmlpull.v1.XmlPullParser;

/**
 * @author hverbeek
 * 
 *         <xs:complexType name="typeOR"> <xs:complexContent> <xs:extension
 *         base="epml:tEpcElement"> <xs:sequence> <xs:choice minOccurs="0">
 *         <xs:element name="configurableConnector" type="epml:typeCOR"/>
 *         </xs:choice> <xs:choice minOccurs="0" maxOccurs="unbounded">
 *         <xs:element name="attribute" type="epml:typeAttribute"/> </xs:choice>
 *         </xs:sequence> </xs:extension> </xs:complexContent> </xs:complexType>
 */

public class EpmlOr extends EpmlAttributableEpcElement {

	public final static String TAG = "or";

	/*
	 * Elements
	 */
	private EpmlConfigurableOr configurableConnector;

	public EpmlOr() {
		super(TAG);

		configurableConnector = null;
	}

	protected boolean importElements(XmlPullParser xpp, Epml epml) {
		if (super.importElements(xpp, epml)) {
			return true;
		}
		if (xpp.getName().equals(EpmlConfigurableOr.TAG)) {
			configurableConnector = new EpmlConfigurableOr();
			configurableConnector.importElement(xpp, epml);
			return true;
		}
		/*
		 * Unknown start tag.
		 */
		return false;
	}

	protected String exportElements() {
		String s = super.exportElements();
		if (configurableConnector != null) {
			s += configurableConnector.exportElement();
		}
		return s;
	}

	public void convertToCEpc(ConfigurableEPC cEpc, Map<String, EPCNode> map, GraphLayoutConnection layout) {
		Connector connector = cEpc.addConnector(getName(), Connector.ConnectorType.OR, configurableConnector != null);
		if (graphics != null) {
			graphics.convertToEpc(cEpc, connector, layout);
		}
		map.put(getId(), connector);
	}
}
