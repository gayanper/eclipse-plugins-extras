package org.gap.eclipse.plugins.extras.core.project;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.handlers.HandlerUtil;

public class DeleteNestedProjectsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		final ISelectionService selectionService = window.getSelectionService();
		final IStructuredSelection selection = ((IStructuredSelection) selectionService.getSelection());
		final IProject project = ((IProject) selection.getFirstElement());

		ArrayList<IProject> projects = new ArrayList<>();
		projects.add(project);
		NestedProjectWalker.walkerFor(project).forEach(projects::add);

		final Selection strucSelection = new Selection(projects);
		final DeleteResourceAction action = new DeleteResourceAction(window) {
			@Override
			public IStructuredSelection getStructuredSelection() {
				return strucSelection;
			}
		};
		action.run();
		return null;
	}

	private class Selection implements IStructuredSelection {
		private List<IProject> projects;

		public Selection(List<IProject> projects) {
			this.projects = projects;
		}

		@Override
		public boolean isEmpty() {
			return projects.isEmpty();
		}

		@Override
		public Object getFirstElement() {
			return !projects.isEmpty() ? projects.get(0) : null;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Iterator iterator() {
			return projects.iterator();
		}

		@Override
		public int size() {
			return projects.size();
		}

		@Override
		public Object[] toArray() {
			return projects.toArray(new Object[projects.size()]);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public List toList() {
			return projects;
		}
	}
}