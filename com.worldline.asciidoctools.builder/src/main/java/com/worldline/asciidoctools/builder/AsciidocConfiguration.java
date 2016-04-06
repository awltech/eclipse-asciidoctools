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
package com.worldline.asciidoctools.builder;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.osgi.service.prefs.BackingStoreException;

import com.worldline.asciidoctools.builder.internal.Activator;

/**
 * 
 * Asciidoc Project Configuration pojo. Contains information about the
 * source/resources location, where to put generated doc etc...
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocConfiguration {

	private static final String TARGETPATH_DEFAULT = "target/generated-docs";

	private static final String TARGETPATH_PROPERTY = "targetPath";

	private static final String STYLESHEETPATH_DEFAULT = "css/stylesheet.css";

	private static final String STYLESHEETPATH_PROPERTY = "stylesheetPath";

	private static final String SOURCESPATH_DEFAULT = "src/main/asciidoc";

	private static final String SOURCESPATH_PROPERTY = "sourcesPath";

	private static final String RESOURCESPATH_DEFAULT = "src/main/doc-resources";

	private static final String RESOURCESPATH_PROPERTY = "resourcesPath";

	private static final String BACKEND_PROPERTY = "backend";

	private static final String BACKEND_DEFAULT = "html";

	private String sourcesPath = SOURCESPATH_DEFAULT;

	private String resourcesPath = RESOURCESPATH_DEFAULT;

	private String targetPath = TARGETPATH_DEFAULT;

	private String stylesheetPath = STYLESHEETPATH_DEFAULT;

	private String backend = BACKEND_DEFAULT;

	public String getBackend() {
		return backend;
	}

	public void setBackend(String backend) {
		this.backend = backend;
	}

	public String getSourcesPath() {
		return sourcesPath;
	}

	public void setSourcesPath(String sourcesPath) {
		this.sourcesPath = sourcesPath;
	}

	public String getResourcesPath() {
		return resourcesPath;
	}

	public void setResourcesPath(String resourcesPath) {
		this.resourcesPath = resourcesPath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
	}

	public String getStylesheetPath() {
		return stylesheetPath;
	}

	public void setStylesheetPath(String stylesheetPath) {
		this.stylesheetPath = stylesheetPath;
	}

	public static AsciidocConfiguration getConfiguration(IProject project) {
		AsciidocConfiguration configuration = new AsciidocConfiguration();
		if (!updateConfigurationFromMaven(configuration, project)) {
			if (!updateConfigurationFromProperties(configuration, project)) {
				updateConfigurationFromSettings(configuration, project);
			}
		}
		return configuration;
	}

	private static IFile getFileFromList(IProject project, String... fileNames) {
		for (String fileName : fileNames) {
			IFile file = project.getFile(fileName);
			if (file.exists()) {
				return file;
			}
		}
		return null;
	}

	private static boolean updateConfigurationFromProperties(AsciidocConfiguration configuration, IProject project) {
		IFile file = getFileFromList(project, "asciidoctools.properties", "conf/asciidoctools.properties");
		if (file == null || !file.exists()) {
			return false;
		}

		Properties properties = new Properties();
		try {
			properties.load(file.getContents());
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
			return false;
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
			return false;
		}

		String backend = properties.getProperty(BACKEND_PROPERTY, BACKEND_DEFAULT);
		String resourcesPath = properties.getProperty(RESOURCESPATH_PROPERTY, RESOURCESPATH_DEFAULT);
		String sourcesPath = properties.getProperty(SOURCESPATH_PROPERTY, SOURCESPATH_DEFAULT);
		String stylesheetPath = properties.getProperty(STYLESHEETPATH_PROPERTY, STYLESHEETPATH_DEFAULT);
		String targetPath = properties.getProperty(TARGETPATH_PROPERTY, TARGETPATH_DEFAULT);

		configuration.setBackend(backend);
		configuration.setResourcesPath(resourcesPath);
		configuration.setSourcesPath(sourcesPath);
		configuration.setStylesheetPath(stylesheetPath);
		configuration.setTargetPath(targetPath);

		return true;
	}

	private static void createSettings(IProject project, String backend, String resourcesPath, String sourcesPath, String stylesheetPath,
			String targetPath) throws BackingStoreException {
		final IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(Activator.PLUGIN_ID);
		if (preferences.keys().length == 0) {
			preferences.put(BACKEND_PROPERTY, backend);
			preferences.put(RESOURCESPATH_PROPERTY, resourcesPath);
			preferences.put(SOURCESPATH_PROPERTY, sourcesPath);
			preferences.put(STYLESHEETPATH_PROPERTY, stylesheetPath);
			preferences.put(TARGETPATH_PROPERTY, targetPath);
			preferences.flush();
		}
	}

	private static boolean updateConfigurationFromSettings(AsciidocConfiguration configuration, IProject project) {
		final IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences preferences = projectScope.getNode(Activator.PLUGIN_ID);
		try {
			createSettings(project, BACKEND_DEFAULT, RESOURCESPATH_DEFAULT, SOURCESPATH_DEFAULT, STYLESHEETPATH_DEFAULT, TARGETPATH_DEFAULT);
			configuration.setBackend(preferences.get(BACKEND_PROPERTY, BACKEND_DEFAULT));
			configuration.setResourcesPath(preferences.get(RESOURCESPATH_PROPERTY, RESOURCESPATH_DEFAULT));
			configuration.setSourcesPath(preferences.get(SOURCESPATH_PROPERTY, SOURCESPATH_DEFAULT));
			configuration.setStylesheetPath(preferences.get(STYLESHEETPATH_PROPERTY, STYLESHEETPATH_DEFAULT));
			configuration.setTargetPath(preferences.get(TARGETPATH_PROPERTY, TARGETPATH_DEFAULT));
		} catch (BackingStoreException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
			return false;
		}
		return true;
	}

	private static boolean updateConfigurationFromMaven(AsciidocConfiguration configuration, IProject project) {
		// Get the asciidoc configuration
		IMavenProjectFacade MavenProjectFacade = MavenPlugin.getMavenProjectRegistry().getProject(project);
		if (MavenProjectFacade == null) {
			return false;
		}

		Plugin asciidocPlugin = null;
		Plugin antCopyPlugin = null;
		if (MavenProjectFacade != null && MavenProjectFacade.getMavenProject() != null) {
			Model model = MavenProjectFacade.getMavenProject().getModel();
			if (model.getBuild() != null) {
				for (Iterator<Plugin> iterator = model.getBuild().getPlugins().iterator(); iterator.hasNext()
						&& (asciidocPlugin == null || antCopyPlugin == null);) {
					Plugin plugin = iterator.next();
					if ("org.asciidoctor".equals(plugin.getGroupId()) && "asciidoctor-maven-plugin".equals(plugin.getArtifactId())) {
						asciidocPlugin = plugin;
					}
					if ("org.apache.maven.plugins".equals(plugin.getGroupId()) && "maven-antrun-plugin".equals(plugin.getArtifactId())) {
						antCopyPlugin = plugin;
					}
				}
			}
		}

		if (asciidocPlugin != null) {
			PluginExecution pluginExecution = null;
			for (Iterator<PluginExecution> iterator = asciidocPlugin.getExecutions().iterator(); iterator.hasNext() && pluginExecution == null;) {
				PluginExecution temp = iterator.next();
				if (temp.getGoals().contains("process-asciidoc")) {
					pluginExecution = temp;
				}
			}

			if (pluginExecution != null && pluginExecution.getConfiguration() instanceof Xpp3Dom) {
				Xpp3Dom xpp3DomConfiguration = (Xpp3Dom) pluginExecution.getConfiguration();
				if (xpp3DomConfiguration.getChild("sourceDirectory") != null) {
					configuration.setSourcesPath(xpp3DomConfiguration.getChild("sourceDirectory").getValue());
				}
				if (xpp3DomConfiguration.getChild(BACKEND_PROPERTY) != null) {
					configuration.setBackend(xpp3DomConfiguration.getChild(BACKEND_PROPERTY).getValue());
				}
				if (xpp3DomConfiguration.getChild("outputDirectory") != null) {
					configuration.setTargetPath(xpp3DomConfiguration.getChild("outputDirectory").getValue());
				}
				if (xpp3DomConfiguration.getChild("attributes") != null
						&& xpp3DomConfiguration.getChild("attributes").getChild("stylesheet") != null) {
					configuration.setStylesheetPath(xpp3DomConfiguration.getChild("attributes").getChild("stylesheet").getValue());
				}
			}
		}

		if (antCopyPlugin != null) {
			PluginExecution pluginExecution = null;
			for (Iterator<PluginExecution> iterator = antCopyPlugin.getExecutions().iterator(); iterator.hasNext() && pluginExecution == null;) {
				PluginExecution temp = iterator.next();
				if (temp.getGoals().contains("run")) {
					pluginExecution = temp;
				}
			}

			if (pluginExecution != null && pluginExecution.getConfiguration() instanceof Xpp3Dom) {
				Xpp3Dom xpp3DomConfiguration = (Xpp3Dom) pluginExecution.getConfiguration();

				if (xpp3DomConfiguration.getChild("target") != null) {
					if (xpp3DomConfiguration.getChild("target").getChild("copy") != null) {
						if (xpp3DomConfiguration.getChild("target").getChild("copy").getChild("fileset") != null) {
							configuration.setResourcesPath(
									xpp3DomConfiguration.getChild("target").getChild("copy").getChild("fileset").getAttribute("dir"));
						}
					}
				}
			}
		}
		return true;
	}

}
