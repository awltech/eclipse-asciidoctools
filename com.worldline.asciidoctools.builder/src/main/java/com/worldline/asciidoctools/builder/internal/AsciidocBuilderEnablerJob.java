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

import java.util.Arrays;
import java.util.Iterator;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;

/**
 * 
 * Implementation that performs the update of the project on which we enable the project builder.
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocBuilderEnablerJob extends WorkspaceJob {

	public AsciidocBuilderEnablerJob() {
		super("Enabling Asciidoc Builder");
	}

	private IProject project;

	public AsciidocBuilderEnablerJob withProject(IProject project) {
		this.project = project;
		return this;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

		// Update the project description.
		boolean hasBuilder = false;
		IProjectDescription description = project.getDescription();
		for (int i = 0; i < description.getBuildSpec().length; i++) {
			ICommand command = description.getBuildSpec()[i];
			if (command != null && "com.worldline.asciidoctools.builder".equals(command.getBuilderName())) {
				hasBuilder = true;
			}
		}

		if (!hasBuilder) {

			ICommand newCommand = description.newCommand();
			newCommand.setBuilderName("com.worldline.asciidoctools.builder");
			ICommand[] copiedBuildSpec = Arrays.copyOf(description.getBuildSpec(), description.getBuildSpec().length + 1);
			copiedBuildSpec[copiedBuildSpec.length - 1] = newCommand;
			description.setBuildSpec(copiedBuildSpec);
			project.setDescription(description, monitor);
		}

		// Update the pom file.
		IFile file = project.getFile("pom.xml");
		if (file.exists()) {
			Model mavenModel = MavenPlugin.getMavenModelManager().readMavenModel(file);
			if (mavenModel != null) {
				Build build = getOrCreateBuild(mavenModel);
				Plugin plugin = getOrCreatePlugin(build);
				if (plugin.getConfiguration() == null) {
					plugin.setConfiguration(new Xpp3Dom("configuration"));
				}
				boolean modified = false;
				if (plugin.getConfiguration() instanceof Xpp3Dom) {
					Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
					Xpp3Dom additionalBuildcommands = dom.getChild("additionalBuildcommands");
					if (additionalBuildcommands == null) {
						additionalBuildcommands = new Xpp3Dom("additionalBuildcommands");
						dom.addChild(additionalBuildcommands);
					}

					boolean hasCommand = false;
					Xpp3Dom[] buildCommandChildren = additionalBuildcommands.getChildren("buildCommand");
					for (int i = 0; i < buildCommandChildren.length && !hasCommand; i++) {
						Xpp3Dom next = buildCommandChildren[i];
						if (next.getChild("name") != null && "com.worldline.asciidoctools.builder".equals(next.getChild("name").getValue())) {
							hasCommand = true;
						}
					}
					if (!hasCommand) {
						Xpp3Dom buildCommand = new Xpp3Dom("buildCommand");
						additionalBuildcommands.addChild(buildCommand);
						Xpp3Dom buildCommandName = new Xpp3Dom("name");
						buildCommand.addChild(buildCommandName);
						buildCommandName.setValue("com.worldline.asciidoctools.builder");
						buildCommand.addChild(new Xpp3Dom("arguments"));
						modified = true;
					}
				}
				if (modified) {
					updateMavenConfiguration(file, mavenModel);
				}
			}
		}
		project.refreshLocal(IResource.DEPTH_ONE, monitor);
		return Status.OK_STATUS;
	}

	private Plugin getOrCreatePlugin(Build build) {
		Plugin eclipsePlugin = null;
		for (Iterator<Plugin> iterator = build.getPlugins().iterator(); iterator.hasNext() && eclipsePlugin == null;) {
			Plugin next = iterator.next();
			if ("org.apache.maven.plugins".equals(next.getGroupId()) && "maven-eclipse-plugin".equals(next.getArtifactId())) {
				eclipsePlugin = next;
			}
		}

		if (eclipsePlugin == null) {
			eclipsePlugin = new Plugin();
			eclipsePlugin.setGroupId("org.apache.maven.plugins");
			eclipsePlugin.setArtifactId("maven-eclipse-plugin");
			eclipsePlugin.setVersion("2.8");
			build.getPlugins().add(eclipsePlugin);
		}

		return eclipsePlugin;
	}

	private static void updateMavenConfiguration(IFile pomFile, Model mavenModel) {
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			IFile backupFile = pomFile.getParent().getFile(new Path("pom.xml." + System.currentTimeMillis() + ".bak"));
			pomFile.move(backupFile.getFullPath(), true, monitor);
			MavenPlugin.getMavenModelManager().createMavenModel(pomFile, mavenModel);
			backupFile.delete(true, monitor);
		} catch (CoreException e) {
			AsciidocBuilderLogger.warn(e.getMessage(), e);
		}
	}

	private static Build getOrCreateBuild(Model mavenModel) {
		// Get or Creation build element
		Build build = mavenModel.getBuild();
		if (build == null) {
			build = new Build();
			mavenModel.setBuild(build);
		}
		return build;
	}

}
