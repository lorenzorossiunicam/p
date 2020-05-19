package org.processmining.plugins.fuzzymodel.miner.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.fluxicon.slickerbox.components.StackedCardsTabbedPane;

public class TestTab {
	public static final JPanel concurrencyParentPanel= new JPanel();
	public static final JPanel lowerControlPanel = new JPanel();
	public static final StackedCardsTabbedPane tabPane = new StackedCardsTabbedPane();
	public static void main(String[] argv) {
			
			JButton bt = new JButton("test");
			concurrencyParentPanel.add(bt);
			bt.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					tabPane.setActive(1);
					Component a = lowerControlPanel.getComponent(0);
					if( a instanceof JTextField){						
						((JTextField) a).setText("change");
					}
				}
			});
			JTextField tf = new JTextField();
			tf.setText("ini");
			lowerControlPanel.add(tf);

			
			tabPane.addTab("Concurrency filter", concurrencyParentPanel);
			tabPane.addTab("Edge filter", lowerControlPanel);
		  JFrame frame = new JFrame("TabTest example");
	        // ensure application exits when window is closed
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.add(tabPane);
	        frame.pack();           // layout components in window
	        frame.setVisible(true); // show the windo
	  }

}
