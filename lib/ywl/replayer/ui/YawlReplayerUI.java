package org.processmining.plugins.ywl.replayer.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.yawlnet.YawlNetConnection;
import org.yawlfoundation.yawl.editor.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.net.NetGraph;

/**
* @author David Piessens
* @email d.a.m.piessens@student.tue.nl
* @version May 29, 2010
*/
public class YawlReplayerUI {
	// reference variable
	private UIPluginContext context;
	
	// steps 
	private int nofSteps;
	private int currentStep;
	private int introductionStep;
	private int mappingStep;
	private int algorithmStep;
	private int weightingStep;
	private int explanationStep;
	
	private Object[] vertices;
	
	// gui for each steps
	private ReplayStep[] replaySteps;

	public YawlReplayerUI(final UIPluginContext context, Object[] vertices) {
		this.context = context;
		this.vertices = vertices;
	}
	
	public Object[] getConfiguration (NetGraph yawl, XLog log) {
		// init local parameter
		YawlNetConnection conn = null;
		
		// init steps and gui
		nofSteps = 0;
		introductionStep = nofSteps++;
		
		// check connection in order to determine whether mapping step is needed of not
		try {
			// connection is found, no need for mapping step
			conn = context.getConnectionManager().getFirstConnection(YawlNetConnection.class, context, yawl, log);
			mappingStep = -1;
		} catch (Exception e) {
			// connection is not found, ask user to map each nodes in flexible model to event classes
			mappingStep = nofSteps++;
		}
		
		// other steps
		algorithmStep = nofSteps++;
		//weightingStep = nofSteps++;
		explanationStep = nofSteps++;
		
		// init gui for each step
		replaySteps = new ReplayStep[nofSteps];
		replaySteps[introductionStep] = new IntroductionStep();
		replaySteps[algorithmStep] = new AlgorithmStep();
		//replaySteps[weightingStep] = new WeightingStep();
		replaySteps[explanationStep] = new ExplanationStep();
		if (mappingStep > 0){	// mapping step is needed
			replaySteps[mappingStep] = new MappingStep(yawl, log, vertices);
		}

		// init result variable
		InteractionResult result = InteractionResult.NEXT;
		
		// set current step
		currentStep = introductionStep;		

		// configure interaction with user
		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= nofSteps) {
				currentStep = nofSteps - 1;
			}
			context.log("Current step: " + currentStep);
			result = context.showWizard("Replay Log in YAWL net", currentStep == 0, currentStep == nofSteps - 1, replaySteps[currentStep]);
			switch (result) {
				case NEXT:
					replaySteps[currentStep].readSettings();
					go(1);
					break;
				case PREV:
					go(-1);
					break;
				case FINISHED:
					// mapping variable
					Collection<Pair<YAWLVertex, XEventClass>> mapping;
					
					// create connection (if necessary)
					if (mappingStep > 0){	// need to create connection
						MappingStep mappingStepGUI = (MappingStep) replaySteps[mappingStep];
						
						// create new connection
						mapping = mappingStepGUI.getMapping();
						
						Set<YAWLVertex> cVertices = new HashSet<YAWLVertex>(vertices.length);
						for (int i = 0; i < vertices.length; i++) {
							cVertices.add((YAWLVertex) vertices[i]);
						}
						context.getConnectionManager().addConnection(new YawlNetConnection(log, mappingStepGUI.getEventClasses(), yawl, cVertices, mapping));
					} else {
						mapping = new HashSet<Pair<YAWLVertex,XEventClass>>();
						//Set<VertexContainer> flexNodes = flex.getNodes();
						
						for (Object vertex : this.vertices){
							Pair<YAWLVertex, XEventClass> newPair = new Pair<YAWLVertex, XEventClass>((YAWLVertex) vertex, conn.getActivitiesFor((YAWLVertex) vertex).iterator().next());
							mapping.add(newPair);
						}
					}
					
					// return the mapping, algorithm, and necessary parameters
					//WeightingStep weightingStepGUI = (WeightingStep) replaySteps[weightingStep];
					//int[] weights = weightingStepGUI.getWeights();
					int cancel = ((AlgorithmStep) replaySteps[algorithmStep]).getCancellation();
					
					boolean cancellation = false;
					if (cancel == 1) {
						cancellation = true;
					}
					//return new Object[] {mapping, ((AlgorithmStep) replaySteps[algorithmStep]).getAlgorithm(), weights[0], weights[1], weights[2], cancellation};
					return new Object[] {mapping, ((AlgorithmStep) replaySteps[algorithmStep]).getAlgorithm(), 0, 0, 0, cancellation};
				default:
					return new Object[] { null };
			}
		}
	}
	
	private int go(int direction) {
		currentStep += direction;
		if (currentStep >= 0 && currentStep < nofSteps) {
			if (replaySteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}
}
