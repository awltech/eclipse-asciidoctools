package com.worldline.asciidoctools.builder.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import com.worldline.asciidoctools.builder.AsciidocBuilderListeners;
import com.worldline.asciidoctools.builder.AsciidocConfiguration;

public class AsciidocProjectBuilder extends IncrementalProjectBuilder {

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		
		if (AsciidocInstance.INSTANCE.getAsciidoctor() == null) {
			return new IProject[0];
		}
		
		try {
			AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(getProject());
			return kind == IncrementalProjectBuilder.FULL_BUILD ? fullBuild(configuration, monitor) : incrementalBuild(configuration, monitor);
		} catch (Exception e) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
		}
		return new IProject[0];
	}

	private IProject[] fullBuild(AsciidocConfiguration configuration, IProgressMonitor monitor) throws CoreException, IOException {
		IFolder resourcesFolder = getProject().getFolder(new Path(configuration.getResourcesPath()));
		IFolder sourcesFolder = getProject().getFolder(new Path(configuration.getSourcesPath()));
		IFolder targetFolder = getProject().getFolder(new Path(configuration.getTargetPath()));
		monitor.subTask("Locating resources to process...");
		AsciidocResourceVisitor visitor = new AsciidocResourceVisitor(sourcesFolder, resourcesFolder);
		if (resourcesFolder.exists()) {
			resourcesFolder.accept(visitor);
		}
		if (sourcesFolder.exists()) {
			sourcesFolder.accept(visitor);
		}

		this.copy(resourcesFolder, visitor.getResourceFiles(), targetFolder, monitor);
		this.generate(sourcesFolder, visitor.getSourceFiles(), targetFolder, configuration, monitor);
		monitor.subTask("Refreshing target folder...");
		targetFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

		return new IProject[0];
	}

	private void generate(IFolder sourcesFolder, Set<IFile> sourceFiles, IFolder targetFolder, AsciidocConfiguration configuration, IProgressMonitor monitor) {

		OptionsBuilder optionsBuilder = OptionsBuilder.options().backend("html").headerFooter(true)
				.attributes(AttributesBuilder.attributes().attribute("stylesheet", configuration.getStylesheetPath()));
		for (IFile sourceFile : sourceFiles) {
			if (sourceFile.exists()) {
				monitor.subTask("Rendering " + sourceFile.getName() + "...");
				try {
					InputStreamReader reader = new InputStreamReader(sourceFile.getContents());
					IFile destinationFile = targetFolder.getFile(sourceFile.getLocation().makeRelativeTo(sourcesFolder.getLocation()).removeFileExtension()
							.addFileExtension("html"));
					if (!destinationFile.getParent().exists()) {
						mkdirs((IFolder) destinationFile.getParent());
					}
					File file = new File(destinationFile.getLocation().toString());
					file.createNewFile();
					FileWriter writer = new FileWriter(file);
					AsciidocInstance.INSTANCE.getAsciidoctor().convert(reader, writer, optionsBuilder);
					writer.close();
					reader.close();
					AsciidocBuilderListeners.INSTANCE.notifyBuild(destinationFile);
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				} catch (IOException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	}

	private void copy(IFolder resourcesFolder, Set<IFile> resourceFiles, IFolder targetFolder, IProgressMonitor monitor) {
		IPath resourcesFolderLocation = resourcesFolder.getLocation();
		for (IFile resourceFile : resourceFiles) {
			IPath resourceFileLocation = resourceFile.getLocation();
			if (resourcesFolderLocation.isPrefixOf(resourceFileLocation)) {
				IFile destinationFile = targetFolder.getFile(resourceFileLocation.makeRelativeTo(resourcesFolderLocation));
				try {
					if (!destinationFile.getParent().exists()) {
						mkdirs((IFolder) destinationFile.getParent());
					}
					monitor.subTask("Copying resource " + destinationFile.getName() + " to target...");
					resourceFile.copy(destinationFile.getFullPath(), true, new NullProgressMonitor());
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
	}

	private IProject[] incrementalBuild(AsciidocConfiguration configuration, IProgressMonitor monitor) throws CoreException, IOException {
		IResourceDelta delta = getDelta(getProject());
		if (delta == null) {
			return this.fullBuild(configuration, monitor);
		}

		IFolder resourcesFolder = getProject().getFolder(new Path(configuration.getResourcesPath()));
		IFolder sourcesFolder = getProject().getFolder(new Path(configuration.getSourcesPath()));
		IFolder targetFolder = getProject().getFolder(new Path(configuration.getTargetPath()));

		monitor.subTask("Locating resources to process...");
		AsciidocResourceVisitor visitor = new AsciidocResourceVisitor(sourcesFolder, resourcesFolder);
		delta.accept(visitor);

		this.copy(resourcesFolder, visitor.getResourceFiles(), targetFolder, monitor);
		this.generate(sourcesFolder, visitor.getSourceFiles(), targetFolder, configuration, monitor);
		monitor.subTask("Refreshing target folder...");

		return new IProject[0];
	}

	private void mkdirs(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				mkdirs((IFolder) parent);
			}
			folder.create(false, true, new NullProgressMonitor());
		}
	}
}
