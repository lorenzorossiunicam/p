package it.unicam.pros.purple.semanticengine.ptnet;


import it.unicam.pros.purple.semanticengine.Configuration;
import it.unicam.pros.purple.util.eventlogs.trace.event.Event;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PTNetUtil {

    public static void main(String[] args) throws Exception {

        InputStream is = new FileInputStream(new File("pt.pnml"));
        Petrinet pt = importFile(is);
        PnmlEngine e = new PnmlEngine("petrinet", pt);
        System.out.println("Init marking: "+ e.getInitConf());
        Map<Configuration, Set<Event>> x = e.getNexts(e.getInitConf());

        for(Configuration c : x.keySet()){

            System.out.println(x.get(c));
            System.out.println(e.getNexts(c));
        }
    }


    public static Petrinet importFile(InputStream input) throws Exception {


        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        Petrinet result = new PetrinetImpl("net");

        dbf.setValidating(false);
        dbf.setIgnoringComments(true);
        dbf.setIgnoringElementContentWhitespace(true);

        doc = dbf.newDocumentBuilder().parse(input);

        // check if root element is a <pnml> tag
        if (!doc.getDocumentElement().getTagName().equals("pnml")) {
            throw new Exception("pnml tag not found");
        }
        Map<String, Place> placesMap = new HashMap<String, Place>();
        NodeList places = doc.getDocumentElement().getElementsByTagName("place");
        for (int i = 0; i < places.getLength(); i++) {
            Node place = places.item(i);
            String p = null;
            try {
                p = place.getAttributes().getNamedItem("id").getNodeValue();
            }
            catch (Exception e){
                continue;
            }
            Place pl = result.addPlace(p);
            pl.getAttributeMap().put("name", place.getNodeName());
            placesMap.put(p, pl);
        }

        Map<String, Transition> transMap = new HashMap<String, Transition>();
        NodeList transitions = doc.getDocumentElement().getElementsByTagName("transition");
        Set<String> traNames = new HashSet<String>();
        for (int i = 0; i < transitions.getLength(); i++) {
            Node trans = transitions.item(i);
            String t = trans.getAttributes().getNamedItem("id").getNodeValue();
            Transition tra = result.addTransition(t);
            String name = getChild(trans, "name").getTextContent();
            if(traNames.contains(name)){
                name += tra.getLabel();
            }
            traNames.add(name);
            tra.getAttributeMap().put("name", name);
            transMap.put(t, tra);
        }

        NodeList arcs = doc.getDocumentElement().getElementsByTagName("arc");
        for (int i = 0; i < arcs.getLength(); i++) {
            Node arc = arcs.item(i);
            String arcId = arc.getAttributes().getNamedItem("id").getNodeValue();
            String arcSource = arcs.item(i).getAttributes().getNamedItem("source").getNodeValue();
            String arcTarget = arcs.item(i).getAttributes().getNamedItem("target").getNodeValue();
            Arc a;
            if(placesMap.containsKey(arcSource)){
                a = result.addArc(placesMap.get(arcSource), transMap.get(arcTarget));
            }else{
                a = result.addArc(transMap.get(arcSource), placesMap.get(arcTarget));
            }
            a.getAttributeMap().put("name", arc.getNodeName());
        }
        return result;
    }

    public static Element getChild(Node parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    public static String getTransitionName(Transition t){
        return t.getAttributeMap().get("name").toString();
    }
}
