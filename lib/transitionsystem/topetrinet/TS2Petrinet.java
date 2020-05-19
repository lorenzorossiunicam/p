package org.processmining.plugins.transitionsystem.topetrinet;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.processmining.framework.boot.Boot;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.plugins.petrify.PetrifyDotG;
import org.processmining.plugins.petrify.PetrifyDotSG;

public class TS2Petrinet {

	// Available options
	private String options = "";

	public TS2Petrinet() {
	}

	public PetrifyDotG convert(PluginContext context, PetrifyDotSG dotSG) {
		/*
		 * TS2PetrinetOutput result = new TS2PetrinetOutput(); TransitionSystem
		 * ts = settings.getTransitionSystem(); PetrifyExport export = new
		 * PetrifyExport();
		 */
		String dotGName = dotSG.getName().replaceAll(".sg", ".g");
		PetrifyDotG result = new PetrifyDotG(dotGName);
		// Show the options dialog
		final GenetUI ui = new GenetUI(this);
		ui.setVisible(true);

		// calling genet from here
		try {
			/*
			 * File exportFile = File.createTempFile("pmt", ".sg");
			 * exportFile.deleteOnExit(); export.write(context,ts,exportFile);
			 */

			// Construct path and command line.
			String cmd = null, path = null;
			if (System.getProperty("os.name", "").toLowerCase().startsWith("windows")) {
				// Run genet on a Windows machine
				context.log("genet still not available in Windows", MessageLevel.ERROR);
				/*
				 * path = System.getProperty("user.dir") +
				 * System.getProperty("file.separator") + "lib" +
				 * System.getProperty("file.separator") + "plugins" +
				 * System.getProperty("file.separator") + "Petrify" +
				 * System.getProperty("file.separator"); cmd = path +
				 * "petrify4.1 -d2 -dead -ip " + options + "\"" +
				 * exportFile.getCanonicalPath() + "\"";
				 */
				//Load libraries required by petrify. 
				System.loadLibrary("cygwin1");
				System.loadLibrary("petrify");

			} else if (System.getProperty("os.name", "").toLowerCase().startsWith("linux")) {
				// Run genet on a Linux machine.
				path = System.getProperty("user.dir") + System.getProperty("file.separator") + Boot.LIB_FOLDER
						+ System.getProperty("file.separator");

				cmd = path + "genet " + options + dotSG.getName() + " -o " + dotGName;
				//                    exportFile.getCanonicalPath();
				context.log("Command to execute:" + cmd, MessageLevel.TEST);

				/*
				 * Do not use double quotes around the canonical path on linux,
				 * as these quotes will end up as part of the path (they willnot
				 * be removed!).
				 */

				// Load libraries required by petrify.
				//System.load(path + "lib" + System.getProperty("file.separator") +
				//            "petrify.lib");
			}

			if (cmd != null) {
				// Run genet.
				Process process = Runtime.getRuntime().exec(cmd);

				StreamReader errorReader = new StreamReader(process.getErrorStream());
				errorReader.start();
				//             context.log("<genet" + " options=\"" + options + "\"/>",
				//                          MessageLevel.TEST);

				//            PetrifyImport pfyimport = new PetrifyImport();
				//            Object[] pnsystem =  pfyimport.importFromStream(context, process.getInputStream(), "Genet", 0);
				//            result.setPetrinet((Petrinet)pnsystem[0]);
				process.waitFor();
				// (For sake of convenience, we copy stderr to ERROR)
				String err = errorReader.getResult();
				if ((err != null) && (err.length() > 0)) {
					context.log(err, MessageLevel.ERROR);
				}
				return new PetrifyDotG(dotGName);
			} else {
				context.log("Unable to execute genet on this platform: " + System.getProperty("os.name", ""),
						MessageLevel.DEBUG);
				//             net = new Petrinet(new Petrinet());
			}

		} catch (Exception e) {
			context.log("Unable to petrify transition system: " + e.toString(), MessageLevel.DEBUG);
		}

		return result;
	}

	/**
	 * 
	 * <p>
	 * Title: StreamReader
	 * </p>
	 * 
	 * <p>
	 * Description: Reads a stream using a separate thread. Used to read
	 * petrify's stdout and stderr, as genet might block on writing these
	 * streams while we're weaiting for it to complete.
	 * </p>
	 */
	static class StreamReader extends Thread {
		private final InputStream is;
		private final StringWriter sw;

