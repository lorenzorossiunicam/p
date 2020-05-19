/**
 * 
 */
package org.processmining.plugins.flex.importing;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.processmining.framework.util.Pair;
import org.processmining.models.flexiblemodel.CancellationRegion;
import org.processmining.models.flexiblemodel.EndTaskNodesSet;
import org.processmining.models.flexiblemodel.Flex;
import org.processmining.models.flexiblemodel.FlexFactory;
import org.processmining.models.flexiblemodel.FlexNode;
import org.processmining.models.flexiblemodel.SetFlex;
import org.processmining.models.flexiblemodel.StartTaskNodesSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * @author aadrians
 * 
 */
public class FlexImporter {

	public static final int NAMENET = 0;

	public static final String STARTTAG = "cnet";

	// result variable
	private Flex flex;
	private StartTaskNodesSet startTaskNodesSet;
	private EndTaskNodesSet endTaskNodesSet;
	private CancellationRegion cancellationRegion;

	public FlexImporter() {
	}

	/**
	 * Assumption : the xml file is valid
	 * 
	 * @param xpp
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	public void importElement(XmlPullParser xpp) throws XmlPullParserException, IOException {
		// utility variables
		Pair<String, String> nodePairInfo = null;
		SetFlex lastSetOfNodes = null;
		Set<Pair<FlexNode, FlexNode>> pairCanceledArcs = null;
		String lastFocusNode = "";
		Stack<String> tagStack = new Stack<String>(); // only used for inputNode and so on

		// initialize some results
		cancellationRegion = new CancellationRegion();
		
		// mapping 
		Map<String, FlexNode> mapIdToFlexNode = new HashMap<String, FlexNode>();
		Map<String, Pair<FlexNode, FlexNode>> mapIdToPairOfFlexNode = new HashMap<String, Pair<FlexNode, FlexNode>>();

		// start parsing
		int eventType = xpp.getEventType(); // has to be XmlPullParser.START_TAG

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				if (xpp.getName().equalsIgnoreCase("name")) {
					tagStack.add("name");
				} else if (xpp.getName().equalsIgnoreCase("startTaskNode")) {
					FlexNode startNode = mapIdToFlexNode.get(xpp.getAttributeValue(0));
					SetFlex setFlex = new SetFlex();
					setFlex.add(startNode);
					startTaskNodesSet = new StartTaskNodesSet();
					startTaskNodesSet.add(setFlex);
				} else if (xpp.getName().equalsIgnoreCase("endTaskNode")) {
					FlexNode endNode = mapIdToFlexNode.get(xpp.getAttributeValue(0));
					SetFlex setFlex = new SetFlex();
					setFlex.add(endNode);
					endTaskNodesSet = new EndTaskNodesSet();
					endTaskNodesSet.add(setFlex);
				} else if (xpp.getName().equalsIgnoreCase("node")) {
					if (tagStack.isEmpty()) { // start of node listing
						nodePairInfo = new Pair<String, String>(xpp.getAttributeValue(0), xpp.getAttributeValue(1));
					} else {
						lastSetOfNodes.add(mapIdToFlexNode.get(xpp.getAttributeValue(0)));
					}
				} else if (xpp.getName().equalsIgnoreCase("inputNode")) {
					tagStack.add("inputNode");
					lastFocusNode = xpp.getAttributeValue(0);
				} else if (xpp.getName().equalsIgnoreCase("outputNode")) {
					tagStack.add("outputNode");
					lastFocusNode = xpp.getAttributeValue(0);
				} else if (xpp.getName().equalsIgnoreCase("inputSet")) {
					tagStack.add("inputSet");
					lastSetOfNodes = new SetFlex();
				} else if (xpp.getName().equalsIgnoreCase("outputSet")) {
					tagStack.add("outputSet");
					lastSetOfNodes = new SetFlex();
				} else if (xpp.getName().equalsIgnoreCase("arc")) {
					if (tagStack.isEmpty()) {
						flex.addArc(mapIdToFlexNode.get(xpp.getAttributeValue(1)),
								mapIdToFlexNode.get(xpp.getAttributeValue(2)));
						mapIdToPairOfFlexNode.put(xpp.getAttributeValue(0),
								new Pair<FlexNode, FlexNode>(mapIdToFlexNode.get(xpp.getAttributeValue(1)),
										mapIdToFlexNode.get(xpp.getAttributeValue(2))));
					} else if (tagStack.peek().equalsIgnoreCase("cancellationRegionNode")) {
						pairCanceledArcs.add(mapIdToPairOfFlexNode.get(xpp.getAttributeValue(0)));
					}
				} else if (xpp.getName().equalsIgnoreCase("cancellationRegionNode")) {
					tagStack.add("cancellationRegionNode");
					lastFocusNode = xpp.getAttributeValue(0);
					pairCanceledArcs = new HashSet<Pair<FlexNode,FlexNode>>();
					lastSetOfNodes = new SetFlex();
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (xpp.getName().equalsIgnoreCase("name")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("inputNode")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("outputNode")) {
					tagStack.pop();
				} else if (xpp.getName().equalsIgnoreCase("inputSet")) {
					tagStack.pop();
					mapIdToFlexNode.get(lastFocusNode).addInputNodes(lastSetOfNodes);
				} else if (xpp.getName().equalsIgnoreCase("outputSet")) {
					tagStack.pop();
					mapIdToFlexNode.get(lastFocusNode).addOutputNodes(lastSetOfNodes);
				} else if (xpp.getName().equalsIgnoreCase("cancellationRegionNode")){
					tagStack.pop();
					cancellationRegion.put(mapIdToFlexNode.get(lastFocusNode), pairCanceledArcs);
					cancellationRegion.setNodeCancellationFor(mapIdToFlexNode.get(lastFocusNode), lastSetOfNodes);
				}
			} else if (eventType == XmlPullParser.TEXT) {
				if (tagStack.peek().equals("name")) {
					if (nodePairInfo == null) {
						// means this is the name of the net
						flex = FlexFactory.newFlex(xpp.getText());
					} else {
						// can only be node
						FlexNode node = flex.addNode(xpp.getText());
						node.setInvisible(nodePairInfo.getSecond().equalsIgnoreCase("true"));
						mapIdToFlexNode.put(nodePairInfo.getFirst(), node);
						nodePairInfo = null;
					}
				}
			}
			eventType = xpp.next();
		} // end of documents
		
		if (flex != null){
			for (FlexNode flexNode : flex.getNodes()){
				flexNode.commitUpdates();
			}
		}
	}

	public Object[] getImportResult (){
		return new Object[] {flex, startTaskNodesSet, endTaskNodesSet, cancellationRegion };
	}
}
