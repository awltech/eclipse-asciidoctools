package com.worldline.asciidoctools.builder;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;

public enum AsciidocBuilderListeners {

	INSTANCE;

	private Collection<AsciidocBuilderListener> listeners = new ArrayList<AsciidocBuilderListener>();

	public void register(AsciidocBuilderListener listener) {
		this.listeners.add(listener);
	}

	public void unregister(AsciidocBuilderListener listener) {
		this.listeners.remove(listener);
	}

	public void notifyBuild(IFile file) {
		for (AsciidocBuilderListener listener : this.listeners) {
			listener.onBuild(file);
		}
	}

	public void notify(Iterable<IFile> files) {
		for (IFile file : files) {
			this.notifyBuild(file);
		}
	}

}
