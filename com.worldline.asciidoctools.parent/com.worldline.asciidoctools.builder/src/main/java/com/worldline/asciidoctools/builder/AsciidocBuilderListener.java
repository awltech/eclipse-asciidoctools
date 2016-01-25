package com.worldline.asciidoctools.builder;

import org.eclipse.core.resources.IFile;

public interface AsciidocBuilderListener {

	void onBuild(IFile file);

}
