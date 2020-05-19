package org.processmining.plugins.filtering;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class PCLFilterUI extends JPanel {

	private static final long serialVersionUID = -1404139380497396857L;

	private PCLFilterSettings settings;
	
	private JPanel thresholdsPanel;
	
	private JLabel thresholdTitle;
	private JLabel l1, l2;
	private NiceIntegerSlider t1, t2;

	PCLFilterUI(){
		
		this.settings = new PCLFilterSettings();
		this.init();
	}
	
	PCLFilterUI(PCLFilterSettings settings){
		
		this.settings = settings;
		this.init();
	}
	
	void init(){

		SlickerFactory factory = SlickerFactory.instance();
		
		this.thresholdsPanel = factory.createRoundedPanel(15, Color.gray);
		
		this.thresholdTitle = factory.createLabel("Thresholds");
		this.thresholdTitle.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 18));
		this.thresholdTitle.setForeground(new Color(40,40,40));
		
		this.t1 = factory.createNiceIntegerSlider("", 0, 100, (int) (this.settings.getRelThreshold() * 100), Orientation.HORIZONTAL);
		this.t2 = factory.createNiceIntegerSlider("", 0, 100, this.settings.getAbsThreshold(), Orientation.HORIZONTAL);
		
		this.l1 = factory.createLabel("Relative significance threshold (%)");
		this.l1.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l1.setForeground(new Color(40,40,40));
		this.l2 = factory.createLabel("Absolute significance threshold");
		this.l2.setHorizontalAlignment(SwingConstants.RIGHT);
		this.l2.setForeground(new Color(40,40,40));
		
		this.thresholdsPanel.setLayout(null);
		this.thresholdsPanel.add(this.thresholdTitle);
		this.thresholdsPanel.add(this.l1);
		this.thresholdsPanel.add(this.t1);
		this.thresholdsPanel.add(this.l2);
		this.thresholdsPanel.add(this.t2);

		this.thresholdsPanel.setBounds(0, 20, 670, 140);
		this.thresholdTitle.setBounds(10, 10, 250, 30);
		this.l1.setBounds(20, 50, 200, 20);
		this.l2.setBounds(20, 80, 200, 20);
		this.t1.setBounds(222, 50, 360, 20);
		this.t2.setBounds(222, 80, 360, 20);
		
		this.setLayout(null);
		this.add(this.thresholdsPanel);
		this.validate();
		this.repaint();
	}
	
	void setSettings(PCLFilterSettings settings){ 
		this.t1.setValue((int) (settings.getRelThreshold() * 100d));
		this.t2.setValue(settings.getAbsThreshold());
	}

	PCLFilterSettings getSettings(){ 		
		this.settings.setRelThreshold(this.t1.getValue() / 100d);
		this.settings.setAbsThreshold(this.t2.getValue());
		return this.settings; 
	}
}
