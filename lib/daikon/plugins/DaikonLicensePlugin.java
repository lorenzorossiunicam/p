package org.processmining.daikon.plugins;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.daikon.model.DaikonLicense;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.Apache20License;

@Plugin(name = "Show Daikon License", parameterLabels = { }, returnLabels = { "Daikon License" }, returnTypes = { Apache20License.class }, help = "Shows Daikon License")
public class DaikonLicensePlugin {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "F. Mannhardt", email = "f.mannhardt@tue.nl")
	@PluginVariant(variantLabel = "Show Daikon License", requiredParameterLabels = { })
	public DaikonLicense show(UIPluginContext context) {
		return new DaikonLicense();
	}
}
