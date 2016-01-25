package com.worldline.asciidoctools.scaffold.internal;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class MavenEnablerHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Retrieve useable input.
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		IProject project = null;

		if (currentSelection instanceof IStructuredSelection) {
			Object object = ((IStructuredSelection) currentSelection).getFirstElement();
			if (object instanceof IProject) {
				project = (IProject) object;
			} else if (object instanceof IJavaProject) {
				project = ((IJavaProject) object).getProject();
			}
		}

		if (project == null) {
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, "Project could not be resolved");
		}

		// Trigger job for modification
		MavenEnablerJob.schedule(project);

		// And returns
		return Status.OK_STATUS;
	}

}
