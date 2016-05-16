package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;

import com.worldline.asciidoctools.editor.internal.Activator;
import com.worldline.asciidoctools.editor.internal.ComparableCompletionProposal;

/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public class AsciidocVariablesProposals {

	public static Collection<ComparableCompletionProposal> getValidCompletionProposals(IDocument document, int offset, String start) {
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		if (start.endsWith("{")) {
			proposals.addAll(getMatchingVariables(document, "{", offset));
		} else if (start.indexOf("{") > -1) {
			proposals.addAll(getMatchingVariables(document, start.substring(start.lastIndexOf("{")), offset));
		} else {
			proposals.addAll(getMatchingVariables(document, "", offset));
		}

		return proposals;
	}

	private static Collection<ComparableCompletionProposal> getMatchingVariables(IDocument document, String prefix, int offset) {
		String realPrefix = prefix.startsWith("{") ? prefix.substring(1) : prefix;
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			try {
				String line = document.get(document.getLineOffset(i), document.getLineLength(i));
				if (line.startsWith(":")) {
					int end = line.indexOf(":", 1);
					String variableName = line.substring(1, end).replace(" ", "").toLowerCase();
					if (realPrefix.length() == 0 || variableName.startsWith(realPrefix)) {
						proposals.add(toCompletionProposal(document, offset, prefix.length(), variableName));
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return proposals;
	}

	public static ComparableCompletionProposal toCompletionProposal(IDocument document, int offset, int replacement, String variable) {
		String replacementString = "{" + variable + "}";
		return new ComparableCompletionProposal(new CompletionProposal(replacementString, offset - replacement, replacement,
				replacementString.length(), Activator.getDefault().getImage("/icons/completion-variable.png"),
				replacementString + " - Add variable to document", null, null), replacement);
	}
}
