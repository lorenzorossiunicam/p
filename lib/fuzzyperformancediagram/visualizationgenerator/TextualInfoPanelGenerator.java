/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 16, 2009
 */
public class TextualInfoPanelGenerator {

	private List<String> listOfDescription;
	private List<String> listOfInfo;

	private Font boldFont = new Font("Arial", Font.BOLD, 12);
	private Font normalFont = new Font("Arial", Font.PLAIN, 12);

	private int defaultHGap = 10;
	private int defaultVGap = 10;

	private int defaultWidth = 300;
	private int defaultHeight = 100;

	public TextualInfoPanelGenerator() {
		listOfDescription = new LinkedList<String>();
		listOfInfo = new LinkedList<String>();

	}

	public void addInfo(String description, String info) {
		listOfDescription.add(description);
		listOfInfo.add(info);
	}

	public void removeInfo(String description, String info) {
		int index = 0;
		while (index < listOfDescription.size()) {
			if (listOfDescription.get(index).equals(description)) {
				if (listOfInfo.get(index).equals(info)) {
					// delete
					listOfDescription.remove(index);
					listOfInfo.remove(index);
					break;
				}
			}
		}
	}

	public void removeInfo(String description) {
		int index = listOfDescription.indexOf(description);
		if (index >= 0) {
			listOfDescription.remove(index);
			listOfInfo.remove(index);
		}
	}

	/**
	 * @return the listOfDescription
	 */
	public List<String> getListOfDescription() {
		return listOfDescription;
	}

	/**
	 * @param listOfDescription
	 *            the listOfDescription to set
	 */
	public void setListOfDescription(List<String> listOfDescription) {
		this.listOfDescription = listOfDescription;
	}

	/**
	 * @return the listOfInfo
	 */
	public List<String> getListOfInfo() {
		return listOfInfo;
	}

	/**
	 * @param listOfInfo
	 *            the listOfInfo to set
	 */
	public void setListOfInfo(List<String> listOfInfo) {
		this.listOfInfo = listOfInfo;
	}

	/**
	 * @return the boldFont
	 */
	public Font getBoldFont() {
		return boldFont;
	}

	/**
	 * @return the normalFont
	 */
	public Font getNormalFont() {
		return normalFont;
	}

	/**
	 * @return the defaultHGap
	 */
	public int getDefaultHGap() {
		return defaultHGap;
	}

	/**
	 * @return the defaultVGap
	 */
	public int getDefaultVGap() {
		return defaultVGap;
	}

	/**
	 * @return the defaultWidth
	 */
	public int getDefaultWidth() {
		return defaultWidth;
	}

	/**
	 * @param defaultWidth
	 *            the defaultWidth to set
	 */
	public void setDefaultWidth(int defaultWidth) {
		this.defaultWidth = defaultWidth;
	}

	/**
	 * @return the defaultHeight
	 */
	public int getDefaultHeight() {
		return defaultHeight;
	}

	/**
	 * @param defaultHeight
	 *            the defaultHeight to set
	 */
	public void setDefaultHeight(int defaultHeight) {
		this.defaultHeight = defaultHeight;
	}

	/**
	 * @param boldFont
	 *            the boldFont to set
	 */
	public void setBoldFont(Font boldFont) {
		this.boldFont = boldFont;
	}

	/**
	 * @param normalFont
	 *            the normalFont to set
	 */
	public void setNormalFont(Font normalFont) {
		this.normalFont = normalFont;
	}

	/**
	 * @param defaultHGap
	 *            the defaultHGap to set
	 */
	public void setDefaultHGap(int defaultHGap) {
		this.defaultHGap = defaultHGap;
	}

	/**
	 * @param defaultVGap
	 *            the defaultVGap to set
	 */
	public void setDefaultVGap(int defaultVGap) {
		this.defaultVGap = defaultVGap;
	}

	public JPanel generatePanelInfo(int rowNum, int columnNum) {
		return generatePanelInfo(rowNum, columnNum, defaultHGap, defaultVGap, defaultWidth, defaultHeight, 0);
	}

	public JPanel generatePanelInfo(int rowNum, int columnNum, int hgap, int vgap) {
		return generatePanelInfo(rowNum, columnNum, hgap, vgap, defaultWidth, defaultHeight, 0);
	}

	public JPanel generatePanelInfo(int rowNum, int columnNum, int hgap, int vgap, int width, int height, int maxChar) {
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(width, height));
		panel.setLayout(new GridLayout(rowNum, columnNum, hgap, vgap));

		int limit = rowNum * columnNum;
		int counter = 0;

		while ((counter < listOfDescription.size()) && (counter < limit)) {
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new BorderLayout());

			JLabel descLabel = new JLabel();
			if (listOfDescription.get(counter).length() > maxChar) {
				wrapLabelText(descLabel, listOfDescription.get(counter), width / columnNum);
				descLabel.setFont(boldFont);
				JScrollPane scrollPane = new JScrollPane(descLabel);

				// add to panel
				labelPanel.add(scrollPane, BorderLayout.NORTH);

			} else {
				wrapLabelText(descLabel, listOfDescription.get(counter), width / columnNum);
				descLabel.setFont(boldFont);

				// add to panel
				labelPanel.add(descLabel, BorderLayout.NORTH);
			}

			JLabel valLabel = new JLabel();
			if (listOfInfo.get(counter).length() > maxChar) {
				wrapLabelText(valLabel, listOfInfo.get(counter), width / columnNum);
				valLabel.setFont(normalFont);
				JScrollPane scrollPane = new JScrollPane(valLabel);

				// add to panel
				labelPanel.add(scrollPane, BorderLayout.CENTER);
			} else {
				wrapLabelText(valLabel, listOfInfo.get(counter), width / columnNum);
				valLabel.setFont(normalFont);

				// add to panel
				labelPanel.add(valLabel, BorderLayout.CENTER);
			}

			panel.add(labelPanel);
			counter++;
		}

		return panel;
	}

	private void wrapLabelText(JLabel label, String text, int containerWidth) {
		FontMetrics fm = label.getFontMetrics(label.getFont());

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(text);

		StringBuffer trial = new StringBuffer();
		StringBuffer real = new StringBuffer("<html>");

		int start = boundary.first();
		for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
			String word = text.substring(start, end);
			trial.append(word);
			int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
			if (trialWidth > containerWidth) {
				trial = new StringBuffer(word);
				real.append("<br/>");
			}
			real.append(word);
		}

		real.append("</html>");

		label.setText(real.toString());
	}
}
