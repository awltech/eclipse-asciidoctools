package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class AsciidocVariablesProposals {

	public static Collection<ICompletionProposal> getValidCompletionProposals(IDocument document, int offset,
			String start) {
		Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if (start.endsWith("{")) {
			proposals.addAll(getMatchingVariables(document, "{", offset));
		} else if (start.indexOf("{") > -1) {
			proposals.addAll(getMatchingVariables(document, start.substring(start.indexOf("{")), offset));
		} else {
			proposals.addAll(getMatchingVariables(document, "", offset));
		}

		return proposals;
	}

	private static Collection<ICompletionProposal> getMatchingVariables(IDocument document, String prefix, int offset) {
		 String realPrefix = prefix.startsWith("{") ? prefix.substring(1) : prefix;
		 Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
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

	public static ICompletionProposal toCompletionProposal(IDocument document, int offset, int replacement,
			String variable) {
		String replacementString = "{" + variable + "}";
		return new CompletionProposal(replacementString, offset - replacement, replacement,
				replacementString.length(), null, replacementString + " - Add variable to document", null, null);
	}
}
