package it.unicam.pros.purple.util.mining.prom.framework;

import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.cli.CLIPluginContext;
import org.processmining.framework.plugin.PluginContext;

public class ContextsFactory {

    public static CLIContext getContext(){
        return new CLIContext();
    }

    public static PluginContext getPluginContext(){
        return new CLIPluginContext(getContext(), null);
    }

}
