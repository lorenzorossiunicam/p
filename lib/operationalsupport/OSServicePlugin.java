package org.processmining.plugins.operationalsupport;

import java.util.Set;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginParameterBinding;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.Pair;
import org.processmining.models.operationalsupport.net.service.OSService;

// @GUIAction(menuGroup = GUIAction.OPERATIONALGROUP, menuName = GUIAction.TESTMENU, showInToolbar = false, showInMenu = true)
@Plugin(name = "Operational Support Service", parameterLabels = {}, returnLabels = { "Operational Support Service" }, returnTypes = { OSService.class }, userAccessible = true)
public class OSServicePlugin {

	private static int DEFAULT_SETUP_PORT = 4444;
	private static int DEFAULT_REQUEST_OFFSET = 15000;
	private static int DEFAULT_MAX_CLIENTS = 2;

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "B.F. van Dongen", email = "b.f.v.dongen@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN)
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = {})
	public OSService startOperationalSupportServerDefault(UIPluginContext context) {
		return start(context, DEFAULT_SETUP_PORT, DEFAULT_REQUEST_OFFSET, DEFAULT_MAX_CLIENTS);
	}

	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = {})
	public OSService startOperationalSupportServerDefault(PluginContext context) {

		return start(context, DEFAULT_SETUP_PORT, DEFAULT_REQUEST_OFFSET, DEFAULT_MAX_CLIENTS);

	}

	// TODO: Make ports configurable
	//	@PluginVariant(variantLabel = "User input settins", requiredParameterLabels = {})
	//	public OSService startOperationalSupportServerUser(GUIPluginContext context) {
	//
	//		String input = JOptionPane.showInputDialog(context.getMainFrame(),
	//				"Enter ports for the operational support service separated with a comma: requestPort");
	//
	//		return start(context, Integer.parseInt(input));
	//	}

	private OSService start(PluginContext context, int port, int offset, int maxClients) {

		OSService service = new OSService(port, offset, maxClients, context.getProvidedObjectManager());

		context.getFutureResult(0).setLabel("Operational Support Service @ " + service); // originally this was the first line of code. Maja put is as the second one.

		// Now find and start a manager
		// First, search for all plugins that are enabled on an object of type 
		// OperatonalSupportService and do not return any parameters;
		Set<Pair<Integer, PluginParameterBinding>> potentialManagers = context.getPluginManager().find(Plugin.class,
				null, context.getClass(), true, true, false, OSService.class);

		// Go through all of these plugins and find the one
		// with the right name
		for (Pair<Integer, PluginParameterBinding> pair : potentialManagers) {

			// Check if the name is the same as the name for the operational support service manager
			if (pair.getSecond().getPlugin().getName().equals(OSServiceManagerPlugin.NAME)) {

				// Instantiate a child of the root context, to carry the manager
				PluginContext child = context.getRootContext().createChildContext(
						"Operational Support Service Manager @ " + service);

				// signal the root context that a child was created
				context.getRootContext().getPluginLifeCycleEventListeners().firePluginCreated(child);

				// invoke the manager asynchronously (in its own context)
				pair.getSecond().invoke(child, service);

				// break the loop as 1 manager suffices
				break;
			}
		}

		return service;
	}

}
