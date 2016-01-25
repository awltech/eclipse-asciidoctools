package com.worldline.asciidoctools.scaffold.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.m2e.core.MavenPlugin;

public class MavenEnablerJob extends WorkspaceJob {

	private IProject project;

	public static void schedule(IProject project) {
		new MavenEnablerJob(project).schedule();
	}

	private MavenEnablerJob(IProject project) {
		super(UserMessages.TITLE.value());
		this.project = project;
	}

	@Override
	public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

		monitor.beginTask(UserMessages.TASK.value(), IProgressMonitor.UNKNOWN);

		// Perform first level validation
		monitor.subTask(UserMessages.SUBTASK_1.value());
		if (this.project == null) {
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, UserMessages.CANCEL_POMNOTFOUND.value());
		}

		IFile pomFile = this.project.getFile("pom.xml");
		if (pomFile == null || !pomFile.exists()) {
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, UserMessages.CANCEL_POMDOESNTEXIST.value());
		}

		Model mavenModel = MavenPlugin.getMavenModelManager().readMavenModel(pomFile);
		if (mavenModel == null) {
			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID, UserMessages.CANCEL_POMNOTPARSEABLE.value());
		}

		monitor.subTask(UserMessages.SUBTASK_2.value());
		boolean modified = false;

		if (MavenEnablerJob.addProperties(mavenModel, monitor).isOK()) {
			modified = true;
		}

		if (MavenEnablerJob.addCopyResources(mavenModel, monitor).isOK()) {
			modified = true;
		}

		if (MavenEnablerJob.addGenerateHtmlResources(mavenModel, monitor).isOK()) {
			modified = true;
		}

		if (modified) {
			monitor.subTask(UserMessages.SUBTASK_3.value());
			MavenEnablerJob.updateMavenConfiguration(pomFile, mavenModel);
			MavenEnablerJob.updateFoldersStructure(project, "/src/main/doc-resources/css", "/src/main/doc-resources/images",
					"/src/main/asciidoc");
			MavenEnablerJob.copyResource(project, "src/main/doc-resources/css/stylesheet.css", "/stylesheet.css");
			MavenEnablerJob.copyResource(project, "/src/main/asciidoc/index.ad", "/index.ad");
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		}

		monitor.subTask(UserMessages.SUBTASK_DONE.value());
		return Status.OK_STATUS;
	}

	private static IStatus addProperties(Model mavenModel, IProgressMonitor monitor) {
		Properties existingProperties = mavenModel.getProperties();
		if (existingProperties == null) {
			existingProperties = new Properties();
			mavenModel.setProperties(existingProperties);
		}

		try {
			Properties newProperties = new Properties();
			newProperties.load(MavenEnablerJob.class.getResourceAsStream("/maven-properties.properties"));
			for (Entry<Object, Object> entry : newProperties.entrySet()) {
				if (!existingProperties.containsKey(entry.getKey())) {
					existingProperties.put(entry.getKey(), entry.getValue());
				}
			}
		} catch (IOException e) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Failed to read properties file", e));
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}

	private static void copyResource(IProject project, String locationInDistantProject, String localResourcePath) {
		IFile file = project.getFile(new Path(locationInDistantProject));
		IProgressMonitor monitor = new NullProgressMonitor();
		try {
			MavenEnablerJob.createFolder((IFolder) file.getParent(), monitor);
			file.create(MavenEnablerJob.class.getResourceAsStream(localResourcePath), false, monitor);
		} catch (CoreException e) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_FILESTRUCTURE.value(), e));
		}
	}

	private static void updateFoldersStructure(IProject project, String... folderNames) {
		IProgressMonitor monitor = new NullProgressMonitor();
		for (String folderName : folderNames) {
			try {
				MavenEnablerJob.createFolder(project.getFolder(new Path(folderName)), monitor);
			} catch (CoreException e) {
				Activator.getDefault().getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_FILESTRUCTURE.value(), e));
			}
		}

	}

	private static void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
		if (folder == null || folder.exists()) {
			return;
		}
		IContainer parent = folder.getParent();
		if (parent instanceof IFolder && !((IFolder) parent).exists()) {
			MavenEnablerJob.createFolder((IFolder) parent, monitor);
		}
		folder.create(false, true, monitor);
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

	private static Plugin getOrCreateAntrunPlugin(Model mavenModel) {

		Build build = getOrCreateBuild(mavenModel);

		// Locate the plugin and returns if exists
		for (Iterator<Plugin> iterator = build.getPlugins().iterator(); iterator.hasNext();) {
			Plugin next = iterator.next();
			if ("maven-antrun-plugin".equals(next.getArtifactId())) {
				return next;
			}
		}

		// Creates if couldn't be found.
		Plugin antrunPlugin = new Plugin();
		antrunPlugin.setArtifactId("maven-antrun-plugin");
		antrunPlugin.setVersion("1.7");
		build.getPlugins().add(antrunPlugin);
		return antrunPlugin;
	}

	private static Xpp3Dom getConfiguration(String fileName) {
		InputStream resourceAsStream = MavenEnablerJob.class.getResourceAsStream(fileName);
		Xpp3Dom dom = null;
		try {
			if (resourceAsStream != null) {
				dom = Xpp3DomBuilder.build(resourceAsStream, "UTF-8", true);
			}
		} catch (XmlPullParserException e2) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_XMLREAD.value(), e2));
		} catch (IOException e2) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_XMLREAD.value(), e2));
		} finally {
			try {
				resourceAsStream.close();
			} catch (IOException e) {
				Activator.getDefault().getLog()
						.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_XMLREAD.value(), e));
			}
		}
		return dom;
	}

	private static IStatus addCopyResources(Model mavenModel, IProgressMonitor monitor) {

		Plugin plugin = getOrCreateAntrunPlugin(mavenModel);
		for (PluginExecution execution : plugin.getExecutions()) {
			if ("asciidoc-copy-resources".equals(execution.getId())) {
				return Status.CANCEL_STATUS;
			}
		}

		PluginExecution execution = new PluginExecution();
		execution.setId("asciidoc-copy-resources");
		execution.setPhase("compile");
		execution.getGoals().add("run");
		plugin.getExecutions().add(execution);

		execution.setConfiguration(MavenEnablerJob.getConfiguration("/asciidoc-copy-resources-configuration.xml"));

		return Status.OK_STATUS;
	}

	private static IStatus addGenerateHtmlResources(Model mavenModel, IProgressMonitor monitor) {

		Build build = getOrCreateBuild(mavenModel);
		Plugin plugin = null;

		for (Iterator<Plugin> iterator = build.getPlugins().iterator(); iterator.hasNext() && plugin == null;) {
			Plugin next = iterator.next();
			for (PluginExecution execution : next.getExecutions()) {
				if ("asciidoc-generate-html".equals(execution.getId())) {
					return Status.CANCEL_STATUS;
				}
			}
		}

		if (plugin == null) {
			plugin = new Plugin();
			plugin.setGroupId("org.asciidoctor");
			plugin.setArtifactId("asciidoctor-maven-plugin");
			plugin.setVersion("0.1.3.1");
			build.addPlugin(plugin);
		}
		PluginExecution execution = new PluginExecution();
		execution.setId("asciidoc-generate-html");
		execution.setPhase("compile");
		execution.getGoals().add("process-asciidoc");
		plugin.getExecutions().add(execution);

		execution.setConfiguration(MavenEnablerJob.getConfiguration("/asciidoc-generate-html.xml"));

		return Status.OK_STATUS;
	}

	private static void updateMavenConfiguration(IFile pomFile, Model mavenModel) {
		try {
			IProgressMonitor monitor = new NullProgressMonitor();
			IFile backupFile = pomFile.getParent().getFile(new Path("pom.xml." + System.currentTimeMillis() + ".bak"));
			pomFile.move(backupFile.getFullPath(), true, monitor);
			MavenPlugin.getMavenModelManager().createMavenModel(pomFile, mavenModel);
			backupFile.delete(true, monitor);
		} catch (CoreException e) {
			Activator.getDefault().getLog()
					.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, UserMessages.EXCEPTION_POMUPDATE.value(), e));
		}
	}

}
