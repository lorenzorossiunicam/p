package org.processmining.plugins.ilpminer;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverSetting;
import org.processmining.plugins.ilpminer.ILPMinerSettings.SolverType;

import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * This class generates a GUI for the ILPMiner plugin, showing a combobox with
 * the available ILP variants and their settings as well as a list of available
 * ILP model extensions, and the settings for which solver to use Add the ILP
 * model file class to the models array to make it available via the GUI
 * 
 * @author T. van der Wiel
 * 
 */
public class ILPMinerUI implements ActionListener {
	/**
	 * The list of available ILP variants used by the GUI to show the user all
	 * options.
	 */
	private VariantListBoxItem[] models;
	/**
	 * The list of available ILP model extensions used by the GUI to show the
	 * user all options applicable to the chosen variant.
	 */
	private final ExtensionCheckBoxItem[] extensions;
	/**
	 * The list of available ILP solvers
	 */
	final SolverListBoxItem[] solvers = {
			new SolverListBoxItem(SolverType.CPLEX,
					"OPL & iLog CPLEX (requires CPLEX license)"),
			new SolverListBoxItem(SolverType.JAVAILP_CPLEX,
					"Java-ILP & iLog CPLEX (requires CPLEX license)"),
			new SolverListBoxItem(SolverType.JAVAILP_LPSOLVE,
					"Java-ILP & LpSolve 5.5") };

	/**
	 * generated
	 */
	private static final long serialVersionUID = -7659028165481473303L;

	private final ILPMinerSettings settings;

	private SlickerFactory slickerFactory;
	private JPanel panel, settingsPanel, extensionsPanel;
	private JComboBox variantBox, solverBox;
	private JLabel licenseLocationLabel;

	public ILPMinerUI() {
		settings = new ILPMinerSettings();
		// load the settings from the registry
		Preferences prefs = Preferences.userNodeForPackage(ILPMiner.class);
		settings.setSolverSetting(SolverSetting.TYPE, SolverType.fromInt(prefs
				.getInt("SolverEnum", SolverType.JAVAILP_LPSOLVE.ordinal())));
		settings.setSolverSetting(SolverSetting.LICENSE_DIR, prefs.get(
				"LicenseDir", ""));

		Class<?>[] strategies = ILPMinerStrategyManager
				.getILPMinerStrategyExtensions();
		extensions = new ExtensionCheckBoxItem[strategies.length];
		int i = 0;
		for (Class<?> c : strategies) {
			extensions[i] = new ExtensionCheckBoxItem(c);
			extensions[i].getCheckBox().addActionListener(this);
			i++;
		}
	}

	public ILPMinerUI(ILPMinerSettings settings) {
		this.settings = settings;

		Class<?>[] strategies = ILPMinerStrategyManager
				.getILPMinerStrategyExtensions();
		extensions = new ExtensionCheckBoxItem[strategies.length];
		int i = 0;
		for (Class<?> c : strategies) {
			extensions[i] = new ExtensionCheckBoxItem(c);
			extensions[i].getCheckBox().addActionListener(this);
			i++;
		}
	}

