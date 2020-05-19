package org.processmining.plugins.socialnetwork.miner.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class SNMinerDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -820927829163996251L;
	private final JPanel mainPanel = new JPanel();
	private final JPanel buttonPanel = new JPanel();
	private final JButton okButton = new JButton();
	private final JButton cancelButton = new JButton();
	private boolean ok;

	/**
	 * Creates the dialog.
	 * 
	 * @param frame
	 *            Parent frame.
	 * @param title
	 *            Dialog title.
	 * @param modal
	 *            Whether dialog is modal.
	 * @param panel
	 *            Dialog setting.
	 */
	public SNMinerDialog(Frame frame, String title, boolean modal, JPanel panel) {
		super(frame, title, modal);
		try {
			jbInit(panel);
			//pack();
			/**
			 * Set a nice size for the dialog, pack() gives ugly results.
			 * Assumption: A screen is at least 900x600...
			 */
			setSize(900, 600);
			/**
			 * And center the dialog on the screen.
			 */
			setLocationRelativeTo(null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initializes the dialog given its setting.
	 * 
	 * @param panel
	 *            The setting panel for the dialog.
	 */
	private void jbInit(JPanel panel) {
		/**
		 * The main panel will contain the given panel and the panel with the
		 * buttons.
		 */
		getContentPane().add(mainPanel);
		BorderLayout mainLayout = new BorderLayout();
		mainPanel.setLayout(mainLayout);
		mainPanel.add(panel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		/**
		 * The cancel button.
		 */
		cancelButton.setText("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible(false);
			}
		});
		buttonPanel.add(cancelButton);
		/**
		 * The ok button.
		 */
		okButton.setText("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		buttonPanel.add(okButton);
		/**
		 * If modal, add button panel.
		 */
		if (isModal()) {
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		}
	}

	/**
	 * Show the dialog and wait until the user has pressed either Cancel or Ok.
	 * 
	 * @return Whether the user has pressed Ok.
	 */
	public boolean showModal() {
		setVisible(true);
		return ok;
	}
}
