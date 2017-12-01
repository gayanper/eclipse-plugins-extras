package org.gap.eclipse.plugins.extras.core.project;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.DeleteResourceAction;

public class DeleteNestedProjectsHandler extends BaseSelectionOperationHandler {

	public DeleteNestedProjectsHandler() {
		super(false);
	}

	@Override
	protected void executeOperation(List<IProject> projects, IProgressMonitor pm, IWorkbenchWindow window) {
		final Selection strucSelection = new Selection(projects);
		final DeleteResourceAction action = new DeleteResourceAction(window) {
			@Override
			public IStructuredSelection getStructuredSelection() {
				return strucSelection;
			}
		};
		action.run();
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