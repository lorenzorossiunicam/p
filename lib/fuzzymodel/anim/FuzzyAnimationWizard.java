package org.processmining.plugins.fuzzymodel.anim;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.plugins.fuzzymodel.miner.filter.FMEventTimeInjectionFilter;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.RoundedPanel;
import com.fluxicon.slickerbox.ui.SlickerCheckBoxUI;

public class FuzzyAnimationWizard {

	private UIPluginContext context;
	private XLog log;
	private int maxLookAhead; 
	private int maxExtraLookAhead;
	private boolean discreteAnimation = false;
	
	private WizardStep[] wizardSteps;
	int lookAheadStep;
	int currentStep;
	int nofSteps;
	
	public FuzzyAnimationWizard(UIPluginContext context, XLog log) {
		this.context = context;
		this.log = log;

		nofSteps = 0;
		lookAheadStep = nofSteps++;
		wizardSteps = new WizardStep[nofSteps];
		wizardSteps[lookAheadStep] = new LookAheadStep();
		currentStep = lookAheadStep;
	}

	public InteractionResult show() {
		InteractionResult result = InteractionResult.NEXT;
		while (true) {
			if (currentStep < 0) {
				currentStep = 0;
			}
			if (currentStep >= nofSteps) {
				currentStep = nofSteps - 1;
			}
			context.log("Current step: " + currentStep);
			result = context
					.showWizard("Fuzzy Animation", currentStep == 0, currentStep == nofSteps - 1, wizardSteps[currentStep]);
			switch (result) {
				case NEXT :
					wizardSteps[currentStep].readSettings();
					go(1);
					break;
				case PREV :
					go(-1);
					break;
				case FINISHED :
					wizardSteps[currentStep].readSettings();
					return result;
				default :
					return result;
			}
		}
	}
	
	private int go(int direction) {
		currentStep += direction;
		if ((currentStep >= 0) && (currentStep < nofSteps)) {
			if (wizardSteps[currentStep].precondition()) {
				return currentStep;
			} else {
				return go(direction);
			}
		}
		return currentStep;
	}

	public int getLookAhead() {
		return maxLookAhead;
	}
	
	public int getExtraLookAhead() {
		return maxExtraLookAhead;
	}
	
	public boolean getDiscreteAnimation() {
		return discreteAnimation;
	}
	
	private abstract class WizardStep extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2978904268514783898L;

		public abstract boolean precondition();

		public abstract void readSettings();
	}

	private class LookAheadStep extends WizardStep {
		/**
		 * 
		 */
		private static final long serialVersionUID = -5629896729801647063L;

		protected NiceIntegerSlider lookaheadSlider;
		protected NiceIntegerSlider extraLookaheadSlider;
		protected JCheckBox discreteAnimBox;
		
		public LookAheadStep() {
			initComponents();
		}

		public boolean precondition() {
			return true;
		}

		private void initComponents() {
			this.setOpaque(false);
			this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			RoundedPanel innerPanel = new RoundedPanel(20);
			innerPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
			innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
			innerPanel.setBackground(new Color(140, 140, 140));
			innerPanel.setMinimumSize(new Dimension(350, 250));
			innerPanel.setMaximumSize(new Dimension(350, 250));
			innerPanel.setPreferredSize(new Dimension(350, 250));
			discreteAnimBox = new JCheckBox("discrete animation (inject timestamps)");
			discreteAnimation = false;
			if (log != null) {
				try {
					//discreteAnimation = (log.getInstance(0).getAuditTrailEntryList().get(0).getTimestamp() == null);
					XEvent firstEvent = log.get(0).get(0);
					Date evttime;
					if (firstEvent.getAttributes().containsKey(XTimeExtension.KEY_TIMESTAMP))
					{
						evttime = XTimeExtension.instance().extractTimestamp(firstEvent);
					}
					else{
						evttime = null;
						//inject time stamp to the log for replaying
						FMEventTimeInjectionFilter tsFilter = new FMEventTimeInjectionFilter();
						for (int i = 0; i < log.size(); i++) {
							XTrace curTrace = log.get(i);
							tsFilter.doFiltering(curTrace);
						}
					}
					discreteAnimation = (evttime == null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			discreteAnimBox.setAlignmentX(JCheckBox.CENTER_ALIGNMENT);
			discreteAnimBox.setSelected(discreteAnimation);
			discreteAnimBox.setForeground(new Color(40, 40, 40));
			discreteAnimBox.setUI(new SlickerCheckBoxUI());
			discreteAnimBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					discreteAnimation = discreteAnimBox.isSelected();
				}
			});
			// lookahead sliders
			lookaheadSlider = new NiceIntegerSlider("Lookahead", 1, 25, 5);
			lookaheadSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent arg0) {
					int max = lookaheadSlider.getValue() - 1;
					extraLookaheadSlider.getSlider().setMaximum(max);
					if (extraLookaheadSlider.getValue() > max) {
						extraLookaheadSlider.setValue(max);
					}
				}
			});
			extraLookaheadSlider = new NiceIntegerSlider("Extra lookahead", 0, 15, 3);
			// assemble GUI
			innerPanel.add(Box.createVerticalStrut(15));
			innerPanel.add(discreteAnimBox);
			innerPanel.add(Box.createVerticalStrut(15));
			innerPanel.add(lookaheadSlider);
			innerPanel.add(Box.createVerticalStrut(8));
			innerPanel.add(extraLookaheadSlider);
			innerPanel.add(Box.createVerticalStrut(15));
			this.add(Box.createHorizontalGlue());
			this.add(innerPanel);
			this.add(Box.createHorizontalGlue());
		}

		public void readSettings() {
			maxLookAhead = lookaheadSlider.getValue();
			maxExtraLookAhead = extraLookaheadSlider.getValue();
			discreteAnimation = discreteAnimBox.isSelected();
		}
	}

}
