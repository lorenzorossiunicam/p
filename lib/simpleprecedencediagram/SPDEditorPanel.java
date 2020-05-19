/**
 * 
 */
package org.processmining.plugins.simpleprecedencediagram;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.BreakIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.model.XLog;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.GraphSelectionModel;
import org.processmining.connections.simpleprecedencediagram.LogSPDConnection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.jgraph.elements.ProMGraphCell;
import org.processmining.models.jgraph.elements.ProMGraphEdge;
import org.processmining.models.jgraph.visualization.ProMJGraphPanel;
import org.processmining.models.simpleprecedencediagram.SPD;
import org.processmining.models.simpleprecedencediagram.SPDEdge;
import org.processmining.models.simpleprecedencediagram.SPDNode;

/**
 * GUI to create mapping between SPD nodes and EventClasses in a log. If it is
 * inactive, it can only be used to view mappings
 * 
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Mar 16, 2009
 */
public class SPDEditorPanel extends JPanel {
	private static final long serialVersionUID = 4057827649808081261L;

	// Internal Data Structure	
	private final PluginContext context; // this is needed to create a connection whenever all nodes already mapped to at least one activity instance
	protected XLog log;
	protected SPD net;
	protected XEventClasses classes;
	protected Map<SPDNode, Set<XEventClass>> relations;

	// data structure for GUI
	protected SortedListModel availableMappingListModel = new SortedListModel(); // available mapping
	protected SortedListModel selectedMappingListModel = new SortedListModel(); // selected
	protected Object selectedGraphElement = null;

