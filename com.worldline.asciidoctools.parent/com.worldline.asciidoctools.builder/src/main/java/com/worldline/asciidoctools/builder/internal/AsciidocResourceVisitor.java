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
