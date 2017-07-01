package org.gap.eclipse.plugins.extras.core.quickdiff;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.quickdiff.IQuickDiffReferenceProvider;
import org.gap.eclipse.plugins.extras.core.PluginActivator;

import com.google.common.collect.Maps;

public class ProjectBaseReferenceProvider implements IQuickDiffReferenceProvider {

	private static final String DEFAULT_PROVIDER = "org.eclipse.ui.internal.editors.quickdiff.LastSaveReferenceProvider";

	private String id;

	private IQuickDiffReferenceProvider delegate;

	private Map<String, String> providerDiffMapper = Maps.newHashMapWithExpectedSize(2);

	private Map<String, IConfigurationElement> configMap = Maps.newHashMapWithExpectedSize(3);

	public ProjectBaseReferenceProvider() {
		this(Platform.getExtensionRegistry());
	}

	public ProjectBaseReferenceProvider(IExtensionRegistry extensionRegistry) {
		populateExtensionConfigs(extensionRegistry);
		populateDiffProviderMapping();
	}

	@Inject
	private void setExtensionRegistry(IExtensionRegistry extensionRegistry) {
		System.out.println(extensionRegistry);
	}

	private void populateDiffProviderMapping() {
		providerDiffMapper.put("org.eclipse.egit.core.GitProvider",
				"org.eclipse.egit.ui.internal.decorators.GitQuickDiffProvider");
		providerDiffMapper.put("org.tigris.subversion.subclipse.core.svnnature",
				"org.tigris.subversion.subclipse.quickdiff.providers.SVNReferenceProvider");
	}

	private void populateExtensionConfigs(IExtensionRegistry extensionRegistry) {
		IConfigurationElement[] configs = extensionRegistry
				.getConfigurationElementsFor("org.eclipse.ui.workbench.texteditor.quickDiffReferenceProvider");

		for (IConfigurationElement element : configs) {
			configMap.put(element.getAttribute("id"), element);
		}

	}

	@Override
	public IDocument getReference(IProgressMonitor monitor) throws CoreException {
		return delegate.getReference(monitor);
	}

	@Override
	public void dispose() {
		if (delegate != null) {
			delegate.dispose();
			delegate = null;
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public void setActiveEditor(ITextEditor editor) {
		IEditorInput input = editor.getEditorInput();
		IResource resource = input.getAdapter(IResource.class);
		if (resource != null) {
			try {
				final String teamProvider = resource.getProject()
						.getPersistentProperty(new QualifiedName("org.eclipse.team.core", "repository"));

				if (providerDiffMapper.containsKey(teamProvider)) {
					IConfigurationElement element = configMap.get(providerDiffMapper.get(teamProvider));
					if (element != null) {
						delegate = (IQuickDiffReferenceProvider) element.createExecutableExtension("class");
					} else {
						updateDefaultDelegate();
					}
				} else {
					updateDefaultDelegate();
				}
			} catch (CoreException e) {
				StatusManager.getManager().handle(toExceptionStatus(e));
			}
		}
	}

	private void updateDefaultDelegate() throws CoreException {
		delegate = (IQuickDiffReferenceProvider) configMap.get(DEFAULT_PROVIDER)
				.createExecutableExtension("class");
	}

	private IStatus toExceptionStatus(Exception e) {
		return new Status(Status.ERROR, PluginActivator.PLUGIN_ID, e.getMessage(), e);
	}

	@Override
	public boolean isEnabled() {
		if (delegate == null) {
			return false;
		}
		return delegate.isEnabled();
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

}
