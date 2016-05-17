package com.worldline.asciidoctools.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.AttributesBuilder;
import org.asciidoctor.OptionsBuilder;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.worldline.asciidoctools.builder.internal.AsciidocBuilderLogger;
import com.worldline.asciidoctools.builder.internal.AsciidocInstance;
import com.worldline.asciidoctools.builder.internal.AsciidocResourceVisitor;

public final class AsciidocBuildHelper {

	private AsciidocBuildHelper() {
	}

	public static void fullBuild(IProject project, IProgressMonitor monitor) throws CoreException, IOException {
		AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(project);
		IFolder resourcesFolder = project.getFolder(new Path(configuration.getResourcesPath()));
		IFolder sourcesFolder = project.getFolder(new Path(configuration.getSourcesPath()));
		IFolder targetFolder = project.getFolder(new Path(configuration.getTargetPath()));
		monitor.subTask("Locating resources to process...");
		AsciidocResourceVisitor visitor = new AsciidocResourceVisitor(sourcesFolder, resourcesFolder);
		if (resourcesFolder.exists()) {
			resourcesFolder.accept(visitor);
		}
		if (sourcesFolder.exists()) {
			sourcesFolder.accept(visitor);
		}

		copy(resourcesFolder, visitor.getResourceFiles(), targetFolder, monitor);
		generate(sourcesFolder, visitor.getSourceFiles(), targetFolder, configuration, monitor);
		monitor.subTask("Refreshing target folder...");
		targetFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

	}

	private static void generate(IFolder sourcesFolder, Set<IFile> sourceFiles, IFolder targetFolder, AsciidocConfiguration configuration,
			IProgressMonitor monitor) {

		OptionsBuilder optionsBuilder = OptionsBuilder.options().backend(configuration.getBackend()).headerFooter(true)
				.attributes(AttributesBuilder.attributes().attribute("stylesheet", configuration.getStylesheetPath()));
		for (IFile sourceFile : sourceFiles) {
			if (sourceFile.exists()) {
				monitor.subTask("Rendering " + sourceFile.getName() + "...");
				try {
					InputStreamReader reader = new InputStreamReader(sourceFile.getContents());
					IFile destinationFile = targetFolder.getFile(
							sourceFile.getLocation().makeRelativeTo(sourcesFolder.getLocation()).removeFileExtension().addFileExtension("html"));
					if (!destinationFile.getParent().exists()) {
						mkdirs((IFolder) destinationFile.getParent());
					}
					File file = new File(destinationFile.getLocation().toString());
					file.createNewFile();
					FileWriter writer = new FileWriter(file);
					monitor.subTask("Loading Asciidoctor Instance...");
					Asciidoctor asciidoctor = AsciidocInstance.INSTANCE.getAsciidoctor();
					monitor.subTask("Rendering " + sourceFile.getName() + "...");
					long begin = System.nanoTime();
					asciidoctor.convert(reader, writer, optionsBuilder);
					writer.close();
					reader.close();
					monitor.subTask(sourceFile.getName() + " rendered successfully...");
					AsciidocBuilderLogger
							.ok("File at " + sourceFile.getFullPath().toString() + " rendered in " + (System.nanoTime() - begin) / 1000000L + " ms.");
					AsciidocBuilderListeners.INSTANCE.notifyBuild(destinationFile);
				} catch (CoreException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
				} catch (IOException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
				}
			}
		}
	}

	private static void copy(IFolder resourcesFolder, Set<IFile> resourceFiles, IFolder targetFolder, IProgressMonitor monitor) {
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
					if (destinationFile.exists()) {
						destinationFile.delete(true, new NullProgressMonitor());
					}
					resourceFile.copy(destinationFile.getFullPath(), true, new NullProgressMonitor());
					AsciidocBuilderLogger
							.ok("File at " + resourceFile.getFullPath().toString() + " copied to output at " + destinationFile.getFullPath() + ".");
					AsciidocBuilderListeners.INSTANCE.notifyBuild(destinationFile);
				} catch (CoreException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
				}
			}
		}
	}

	public static void incrementalBuild(IProject project, IResourceDelta delta, IProgressMonitor monitor) throws CoreException, IOException {
		AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(project);
		if (delta == null) {
			fullBuild(project, monitor);
			return;
		}

		IFolder resourcesFolder = project.getFolder(new Path(configuration.getResourcesPath()));
		IFolder sourcesFolder = project.getFolder(new Path(configuration.getSourcesPath()));
		IFolder targetFolder = project.getFolder(new Path(configuration.getTargetPath()));

		monitor.subTask("Locating resources to process...");
		AsciidocResourceVisitor visitor = new AsciidocResourceVisitor(sourcesFolder, resourcesFolder);
		delta.accept(visitor);

		copy(resourcesFolder, visitor.getResourceFiles(), targetFolder, monitor);
		generate(sourcesFolder, visitor.getSourceFiles(), targetFolder, configuration, monitor);
		monitor.subTask("Refreshing target folder...");

	}
	
	public static void filesBuild(IProject project, Set<IFile> resourcesToBuild, IProgressMonitor monitor) throws CoreException, IOException {
		AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(project);

		IFolder resourcesFolder = project.getFolder(new Path(configuration.getResourcesPath()));
		IFolder sourcesFolder = project.getFolder(new Path(configuration.getSourcesPath()));
		IFolder targetFolder = project.getFolder(new Path(configuration.getTargetPath()));

		monitor.subTask("Locating resources to process...");
		
		AsciidocResourceVisitor visitor = new AsciidocResourceVisitor(sourcesFolder, resourcesFolder);
		for (IFile file : resourcesToBuild) {
			visitor.processFile(file);
		}

		copy(resourcesFolder, visitor.getResourceFiles(), targetFolder, monitor);
		generate(sourcesFolder, visitor.getSourceFiles(), targetFolder, configuration, monitor);
		monitor.subTask("Refreshing target folder...");

	}

	private static void mkdirs(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				mkdirs((IFolder) parent);
			}
			folder.create(false, true, new NullProgressMonitor());
		}
	}

}
