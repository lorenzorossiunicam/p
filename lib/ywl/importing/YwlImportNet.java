package org.processmining.plugins.ywl.importing;

import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import org.jgraph.graph.AttributeMap.SerializablePoint2D;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.framework.abstractplugins.AbstractImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.MultipleAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLPort;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.interfaceE.YLogGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceLogGatewayClient;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
@Plugin(name = "YAWL specification", 
		parameterLabels = { "Filename" }, 
		returnLabels = { "Yawl Net" }, 
		returnTypes = { NetGraph.class }
)
@UIImportPlugin(description = "Yawl specification", extensions = { "yawl" })
public class YwlImportNet extends AbstractImportPlugin {
	
	private NetGraph[] model;
	private int maxSubnets = 100;
	
	private InputCondition inputCondition;
	private OutputCondition outputCondition;
	
	private HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
	private HashSet<Condition> conditions = new HashSet<Condition>();
	private HashSet<AtomicTask> atomicTasks = new HashSet<AtomicTask>();
	private HashSet<MultipleAtomicTask> multipleAtomicTasks = new HashSet<MultipleAtomicTask>();
	private HashSet<CompositeTask> compositeTasks = new HashSet<CompositeTask>();
	private HashSet<YAWLTask> tasksWithCancellationSets = new HashSet<YAWLTask>();
	
	public boolean bSubnet = false;

	protected Object importFromStream(PluginContext context, InputStream input, String filename, long fileSizeInBytes)
	throws Exception {

		// retrieve absolute path opened file
		String location = this.getFile().getPath();
		
		// import file in YAWL
		YAWLEngineProxy.getInstance().engineFormatFileImport(location);
		
		// retrieve graph from import
		NetGraph graph = YAWLEditorDesktop.getInstance().getSelectedGraph();
		
		// TEST purposes: get the accompanying log from the engine
		String specIdentifier = SpecificationModel.getInstance().getUniqueID();
		String specURIFull = SpecificationModel.getInstance().getId();
		String specURI = specURIFull.substring(0, specURIFull.length() - 5);
		String specVersion = SpecificationModel.getInstance().getVersionNumber().getVersion();

		String EngineXESLog = getEngineLog(specIdentifier, specVersion, specURI);
		String ResServiceXESLog = getResServiceLog(specIdentifier, specVersion, specURI);
		String MergedXESLog = getMergedLog(specIdentifier, specVersion, specURI);
		
		// write logs to a file
		
		writeEngineXESLogToFile(EngineXESLog, specURI);
		writeResServiceXESLogToFile(ResServiceXESLog, specURI);
		writeMergedXESLogToFile(MergedXESLog, specURI);
		
		this.model = new NetGraph[maxSubnets];		
	    this.model[0] = graph;
	    parseModel(this.model[0], false); 
	    setGraphProperties(this.model[0], this.bSubnet);
	    fixLabels(this.model);
	    
		return model[0];		
	}
	
