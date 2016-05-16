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
public class AsciidocAnchorsProposals {

	public static Collection<ComparableCompletionProposal> getValidCompletionProposals(IDocument document, int offset, String start) {
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		if (start.endsWith("<<")) {
			proposals.addAll(getMatchingVariables(document, "<<", offset));
		} else if (start.indexOf("<<") > -1) {
			proposals.addAll(getMatchingVariables(document, start.substring(start.indexOf("<<")), offset));
		} else {
			proposals.addAll(getMatchingVariables(document, "", offset));
		}

		return proposals;
	}

	private static Collection<ComparableCompletionProposal> getMatchingVariables(IDocument document, String prefix, int offset) {
		String realPrefix = prefix.startsWith("<<") ? prefix.substring(2) : prefix;
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		for (int i = 0; i < document.getNumberOfLines(); i++) {
			try {
				String line = document.get(document.getLineOffset(i), document.getLineLength(i));
				if (line.startsWith("[[")) {
					int end = line.indexOf("]]", 2);
					String variableName = line.substring(2,
							end)/* .replace(" ", "").toLowerCase() */;
					if (realPrefix.length() == 0 || variableName.startsWith(realPrefix)) {
						proposals.add(toCompletionProposal(document, offset, prefix.length(), variableName, prefix.length()));
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return proposals;
	}

	public static ComparableCompletionProposal toCompletionProposal(IDocument document, int offset, int replacement, String variable, int priority) {
		String replacementString = "<<" + variable + ",>>";
		return new ComparableCompletionProposal(
				new CompletionProposal(replacementString, offset - replacement, replacement, replacementString.length() - 2,
						Activator.getDefault().getImage("/icons/completion-gotoanchor.png"), replacementString + " - Add link to anchor", null, null),
				priority);
	}
}
