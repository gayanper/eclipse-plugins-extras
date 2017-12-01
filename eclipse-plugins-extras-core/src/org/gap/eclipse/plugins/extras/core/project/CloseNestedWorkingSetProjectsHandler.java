package org.gap.eclipse.plugins.extras.core.project;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;

public class CloseNestedWorkingSetProjectsHandler extends CloseNestedProjectsHandler {

	@Override
	protected List<IProject> resolve(IStructuredSelection selection) {
		final IWorkingSet workingSet = (IWorkingSet) selection.getFirstElement();
		return Stream.of(workingSet.getElements()).map(a -> a.getAdapter(IProject.class)).filter(p -> p.isOpen())
				.collect(Collectors.toList());
	}
}