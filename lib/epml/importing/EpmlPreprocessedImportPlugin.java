package org.processmining.plugins.epml.importing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.epc.ConfigurableEPC;
import org.processmining.models.graphbased.directed.epc.EPCFactory;
import org.processmining.plugins.epml.Epml;
import org.processmining.plugins.epml.exporting.EPCEpmlConnection;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

@Plugin(name = "Import C-EPC from EPML file (use preprocess)", parameterLabels = { "Filename" }, returnLabels = { "Configurable EPC" }, returnTypes = { ConfigurableEPC.class })
@UIImportPlugin(description = "EPML EPC files (use preprocess)", extensions = { "epml" })
public class EpmlPreprocessedImportPlugin extends AbstractImportPlugin {

	protected FileFilter getFileFilter() {
		return new FileNameExtensionFilter("EPML files (use preprocess)", "epml");
	}

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
			throws Exception {

		/*
		 * Preprocess and correct input.
		 */
		String xmlString = preprocessXML(input);
		/*
		 * Get an XML pull parser.
		 */
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		/*
		 * Initialize the parser on the provided input.
		 */
		xpp.setInput(new StringReader(xmlString));
		/*
		 * Get the first event type.
		 */
		int eventType = xpp.getEventType();
		Epml epml = new Epml();

		/*
		 * Skip whatever we find until we've found a start tag.
		 */
		while (eventType != XmlPullParser.START_TAG) {
			eventType = xpp.next();
		}
		/*
		 * Check whether start tag corresponds to PNML start tag.
		 */
		if (xpp.getName().equals(Epml.TAG)) {
			/*
			 * Yes it does. Import the PNML element.
			 */
			epml.importElement(xpp, epml);
		} else {
			/*
			 * No it does not. Return null to signal failure.
			 */
			epml.log(Epml.TAG, xpp.getLineNumber(), "Expected epml");
		}
		if (epml.hasErrors()) {
			context.getProvidedObjectManager().createProvidedObject("Log of EPML import", epml.getLog(), XLog.class,
					context);
			return null;
		}

		ConfigurableEPC cEpc = EPCFactory.newConfigurableEPC("C-EPC imported from " + filename);
		/*
		 * Set the label of the Petri net.
		 */
		context.getFutureResult(0).setLabel("C-EPC imported from " + filename);

		/*
		 * Add connection between CEPC and Epml.
		 */
		context.addConnection(new EPCEpmlConnection(cEpc, epml));

		GraphLayoutConnection layout = new GraphLayoutConnection(cEpc);
		epml.convertToCEpc(cEpc, layout);
		return cEpc;
	}

