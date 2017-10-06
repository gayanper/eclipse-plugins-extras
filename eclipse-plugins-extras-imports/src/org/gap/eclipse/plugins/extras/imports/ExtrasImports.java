package org.gap.eclipse.plugins.extras.imports;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class ExtrasImports implements IStartup {
	private static final ImportEntryList IMPORT_ENTRY_LIST = new ImportEntryList();

	public void load() {
		addToDefault();
		addToInstance();
	}

	private void addToInstance() {
		final ScopedPreferenceStore javaUIStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, JavaUI.ID_PLUGIN);
		final Imports imports = new Imports(
				javaUIStore.getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS));
		imports.mergeImports(IMPORT_ENTRY_LIST.entries());
		javaUIStore.setValue(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, imports.toSetting());
		try {
			javaUIStore.save();
		} catch (IOException e) {
			PluginActivator.getDefault().log(IStatus.ERROR, "Error loading default imports.", e);
		}
	}

	private void addToDefault() {
		final ScopedPreferenceStore javaUIStore = new ScopedPreferenceStore(DefaultScope.INSTANCE, JavaUI.ID_PLUGIN);
		final Imports imports = new Imports(
				javaUIStore.getString(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS));
		imports.mergeImports(IMPORT_ENTRY_LIST.entries());
		javaUIStore.setValue(PreferenceConstants.CODEASSIST_FAVORITE_STATIC_MEMBERS, imports.toSetting());
		try {
			javaUIStore.save();
		} catch (IOException e) {
			PluginActivator.getDefault().log(IStatus.ERROR, "Error loading default imports.", e);
		}
	}

	@Override
	public void earlyStartup() {
		Job updateImports = new Job("Extras Import Updater") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				load();
				return Status.OK_STATUS;
			}
		};
		updateImports.schedule();
	}
}
