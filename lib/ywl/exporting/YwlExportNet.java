package org.processmining.plugins.ywl.exporting;

import java.io.File;
import java.io.IOException;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.specification.SpecificationArchiveHandler;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.YAWLEditorDesktop;
import org.yawlfoundation.yawl.editor.thirdparty.engine.EngineSpecificationExporter;


@Plugin(name = "YAWL export", returnLabels = {}, returnTypes = {}, parameterLabels = { "YwlNet", "File" }, userAccessible = true)
@UIExportPlugin(description = "YAWL Specification", extension = "yawl")
public class YwlExportNet {

    @PluginVariant(variantLabel = "YAWL export", requiredParameterLabels = { 0, 1 })
	public synchronized void exportToYAWLFile(PluginContext context, NetGraph graph, File file)throws IOException {

        //close any specification
        SpecificationArchiveHandler.getInstance().closeWithoutSaving();

        //retrieve specification from graph
        YAWLEditorDesktop.getInstance().openNet(graph);

        //the actual exporting
		EngineSpecificationExporter.exportEngineSpecToFile(SpecificationModel.getInstance(), file.getAbsolutePath());

	}

}
