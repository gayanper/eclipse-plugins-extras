package org.gap.eclipse.plugins.extras.core.project;

import java.util.function.Consumer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

final class NestedProjectWalker {
	private IPath rootLocation;

	private NestedProjectWalker(IProject root) {
		this.rootLocation = root.getLocation();
	}

	public static NestedProjectWalker walkerFor(IProject root) {
		return new NestedProjectWalker(root);
	}

	public void forEach(Consumer<IProject> consumer) {
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (isNested(p)) {
				consumer.accept(p);
			}
		}
	}

	private boolean isNested(IProject p) {
		final IPath pLocation = p.getLocation();
		return (((pLocation.segmentCount() - rootLocation.segmentCount()) > 0) && rootLocation.isPrefixOf(pLocation));
	}
}
