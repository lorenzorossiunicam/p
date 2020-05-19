package org.processmining.plugins.psd.ui;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.scalableview.ScalableComponent;

public class PSDReplaceableScalableComponent implements ScalableComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5523244166295449010L;
	private ScalableComponent currentComponent;
	
	boolean patternSelected;
	
	public PSDReplaceableScalableComponent(ScalableComponent component) {
		this.currentComponent = component;
	}

	public JComponent getComponent() {
		return currentComponent.getComponent();
	}

	public double getScale() {
		return currentComponent.getScale();
	}

	public void setScale(double newScale) {
		currentComponent.setScale(newScale);
	}

	public void replaceComponent(ScalableComponent component) {
		currentComponent = component;
	}
	
	public void addUpdateListener(UpdateListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void removeUpdateListener(UpdateListener listener) {
		// TODO Auto-generated method stub
		
	}
}

