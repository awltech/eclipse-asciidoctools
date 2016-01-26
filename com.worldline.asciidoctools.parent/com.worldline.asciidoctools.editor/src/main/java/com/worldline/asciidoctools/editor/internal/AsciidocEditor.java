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
import java.net.MalformedURLException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;

import com.worldline.asciidoctools.builder.AsciidocBuilderListener;
import com.worldline.asciidoctools.builder.AsciidocBuilderListeners;
import com.worldline.asciidoctools.builder.AsciidocConfiguration;

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
			IPath projectRelativePath = file.getProjectRelativePath();
			AsciidocConfiguration configuration = AsciidocConfiguration.getConfiguration(file.getProject());
			if (configuration != null) {
				IPath sourcesPath = new Path(configuration.getSourcesPath());
				IPath targetPath = new Path(configuration.getTargetPath());
				if (sourcesPath.isPrefixOf(projectRelativePath)) {
					IPath removeFirstSegments = projectRelativePath.removeFirstSegments(sourcesPath.segmentCount()).removeFileExtension()
							.addFileExtension(configuration.getBackend());
					IFile targetFile = file.getProject().getFolder(targetPath).getFile(removeFirstSegments);
					return targetFile;
				}
			}
		}
		return null;
	}

	@Override
	public void onBuild(IFile file) {
		IFile targetFile = getDestinationFile();
		if (targetFile != null && targetFile.equals(file)) {
			refreshBrowser(targetFile);
		}
	}
}