	/**
	 * Initializes the user interface
	 * 
	 * @return Settingspanel
	 */
	public JComponent initComponents() {
		slickerFactory = SlickerFactory.instance();
		// create panels
		panel = slickerFactory.createRoundedPanel();

		// create a layout for all panels
		panel.setLayout(new TableLayout(new double[][] { { 150, 350 },
				{ 30, 30, 30, 30, 30, 60, 60 } }));

		// create the combobox with available ILP solvers and the solver
		// settings panels
		solverBox = new JComboBox(solvers);// slickerFactory.createComboBox(solvers);
		
		for (int i = 0; i < solvers.length; i++) {
			if (settings.getSolverSetting(SolverSetting.TYPE) == solvers[i]
					.getType()) {
				solverBox.setSelectedItem(solvers[i]);
			}
		}
		solverBox.addActionListener(this);

		licenseLocationLabel = slickerFactory
				.createLabel("<html><p>License location:</p></html>");
		licenseLocationLabel.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				JOptionPane
						.showMessageDialog(
								null,
								"In your computer properties, add an environment variable with key 'ILOG_LICENSE_FILE' and value the absolute location of your license file (standard 'C:\\ILOG\\ILM\\access.ilm').\r\nYou can obtain a license file via the IBM Academic Initiative.\r\nHow to set environment variables varies per operating system.",
								"Help", JOptionPane.INFORMATION_MESSAGE);
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseClicked(MouseEvent e) {
			}
		});

		updateSolver();

		panel.add(slickerFactory
				.createLabel("<html><h2>ILP Solver</h2></html>"), "0, 0, 1, 0");
		panel.add(solverBox, "0, 1, 1, 1");
		panel.add(licenseLocationLabel, "0, 2, 1, 2");
		panel
				.add(slickerFactory
						.createLabel("<html><h2>ILP Variant</h2></html>"),
						"0, 3, 1, 3");
		panel.add(settingsPanel, "0, 5, 1, 5");
		panel.add(extensionsPanel, "0, 6, 1, 6");

		return panel;
	}

	/**
	 * Gets the ILP model specific settings panel via reflection. Also creates
	 * an ILPModelSettings instance for the specific ILP model and links it to
	 * <code>this.settings</code> for future use. Lastly it will create
	 * actionListeners for the ILP model specific settings panel that update the
	 * aforementioned ILlogModelSettings instance.
	 * 
	 * @param slickerFactory
	 * @return settings JPanel
	 */
	public void updateSettings() {
		try {
			Method m = ((VariantListBoxItem) variantBox.getSelectedItem())
					.getItem().getMethod("getSettingsGUI",
							new Class[] { SlickerFactory.class, Class.class });
			Object[] result = (Object[]) m.invoke(null, new Object[] {
					slickerFactory,
					((VariantListBoxItem) variantBox.getSelectedItem())
							.getItem() });
			settings.setModelSettings((ILPModelSettings) result[1]);

			if (settingsPanel != null) {
				panel.remove(settingsPanel);
			}
			settingsPanel = (JPanel) result[0];
			((TableLayout) panel.getLayout()).setRow(5, settingsPanel
					.getPreferredSize().getHeight());
			panel.add(settingsPanel, "0, 5, 1, 5");
		} catch (Exception ex) {
			settingsPanel = slickerFactory.createRoundedPanel();
		}
	}

	/**
	 * returns the current settings
	 * 
	 * @return ILPMinerSettings
	 */
	public ILPMinerSettings getSettings() {
		return settings;
	}

	// Handles the user interaction with the GUI
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(solverBox)) {
			// changed which solver to use -> update the gui
			settings
					.setSolverSetting(SolverSetting.TYPE,
							((SolverListBoxItem) solverBox.getSelectedItem())
									.getType());

			updateSolver();
		} else if (e.getSource().equals(variantBox)) {
			// ILP model variant changed -> update ILP model specific GUI
			settings.setVariant(((VariantListBoxItem) variantBox
					.getSelectedItem()).getItem());

			@SuppressWarnings("unused")
			Class<?>[] strategies = ILPMinerStrategyManager
					.getILPMinerStrategies((SolverType) settings
							.getSolverSetting(SolverSetting.TYPE));

			updateSettings();
			updateExtensions();
		} else if (e.getSource().getClass().equals(JCheckBox.class)) {
			// Probably an extension checkbox was (un)checked, so just update
			// the complete list of extensions used in the settings
			settings.clearExtensions();
			// first check & enable/disable all 'parent' extensions such that
			// the settings will be updated correctly in one go
			for (int i = 0; i < extensions.length; i++) {
				fixParentCheckbox(extensions[i]);
			}
			// update settings
			for (int i = 0; i < extensions.length; i++) {
				if (extensions[i].getCheckBox().isSelected()) {
					settings.addExtension(extensions[i].getItem());
				}
			}
		}
	}

	/**
	 * Enables/disables-and-checks all extension checkboxes that are 'required
	 * for'/'a parent of' the given extension. Will fix the parent's parents via
	 * recursion.
	 * 
	 * @param ExtensionCheckBoxItem
	 *            containing the checkbox for which to check the parents
	 */
	private void fixParentCheckbox(ExtensionCheckBoxItem e) {
		try {
			// get the parent extension class (null if none required)
			Class<?> c = (Class<?>) e.get(ExtensionProperties.PARENTEXTENSION);
			if (c != null) {
				// find parent checkbox
				for (int i = 0; i < extensions.length; i++) {
					if (extensions[i].getItem().equals(c)) {
						// update parent checkbox
						if (e.getCheckBox().isSelected()) {
							extensions[i].getCheckBox().setSelected(true);
							// do recursive fix since parent is now selected as
							// well
							fixParentCheckbox(extensions[i]);
							extensions[i].getCheckBox().setEnabled(false);
						} else {
							extensions[i].getCheckBox().setEnabled(true);
						}
					}
				}
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * updates the variant list to show all strategies available for this solver
	 */
	private void updateSolver() {
		Class<?>[] strategies = ILPMinerStrategyManager
				.getILPMinerStrategies((SolverType) settings
						.getSolverSetting(SolverSetting.TYPE));
		models = new VariantListBoxItem[strategies.length];
		int i = 0;
		VariantListBoxItem inSettings = new VariantListBoxItem(null);
		for (Class<?> c : strategies) {
			models[i] = new VariantListBoxItem(c);
			if (c == settings.getVariant()) {
				inSettings = models[i];
			}
			i++;
		}
		Arrays.sort(models);

		// create the combobox with available ILP models
		if (variantBox != null) {
			panel.remove(variantBox);
		}
		variantBox = new JComboBox(models);//slickerFactory.createComboBox(models);
		variantBox.setSelectedItem(inSettings);
		variantBox.addActionListener(this);
		panel.add(variantBox, "0, 4, 1, 4");

		if ((SolverType) settings.getSolverSetting(SolverSetting.TYPE) == SolverType.CPLEX) {
			if (System.getenv("ILOG_LICENSE_FILE") == null) {
				licenseLocationLabel
						.setText("<html><p><strong style=\"color:#990000;\">Please set the ILOG_LICENSE_FILE environment variable to your license</strong></p><p><strong style=\"color:#990000;\">file location and restart ProM6.</strong> Click <a href=\"\">here</a> if you don't know how.</p></html>");
			} else {
				licenseLocationLabel
						.setText("<html><p>Current iLog license location: "
								+ System.getenv("ILOG_LICENSE_FILE")
								+ ".</p></html>");
			}
		} else {
			licenseLocationLabel.setText("");
		}

		updateSettings();
		updateExtensions();
	}

	/**
	 * updates the extensions checkbox list panel to show only the extensions
	 * available for the ILP model variant currently in the settings
	 */
	private void updateExtensions() {
		if (extensionsPanel != null) {
			panel.remove(extensionsPanel);
		}
		// make a list of all available extensions
		ArrayList<ExtensionCheckBoxItem> list = new ArrayList<ExtensionCheckBoxItem>();
		for (int i = 0; i < extensions.length; i++) {
			extensions[i].getCheckBox().setSelected(false);
			try {
				// retrieve and compare the ILP model variant (super)class
				Class<?>[] c = (Class<?>[]) extensions[i]
						.get(ExtensionProperties.MODELCLASS);
				for (int j = 0; j < c.length; j++) {
					if (isSuperModel(settings.getVariant(), c[j])) {
						list.add(extensions[i]);
					}
				}
			} catch (Exception e) {
			}
		}
		// make a new tablelayout
		double[] sizes = new double[list.size() + 1];
		for (int i = 0; i < list.size() + 1; i++) {
			sizes[i] = 60;
		}
		extensionsPanel = slickerFactory.createRoundedPanel();
		extensionsPanel.setLayout(new TableLayout(new double[][] {
				{ TableLayoutConstants.FILL }, sizes }));
		// add all components
		extensionsPanel.add(slickerFactory
				.createLabel("<html><h2>ILP Extensions</h2></html>"), "0, 0");
		for (int i = 0; i < list.size(); i++) {
			extensionsPanel.add(list.get(i).getCheckBox(), "0, " + (i + 1));
		}
		// update the main GUI
		((TableLayout) panel.getLayout()).setRow(6, extensionsPanel
				.getPreferredSize().getHeight());
		panel.add(extensionsPanel, "0,6,1,6");

		panel.validate();
		try {
			panel.getRootPane().validate();
		} catch (Exception e) {
		}
	}

	/**
	 * checks if the model equals the supermodel or if the supermodel is a
	 * superclass of model
	 * 
	 * @param model
	 * @param supermodel
	 * @return boolean
	 */
	private boolean isSuperModel(Class<?> model, Class<?> supermodel) {
		Class<?> curr = model;
		while (curr != null) {
			// if supermodel is ILPModel, model must have it as a superclass
			// (but since its abstract, this would be a strange minimal
			// requirement).
			if (supermodel.equals(curr)
					|| supermodel.equals(ILPModelJavaILP.class)) {
				return true;
			}
			curr = curr.getSuperclass();
		}
		return false;
	}

	/**
	 * Enumeration for ILP model variant properties (for easy retrieval via the
	 * ListBoxItem class)
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	public enum Properties {
		NAME("getName"), AUTHOR("getAuthor"), DESCRIPTION("getDescription");
		private final String value;

		Properties(String functionName) {
			value = functionName;
		}

		String getFunctionName() {
			return value;
		}
	};

	/**
	 * Enumeration for ILP model extension extra properties (for easy retrieval
	 * via the ExtensionListBoxItem class)
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	public enum ExtensionProperties {
		PARENTEXTENSION("getParentExtensionClass"), MODELCLASS(
				"getExtensionSuperClass");
		private final String value;

		ExtensionProperties(String functionName) {
			value = functionName;
		}

		String getFunctionName() {
			return value;
		}
	};

	/**
	 * Provides easy acces to the ILP model variants static properties
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	private class VariantListBoxItem implements Comparable<VariantListBoxItem> {
		private final Class<?> c;

		public VariantListBoxItem(Class<?> item) {
			c = item;
		}

		/**
		 * For internal use only! 'Protected' is for use of inheritance
		 * Retrieves a static function value from the associated class
		 * 
		 * @param function
		 *            name
		 * @return function return value
		 */
		protected Object get(String functionName) {
			try {
				Method m = c.getMethod(functionName,
						new Class[] { Class.class });
				return m.invoke(null, new Object[] { c });
			} catch (Exception e) {
			}
			return null;
		}

		/**
		 * Retrieves a static property from the associated ILP model variant
		 * 
		 * @param property
		 * @return property return value
		 */
		public Object get(Properties property) {
			return get(property.getFunctionName());
		}

		/**
		 * returns the associated class
		 * 
		 * @return Class
		 */
		public Class<?> getItem() {
			return c;
		}

		public String toString() {
			return (String) get(Properties.NAME);
		}

		public int compareTo(VariantListBoxItem o) {
			return toString().compareTo(o.toString());
		}
	}

	/**
	 * Links an ILP model extension with a checkbox and provides easy acces to
	 * the models static properties
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	private class ExtensionCheckBoxItem extends VariantListBoxItem {
		private final JCheckBox checkBox;

		public ExtensionCheckBoxItem(Class<?> item) {
			super(item);
			checkBox = new JCheckBox();
			checkBox.setText("<html><p><b>" + get(Properties.NAME)
					+ "</b></p><p>" + get(Properties.AUTHOR) + "</p><p>"
					+ get(Properties.DESCRIPTION) + "</p></html>");
		}

		/**
		 * Retrieves a static property from the associated extension
		 * 
		 * @param property
		 * @return property return value
		 */
		public Object get(ExtensionProperties type) {
			return get(type.getFunctionName());
		}

		public JCheckBox getCheckBox() {
			return checkBox;
		}
	}

	/**
	 * Provides easy acces to the properties of the chosen ILP solver
	 * 
	 * @author T. van der Wiel
	 * 
	 */
	private class SolverListBoxItem {
		private final String text;
		private final SolverType type;

		public SolverListBoxItem(SolverType type, String name) {
			text = name;
			this.type = type;
		}

		public String toString() {
			return text;
		}

		public SolverType getType() {
			return type;
		}
	}
}