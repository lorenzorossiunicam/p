/**
 * 
 */
package org.processmining.plugins.fuzzyperformancediagram;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.processmining.models.performancemeasurement.GlobalSettingsData;
import org.processmining.models.performancemeasurement.dataelements.CaseKPIData;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.StatisticTableGenerator;
import org.processmining.plugins.fuzzyperformancediagram.visualizationgenerator.TextualInfoPanelGenerator;

/**
 * @author arya
 * @email arya.adriansyah@gmail.com
 * @version Apr 16, 2009
 */
public class CaseKPIInformationPanel extends JPanel {

	private static final long serialVersionUID = 3702038703829954047L;
	private JPanel textualInfoPanel;
	private final JPanel tablePanel;

	public CaseKPIInformationPanel(CaseKPIData caseKPIData, GlobalSettingsData globalSettingsData) {
		// case textual info
		initializeTextualInfo(caseKPIData, globalSettingsData);

		// table info 
		tablePanel = new JPanel();
		JTable table = StatisticTableGenerator.generateStatsTable("Throughput time", caseKPIData
				.getCaseThroughputTimeData(), globalSettingsData);

		table.setPreferredScrollableViewportSize(new Dimension(450, Math.round((float) table.getPreferredSize()
				.getHeight())));
		JScrollPane throughputInfoScrlPane = new JScrollPane(table);
		tablePanel.add(throughputInfoScrlPane);
		tablePanel.setPreferredSize(new Dimension(460, 135));
		add(tablePanel);
	}

	public void initializeTextualInfo(CaseKPIData caseKPIData, GlobalSettingsData globalSettingsData) {
		textualInfoPanel = new JPanel();
		textualInfoPanel.setLayout(new GridLayout(1, 1));

		// init utility
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(8);
		nf.setMinimumFractionDigits(0);

		// generate textual info panel
		TextualInfoPanelGenerator textualInfoPanelGenerator = new TextualInfoPanelGenerator();
		textualInfoPanelGenerator.addInfo("Number of cases", nf.format(caseKPIData.getNumCases()) + " cases");
		textualInfoPanelGenerator.addInfo("TIME UNIT", globalSettingsData.getTimeUnitLabel());
		textualInfoPanelGenerator.addInfo("Number of taken sequences", nf.format(caseKPIData.getNumUniqueTraces())
				+ " sequences");
		textualInfoPanelGenerator.addInfo("Number of fitting cases", nf.format(caseKPIData.getNumFittingCase())
				+ " cases");
		textualInfoPanelGenerator.addInfo("Executed events per resource", nf.format(caseKPIData
				.getExecutedEventsPerResource())
				+ " events/resource");
		textualInfoPanelGenerator.addInfo("Arrival rate", nf.format(caseKPIData.getArrivalRate()
				* globalSettingsData.getDividerValue())
				+ " case/time unit");
		textualInfoPanelGenerator.addInfo("Executed activity per resource", nf.format(caseKPIData
				.getExecutedActivityPerResource())
				+ " activities/resource");
		textualInfoPanelGenerator.addInfo("Number of involved resources", nf.format(caseKPIData
				.getNumInvolvedResource())
				+ " resources");
		textualInfoPanelGenerator.addInfo("Number of resource per case", nf.format(caseKPIData.getResourcePerCase()));
		textualInfoPanelGenerator.addInfo("Number of involved teams", nf.format(caseKPIData.getNumInvolvedTeams())
				+ " teams");
		textualInfoPanel.add(textualInfoPanelGenerator.generatePanelInfo(5, 2, 10, 10, 600, 200, 110));
		textualInfoPanel.setPreferredSize(new Dimension(610, 250));

		add(textualInfoPanel);
	}

	public void updateStatisticWithBoundary(double slowBoundaryPercentage, double fastBoundaryPercentage,
			CaseKPIData caseKPIData, GlobalSettingsData globalSettingsData) {
		tablePanel.removeAll();
		JScrollPane throughputInfoScrlPane = new JScrollPane(StatisticTableGenerator.generateStatsTable(
				"Throughput time", caseKPIData.getCaseThroughputTimeData(), globalSettingsData));
		tablePanel.add(throughputInfoScrlPane);
		repaint();
	}
}