		StreamReader(InputStream is) {
			this.is = is;
			sw = new StringWriter();
		}

		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1) {
					sw.write(c);
					System.err.write(c);
				}
			} catch (IOException e) {
				;
			}
		}

		String getResult() {
			return sw.toString();
		}
	}

	/*
	 * Options
	 */

	public void setCC(boolean result) {
		if (result) {
			options += "-cc ";
		}
	}

	public void setMine(boolean result) {
		if (result) {
			options += "-pm ";
		}
	}

	public void setRec(boolean result) {
		if (result) {
			options += "-rec ";
		}
	}

	public void setMaxbound(String result) {
		if (new Integer(result).intValue() > 1) {
			options += "-k " + result + " ";
		}
	}

	public void setMinbound(String result) {
		if (new Integer(result).intValue() > 1) {
			options += "-min " + result + " ";
		}
	}

	public void setProjectList(String result) {
		if (result.length() > 0) {
			options += "-prj " + result + " ";
		}
	}

	public void setCov(String result) {
		if (result.length() > 0) {
			if (new Integer(result).intValue() > 1) {
				options += "-cov " + result + " ";
			}
		}
	}

	public void setMG(boolean result) {
		if (result) {
			options += "-mg ";
		}
	}
}

/**
 * 
 * <p>
 * Title: GenetUI
 * </p>
 * 
 * <p>
 * Description: Dialog to obtain the available options.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Josep Carmona (copy from Eric Verbeek's pfy gui)
 * @version 1.0
 */
class GenetUI extends JDialog implements ActionListener {
	private static final long serialVersionUID = 2117086386113912833L;
	TS2Petrinet ts2pn;
	JRadioButton synthRadiobutton;
	JRadioButton mineRadiobutton;
	JRadioButton recRadiobutton;
	JRadioButton ccRadiobutton;
	JTextField maxboundTextField;
	JTextField minboundTextField;
	JTextField prjlistTextField;
	JTextField covTextField;
	JCheckBox mgCheckbox;
	JButton doneButton;

	private final static String about = "<html><center>This plug-in uses Genet. Genet is a tool by Josep Carmona<br>"
			+ "(For the time being, only available for Linux/Sun) .</center></html>";

	public GenetUI(TS2Petrinet ts2pn) {
		setTitle("genet options...");
		setModal(true);
		this.ts2pn = ts2pn;

		try {
			setUndecorated(false);
			jbInit();
			pack();
			//          CenterOnScreen.center(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		int i = 0;
		getContentPane().setLayout(new GridBagLayout());

		synthRadiobutton = new JRadioButton("Synthesis");
		mineRadiobutton = new JRadioButton("Mining");
		mineRadiobutton.setSelected(true);
		recRadiobutton = new JRadioButton("Recursive Mining");
		ccRadiobutton = new JRadioButton("State machine decomposition");

		ButtonGroup group = new ButtonGroup();
		group.add(synthRadiobutton);
		group.add(mineRadiobutton);
		group.add(recRadiobutton);
		group.add(ccRadiobutton);

		getContentPane().add(
				synthRadiobutton,
				new GridBagConstraints(0, i++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		getContentPane().add(
				mineRadiobutton,
				new GridBagConstraints(0, i++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				recRadiobutton,
				new GridBagConstraints(0, i++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				ccRadiobutton,
				new GridBagConstraints(0, i++, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("SYNTHESIS AND MINING OPTIONS"),
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("Max Bound"),
				new GridBagConstraints(0, i, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		maxboundTextField = new JTextField("1");
		maxboundTextField.setColumns(3);

		getContentPane().add(
				maxboundTextField,
				new GridBagConstraints(1, i++, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("Min Bound"),
				new GridBagConstraints(0, i, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		minboundTextField = new JTextField("1");
		minboundTextField.setColumns(3);
		getContentPane().add(
				minboundTextField,
				new GridBagConstraints(1, i++, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("Projected signals"),
				new GridBagConstraints(0, i, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		prjlistTextField = new JTextField("");
		prjlistTextField.setColumns(10);
		getContentPane().add(
				prjlistTextField,
				new GridBagConstraints(1, i++, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("MINING OPTIONS"),
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		getContentPane().add(
				new JLabel("Minimal number of regions covering an event"),
				new GridBagConstraints(0, i, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
		covTextField = new JTextField("");
		covTextField.setColumns(10);
		getContentPane().add(
				covTextField,
				new GridBagConstraints(1, i++, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		mgCheckbox = new JCheckBox("Force deriving a marked graph");
		getContentPane().add(
				mgCheckbox,
				new GridBagConstraints(0, i++, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		doneButton = new JButton("Done");
		getContentPane().add(
				doneButton,
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

		doneButton.addActionListener(this);
		doneButton.requestFocusInWindow();

		getContentPane().add(
				new JLabel(about),
				new GridBagConstraints(0, i++, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == doneButton) {
			ts2pn.setMine(mineRadiobutton.isSelected());
			ts2pn.setCC(ccRadiobutton.isSelected());
			ts2pn.setRec(recRadiobutton.isSelected());
			ts2pn.setMaxbound(maxboundTextField.getText());
			ts2pn.setMinbound(minboundTextField.getText());
			ts2pn.setProjectList(prjlistTextField.getText());
			ts2pn.setCov(covTextField.getText());
			ts2pn.setMG(mgCheckbox.isSelected());
			dispose();
		}
	}
}
