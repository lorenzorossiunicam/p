package org.processmining.plugins.NoiseGenerator;

import java.util.Random;

import javax.swing.JOptionPane;

import nl.tue.astar.AStarException;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.finalmarkingprovider.MarkingEditorPanel;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.utils.ProvidedObjectHelper;

import com.fluxicon.slickerbox.components.NiceDoubleSlider;
import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class Noise {


	public XLog plugin(UIPluginContext context, XLog log)
	{
		return plugin(context,log,null);
	}
	

	@Plugin(name = "Add noise to an event log",
			parameterLabels = { "Log", "Petri Net" },
			returnLabels = { "XLog"},
			returnTypes = { XLog.class },
			userAccessible = true,
			help = "Add noise to an event log. Each trace is guaranteed to be not replayed on the process model given as input")
			@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M. de Leoni", email = "m.d.leoni@tue.nl")
	public XLog plugin(UIPluginContext context, XLog log, PetrinetGraph model) 
	{
		MyPropertyPanel panel=new MyPropertyPanel("");
		NiceDoubleSlider swapSlider = SlickerFactory.instance().createNiceDoubleSlider("", 0, 0.8, 0.3,
				Orientation.HORIZONTAL);
		NiceDoubleSlider addSlider = SlickerFactory.instance().createNiceDoubleSlider("", 0, 0.8, 0.3,
				Orientation.HORIZONTAL);
		NiceDoubleSlider removeSlider = SlickerFactory.instance().createNiceDoubleSlider("", 0, 0.8, 0.3,
				Orientation.HORIZONTAL);
		NiceIntegerSlider bucketSlider=SlickerFactory.instance().createNiceIntegerSlider("", 1, 10, 3, Orientation.HORIZONTAL);
		//panel.addProperty("The following parameters can be configured. Pass over the component for tooltip.", null);
		panel.addProperty("Maximum Swap Probability", swapSlider);
		swapSlider.setToolTipText("The probability of any event to be swapped with the subsequent.");
		panel.addProperty("Maximum Add Probability", addSlider);
		addSlider.setToolTipText("The probability of replacing any event for any activity A with two subsequent events for activity A (i.e. an event for A is added");
		panel.addProperty("Maximum Remove Probability", removeSlider);
		addSlider.setToolTipText("The probability of removing any event");
		panel.addProperty("Number of Buckets", bucketSlider);		
		addSlider.setToolTipText("The number N of buckets in which the event log is split. For the first bucket, the probabilities above are "
				+ "multiplied by 1/N; for the second bucket, they are multiplied by 2/N; etc. until the last bucket where they are multiplied by N/N (i.e. "
				+ "they are not reduced)");
		
		InteractionResult result = context.
				showConfiguration("Specify the probability (between 1 and 99) of an event to be removed", panel);
		if (result==null)
		{
			context.getFutureResult(0).cancel(true);
			return null;
		}
		double swapProb=swapSlider.getValue();
		double addProb=addSlider.getValue();
		double removeProb=removeSlider.getValue();
		int numsBuckets=bucketSlider.getValue();
		return addNoise(context,log,model,swapProb,addProb,removeProb,numsBuckets);
	}
	
	/**
	 * Plug-in engine with GUI
	 * @param log The original event log to which to add noise
	 * @param model The model against which each manipulated trace is checked. Every trace is such that it deviates from this model.
	 * @param swapProb The probability of an event to be swapped with the subsequent.
	 * @param addProb The probability of replacing any event for any activity A with two subsequent events for activity A (i.e. an event for A is added).  
	 * @param removeProb The probability of removing any event.
	 * @param numsBuckets The number N of buckets in which the event log is split. For the first bucket, the probabilities above are
	 * multiplied by 1/N; for the second bucket, they are multiplied by 2/N; etc. until the last bucket where they are multiplied by N/N (i.e.\
	 * they are not reduced)
	 * @return
	 */
	public XLog addNoise(PluginContext context,XLog log,PetrinetGraph model, double swapProb, double addProb, double removeProb, int numsBuckets) 
	{
		XLog newLog=XFactoryRegistry.instance().currentDefault().createLog((XAttributeMap) log.getAttributes().clone());
		context.getProgress().setMaximum(log.size());
		context.getProgress().setValue(0);
		Random randomGenerator=new Random();
		int counter=0;
		int logSize=log.size();

		for(XTrace originalTrace : log)
		{
			XTrace newTrace;
			int buckNum=(counter++)/(logSize/numsBuckets)+1;
			float shadingError=(((float)buckNum)/numsBuckets);
			int attempt=0;

			XTrace trace=originalTrace;
			try {
				newTrace=XFactoryRegistry.instance().currentDefault().createTrace((XAttributeMap) originalTrace.getAttributes().clone());
				do
				{					
					//REMOVE EVENT
					for (XEvent event : trace)
					{
						if (randomGenerator.nextDouble()>removeProb*shadingError)
							newTrace.add((XEvent) event.clone());
					}

					//ADD EVENT
					for (int i=0;i<newTrace.size();i++)
						if (randomGenerator.nextDouble()<addProb*shadingError)
							newTrace.add(i, newTrace.get(i));

					for (int i=1;i<newTrace.size();i++)
					{
						double gen=randomGenerator.nextDouble();
						if (gen<swapProb*shadingError)
						{
							XEvent event=newTrace.remove(i);
							newTrace.add(i-1,event);
						}	
					}
					if ((++attempt) % 5 == 0)
					{
						trace=(XTrace) newTrace.clone();
						attempt=0;
					}
				}
				while (model!=null && fitModel(context,log,newTrace,model));
			} catch (AStarException e) {
				throw(new RuntimeException("Problems with checking the conformance: "+e.getMessage()));
			}
			attempt=0;
			newLog.add(newTrace);
			context.getProgress().inc();
		}
		return newLog;
	}

	private boolean fitModel(PluginContext context, XLog log,XTrace trace, PetrinetGraph model) throws AStarException {
		XLog newLog=new XLogImpl((XAttributeMap) log.getAttributes().clone());	
		XEventClass evClassDummy = new XEventClass("DUMMY", -1);
		newLog.add(trace);
		TransEvClassMapping mapping = new TransEvClassMapping(XLogInfoImpl.STANDARD_CLASSIFIER, evClassDummy);
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(newLog);	
		for (XEventClass ec : logInfo.getEventClasses().getClasses()) {
			String activityName=ec.toString().split("\\+")[0];
			Transition transitionObject=null;
			for(Transition t :	model.getTransitions())
			{
				if (t.getLabel().equalsIgnoreCase(activityName))
				{
					transitionObject=t;
					break;
				}
			}
			assert(transitionObject!=null);
			mapping.put(transitionObject, ec);
		}
		
		CostBasedCompleteParam parameter = createParameters(newLog, mapping, model, context);	
		
		PNRepResult result = new PNLogReplayer().replayLog(null, model, newLog, mapping, 
				new PetrinetReplayerWithoutILP(), parameter);
		return(((Double)result.getInfo().get(PNRepResult.TRACEFITNESS))==1);
	}
	
	private static CostBasedCompleteParam createParameters(XLog log, TransEvClassMapping mapping, PetrinetGraph net,
			PluginContext context) {
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, mapping.getEventClassifier());
		CostBasedCompleteParam parameter = new CostBasedCompleteParam(logInfo.getEventClasses().getClasses(),
				mapping.getDummyEventClass(), net.getTransitions(), 1, 1);
		parameter.setGUIMode(false);
		parameter.setCreateConn(false);

		Marking initMarking = null;
		try {
			initMarking = context.getConnectionManager()
					.getFirstConnection(InitialMarkingConnection.class, context, net)
					.getObjectWithRole(FinalMarkingConnection.MARKING);
			if (initMarking.isEmpty()) {
				int yn = JOptionPane.showConfirmDialog(null, "The initial marking is empty. Is this intended?",
						"Initial Marking", JOptionPane.YES_NO_OPTION);
				if (yn == JOptionPane.NO_OPTION) {
					JOptionPane.showMessageDialog(null,
							"Please create the correct initial marking using the appropriate plug-in");
					return null;
				}
			}
		} catch (Exception e) {
			initMarking = createMarking((UIPluginContext) context, net, "Initial Marking");
			ProvidedObjectHelper.publish(context, "Initial Marking for " + net.getLabel(), initMarking, Marking.class,
					false);
			context.getConnectionManager().addConnection(new InitialMarkingConnection(net, initMarking));
		}
		parameter.setInitialMarking(initMarking);

		Marking finalMarking = null;
		try {
			finalMarking = context.getConnectionManager()
					.getFirstConnection(FinalMarkingConnection.class, context, net)
					.getObjectWithRole(FinalMarkingConnection.MARKING);
			if (finalMarking.isEmpty()) {
				int yn = JOptionPane.showConfirmDialog(null, "The final marking is empty. Is this intended?",
						"Final Marking", JOptionPane.YES_NO_OPTION);
				if (yn == JOptionPane.NO_OPTION) {
					JOptionPane.showMessageDialog(null,
							"Please create the correct final marking using the appropriate plug-in");
					return null;
				}
			}

		} catch (Exception e) {
			finalMarking = createMarking((UIPluginContext) context, net, "Final Marking");
			ProvidedObjectHelper.publish(context, "Final Marking for " + net.getLabel(), finalMarking, Marking.class,
					false);
			context.getConnectionManager().addConnection(new FinalMarkingConnection(net, finalMarking));
		}
		parameter.setFinalMarkings(new Marking[] { finalMarking });
		parameter.setMaxNumOfStates(Integer.MAX_VALUE);
		return parameter;
	}
	
	private static Marking createMarking(UIPluginContext context, PetrinetGraph net, String title) {
		MarkingEditorPanel editor = new MarkingEditorPanel(title);

		return editor.getMarking(context, net);
	}	
}
