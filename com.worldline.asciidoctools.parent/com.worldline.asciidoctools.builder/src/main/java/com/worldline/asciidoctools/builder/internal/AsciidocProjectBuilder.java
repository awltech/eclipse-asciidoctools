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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
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

	private void generate(IFolder sourcesFolder, Set<IFile> sourceFiles, IFolder targetFolder, AsciidocConfiguration configuration,
			IProgressMonitor monitor) {

		OptionsBuilder optionsBuilder = OptionsBuilder.options().backend("html").headerFooter(true)
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
					AsciidocBuilderLogger.info(
							"File at " + sourceFile.getFullPath().toString() + " rendered in " + (System.nanoTime() - begin) / 1000000L + " ms.");
					AsciidocBuilderListeners.INSTANCE.notifyBuild(destinationFile);
				} catch (CoreException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
				} catch (IOException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
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
					if (destinationFile.exists()) {
						destinationFile.delete(true, new NullProgressMonitor());
					}
					resourceFile.copy(destinationFile.getFullPath(), true, new NullProgressMonitor());
					AsciidocBuilderLogger
							.info("File at " + resourceFile.getFullPath().toString() + " copied to output at " + destinationFile.getFullPath() + ".");
					AsciidocBuilderListeners.INSTANCE.notifyBuild(destinationFile);
				} catch (CoreException e) {
					AsciidocBuilderLogger.warn(e.getMessage(), e);
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
