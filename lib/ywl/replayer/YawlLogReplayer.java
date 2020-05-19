package org.processmining.plugins.ywl.replayer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.yawlnet.YawlGraphAnimationConnection;
import org.processmining.models.connections.yawlnet.YawlLogReplayResultConnection;
import org.processmining.models.graphbased.directed.yawl.YawlPerformanceResult;
import org.processmining.models.graphbased.directed.yawl.YawlReplayResult;
import org.processmining.models.graphbased.directed.yawl.animation.GraphAnimation;
import org.processmining.models.graphbased.directed.yawlperformancediagram.YPD;
import org.processmining.plugins.ywl.replayer.ui.YawlReplayerUI;
import org.yawlfoundation.yawl.editor.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.CompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.Condition;
import org.yawlfoundation.yawl.editor.elements.model.InputCondition;
import org.yawlfoundation.yawl.editor.elements.model.MultipleAtomicTask;
import org.yawlfoundation.yawl.editor.elements.model.MultipleCompositeTask;
import org.yawlfoundation.yawl.editor.elements.model.OutputCondition;
import org.yawlfoundation.yawl.editor.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
@Plugin(name = "Replay Log in YAWL net", 
		returnLabels = { "YAWL net replay result", "Performance result", "Replay animation on yawl net"}, 
		returnTypes = { YawlReplayResult.class, YawlPerformanceResult.class, GraphAnimation.class }, 
		parameterLabels = { "YAWL net", "Event Log" }, 
		help = "Replay log in YAWL net.", 
		userAccessible = true)
public class YawlLogReplayer {
	
	private int nrOfVertices;
	private int index = 0;
	
	private InputCondition inputCondition;
	private OutputCondition outputCondition;
	private Object[] vertices;
	
	private HashSet<YAWLFlowRelation> flows = new HashSet<YAWLFlowRelation>();
	private HashSet<Condition> conditions = new HashSet<Condition>();
	private HashSet<AtomicTask> atomicTasks = new HashSet<AtomicTask>();
	private HashSet<MultipleAtomicTask> multipleAtomicTasks = new HashSet<MultipleAtomicTask>();
	private HashSet<CompositeTask> compositeTasks = new HashSet<CompositeTask>();
	private HashSet<MultipleCompositeTask> multipleCompositeTasks = new HashSet<MultipleCompositeTask>();
	private HashSet<YAWLTask> tasksWithCancellationSets = new HashSet<YAWLTask>();
	
