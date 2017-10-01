package org.gap.eclipse.plugins.extras.core.project;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.gap.eclipse.plugins.extras.core.PluginActivator;

public class CloseNestedProjectsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final IProgressService progressService = window.getWorkbench().getProgressService();
		
		final ISelectionService selectionService = window.getSelectionService();
		final IStructuredSelection selection = ((IStructuredSelection) selectionService.getSelection());
		final IProject project = ((IProject) selection.getFirstElement());

		try {
			progressService.run(true, false, pm -> closeAllProjects(project, pm));
		} catch (InvocationTargetException | InterruptedException e) {
			PluginActivator.getDefault().log(Status.INFO, "Stopped project closing job.", e);
		}
		return null;
	}

	private void closeAllProjects(IProject project, IProgressMonitor pm) {
		final Consumer<IProject> projectConsumer = p -> {
			if (p.isOpen()) {
				try {
					p.close(pm);
				} catch (CoreException e) {
					PluginActivator.getDefault().log(Status.ERROR, "Error closing projects.", e);
				}
			}
		};
		projectConsumer.accept(project);
		NestedProjectWalker.walkerFor(project).forEach(projectConsumer);
	}
}