package it.unicam.pros.guidedsimulator.util.mining.prom.framework;

import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.contexts.uitopia.UIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;

public class ContextsFactory {

    public static CLIContext getContext(){
        return new CLIContext();
    }

    public static PluginContext getPluginContext(){
        return new CLIPluginContext(getContext(), null);
    }

}
