package org.gap.eclipse.plugins.extras.core.project;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchWindow;
import org.gap.eclipse.plugins.extras.core.PluginActivator;

public class CloseNestedProjectsHandler extends BaseSelectionOperationHandler {

	@Override
	protected void executeOperation(List<IProject> projects, IProgressMonitor pm, IWorkbenchWindow window) {
		projects.forEach(p -> {
			if (p.isOpen()) {
				try {
					p.close(pm);
				} catch (CoreException e) {
					PluginActivator.getDefault().log(Status.ERROR, "Error closing projects.", e);
				}
			}
		});
	}
}