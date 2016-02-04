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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * Eclipse Asciidoc Resource visitor that retrieves files that are modified/added for Asciidoc-related refresh
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocResourceVisitor implements IResourceDeltaVisitor, IResourceVisitor {

	private final IFolder sourceFolder;
	private final IFolder resourceFolder;

	private final Set<IFile> sourceFiles = new HashSet<>();
	private final Set<IFile> resourceFiles = new HashSet<>();

	public Set<IFile> getSourceFiles() {
		return sourceFiles;
	}

	public Set<IFile> getResourceFiles() {
		return resourceFiles;
	}

	public AsciidocResourceVisitor(IFolder sourceFolder, IFolder resourceFolder) {
		this.sourceFolder = sourceFolder;
		this.resourceFolder = resourceFolder;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		if (resource instanceof IFile) {
			this.processFile((IFile) resource);
		}
		return true;
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IFile) {
			this.processFile((IFile) resource);
		}
		return true;
	}

	public void processFile(IFile file) {
		if (this.sourceFolder.getLocation().isPrefixOf(file.getLocation())) {
			if ("ad".equals(file.getFileExtension()) || "asciidoc".equals(file.getFileExtension())) {
				this.sourceFiles.add(file);
			}
		}
		if (this.resourceFolder.getLocation().isPrefixOf(file.getLocation())) {
			this.resourceFiles.add(file);
		}
	}

}
