/**
 * AsciidocTools by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.worldline.asciidoctools.builder.internal;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.worldline.asciidoctools.builder.AsciidocBuildHelper;

/**
 * 
 * Asciidoc Project Builder
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocProjectBuilder extends IncrementalProjectBuilder {

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {

		if (AsciidocInstance.INSTANCE.getAsciidoctor() == null) {
			return new IProject[0];
		}

		try {
			if (kind == IncrementalProjectBuilder.FULL_BUILD) {
				AsciidocBuildHelper.fullBuild(getProject(), monitor);
			} else {
				AsciidocBuildHelper.incrementalBuild(getProject(), getDelta(getProject()), monitor);
			}
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return new IProject[0];
	}

}
