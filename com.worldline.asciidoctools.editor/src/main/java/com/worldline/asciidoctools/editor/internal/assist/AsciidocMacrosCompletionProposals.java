package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public enum AsciidocMacrosCompletionProposals {
	LINK_COMPLETION("link:/path/to/link[] - Addition of link", "link:[]", "link:",5),
	IMAGE_COMPLETION("image:/path/to/image[] - Addition of image", "image:[]", "image:", 6),
	MAILTO_COMPLETION("mailto:aaa@bbb.com[] - Addition of mailto link", "mailto:[]", "image:", 7);

	private final String message;
	private final String contents;
	private final int cursor;
	private final String trigger;

	private AsciidocMacrosCompletionProposals(String message, String contents, String trigger, int cursor) {
		this.message = message;
		this.contents = contents;
		this.cursor = cursor;
		this.trigger = trigger;
	}

	private AsciidocMacrosCompletionProposals(String message, String contents, String trigger) {
		this(message, contents, trigger, contents.length());
	}

	public ICompletionProposal toCompletionProposal(IDocument document, int offset, int replacement) {
		return new CompletionProposal(this.contents.substring(replacement), offset, 0, this.cursor - replacement, null,
				this.message, null, this.message);
	}

	public ICompletionProposal toCompletionProposal(IDocument document, int offset) {
		return toCompletionProposal(document, offset, 0);
	}

	public static Collection<ICompletionProposal> getValidCompletionProposals(IDocument document, int offset,
			String start) {
		Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		if ("".equals(start)) {
			for (AsciidocMacrosCompletionProposals proposalElement : AsciidocMacrosCompletionProposals.values()) {
				proposals.add(proposalElement.toCompletionProposal(document, offset));
			}
		} else {
			for (AsciidocMacrosCompletionProposals proposalElement : AsciidocMacrosCompletionProposals.values()) {
				if (proposalElement.trigger.startsWith(start)) {
					proposals.add(proposalElement.toCompletionProposal(document, offset, start.length()));
				}
			}
		}
		return proposals;
	}
}