	// GUI Components
	protected ProMJGraphPanel graphVisPanel;
	protected JTabbedPane southTabs;
	protected ViewSpecificAttributeMap viewSpecificMap;
	protected JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);

	private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

	// GUI for mapping
	private final JTextField txtFldNodeLabel = new JTextField(50);

	private final JLabel lblAvailableSelection = new JLabel("Available selection");
	private final JList lstNodeMapping = new JList();
	private final JScrollPane scrollPaneNodeMapping = new JScrollPane();

	private final JButton btnAddMapping = new JButton("Add Mapping -->");
	private final JButton btnRemoveMapping = new JButton("<-- Remove Mapping");
	private final JButton btnUpdateMapping = new JButton("Update Node Mapping");

	private final JLabel lblSelectedSelection = new JLabel("Selected mapping");
	private final JList lstSelectedNodeMapping = new JList();
	private final JScrollPane scrollPaneSelectedNodeMapping = new JScrollPane();

	private final JLabel instructionLbl = new JLabel();
	private final JLabel tobeMappedlbl = new JLabel();

	/**
	 * Constructor for GUI
	 * 
	 * @param graph
	 * @param log
	 * @param classes
	 * @param relations
	 * @param context
	 * @param isEditable
	 *            true if it enables map editing, false if it is only view
	 *            mapping
	 */
	public SPDEditorPanel(SPD graph, XLog log, XEventClasses classes, Map<SPDNode, Set<XEventClass>> relations,
			PluginContext context, boolean isEditable) {
		// set internal data structure
		net = graph;
		this.log = log;
		this.context = context;
		this.classes = classes;
		this.relations = relations;

		// set GUI
		setLayout(new BorderLayout());

		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);

		viewSpecificMap = new ViewSpecificAttributeMap();

		graphVisPanel = ProMJGraphVisualizer.instance().visualizeGraph(context, net, viewSpecificMap);
		splitPane.setLeftComponent(graphVisPanel);

		southTabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		splitPane.setRightComponent(southTabs);

		this.add(splitPane, BorderLayout.CENTER);

		// set the action whenever a node in the graph is selected
		GraphSelectionModel model = graphVisPanel.getGraph().getSelectionModel();
		model.setSelectionMode(GraphSelectionModel.SINGLE_GRAPH_SELECTION);
		model.addGraphSelectionListener(new GraphSelectionListener() {

			@SuppressWarnings("unchecked")
			public void valueChanged(GraphSelectionEvent evt) {
				for (Object cell : evt.getCells()) {
					if (evt.isAddedCell(cell)) {
						if (cell instanceof ProMGraphCell) {
							ProMGraphCell node = (ProMGraphCell) cell;
							SPDNode spdNode = (SPDNode) node.getNode();
							setCurrentlySelectedGraphElement(spdNode);
							populateDataBasedOnNode(spdNode);
						} else {
							if (cell instanceof ProMGraphEdge) {
								ProMGraphEdge edge = (ProMGraphEdge) cell;
								SPDEdge<SPDNode, SPDNode> spdEdge = (SPDEdge<SPDNode, SPDNode>) edge.getEdge();
								setCurrentlySelectedGraphElement(spdEdge);
							}
						}
					}
				}

			}
		});

		// populate the editor component at the bottom
		addEditorComponent(classes.getClasses(), isEditable);
	}

	/**
	 * Main method to be called in order to add mapping panel
	 * 
	 * @param availableMappings
	 */
	public void addEditorComponent(Collection<XEventClass> availableMappings, boolean isEditable) {
		southTabs.addTab("<html>Simple Precedence Diagram Editor</html>", new JScrollPane(getGraphEditorComponent(
				availableMappings, isEditable)));
	}

	/**
	 * Update graph element which is currently selected
	 * 
	 * @param cell
	 */
	private void setCurrentlySelectedGraphElement(Object cell) {
		selectedGraphElement = cell;
	}

	/**
	 * Shows information about a node in editor panel
	 * 
	 * @param spdNode
	 */
	private void populateDataBasedOnNode(SPDNode spdNode) {
		// reset selection list
		resetSelectionList();

		// populate selected nodes
		List<Object> listEventClasses = new LinkedList<Object>();
		for (XEventClass eventClass : relations.get(spdNode)) {
			listEventClasses.add(eventClass);
		}
		selectedMappingListModel.addAll(listEventClasses);

		// set label
		txtFldNodeLabel.setText(spdNode.getLabel());

		// update available selection list
		availableMappingListModel.removeAll(listEventClasses.toArray());
	}

	/**
	 * empty selected mapping list and populate available mapping list as much
	 * as possible
	 * 
	 * @param array
	 */
	private void resetSelectionList() {
		availableMappingListModel.addAll(selectedMappingListModel.getAllElements().toArray());
		selectedMappingListModel.removeAllElements();
		txtFldNodeLabel.setText("");
	}

	/**
	 * private method to create GUI elements of editor component
	 * 
	 * @param availableMappings
	 * @return
	 */
	private JComponent getGraphEditorComponent(Collection<XEventClass> availableMappings, boolean isEditable) {
		/**
		 * Instruction Panel
		 */
		if (isEditable) {
			instructionLbl
					.setText("Instruction : Please map each SPD node onto one or more event classes. Select a node from diagram in order to view/edit its mapping.");
			instructionLbl.setForeground(Color.RED);

			String message = "SPD nodes to be mapped : ";
			int size = net.getNodes().size();
			int counter = 0;
			for (SPDNode node : net.getNodes()) {
				if (counter == size - 1) {
					message += "[" + node.getLabel() + "]";
				} else {
					message += "[" + node.getLabel() + "], ";
				}
				counter++;
			}
			wrapLabelText(tobeMappedlbl, message, 1100);
		} else {
			instructionLbl.setText("Instruction : Select a node from diagram in order to view its mapping.");
		}

		JPanel instructionPanel = new JPanel();
		instructionPanel.setLayout(new BorderLayout());
		instructionPanel.add(instructionLbl, BorderLayout.NORTH);
		if (isEditable) {
			instructionPanel.add(tobeMappedlbl, BorderLayout.CENTER);
		}

		/**
		 * Top mapping panel
		 */
		JLabel lblNodeIndentifier = new JLabel("Node Label");
		JPanel topMappingPanel = new JPanel();
		topMappingPanel.add(lblNodeIndentifier);
		topMappingPanel.add(txtFldNodeLabel);

		/**
		 * Center mapping Panel
		 */
		JPanel centerMappingPanel = new JPanel();
		centerMappingPanel.setLayout(new GridBagLayout());

		// available selection list
		availableMappingListModel.addAll(availableMappings.toArray());

		lstNodeMapping.setModel(availableMappingListModel);
		lstNodeMapping.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneNodeMapping.getViewport().setView(lstNodeMapping);

		// selected selection list
		lstSelectedNodeMapping.setModel(selectedMappingListModel);
		lstSelectedNodeMapping.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		scrollPaneSelectedNodeMapping.getViewport().setView(lstSelectedNodeMapping);

		// add mapping button
		btnAddMapping.addActionListener(new AddListener());

		// remove mapping button
		btnRemoveMapping.addActionListener(new RemoveListener());

		// update mapping button
		btnUpdateMapping.addActionListener(new UpdateListener());

		setEditable(isEditable);

		// populate mapping panel
		centerMappingPanel.add(lblAvailableSelection, new GridBagConstraints(0, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, EMPTY_INSETS, 0, 0));
		centerMappingPanel.add(scrollPaneNodeMapping, new GridBagConstraints(0, 3, 1, 5, .5, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, EMPTY_INSETS, 0, 0));

		centerMappingPanel.add(btnAddMapping, new GridBagConstraints(1, 3, 1, 2, 0, .25, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, EMPTY_INSETS, 0, 0));
		centerMappingPanel.add(btnRemoveMapping, new GridBagConstraints(1, 5, 1, 2, 0, .25, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

		centerMappingPanel.add(lblSelectedSelection, new GridBagConstraints(2, 2, 1, 1, 0, 0,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, EMPTY_INSETS, 0, 0));
		centerMappingPanel.add(scrollPaneSelectedNodeMapping, new GridBagConstraints(2, 3, 1, 5, .5, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, EMPTY_INSETS, 0, 0));

		centerMappingPanel.add(btnUpdateMapping, new GridBagConstraints(1, 7, 1, 2, 0, .25, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));

		/**
		 * Main mapping panel
		 */
		JPanel mappingPanel = new JPanel();
		mappingPanel.setLayout(new BorderLayout());
		mappingPanel.add(topMappingPanel, BorderLayout.NORTH);
		mappingPanel.add(centerMappingPanel, BorderLayout.CENTER);

		/**
		 * Main panel
		 */
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(instructionPanel, BorderLayout.NORTH);
		mainPanel.add(mappingPanel, BorderLayout.CENTER);
		return mainPanel;
	}

	/**
	 * enable/disable editing
	 * 
	 * @param isEditable
	 */
	private void setEditable(boolean isEditable) {
		btnAddMapping.setEnabled(isEditable);
		btnRemoveMapping.setEnabled(isEditable);
		btnUpdateMapping.setEnabled(isEditable);
		txtFldNodeLabel.setEditable(isEditable);
	}

	/**
	 * Add to list of selected mapping
	 * 
	 * @author arya
	 * @email arya.adriansyah@gmail.com
	 * @version Mar 19, 2009
	 */
	private class AddListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object[] selectedValues = lstNodeMapping.getSelectedValues();
			if (selectedValues != null) {
				selectedMappingListModel.addAll(selectedValues);
				availableMappingListModel.removeAll(selectedValues);
			}
		}
	}

	/**
	 * Remove from list of selected mapping
	 * 
	 * @author arya
	 * @email arya.adriansyah@gmail.com
	 * @version Mar 19, 2009
	 */
	private class RemoveListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Object[] selectedValues = lstSelectedNodeMapping.getSelectedValues();
			if (selectedValues != null) {
				availableMappingListModel.addAll(selectedValues);
				selectedMappingListModel.removeAll(selectedValues);
			}
		}
	}

	/**
	 * Update mapping and label of a node
	 * 
	 * @author arya
	 * @email arya.adriansyah@gmail.com
	 * @version Mar 19, 2009
	 */
	private class UpdateListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (selectedGraphElement instanceof SPDNode) {
				SPDNode selectedNode = (SPDNode) selectedGraphElement;

				// update node label
				selectedNode.setLabel(txtFldNodeLabel.getText());

				// update mapping
				relations.remove(selectedNode);

				Set<XEventClass> setEventClass = new HashSet<XEventClass>();
				// add relation related to selected node
				for (Object elements : selectedMappingListModel.getAllElements()) {
					setEventClass.add((XEventClass) elements);
				}
				relations.put(selectedNode, setEventClass);

				viewSpecificMap.putViewSpecific(selectedNode, AttributeMap.LABEL, selectedNode.getLabel());
				viewSpecificMap.putViewSpecific(selectedNode, AttributeMap.TOOLTIP, selectedNode.getLabel());
				viewSpecificMap.putViewSpecific(selectedNode, AttributeMap.SHOWLABEL, !selectedNode.getLabel().equals(
						""));

				// inform how many nodes still needs to be mapped before a connection can be created/edited
				List<String> unmappedNodes = new LinkedList<String>();
				for (SPDNode node : net.getNodes()) {
					if (relations.get(node).size() <= 0) {
						unmappedNodes.add(node.getLabel());
					}
				}
				if (unmappedNodes.size() > 0) {
					String message = "SPD nodes to be mapped : ";
					int size = unmappedNodes.size();
					int counter = 0;

					for (int i = 0; i < unmappedNodes.size(); i++) {
						if (counter == size - 1) {
							message += "[" + unmappedNodes.get(i) + "]";
						} else {
							message += "[" + unmappedNodes.get(i) + "], ";
						}
						counter++;
					}
					wrapLabelText(tobeMappedlbl, message, 1100);
				} else {
					// create collection of pair
					Collection<Pair<SPDNode, XEventClass>> newCol = new LinkedList<Pair<SPDNode, XEventClass>>();
					for (SPDNode node : relations.keySet()) {
						for (XEventClass eventClass : relations.get(node)) {
							Pair<SPDNode, XEventClass> newPair = new Pair<SPDNode, XEventClass>(node, eventClass);
							newCol.add(newPair);
						}
					}

					// create new connection
					context.getConnectionManager().addConnection(new LogSPDConnection(log, classes, net, newCol));
					JOptionPane.showMessageDialog(new JPanel(),
							"All SPD node are successfully mapped to Event Class(es)", "Information",
							JOptionPane.INFORMATION_MESSAGE);

					// unenabled all buttons
					setEditable(false);

					// set text
					instructionLbl.setText("Instruction : Select a node from diagram in order to view its mapping.");
					tobeMappedlbl.setText("");
				}

				// refresh the figure
				
			}
		}
	}

	/**
	 * Private method to wrap text in a label
	 * 
	 * @param label
	 * @param text
	 * @param containerWidth
	 */
	private void wrapLabelText(JLabel label, String text, int containerWidth) {
		FontMetrics fm = label.getFontMetrics(label.getFont());

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			String word = text.substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);
				real.append("<br/>");
			}
			real.append(word);
		}

		real.append("</html>");

		label.setText(real.toString());
	}

	/**
	 * Class to represent list model using set as internal data structure
	 * 
	 * @author arya
	 * @email arya.adriansyah@gmail.com
	 * @version Mar 19, 2009
	 */
	class SortedListModel extends AbstractListModel {
		private static final long serialVersionUID = -521463432733745622L;
		private final SortedSet<Object> model = new TreeSet<Object>();

		public void addAll(Object elements[]) {
			Collection<Object> c = Arrays.asList(elements);
			model.addAll(c);
			fireContentsChanged(this, 0, getSize());
		}

		public void addAll(List<Object> elements) {
			model.addAll(elements);
			fireContentsChanged(this, 0, getSize());
		}

		public void add(Object element) {
			model.add(element);
			fireContentsChanged(this, 0, getSize());
		}

		public void removeAll(Object elements[]) {
			Collection<Object> c = Arrays.asList(elements);
			model.removeAll(c);
			fireContentsChanged(this, 0, getSize());
		}

		public void removeAllElements() {
			model.clear();
		}

		public Object getElementAt(int index) {
			if (index < model.size()) {
				return model.toArray()[index];
			} else {
				return model.toArray()[model.size() % index];
			}
		}

		public Set<Object> getAllElements() {
			return model;
		}

		public int getSize() {
			return model.size();
		}

		public boolean contains(Object object) {
			return model.contains(object);
		}

	}

}