	private void writeMergedXESLogToFile(String log, String uri) {
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("logs/" + uri + "MergedLog.xes"));
			out.write(log);
			out.close();
		}
		catch (IOException e)		{
			System.out.println("Exception when writing Engine log ");
		}	
	}

	private void writeResServiceXESLogToFile(String log, String uri) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("logs/" + uri + "ResServiceLog.xes"));
			out.write(log);
			out.close();
		}
		catch (IOException e)		{
			System.out.println("Exception when writing Engine log ");
		}		
	}

	private void writeEngineXESLogToFile(String log, String uri) {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("logs/" + uri + "EngineLog.xes"));
			out.write(log);
			out.close();
		}
		catch (IOException e)		{
			System.out.println("Exception when writing Engine log ");
		}	
	}

	private void fixLabels(NetGraph[] model) {
		
	    // fix label positions
		int j = 0;
		while (model[j] != null) {
			Object[] lCells = model[j].getRoots(); 
			for (int i=0; i < lCells.length; i++) {
				model[j].setElementLabelInsideUpdate((GraphCell) lCells[i], model[j].getElementLabel((GraphCell) lCells[i])); 
			}
			j++;
		}	
	}
	
	@SuppressWarnings("unchecked")
	public void setGraphProperties(NetGraph net, boolean bSubnet) {
		
		// set JGraph graph properties and remove YAWL net functionality	
		net.setGridVisible(false);	
		net.setEditable(false);
		net.setMoveable(true);
		net.setConnectable(false);
		net.setDisconnectable(false);
		net.setSizeable(false);
		net.getGraphLayoutCache().setMovesChildrenOnExpand(true);
		net.getGraphLayoutCache().setResizesParentsOnCollapse(true);
		net.getGraphLayoutCache().setMovesParentsOnCollapse(true);
		net.getGraphLayoutCache().setAutoSizeOnValueChange(true);
		net.setSelectionEnabled(true);
		
		// redo lay-out; elements from subnets might've been be added to net and messed up the lay-out
		
		if (bSubnet) {
			JGraphFacade facade = new JGraphFacade(net); // Pass the facade the JGraph instance
			JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout(); // Create an instance of the appropriate layout
			facade.setOrdered(true);
			facade.setEdgePromotion(true);
			facade.setDirected(false);
			//facade = fixParallelEdges(facade, 15);
			layout.setDeterministic(false);
			layout.setCompactLayout(false);
			layout.setFineTuning(true);
			layout.setParallelEdgeSpacing(20);
			layout.setOrientation(SwingConstants.WEST);
			layout.run(facade); // Run the layout on the facade. Note that layouts do not implement the Runnable interface, to avoid confusion
			Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
			net.getGraphLayoutCache().edit(nested); // Apply the results to the actual graph
		}
			
	    net.repaint();   
		
		MouseListener[] listeners = net.getMouseListeners(); 
		
		/* Listeners:
		 * 0 BasicGraphUI$MouseHandler
		 * 1 NetPopupListener
		 * 2 ElementDoubleClickListener
		 * 3 ElementControlClickListener
		 * 4 ToolTipManager
		 */
		
		for (int i =0; i < listeners.length;i++) {
			if (listeners[i] instanceof org.yawlfoundation.yawl.editor.net.NetPopupListener) {
				net.removeMouseListener(listeners[i]);
				NetPopupListener newPopup = new NetPopupListener(net);
				net.addMouseListener(newPopup);
			}
			if (i == 2) {
				net.removeMouseListener(listeners[i]);	
			}
		}
		
		// TODO: remove ToolTipManager (only used for debugging purposes...)
		
		// update new net properties in list of NetGraphs
		for (int i = 0; true ; i++) {
			if (this.model[i].getName() == net.getName()) {
				this.model[i] = net;
				break;
			}
		}
	}
	
	private void parseModel(NetGraph model, boolean subnet) {
		// only do this once, start from main net and unfold composite task
		
		// TODO create separate sets for multiple atomic/composite tasks
	    Object[] cells = model.getRoots();    
	    for (int i=0; i < cells.length; i++) {
	      if (cells[i] instanceof VertexContainer) {
	         cells[i] = ((VertexContainer) cells[i]).getVertex(); 
	      }
	      if (cells[i] instanceof InputCondition && !subnet) {
	        inputCondition = (InputCondition) cells[i];
	      }
	      if (cells[i] instanceof OutputCondition && !subnet) {
	        outputCondition = (OutputCondition) cells[i];
	      }
	      if (cells[i] instanceof Condition) {
	        conditions.add((Condition) cells[i]);
	      }
	      if (cells[i] instanceof AtomicTask) {
	        atomicTasks.add((AtomicTask) cells[i]);
	        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
	          tasksWithCancellationSets.add((YAWLTask) cells[i]);
	        }
	      }
	      if (cells[i] instanceof MultipleAtomicTask) {
	    	  multipleAtomicTasks.add((MultipleAtomicTask) cells[i]);
		      if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
		    	  tasksWithCancellationSets.add((YAWLTask) cells[i]);
			  }
	      }
	      if (cells[i] instanceof CompositeTask ||
	          cells[i] instanceof MultipleCompositeTask ) {
	        //compositeTasks.add((CompositeTask) cells[i]);
	        
	        // recursively add the elements for the subnet
	        NetGraph subgraph = null;
	        String subnetName = null;
	        if (cells[i] instanceof CompositeTask) {
	        	subnetName = ((CompositeTask) cells[i]).getUnfoldingNetName();
	        }
	        if (cells[i] instanceof MultipleCompositeTask) {
	        	subnetName = ((MultipleCompositeTask) cells[i]).getUnfoldingNetName();
	        }
	    	int k = 0;
	    	while (YAWLEditorDesktop.getInstance().getSelectedGraph() != null) {
	    	     if (YAWLEditorDesktop.getInstance().getTitleAt(k) == subnetName) {
	    	    	 YAWLEditorDesktop.getInstance().setSelectedIndex(k);
	    	    	 subgraph = YAWLEditorDesktop.getInstance().getSelectedGraph();
	    	    	 break;
	    	     } else {
	    	    	 k++;
	    	     }
	    	}
	    	parseModel(subgraph, true);
	    	// end 'recursion'
	    	
	        if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
	          tasksWithCancellationSets.add((YAWLTask) cells[i]);
	        }
	      }
	      if (cells[i] instanceof YAWLFlowRelation) {
	        flows.add((YAWLFlowRelation) cells[i]);        
	      }
	      
