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
package com.worldline.asciidoctools.editor.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Collections;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ContentAssistAction;

import com.worldline.asciidoctools.builder.AsciidocBuildHelper;
import com.worldline.asciidoctools.builder.AsciidocBuilderListener;
import com.worldline.asciidoctools.builder.AsciidocBuilderListeners;
import com.worldline.asciidoctools.builder.AsciidocConfiguration;
import com.worldline.asciidoctools.editor.internal.assist.AsciidocSourceViewerConfiguration;

/**
 * 
 * Asciidoc Dual Screen Editor implementation
 * 
 * @author mvanbesien <mvaawl@gmail.com>
 * @since 0.1
 *
 */
public class AsciidocEditor extends TextEditor implements AsciidocBuilderListener {

	private Browser browser;

	private IEditorInput editorInput;

	private Composite editionZone;

	private Composite renderingZone;

	private Scale horizontalScale;

	private Scale verticalScale;

	private Composite background;

	private Button tiltButton;

	private Button refreshButton;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		this.editorInput = input;
		super.init(site, input);
		setSourceViewerConfiguration(new AsciidocSourceViewerConfiguration(getPreferenceStore()));
		AsciidocBuilderListeners.INSTANCE.register(this);
	}

	@Override
	public void dispose() {
		AsciidocBuilderListeners.INSTANCE.unregister(this);
		super.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {

		this.background = new Composite(parent, SWT.NONE);
		background.setLayout(new FormLayout());

		this.editionZone = new Composite(background, SWT.BORDER);
		editionZone.setLayout(new FillLayout());

		this.renderingZone = new Composite(background, SWT.BORDER);
		renderingZone.setLayout(new FillLayout());

		super.createPartControl(editionZone);
		this.browser = new Browser(renderingZone, SWT.NONE);

		// Listener added to prevent navigation using hyperlinks
		this.browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event) {
				if (((Browser) event.getSource()).getUrl() != null && !"about:blank".equals(((Browser) event.getSource()).getUrl())) {
					try {
						URI realFileURI = new File(getDestinationFile().getLocation().toString()).toURI();
						URI expectedFileURI = URI.create(event.location);
						event.doit = realFileURI.getPath().equals(expectedFileURI.getPath());
					} catch (Exception e) {
						Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
					}
				}
			}
		});

		this.horizontalScale = new Scale(background, SWT.HORIZONTAL);
		this.horizontalScale.setMinimum(5);
		this.horizontalScale.setMaximum(95);
		this.horizontalScale.setIncrement(5);
		this.horizontalScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Scale scale = (Scale) e.getSource();
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				if (scale.getSelection() != preferenceStore.getInt("editorRatio")) {
					preferenceStore.setValue("editorRatio", scale.getSelection());
					refreshLayout();
				}
			}
		});

		this.verticalScale = new Scale(background, SWT.VERTICAL);
		this.verticalScale.setMinimum(5);
		this.verticalScale.setMaximum(95);
		this.verticalScale.setIncrement(5);
		this.verticalScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Scale scale = (Scale) e.getSource();
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				if (scale.getSelection() != preferenceStore.getInt("editorRatio")) {
					preferenceStore.setValue("editorRatio", scale.getSelection());
					refreshLayout();
				}
			}
		});

		this.tiltButton = new Button(background, SWT.PUSH);
		this.tiltButton.setImage(Activator.getDefault().getImage("icons/tilt_icon.gif"));
		this.tiltButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				boolean verticalSplit = preferenceStore.getBoolean("verticalSplit");
				preferenceStore.setValue("verticalSplit", !verticalSplit);
				refreshLayout();
			}
		});

		this.refreshButton = new Button(background, SWT.PUSH);
		this.refreshButton.setImage(Activator.getDefault().getImage("icons/refresh_icon.gif"));
		this.refreshButton.setToolTipText("Refresh Rendering View");
		this.refreshButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshBrowser(getDestinationFile());
			}
		});

		// Mettre les images et les tooltips
		this.refreshLayout();
		this.refreshBrowser(getDestinationFile());

		// Workaround to find : disable the Ctrl+I binding for this editor ?
		// Add bold/italic shortcuts.
		this.getSourceViewer().getTextWidget().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == 'b' && (e.stateMask & SWT.CTRL) != 0) {
					toggleMarker(getSourceViewer(), "*");
				}
				if (e.keyCode == 't' && (e.stateMask & SWT.CTRL) != 0) {
					toggleMarker(getSourceViewer(), "_");
				}
			}
			
			private boolean toggleMarker(ISourceViewer sourceViewer, String marker) {
				try {
					Point selectedRange = sourceViewer.getSelectedRange();
					String selection = sourceViewer.getDocument().get(selectedRange.x, selectedRange.y);
					String previous = sourceViewer.getDocument().get(selectedRange.x - 1, 1).trim();
					String next = sourceViewer.getDocument().get(selectedRange.x + selectedRange.y, 1).trim();
					if (previous.endsWith(marker) && next.startsWith(marker)) {
						sourceViewer.getDocument().replace(selectedRange.x - 1, selectedRange.y + 2, selection);
					} else {
						sourceViewer.getDocument().replace(selectedRange.x, selectedRange.y, marker + selection + marker);
					}
					return true;
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
				return false;
			}
		});
	}

	public void refreshLayout() {

		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean verticalSplit = preferenceStore.getBoolean("verticalSplit");
		int editorRatio = preferenceStore.contains("editorRatio") ? preferenceStore.getInt("editorRatio") : 50;

		if (verticalSplit) {
			this.tiltButton.setToolTipText("Switch To Horizontal Split");
			this.verticalScale.setVisible(false);
			this.verticalScale.setToolTipText("");
			this.horizontalScale.setVisible(true);
			this.horizontalScale.setToolTipText(String.format("Edition Display Ratio: %s%%", editorRatio));
			FormDataBuilder.on(tiltButton).bottom().left().top(editionZone);
			FormDataBuilder.on(refreshButton).bottom().right().top(editionZone);
			FormDataBuilder.on(horizontalScale).bottom().left(tiltButton).right(refreshButton);
			FormDataBuilder.on(editionZone).left().right(editorRatio).top().bottom(horizontalScale);
			FormDataBuilder.on(renderingZone).left(editorRatio).right().top().bottom(horizontalScale);
		} else {
			this.tiltButton.setToolTipText("Switch To Vertical Split");
			this.verticalScale.setVisible(true);
			this.verticalScale.setToolTipText(String.format("Edition Display Ratio: %s%%", editorRatio));
			this.horizontalScale.setVisible(false);
			this.horizontalScale.setToolTipText("");
			FormDataBuilder.on(tiltButton).top().right().left(editionZone);
			FormDataBuilder.on(refreshButton).bottom().right().left(editionZone);
			FormDataBuilder.on(verticalScale).right().top(tiltButton).bottom(refreshButton);
			FormDataBuilder.on(editionZone).left().right(verticalScale).top().bottom(editorRatio);
			FormDataBuilder.on(renderingZone).left().right(verticalScale).bottom().top(editorRatio);
		}

		this.verticalScale.setSelection(editorRatio);
		this.horizontalScale.setSelection(editorRatio);

		// this.background.update();
		this.background.layout(true);
	}

	public void refreshBrowser(IFile targetFile) {
		if (targetFile != null) {
			final File targetFSFile = new File(targetFile.getLocation().toString());
			if (targetFSFile.exists()) {

				browser.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							browser.setUrl(targetFSFile.toURI().toURL().toString());
							browser.refresh();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
	}

	private IFile getDestinationFile() {
		if (this.editorInput instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) this.editorInput;
			IFile file = input.getFile();
			AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(file.getProject());
			if (configuration != null) {
				return findMatchingFile(configuration, file);
			}
		}
		return null;
	}

	private static final IFile findMatchingFile(AsciidocConfiguration configuration, IFile inputFile) {
		IPath sourcesPath = new Path(configuration.getSourcesPath());
		IPath targetPath = new Path(configuration.getTargetPath());
		IPath fileParentPath = inputFile.getParent().getProjectRelativePath();
		if (sourcesPath.isPrefixOf(fileParentPath)) {
			IPath outputFolderPath = targetPath.append(fileParentPath.removeFirstSegments(sourcesPath.segmentCount()));
			IFolder outputFolder = inputFile.getProject().getFolder(outputFolderPath);
			if (outputFolder.exists()) {
				try {
					for (IResource resource : outputFolder.members()) {
						if (resource.exists() && resource instanceof IFile && resource.isAccessible()) {
							IFile matchingFile = (IFile) resource;
							if (matchingFile.getFullPath().removeFileExtension().lastSegment()
									.equals(inputFile.getFullPath().removeFileExtension().lastSegment())) {
								return matchingFile;
							}
						}
					}
				} catch (CoreException e) {
					Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage(), e));
				}
			}
		}
		return null;
	}

	private IFile getStyleSheetFile() {
		if (this.editorInput instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) this.editorInput;
			IFile file = input.getFile();
			AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(file.getProject());
			if (configuration != null) {
				IPath targetPath = new Path(configuration.getTargetPath()).append(configuration.getStylesheetPath());
				return file.getProject().getFile(targetPath);
			}
		}
		return null;
	}

	@Override
	public void onBuild(IFile file) {
		IFile targetFile = getDestinationFile();
		if (targetFile != null) {
			if (targetFile.equals(file) || getStyleSheetFile().equals(file)) {
				refreshBrowser(targetFile);
			}
		}
	}

	@Override
	protected void createActions() {
		super.createActions();
		IAction action = new ContentAssistAction(ResourceBundle.getBundle("AsciidocEditorAction"), "ContentAssistProposal.", this);
		String id = Activator.PLUGIN_ID.concat(".contentassist");
		action.setActionDefinitionId(id);
		setAction("ContentAssistProposal", action);
		markAsStateDependentAction("ContentAssistProposal", true);
		setActionActivationCode(id, ' ', -1, SWT.CTRL);
	}

	@Override
	public void doSave(final IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if (this.editorInput instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) this.editorInput;
			final IFile file = input.getFile();
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						AsciidocBuildHelper.filesBuild(file.getProject(), Collections.singleton(file), progressMonitor);
						onBuild(file);
					} catch (CoreException e) {
						Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Error while rebuilding file", e));
					} catch (IOException e) {
						Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, "Error while rebuilding file", e));
					}
				}
			});

		}

	}
}
