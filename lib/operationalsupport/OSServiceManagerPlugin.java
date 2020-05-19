package org.processmining.plugins.operationalsupport;

import java.io.IOException;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.Progress;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.socket.ServiceEnvironment;
import org.processmining.models.operationalsupport.net.service.OSService;

@Plugin(name = OSServiceManagerPlugin.NAME, parameterLabels = { "Service" }, returnLabels = {}, returnTypes = {}, userAccessible = true)
public class OSServiceManagerPlugin implements ServiceEnvironment {

	public final static String NAME = "Operational Support Service Manager";
	private final static int MAX = 10;

	private PluginContext context;

	private Progress progress;

	//	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "M. Pesic", email = "m.pesic@tue.nl", uiLabel = UITopiaVariant.USEPLUGIN)
	@PluginVariant(variantLabel = "Default settings", requiredParameterLabels = { 0 })
	public void manageUI(UIPluginContext context, OSService service) throws IOException {
		manage(context, service);
	}

	@PluginVariant(requiredParameterLabels = { 0 })
	public void manage(PluginContext context, OSService service) throws IOException {

		this.context = context;
		progress = context.getProgress();

		progress.setCaption("Operational Support Service Manager @ " + service);
		progress.setMaximum(MAX + 1);
		progress.setIndeterminate(false);
		progress.setValue(1);

		log("Started server at " + service);
		try {
			// opens the port
			service.start(this, context.getExecutor());
		} catch (IOException e) {
			throw new IOException("The port " + service + " is already in use.");
		}

		// Execution of this plugin blocks until cancelled.

		service.stop(this);
		log("Stopped server at " + service);

		context.getProgress().setValue(11);
		this.context = null;
	}

	public boolean isCancelled() {
		return ((context == null) || (progress == null) ? true : progress.isCancelled());
	}

	public void log(String message, MessageLevel level) {
		if (context != null) {
			context.log(message, level);
		}
	}

	public void log(String message) {
		if (context != null) {
			context.log(message);
		}
	}

	public void log(Throwable t) {
		if (context != null) {
			context.log(t);
		}
	}

	public void cancel() {
		context.getProgress().cancel();
	}

	public void stillAlive() {
		if (progress != null) {
			synchronized (progress) {
				int val = progress.getValue();
				val = (val % MAX) + 1;
				progress.setValue(val);
			}
		}

	}
}
