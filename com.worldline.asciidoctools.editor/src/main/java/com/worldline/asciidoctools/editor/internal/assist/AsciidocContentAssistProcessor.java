package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.worldline.asciidoctools.editor.internal.ComparableCompletionProposal;

/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
// http://www.ibm.com/developerworks/library/os-ecca/
public class AsciidocContentAssistProcessor implements IContentAssistProcessor {

	@Override
	public ComparableCompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		List<ComparableCompletionProposal> results = new ArrayList<ComparableCompletionProposal>();
		IDocument document = viewer.getDocument();

		if (viewer.getSelectedRange().y == 0) {
			try {
				int position = getCurrentWordStart(document, offset);
				String start = document.get(position, offset - position);
				results.addAll(AsciidocVariablesProposals.getValidCompletionProposals(document, offset, start));
				if (isCurrentLineStart(document, offset)) {
					results.addAll(AsciidocBlocksAndHeadersProposals.getValidCompletionProposals(document, offset));
				}
				if (isCurrentLineStart(document, offset) || lineStartsWith(document, offset, "<<")) {
					results.addAll(AsciidocAnchorsProposals.getValidCompletionProposals(document, offset, start));
				}
				results.addAll(AsciidocMacrosCompletionProposals.getValidCompletionProposals(document, offset, start));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else {

		}
		Collections.sort(results);
		return results.toArray(new ComparableCompletionProposal[0]);
	}

	private boolean lineStartsWith(IDocument document, int offset, String string) {
		try {
			int lineOfOffset = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(lineOfOffset);
			if (lineOffset < offset) {
				return document.get(lineOffset, offset).trim().startsWith(string);	
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
	}

	private int getCurrentWordStart(IDocument document, int offset) {
		for (int i = offset - 1; i > 0; i--) {
			try {
				char c = document.getChar(i);
				if (c == ' ' || c == '\r' || c == '\n' || c == '\t') {
					return i + 1;
				}
			} catch (BadLocationException e) {
			}
		}
		return -1;
	}

	private boolean isCurrentLineStart(IDocument document, int offset) {
		try {
			int lineOfOffset = document.getLineOfOffset(offset);
			int lineOffset = document.getLineOffset(lineOfOffset);
			return offset == lineOffset;
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return new IContextInformation[0];
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[0];
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		return new char[0];
	}

	@Override
	public String getErrorMessage() {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
