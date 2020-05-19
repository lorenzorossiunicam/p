/**
 * 
 */
package org.processmining.plugins.performancemeasurement;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.processmining.models.performancemeasurement.GlobalSettingsData;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 23, 2009
 */
public class GlobalSettingsDataPanel extends JPanel {
	private static final long serialVersionUID = -7348768989178281482L;

	// i/o related GUI
	private final JComboBox timeUnitComboBox;
	private final JTextField fastestBound;
	private final JTextField slowestBound;

	// reference to internal data
	private final GlobalSettingsData globalSettingsData;

	public GlobalSettingsDataPanel(GlobalSettingsData initialGlobalSettingsData) {
		globalSettingsData = initialGlobalSettingsData;

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(2, 4, 50, 3));
		mainPanel.setPreferredSize(new Dimension(700, 50));

		JLabel timeUnitlbl = new JLabel();
		timeUnitlbl.setText("Time Unit");

		timeUnitComboBox = new JComboBox(initialGlobalSettingsData.getAvailableTimeUnitLabel());
		timeUnitComboBox.setSelectedItem(initialGlobalSettingsData.getTimeUnitLabel());

		JLabel fastestBoundlbl = new JLabel();
		fastestBoundlbl.setText("Fastest precentage (%)");

		fastestBound = new JTextField();
		fastestBound.setText(String.valueOf(initialGlobalSettingsData.getFastestBoundPercentage() * 100));

		JLabel slowestBoundlbl = new JLabel();
		slowestBoundlbl.setText("Slowest precentage (%)");

		slowestBound = new JTextField();
		slowestBound.setText(String.valueOf(initialGlobalSettingsData.getSlowestBoundPercentage() * 100));

		JButton applySettingButton = new JButton();
		applySettingButton.setText("Apply Settings");
		applySettingButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					if ((Double.compare(Double.valueOf(fastestBound.getText()), 100.0) <= 0)
							&& (Double.compare(Double.valueOf(fastestBound.getText()), 0.0) >= 0)
							&& (Double.compare(Double.valueOf(slowestBound.getText()), 100.0) <= 0)
							&& (Double.compare(Double.valueOf(slowestBound.getText()), 0.0) >= 0)) {

						// update GlobalSettingData
						globalSettingsData.setFastestBoundPercentage(Double.valueOf(fastestBound.getText()) / 100.0);
						globalSettingsData.setSlowestBoundPercentage(Double.valueOf(slowestBound.getText()) / 100.0);
						globalSettingsData.setSelectedTimeUnit(getSelectedTimeUnit());

						JOptionPane
								.showMessageDialog(
										new JPanel(),
										"Settings updated. Change will take effect after currently opened visualization panel are closed.",
										"Success", JOptionPane.INFORMATION_MESSAGE);
					}
				} catch (NumberFormatException exc) {
					JOptionPane.showMessageDialog(new JPanel(), "Please insert a correct number format.", "Error",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception exc) {
					JOptionPane.showMessageDialog(new JPanel(), "Exception in setting global setting data.", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// add all element
		mainPanel.add(timeUnitlbl);
		mainPanel.add(timeUnitComboBox);
		mainPanel.add(fastestBoundlbl);
		mainPanel.add(fastestBound);
		mainPanel.add(new JPanel());
		mainPanel.add(new JPanel());
		mainPanel.add(slowestBoundlbl);
		mainPanel.add(slowestBound);

		setPreferredSize(new Dimension(900, 70));

		// add it to the main panel
		add(mainPanel);
		add(applySettingButton);
	}

	private int getSelectedTimeUnit() {
		String selectedItem = (String) timeUnitComboBox.getSelectedItem();
		if (selectedItem.equals("second")) {
			return GlobalSettingsData.SECOND;
		} else if (selectedItem.equals("minute")) {
			return GlobalSettingsData.MINUTE;
		} else if (selectedItem.equals("hour")) {
			return GlobalSettingsData.HOUR;
		} else if (selectedItem.equals("day")) {
			return GlobalSettingsData.DAY;
		}
		return GlobalSettingsData.MILLISECOND;
	}
}