	@SuppressWarnings("unchecked")	// this is needed because we cast configuration result
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "David Piessens", email = "d.a.m.piessens@student.tue.nl")
	@PluginVariant(variantLabel = "From YAWL net and Log", requiredParameterLabels = {0, 1 })
	public Object[] replayLog(final UIPluginContext context, NetGraph yawl, XLog log) throws Exception {
		
		// retrieve all vertices and edges from the yawl net (incl subnets)
		nrOfVertices = 0;
		
		parseModel(yawl, false);
		
		vertices = new Object[nrOfVertices];
		createVertexList(yawl, false);
		
		// reconnect flows; if flow was connected to ingoing flow of CompositeTask, now connect to
		// outgoing flow of inputcondition in subnet, same for outgoing flow
		parseFlows();
					
		// get configuration values from user
		// checking for connection is performed inside YawlReplayerGUI class
		YawlReplayerUI yawlReplayerUI = new YawlReplayerUI(context, vertices);
 		Object[] resultConfiguration = yawlReplayerUI.getConfiguration(yawl, log);
		
		// if all paramaters are set, replay log
		if (resultConfiguration[0] != null){
			context.log("replay is performed. All parameters are set.");
			
			// we need model, log, mapping, weight of missing tokens, remaining tokens, and heuristic distance in order to replay things
			// try to convert type first
			Collection <Pair<YAWLVertex, XEventClass>> mapping = (Collection <Pair<YAWLVertex, XEventClass>>) resultConfiguration[0];
			return replayLogPrivate(context, yawl, log, vertices, flows, mapping,  (IYwlLogReplayAlgorithm) resultConfiguration[1], 
					(Integer) resultConfiguration[2], (Integer) resultConfiguration[3], (Integer) resultConfiguration[4],
					(Boolean) resultConfiguration[5]);
		} else {
			context.log("replay is not performed because not enough parameter is submitted");
			context.getFutureResult(0).cancel(true);
			context.getFutureResult(1).cancel(true);
			context.getFutureResult(2).cancel(true);
			return null;
		}
	}

	private Object[] replayLogPrivate(UIPluginContext context, NetGraph yawl, XLog log, Object[] vertices, HashSet<YAWLFlowRelation> flows, 
			Collection<Pair<YAWLVertex, XEventClass>> mapping, IYwlLogReplayAlgorithm iYwlLogReplayAlgorithm,
			int misTokenWeight, int remTokenWeight, int heurDistanceWeight, boolean cancellationOption) {
		long startComputation = System.currentTimeMillis();

		// progress update
		Progress progress = context.getProgress();
		progress.setMinimum(0);
		progress.setMaximum(log.size() + 4);
		progress.setIndeterminate(false);

		// for each trace, replay according to the algorithm
		Object[] replayRes = iYwlLogReplayAlgorithm.replayLog(context, yawl, log, vertices, flows, mapping, misTokenWeight,
				                    remTokenWeight, heurDistanceWeight, cancellationOption); 

		// split the result into variables
		YPD ypd = (YPD) replayRes[0];
		YawlReplayResult yawlReplayResult = new YawlReplayResult(ypd);
		YawlPerformanceResult yawlPerformanceResult = (YawlPerformanceResult) replayRes[1];
		GraphAnimation graphAnimation = (GraphAnimation) replayRes[2]; 
		
		// create connection
		context.getConnectionManager().addConnection(new YawlLogReplayResultConnection("Connection between log " + XConceptExtension.instance().extractName(log) + " and yawl net " + yawl.getName() + " and all replay result", 
				yawl, yawlPerformanceResult, yawlReplayResult));
		context.getConnectionManager().addConnection(new YawlGraphAnimationConnection("Connection between yawl net " + yawl.getName() + " and its graph animation",
				yawl, graphAnimation));
		
		// output time spent for calculation
		long endComputation = System.currentTimeMillis();

		// set label
		context.log("Performance calculation is performed in " + ((endComputation - startComputation) / (double) 1000)
				+ "seconds", MessageLevel.DEBUG);
		
		context.getFutureResult(0).setLabel("Result of replaying log " + XConceptExtension.instance().extractName(log) + " on yawl net " + yawl.getName());
		context.getFutureResult(1).setLabel("Performance of " + XConceptExtension.instance().extractName(log) + " on yawl net " + yawl.getName());
		context.getFutureResult(2).setLabel("Replay animation of " + XConceptExtension.instance().extractName(log) + " on yawl net " + yawl.getName());
		
		Object[] resultObjects = new Object[] { yawlReplayResult, yawlPerformanceResult, graphAnimation};
		
		return resultObjects;
	}
	
	private void parseFlows() {
		
		// Reconnect incoming flows for composite tasks
			for (int i=0; i < vertices.length; i++) {
				if (vertices[i] instanceof VertexContainer) {
					vertices[i] = ((VertexContainer) vertices[i]).getVertex(); 
				}
				Set<YAWLFlowRelation> incomingFlows = new HashSet<YAWLFlowRelation>(((YAWLVertex) vertices[i]).getIncomingFlows().size()); 
				incomingFlows =	((YAWLVertex) vertices[i]).getIncomingFlows();
				
				for (YAWLFlowRelation inFlow : incomingFlows) {

					if (inFlow.getSource() instanceof YAWLFlowRelation) {
			    		//inFlow.setSource(((YAWLFlowRelation) inFlow.getSource()).getSourceVertex());
						inFlow.setSource(((YAWLFlowRelation) inFlow.getSource()).getSourceVertex());
					}
					else if (inFlow.getSourceVertex() instanceof CompositeTask) {
						
						String subnetName = ((CompositeTask) inFlow.getSourceVertex()).getUnfoldingNetName();
						NetGraph subgraph = null;
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
						Object[] cells = subgraph.getRoots();
						for (int j=0; j < cells.length; j++) {
							if (cells[j] instanceof VertexContainer) {		     
								cells[j] = ((VertexContainer) cells[j]).getVertex();
							}
					    	if (cells[j] instanceof OutputCondition) {
								Set<YAWLFlowRelation> incomingFlowsSub = new HashSet<YAWLFlowRelation>(((OutputCondition) cells[j]).getIncomingFlows().size()); 
								incomingFlowsSub =((OutputCondition) cells[j]).getIncomingFlows();
								if (incomingFlowsSub.size() >= 1) {
									// connect to output condition
									for (YAWLFlowRelation incomingSub : incomingFlowsSub) {
										inFlow.setSource(((YAWLFlowRelation) incomingSub).getSource());
										break;
									}
								}
//					    		YAWLFlowRelation subnetFlow = ((OutputCondition) cells[j]).getOnlyIncomingFlow();		    	
//					    		inFlow.setSource(subnetFlow.getSource());
					    	}
						}
					}
				}
			}
			for (int i=0; i < vertices.length; i++) {
				if (vertices[i] instanceof VertexContainer) {
					vertices[i] = ((VertexContainer) vertices[i]).getVertex(); 
				}
				Set<YAWLFlowRelation> outgoingFlows = new HashSet<YAWLFlowRelation>(((YAWLVertex) vertices[i]).getOutgoingFlows().size()); 
				outgoingFlows =	((YAWLVertex) vertices[i]).getOutgoingFlows();
				
				for (YAWLFlowRelation outFlow : outgoingFlows) {
					if (outFlow.getTargetVertex() instanceof CompositeTask) {
						
						String subnetName = ((CompositeTask) outFlow.getTargetVertex()).getUnfoldingNetName();
						NetGraph subgraph = null;
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
						Object[] cells = subgraph.getRoots();
						for (int j=0; j < cells.length; j++) {
							if (cells[j] instanceof VertexContainer) {		     
								cells[j] = ((VertexContainer) cells[j]).getVertex();
							}
					    	if (cells[j] instanceof InputCondition) {
					    		YAWLFlowRelation subnetFlow = ((InputCondition) cells[j]).getOnlyOutgoingFlow();
					    		outFlow.setTarget(subnetFlow.getTarget());
					    	}
						}
					}
				}
			}
			
	}
							
	private void parseModel(NetGraph model, boolean subnet) {
		// only do this once, start from main net and unfold composite task
				
		// TODO create separate sets for multiple atomic/composite tasks
	    Object[] cells = model.getRoots();  
	    for (int i=0; i < cells.length; i++) {
	      //if (cells[i] instanceof VertexContainer) {
	      if (cells[i] instanceof VertexContainer) {		     
	         cells[i] = ((VertexContainer) cells[i]).getVertex(); 
	         if ((!(cells[i] instanceof CompositeTask) && !(cells[i] instanceof MultipleCompositeTask)) &&
	           (!((cells[i] instanceof InputCondition || cells[i] instanceof OutputCondition) && subnet))) {
	        	 nrOfVertices++;
	         }
	      } else if ((cells[i] instanceof InputCondition || cells[i] instanceof OutputCondition) && !subnet) {
	    	  nrOfVertices++;
	      } else if (cells[i] instanceof Condition) {
	    	  nrOfVertices++;
	      } 
	      
	      if (cells[i] instanceof OutputCondition && subnet &&
	    		    ((OutputCondition) cells[i]).getIncomingFlows().size() > 1) {
	    	  nrOfVertices++;
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
	      if (cells[i] instanceof MultipleAtomicTask ) {
	    	  multipleAtomicTasks.add((MultipleAtomicTask) cells[i]);
		      if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
		    	  tasksWithCancellationSets.add((YAWLTask) cells[i]);
		      }
		  }
	      if (cells[i] instanceof CompositeTask ||
	          cells[i] instanceof MultipleCompositeTask ) {
	        
	        
	        // recursively add the elements for the subnet
	        NetGraph subgraph = null;
	        
	        String subnetName = null;
	        if (cells[i] instanceof CompositeTask) {
	        	compositeTasks.add((CompositeTask) cells[i]);
	        	subnetName = ((CompositeTask) cells[i]).getUnfoldingNetName();	
	        }
	        if (cells[i] instanceof MultipleCompositeTask) {
	        	multipleCompositeTasks.add((MultipleCompositeTask) cells[i]);
	        	subnetName = ((CompositeTask) cells[i]).getUnfoldingNetName();	
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
	      if (cells[i] instanceof YAWLFlowRelation &&
	    	 !((subnet && ((YAWLFlowRelation) cells[i]).getSourceVertex() instanceof InputCondition) ||
	    	  (subnet && ((YAWLFlowRelation) cells[i]).getTargetVertex() instanceof OutputCondition))) {
	        flows.add((YAWLFlowRelation) cells[i]);        
	      }
	    }
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
	  
	  public void createVertexList(NetGraph net, boolean subnet) {
		    Object[] cells = net.getRoots();  
		    for (int i=0; i < cells.length; i++) {
		      if (cells[i] instanceof VertexContainer) {
		         cells[i] = ((VertexContainer) cells[i]).getVertex(); 
		      }
		      if (cells[i] instanceof InputCondition && !subnet) {
		    	  
		    	  vertices[index] = cells[i];
		    	  index++;
		      }
		      if (cells[i] instanceof OutputCondition && !subnet) {
		    	  
		    	  vertices[index] = cells[i];
		    	  index++;
		      }
		      
		      if (cells[i] instanceof OutputCondition && subnet &&
		          ((OutputCondition) cells[i]).getIncomingFlows().size() > 1) {
		    	  
		    	  vertices[index] = cells[i];
		    	  index++;
		      }
		      
		      if (cells[i] instanceof Condition) {
		    	  
		    	  vertices[index] = cells[i];
		    	  index++;
		      }
		      if (cells[i] instanceof AtomicTask ||
		          cells[i] instanceof MultipleAtomicTask ) {
		    	  //TODO: MI
		       	  
		    	  vertices[index] = cells[i];
		    	  index++;
		    	  if (((YAWLTask) cells[i]).getCancellationSet().getSetMembers().size() > 0) {
		    		  tasksWithCancellationSets.add((YAWLTask) cells[i]);
		    	  }
		      }
		      if (cells[i] instanceof CompositeTask ||
		          cells[i] instanceof MultipleCompositeTask ) {
		    	  
		    	  // Don't include subnets in the mapping and YPD
		    	  //vertices[index] = cells[i];
		    	  //index++;
		        
		    	  // recursively add the elements for the subnet
		    	  NetGraph subgraph = null;
		    	  String subnetName = ((CompositeTask) cells[i]).getUnfoldingNetName();	
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
		    	  createVertexList(subgraph, true);
		      }
		    }
	  }
}