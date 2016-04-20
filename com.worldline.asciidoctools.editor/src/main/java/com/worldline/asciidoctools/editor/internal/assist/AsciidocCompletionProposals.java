package com.worldline.asciidoctools.editor.internal.assist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public enum AsciidocCompletionProposals {
	LINK_COMPLETION("link:/path/to/link[] - Addition of link", "link:[]",
			5), IMAGE_COMPLETION("image:/path/to/image[] - Addition of image", "image:[]", 6);

	private final String message;
	private final String contents;
	private final int cursor;

	private AsciidocCompletionProposals(String message, String contents, int cursor) {
		this.message = message;
		this.contents = contents;
		this.cursor = cursor;
	}

	private AsciidocCompletionProposals(String message, String contents) {
		this(message, contents, contents.length());
	}

	public ICompletionProposal toCompletionProposal(IDocument document, int offset, int replacement) {
		return new CompletionProposal(this.contents, offset, replacement, this.cursor, null, this.message, null,
				this.message);
	}

	public ICompletionProposal toCompletionProposal(IDocument document, int offset) {
		return toCompletionProposal(document, offset, 0);
	}
}
