package org.gap.eclipse.plugins.extras.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PluginActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.gap.eclipse.plugins.extras.core";

	private static PluginActivator instance = null;

	public PluginActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	public static PluginActivator getDefault() {
		return instance;
	}
}
