package org.processmining.plugins.ywl.replayer.ui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.util.Pair;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.MultipleAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import com.fluxicon.slickerbox.factory.SlickerFactory;
import com.fluxicon.slickerbox.ui.SlickerComboBoxUI;


/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class MappingStep extends ReplayStep {
	private static final long serialVersionUID = -7657691516311492851L;

	public static final String NOTMAPPED = "<Unmapped>";
	
	/**
	 * Internal data
	 */
	private Object[] yawlNodes;
	private List<JComboBox> comboBoxes = new LinkedList<JComboBox>();
	private XEventClasses eventClasses;

	public MappingStep(NetGraph yawl, XLog log, Object[] vertices){
		initComponents(yawl, log, vertices);
	}
	
	public boolean precondition() {
		return true;
	}

	public void readSettings() {}
	
	public String getLabel(Object node) {
		// TODO Deal with Multiple instances
		String value = null;
		if (node instanceof OutputCondition) {
			value = "Output Condition: " + (((OutputCondition) node).getEngineId());
		}
		if (node instanceof InputCondition) {
			value = "Input Condition: " + (((InputCondition) node).getEngineId());
		}
		if (node instanceof Condition) {
			value = "Condition: " + (((Condition) node).getEngineId());
		}
		if (node instanceof AtomicTask) {
			value = "Atomic Task: " + (((AtomicTask) node).getEngineId());
		}
		if (node instanceof CompositeTask) {
			value = "Composite Task: " + (((CompositeTask) node).getEngineId());
		}		
		if (node instanceof MultipleAtomicTask) {
			value = "Multiple Atomic Task: " + (((MultipleAtomicTask) node).getEngineId());
		}
		if (node instanceof MultipleCompositeTask) {
			value = "Multiple Composite Task" + (((MultipleCompositeTask) node).getEngineId());
		}		
		return value;
	}

	private void initComponents(NetGraph yawl, XLog log, Object[] vertices){
		// util objects
		SlickerFactory slickerFactory = SlickerFactory.instance();
		
		// sort vertices array
		// the elements of the array don't implement the comparable so have to work around to implement the sort
		String[] verticesNames = new String[vertices.length];	
		for (int i = 0; i < vertices.length; i++)  {
			verticesNames[i] = getLabel(vertices[i]);
		}
		Object[] newVertices = new Object[vertices.length];
		Arrays.sort(verticesNames);
		for (int i = 0; i < vertices.length; i++) {
			for (int j = 0; j < vertices.length; j++) {
				if (verticesNames[i].equals(getLabel(vertices[j]))) {
					newVertices[i] = vertices[j];
				}
			}
		}
		
		this.yawlNodes = newVertices;

		setLayout(new BorderLayout());
    	String body = "<p>Map the nodes on the left side panel to an event class on the right side panel.";
    	body += " Take the following rules into account:<ol><li> 'start' events of tasks should be mapped";
    	body += " with the corresponding Task,</li><li> conditions should be mapped with the corresponding";
    	body += " 'complete' event for the task for which it is an output place, and</li><li> the starting condition";
    	body += " can be left unmapped.</li></ol></p>";
    	add(slickerFactory.createLabel("<html><h1>Mapping</h1>" + body), BorderLayout.NORTH);
				
		// init mapping panel
		JPanel insidePanel = slickerFactory.createRoundedPanel();
		insidePanel.setLayout(new TableLayout());

		// init row size
		double rowheight[] = new double[yawlNodes.length]; 
		for (int i=0; i < rowheight.length; i++){
			rowheight[i] = 30;
		}
		
		// init table size
		double size[][] = {{TableLayout.FILL, 10, TableLayout.FILL}, rowheight};
    	insidePanel.setLayout(new TableLayout(size));

    	// create array of available event classes
		XLogInfo summary = XLogInfoFactory.createLogInfo(log);
		
		// init event classes 
		this.eventClasses = summary.getEventClasses();
		
		Collection<XEventClass> colEventClasses = eventClasses.getClasses(); 
		Object[] arrEvClasses = colEventClasses.toArray();
		Arrays.sort(arrEvClasses);
		
		Object[] availEventClasses = new Object[arrEvClasses.length + 1];
		availEventClasses[0] = NOTMAPPED;
		
		for (int i=0; i < arrEvClasses.length; i++){
			availEventClasses[i+1] = arrEvClasses[i];
		}
		
    	// insert correct item
		for (int i=0; i < yawlNodes.length; i++){			
			// add to comboboxes
			JComboBox combo = new JComboBox(availEventClasses);
			combo.setPreferredSize(new Dimension(150,25));
			combo.setSize(new Dimension(150,25));
			combo.setMinimumSize(new Dimension(150,25));
			combo.setUI(new SlickerComboBoxUI());
			String currEvNodeStr = getLabel(yawlNodes[i]);
			for (int j=0; j < availEventClasses.length; j++){
				if (availEventClasses[j].toString().equals(currEvNodeStr)){
					combo.setSelectedIndex(j);
					break;
				}
			}
			
			// add to GUI 
			insidePanel.add(slickerFactory.createLabel(getLabel(yawlNodes[i])), "0, " + i);
			insidePanel.add(combo, "2, " + i);
			comboBoxes.add(combo);
		}

		JScrollPane scrollPane = new JScrollPane(insidePanel);
		
    	add(scrollPane, BorderLayout.CENTER);
	}

	public Collection<Pair<YAWLVertex, XEventClass>> getMapping() {
		Collection<Pair<YAWLVertex, XEventClass>> result = new HashSet<Pair<YAWLVertex,XEventClass>>();
		for (int i=0; i < yawlNodes.length; i++){
			Object selectedItem = comboBoxes.get(i).getSelectedItem();
			Pair<YAWLVertex, XEventClass> newPair;
			if (selectedItem instanceof XEventClass){
				newPair = new Pair<YAWLVertex, XEventClass>((YAWLVertex)yawlNodes[i], ((XEventClass) comboBoxes.get(i).getSelectedItem()));
			} else {
				newPair = new Pair<YAWLVertex, XEventClass>((YAWLVertex)yawlNodes[i], null);
			}
			result.add(newPair);
		}
		return result;
	}
	
	public XEventClasses getEventClasses(){
		return this.eventClasses;
	}
}
