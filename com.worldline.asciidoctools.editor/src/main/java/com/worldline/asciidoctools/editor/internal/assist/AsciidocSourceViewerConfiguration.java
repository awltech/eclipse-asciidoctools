package com.worldline.asciidoctools.editor.internal.assist;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

//https://wiki.eclipse.org/FAQ_How_do_I_add_Content_Assist_to_my_editor%3F
//http://www.ibm.com/developerworks/library/os-ecca/
/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class AsciidocSourceViewerConfiguration extends TextSourceViewerConfiguration {

	public AsciidocSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistant contentAssistant = new ContentAssistant();
		IContentAssistProcessor processor = new AsciidocAssistProcessor();
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		return contentAssistant;
	}
}
