package org.gap.eclipse.plugins.extras.core.project;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;
import org.gap.eclipse.plugins.extras.core.PluginActivator;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

public abstract class BaseSelectionOperationHandler extends AbstractHandler {

	private boolean useProgressService;

	public BaseSelectionOperationHandler(boolean useProgressService) {
		this.useProgressService = useProgressService;
	}

	public BaseSelectionOperationHandler() {
		this(true);
	}

	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		final ISelectionService selectionService = window.getSelectionService();
		final IStructuredSelection selection = ((IStructuredSelection) selectionService.getSelection());
		final List<IProject> selectionProjects = resolve(selection);

		if (useProgressService) {
			final IProgressService progressService = window.getWorkbench().getProgressService();
			try {
				progressService.run(true, false, pm -> executeOnSelection(selectionProjects, pm, window));
			} catch (InvocationTargetException | InterruptedException e) {
				PluginActivator.getDefault().log(Status.INFO, "Stopped job for [" + getClass().getName() + "]", e);
			}
		} else {
			executeOnSelection(selectionProjects, null, window);
		}
		return null;
	}

	private void executeOnSelection(List<IProject> projects, IProgressMonitor pm, IWorkbenchWindow window) {
		List<IProject> projectsToOperate = new ArrayList<>(projects);
		projects.forEach(p -> NestedProjectWalker.walkerFor(p).forEach(projectsToOperate::add));
		executeOperation(projectsToOperate, pm, window);
	}

	protected abstract void executeOperation(List<IProject> projects, IProgressMonitor pm, IWorkbenchWindow window);

	@SuppressWarnings("unchecked")
	protected List<IProject> resolve(IStructuredSelection selection) {
		return Lists
				.newArrayList(Iterators.transform(selection.iterator(), o -> (IProject) o));
	}
}