	private String preprocessXML(InputStream input) {
		String formattedOutput = "";
		try {

			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer(new StreamSource(new ByteArrayInputStream(PREPROCESS
					.getBytes())));

			StreamSource xmlSource = new StreamSource(input);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(xmlSource, new StreamResult(baos));

			formattedOutput = baos.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return formattedOutput;
	}

	static private String PREPROCESS = ""
			+ "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<xsl:stylesheet version=\"1.0\""
			+ "  xmlns:epml = \"http://www.epml.de\""
			+ "  xmlns:xsl  = \"http://www.w3.org/1999/XSL/Transform\">"
			+ ""
			+ "<xsl:output encoding=\"UTF-8\"/>"
			+ ""
			+ "<xsl:variable name=\"ids\" select=\"//@id[not(parent::epc)]\"/>"
			+ ""
			+ "<xsl:variable name=\"epcIds\" select=\"//@epcId | //epc[not(@epcId)]/@id\"/>"
			+ ""
			+ "<!-- Calculate the maximum @id number -->"
			+ "<xsl:variable name=\"max-id\">"
			+ "    <xsl:for-each select=\"$ids\">"
			+ "        <xsl:sort data-type=\"number\"/>"
			+ "        <xsl:if test=\"position() = last()\">"
			+ "            <xsl:value-of select=\"current()\"/>"
			+ "        </xsl:if>"
			+ "    </xsl:for-each>"
			+ "</xsl:variable>"
			+ ""
			+ "<!-- Calculate the maximum @epcId number -->"
			+ "<xsl:variable name=\"max-epcId\">"
			+ "    <xsl:for-each select=\"$epcIds\">"
			+ "        <xsl:sort data-type=\"number\"/>"
			+ "        <xsl:if test=\"position() = last()\">"
			+ "            <xsl:value-of select=\"current()\"/>"
			+ "        </xsl:if>"
			+ "    </xsl:for-each>"
			+ "</xsl:variable>"
			+ ""
			+ "<!-- If the top level <epml> element isn't namespaced, add the right namespace -->"
			+ "<!-- If there's no top level <coordinates>, insert one -->"
			+ "<!-- If there's no top level <directory>, insert one -->"
			+ "<xsl:template match=\"/epml:epml | epml\">"
			+ "    <xsl:element name=\"epml:epml\">"
			+ "        <xsl:apply-templates select=\"@*\"/>"
			+ "        <xsl:choose>"
			+ "        <xsl:when test=\"not(coordinates)\">"
			+ "            <xsl:apply-templates select=\"graphicsDefault\"/>"
			+ "            <coordinates xOrigin=\"leftToRight\" yOrigin=\"topToBottom\"/>"
			+ "            <xsl:apply-templates select=\"definitions | attributeTypes | directory\"/>"
			+ "        </xsl:when>"
			+ "        <xsl:otherwise>"
			+ "            <xsl:apply-templates select=\"node()[name() != 'epc']\"/>"
			+ "        </xsl:otherwise>"
			+ "        </xsl:choose>"
			+ "        <xsl:if test=\"not(directory)\">"
			+ "            <directory>"
			+ "                <xsl:apply-templates select=\"epc|epml:epc\"/>"
			+ "            </directory>"
			+ "        </xsl:if>"
			+ "    </xsl:element>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- If an <epc> element has no @epcID but does have an @id, use that instead -->"
			+ "<xsl:template match=\"epc[not(@epcId)]\">"
			+ "    <xsl:copy>"
			+ "        <xsl:call-template name=\"epcId-template\">"
			+ "            <xsl:with-param name=\"epcId-param\" select=\"@id\"/>"
			+ "        </xsl:call-template>"
			+ "        <xsl:apply-templates select=\"@*|node()\"/>"
			+ "    </xsl:copy>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- Drop the epc/@id attribute if we're replacing it with epc/@epcId -->"
			+ "<xsl:template match=\"epc[not(@epcId)]/@id\"/>"
			+ ""
			+ "<!-- If epc/@epcId is zero, renumber it -->"
			+ "<xsl:template match=\"epc/@epcId\" name=\"epcId-template\">"
			+ "    <xsl:param name=\"epcId-param\" select=\"current()\"/>"
			+ "    <xsl:attribute name=\"epcId\">"
			+ "        <xsl:choose>"
			+ "        <xsl:when test=\"$epcId-param = 0\">"
			+ "            <xsl:value-of select=\"$max-epcId + 1\"/>"
			+ "        </xsl:when>"
			+ "        <xsl:otherwise>"
			+ "            <xsl:value-of select=\"$epcId-param\"/>"
			+ "        </xsl:otherwise>"
			+ "        </xsl:choose>"
			+ "    </xsl:attribute>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- If epc component id is zero, renumber it -->"
			+ "<xsl:template match=\"*[name() != 'epc']/@id | flow/@source | flow/@target\" name=\"rule1\">"
			+ "    <xsl:attribute name=\"{name()}\">"
			+ "        <xsl:choose>"
			+ "            <xsl:when test=\"current() = 0\">"
			+ "                <xsl:value-of select=\"$max-id + 1\"/>"
			+ "            </xsl:when>"
			+ "            <xsl:otherwise>"
			+ "                <xsl:value-of select=\"current()\"/>"
			+ "            </xsl:otherwise>"
			+ "        </xsl:choose>"
			+ "    </xsl:attribute>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- Renumber arcs if their id is the same as any event OR if they happen to be zero -->"
			+ "<xsl:template match=\"arc[(@id = //epc/*[name() != 'arc']/@id) or (@id = 0)]/@id\">"
			+ "    <xsl:attribute name=\"id\">"
			+ "        <xsl:value-of select=\"current() + $max-id + count(//*[name() != 'epc'][@id = 0])\"/>"
			+ "        <!-- the count term above corrects for the case in which #rule1 uses"
			+ "             $max-id + 1 to label a non-arc component which had @id of 0 -->"
			+ "    </xsl:attribute>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- If someone has namespaced the internal EPML elements (which is what the EPML scheme ought've required"
			+ "     but currently forbids) then convert to the equivalent unqualified element -->"
			+ "<xsl:template match=\"epml:*\">"
			+ "    <xsl:element name=\"{local-name()}\">"
			+ "        <xsl:apply-templates select=\"@*|node()\"/>"
			+ "    </xsl:element>"
			+ "</xsl:template>"
			+ ""
			+ "<!-- Use an identity template so that everything that doesn't need workarounds gets passed through unchanged. -->"
			+ "<xsl:template match=\"@*|node()\">" + "    <xsl:copy>"
			+ "        <xsl:apply-templates select=\"@*|node()\"/>" + "    </xsl:copy>" + "</xsl:template>" + ""
			+ "</xsl:stylesheet>";

}
