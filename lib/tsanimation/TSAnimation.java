package org.processmining.plugins.tsanimation;

import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.models.animation.Animation;
import org.processmining.models.animation.AnimationLog;
import org.processmining.models.animation.EdgeAnimation;
import org.processmining.models.animation.NodeAnimation;
import org.processmining.models.animation.NodeAnimationKeyframe;
import org.processmining.models.animation.TokenAnimation;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphEdge;
import org.processmining.models.graphbased.directed.AbstractDirectedGraphNode;
import org.processmining.models.graphbased.directed.transitionsystem.State;
import org.processmining.models.graphbased.directed.transitionsystem.Transition;
import org.processmining.models.graphbased.directed.transitionsystem.payload.event.EventPayloadTransitionSystem;

public class TSAnimation extends Animation {

	private EventPayloadTransitionSystem ts;

	public TSAnimation(PluginContext context, EventPayloadTransitionSystem ts, XLog log) {
		super(context, ts, log);
		this.ts = ts;
	}

	/**
	 * Creates animations for the given trace. Shows progress using the given
	 * progress bar.
	 * 
	 * @param trace
	 *            The given trace.
	 * @param progress
	 *            The given progress bar.
	 * @throws IndexOutOfBoundsException
	 */
	protected void createAnimations(XTrace trace, Progress progress) throws IndexOutOfBoundsException {
		/*
		 * Create a case animation for this trace.
		 */
		if (trace.isEmpty()) {
			// We're done :-).
			return;
		}
		// Time of first event in trace.
		long start = getEventTime(trace.get(0)).getTime();
		// Time of last event in trace.
		long end = getEventTime(trace.get(trace.size() - 1)).getTime();
		addTokenAnimation(new TokenAnimation(trace, start, end));

		/*
		 * Create state and transition animations for this trace.
		 */
		// Progress counter.
		int counter = 0;
		// Time of previous event in trace. 
		long prevEventTime = -1;
		long lastEndTime = -1;
		for (int i = 0; i < trace.size(); i++) {
			/*
			 * Update progress counter.
			 */
			counter++;
			if (counter > 299) {
				progress.setValue(progress.getValue() + counter);
				counter = 0;
			}

			// Transition corresponding to current (i) event in trace.
			Transition transition = ts.getTransition(trace, i);
			if (transition != null) {
				// Source state of transition.
				State sourceState = transition.getSource();
				// Target state of transition.
				State targetState = transition.getTarget();
				// Time of current event in trace.
				long eventTime = getEventTime(trace.get(i)).getTime();

				/*
				 * The animation has some problems if the start and end time of
				 * transitions coincide. To prevent this, we tweak both the
				 * start and the end time.
				 */
				long startTime = (i > 0 ? prevEventTime : eventTime);
				/*
				 * Start time should be at least the last used end time.
				 * Otherwise, causality might be violated.
				 */
				if (startTime < lastEndTime) {
					startTime = lastEndTime;
				}
				long endTime = eventTime;
				/*
				 * The end time should exceed the start time.
				 */
				if (endTime <= startTime) {
					endTime = startTime + 1;
				}
				/*
				 * Remember the end time.
				 */
				lastEndTime = endTime;

				/*
				 * Create a token animation for the current transition. Use time
				 * of current event as time when case arrives at target state.
				 * Use of time of previous event (if any) as time when case
				 * departs from source state.
				 */
				addTokenAnimation(transition, new TokenAnimation(trace, startTime, endTime));

				/*
				 * Create a state animation for the source state if this is the
				 * first event. Case is always created in this state. Case is
				 * only terminated in this state if trace contains only one
				 * event.
				 */
				if (i == 0) {
					addKeyframe(sourceState, new NodeAnimationKeyframe(trace, startTime, true, trace.size() == 1));
				}

				/*
				 * Create a state animation for the target state. Case is never
				 * created in this state. Case is only terminated in this state
				 * if this event is last event.
				 */
				addKeyframe(targetState, new NodeAnimationKeyframe(trace, endTime, false, i == trace.size() - 1));

				/*
				 * Update the boundaries for the entire animation, if needed.
				 */
				updateBoundaries(eventTime);

				/*
				 * Update previous event time.
				 */
				prevEventTime = eventTime;
			}
		}
		/*
		 * Update progress counter.
		 */
		progress.setValue(progress.getValue() + counter);
		counter = 0;
	}
	
	public void paintNodeBackground(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width, double height) {
		Ellipse2D ellipse = new Ellipse2D.Double(x, y, width, height);
		g2d.fill(ellipse);
	}

	public void paintNodeBorder(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width, double height) {
		Ellipse2D ellipse = new Ellipse2D.Double(x, y, width, height);
		g2d.draw(ellipse);
	}

	public void paintNodeText(AbstractDirectedGraphNode node, Graphics2D g2d, double x, double y, double width, double height) {
		State state = (State) node;
		String label = state.getIdentifier().toString();
		Rectangle2D bound = g2d.getFontMetrics().getStringBounds(label, g2d);
		g2d.drawString(label, (int) (x + (width - bound.getWidth()) / 2) , (int) (y + (height + bound.getHeight()) / 2));
	}
	
	public void paintTokenLabel(AbstractDirectedGraphEdge<?, ?> edge, XTrace trace, Graphics2D g2d, double x, double y) {
		String traceName = AnimationLog.getTraceName(trace);
		Rectangle2D bound = g2d.getFontMetrics().getStringBounds(traceName, g2d);
		g2d.drawString(traceName, (int) x, (int) (y - bound.getHeight() / 2));
		g2d.drawString(edge.getLabel(), (int) x, (int) (y + bound.getHeight() / 2));
	}

	public float getActivity(long modelTime, long maxTaskDelay) {
		float maxActivity = 0;
		float activity = 0;
		for (NodeAnimation anim : getNodeAnimations()) {
			maxActivity++;
			if (anim.getKeyframe(modelTime).getTime() > maxTaskDelay) {
				activity++;
			}
		}
		for (EdgeAnimation anim : getEdgeAnimations()) {
			maxActivity++;
			activity += anim.getKeyframe(modelTime).getTokenAnimations().size();
		}
		return activity / maxActivity;
	}
}
