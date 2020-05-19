package org.processmining.plugins.socialnetwork.miner;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.socialnetwork.SocialNetwork;
import org.processmining.plugins.socialnetwork.miner.gui.PanelSubcontracting;
import org.processmining.plugins.socialnetwork.miner.miningoperation.BasicOperation;
import org.processmining.plugins.socialnetwork.miner.miningoperation.OperationFactory;
import org.processmining.plugins.socialnetwork.miner.miningoperation.UtilOperation;

import cern.colt.matrix.DoubleMatrix2D;

@Plugin(name = "Social Network (SC) miner", level = PluginLevel.PeerReviewed, parameterLabels = { "Log" }, returnLabels = { "Social Network (SC)" }, returnTypes = { SocialNetwork.class }, userAccessible = true)
public class SNSCMiner {

	private BasicOperation baseOprtation = null;

	@UITopiaVariant(uiLabel = "Mine for a Subcontracting Social Network", affiliation = UITopiaVariant.EHV, author = "M. Song", email = "m.song@unist.ac.kr")
	@PluginVariant(variantLabel = "Select options to use", requiredParameterLabels = { 0 })
	public SocialNetwork main(final UIPluginContext context, final XLog log) {
		/**
		 * No summary provided, use default summary.
		 */
		PanelSubcontracting input = new PanelSubcontracting();
//		SNMinerDialog dialog = new SNMinerDialog(context.getMainFrame(), "Settings for SN miner", true, input);
//		dialog.showModal();
		InteractionResult result = context.showConfiguration("Settings for SN miner", input);
		if (result == InteractionResult.CONTINUE) {
			int indexType = SNMinerOptions.SUBCONTRACTING + ((input.getConsiderDirectSuccession() == true) ? 1 : 0) * 10
					+ ((input.getConsiderMultipleTransfers() == true) ? 1 : 0);
			baseOprtation = OperationFactory.getOperation(indexType, log);
			double beta = Double.valueOf(input.getBeta());
			int depth = Integer.valueOf(input.getDepth());
			DoubleMatrix2D matrix = baseOprtation.calculation(beta, depth);
			return UtilOperation.generateSN(matrix, baseOprtation.getOriginatorList());
		}
		return null;
	}

	@PluginVariant(variantLabel = "Use default options", requiredParameterLabels = { 0 })
	public SocialNetwork socialnetwork(PluginContext context, XLog log) {
		PanelSubcontracting input = new PanelSubcontracting();
		int indexType = SNMinerOptions.SUBCONTRACTING + ((input.getConsiderDirectSuccession() == true) ? 1 : 0) * 10
				+ ((input.getConsiderMultipleTransfers() == true) ? 1 : 0);
		baseOprtation = OperationFactory.getOperation(indexType, log);
		double beta = Double.valueOf(input.getBeta());
		int depth = Integer.valueOf(input.getDepth());
		DoubleMatrix2D matrix = baseOprtation.calculation(beta, depth);
		return UtilOperation.generateSN(matrix, baseOprtation.getOriginatorList());
	}
}