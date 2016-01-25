package com.worldline.asciidoctools.builder;

import java.util.Iterator;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IProject;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.IMavenProjectFacade;

public class AsciidocConfiguration {

	private String sourcesPath = "src/main/asciidoc";

	private String resourcesPath = "src/main/doc-resources";

	private String targetPath = "target/generated-docs";

	private String stylesheetPath = "css/stylesheet.css";

	private String backend = "html";

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

		// Get the asciidoc configuration
		IMavenProjectFacade MavenProjectFacade = MavenPlugin.getMavenProjectRegistry().getProject(project);
		if (MavenProjectFacade == null) {
			return configuration;
		}

		Plugin asciidocPlugin = null;
		Plugin antCopyPlugin = null;
		if (MavenProjectFacade != null && MavenProjectFacade.getMavenProject() != null) {
			Model model = MavenProjectFacade.getMavenProject().getModel();
			if (model.getBuild() != null) {
				for (Iterator<Plugin> iterator = model.getBuild().getPlugins().iterator(); iterator.hasNext() && (asciidocPlugin == null || antCopyPlugin == null);) {
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
				if (xpp3DomConfiguration.getChild("backend") != null) {
					configuration.setBackend(xpp3DomConfiguration.getChild("backend").getValue());
				}
				if (xpp3DomConfiguration.getChild("outputDirectory") != null) {
					configuration.setTargetPath(xpp3DomConfiguration.getChild("outputDirectory").getValue());
				}
				if (xpp3DomConfiguration.getChild("attributes") != null && xpp3DomConfiguration.getChild("attributes").getChild("stylesheet") != null) {
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
							configuration.setResourcesPath(xpp3DomConfiguration.getChild("target").getChild("copy").getChild("fileset").getAttribute("dir"));
						}
					}
				}
			}
		}
		return configuration;
	}

}