//	      if (subnet) {
//	  		MouseListener[] listeners = model.getMouseListeners(); 
//	  		for (int x =0; x < listeners.length;x++) {
//				if (listeners[x] instanceof org.yawlfoundation.yawl.editor.net.NetPopupListener) {
//					model.removeMouseListener(listeners[x]);
//					NetPopupListener newPopup = new NetPopupListener(model);
//					model.addMouseListener(newPopup);
//				}
//				if (x == 2) {
//					model.removeMouseListener(listeners[x]);	
//				}
//			}
//	      }
	    }
	  }

	  public NetGraph getModel(int i) {
	    return this.model[i];
	  }

	  public InputCondition getInputCondition() {
	    return this.inputCondition;
	  }

	  public OutputCondition getOutputCondition() {
	    return this.outputCondition;
	  }
	  
	  public Set<Condition> getConditions() {
	    return this.conditions;
	  }
	  
	  public Set<AtomicTask> getAtomicTasks() {
	    return this.atomicTasks;
	  }

	  public Set<CompositeTask> getCompositeTasks() {
	    return this.compositeTasks;
	  }
	  
	  public Set<YAWLFlowRelation> getFlows() {
	    return this.flows;
	  }
	  
	  public Set<YAWLTask> getTasksWithCancellationSets() {
	    return this.tasksWithCancellationSets;
	  }
	  
	  @SuppressWarnings("unchecked")
	private JGraphFacade fixParallelEdges(JGraphFacade facade, double spacing) {
			ArrayList edges = new ArrayList(facade.getEdges());
			for (Object edge : edges) {
				List points = facade.getPoints(edge);
				if (points.size() != 2) {
					continue;
				}
				Object sourceCell = facade.getSource(edge);
				Object targetCell = facade.getTarget(edge);
				Object sourcePort = facade.getSourcePort(edge);
				Object targetPort = facade.getTargetPort(edge);
				Object[] between = facade.getEdgesBetween(sourcePort, targetPort, false);
				if ((between.length == 1) && !(sourcePort == targetPort)) {
					continue;
				}
				Rectangle2D sCP = facade.getBounds(sourceCell);
				Rectangle2D tCP = facade.getBounds(targetCell);
				Point2D sPP = GraphConstants.getOffset(((YAWLPort) sourcePort).getAttributes());
				// facade. getBounds (sourcePort ) ;

				if (sPP == null) {
					sPP = new Point2D.Double(sCP.getCenterX(), sCP.getCenterY());
				}
				Point2D tPP = GraphConstants.getOffset(((YAWLPort) targetPort).getAttributes());
				// facade.getBounds(sourcePort);

				if (tPP == null) {
					tPP = new Point2D.Double(tCP.getCenterX(), tCP.getCenterY());
				}

				if (sourcePort == targetPort) {
					assert (sPP.equals(tPP));
					double x = sPP.getX();
					double y = sPP.getY();
					for (int i = 2; i < between.length + 2; i++) {
						List newPoints = new ArrayList(5);
						newPoints.add(new Point2D.Double(x - (spacing + i * spacing), y));
						newPoints.add(new Point2D.Double(x - (spacing + i * spacing), y - (spacing + i * spacing)));
						newPoints.add(new Point2D.Double(x, y - (2 * spacing + i * spacing)));
						newPoints.add(new Point2D.Double(x + (spacing + i * spacing), y - (spacing + i * spacing)));
						newPoints.add(new Point2D.Double(x + (spacing), y - (spacing / 2 + i * spacing)));
						facade.setPoints(between[i - 2], newPoints);
					}

					continue;
				}

				double dx = (sPP.getX()) - (tPP.getX());
				double dy = (sPP.getY()) - (tPP.getY());
				double mx = (tPP.getX()) + dx / 2.0;
				double my = (tPP.getY()) + dy / 2.0;
				double slope = Math.sqrt(dx * dx + dy * dy);
				for (int i = 0; i < between.length; i++) {
					List newPoints = new ArrayList(3);
					double pos = 2 * i - (between.length - 1);
					if (facade.getSourcePort(between[i]) == sourcePort) {
						newPoints.add(sPP);
						newPoints.add(tPP);
					} else {
						newPoints.add(tPP);
						newPoints.add(sPP);
					}
					if (pos != 0) {
						pos = pos / 2;
						double x = mx + pos * spacing * dy / slope;
						double y = my - pos * spacing * dx / slope;
						newPoints.add(1, new SerializablePoint2D.Double(x, y));
					}
					facade.setPoints(between[i], newPoints);
				}
			}
			return facade;
		}
	  
	  public void updateSubnet(NetGraph subnet) {
		  
	  }
	  
	    public String getEngineLog(String specIdentifier, String specVersion, String specURI) {
	    	
//	        String specIdentifier = "UID_d20f1e77-2713-4db5-a558-15717da012e9";
//	        String specVersion = "0.38";
//	        String specURI = "ProcessGiveMoney";
	     
	        YLogGatewayClient logClient = new YLogGatewayClient("http://localhost:8080/yawl/logGateway") ;
	        YSpecificationID specID = new YSpecificationID(specIdentifier, specVersion, specURI);
	        try {
	            String handle = logClient.connect("admin", "YAWL");
	            String xml = logClient.getSpecificationXESLog(specID, handle);
	            System.out.println(xml);
	            return xml;
	        }
	        catch (IOException ioe) {
	            return "io exception";
	        }
	    }
	    
	    public String getResServiceLog(String specIdentifier, String specVersion, String specURI) {
//	        String specIdentifier = "UID_d20f1e77-2713-4db5-a558-15717da012e9";
//	        String specVersion = "0.38";
//	        String specURI = "ProcessGiveMoney";
	     
	        String resURL = "http://localhost:8080/resourceService/logGateway";
	        ResourceLogGatewayClient resClient = new ResourceLogGatewayClient(resURL);
	        try {
	            String handle = resClient.connect("admin", "YAWL");
	            String xml = resClient.getSpecificationXESLog(specIdentifier, specVersion, specURI, handle);
	            System.out.println(xml);
	            return xml;
	        }
	        catch (IOException ioe) {
	            return "io exception";
	        }
	    }
	    
	    public String getMergedLog(String specIdentifier, String specVersion, String specURI) {
//	        String specIdentifier = "UID_d20f1e77-2713-4db5-a558-15717da012e9";
//	        String specVersion = "0.38";
//	        String specURI = "ProcessGiveMoney";
	     
	        String resURL = "http://localhost:8080/resourceService/logGateway";
	        ResourceLogGatewayClient resClient = new ResourceLogGatewayClient(resURL);
	        try {
	            String handle = resClient.connect("admin", "YAWL");
	            String xml = resClient.getMergedXESLog(specIdentifier, specVersion, specURI, handle);
	            System.out.println(xml);
	            return xml;
	        }
	        catch (IOException ioe) {
	            return "io exception";
	        }
	    }



	  
	  

}


