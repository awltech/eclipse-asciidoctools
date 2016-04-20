package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public enum AsciidocBlocksAndHeadersProposals {
	TITLE_LEVEL1_COMPLETION("Addition of 1st level title", "= "),
	TITLE_LEVEL2_COMPLETION("Addition of 2nd level title", "== "),
	TITLE_LEVEL3_COMPLETION("Addition of 3rd level title", "=== "),
	SEPARATOR_COMPLETION("Addition of separator", "\n''''\n\n"),
	PASSTHROUGH_COMPLETION("Addition of passthrough block", "\n++++\n\n++++\n", 6),
	QUOTE_COMPLETION("Addition of quote block", "\n____\n\n____\n", 6),
	LITERAL_COMPLETION("Addition of literal block", "\n....\n\n....\n", 6),
	SOURCE_COMPLETION("Addition of source block", "\n----\n\n----\n", 6)
	;

	private final String message;
	private final String contents;
	private final int cursor;

	private AsciidocBlocksAndHeadersProposals(String message, String contents, int cursor) {
		this.message = message;
		this.contents = contents;
		this.cursor = cursor;
	}
	
	private AsciidocBlocksAndHeadersProposals(String message, String contents) {
		this(message, contents, contents.length());
	}

	public ICompletionProposal toCompletionProposal(IDocument document, int offset) {
		return new CompletionProposal(this.contents, offset, 0, this.cursor, null,
				this.message, null, this.message);
	}

	public static Collection<ICompletionProposal> getValidCompletionProposals(IDocument document, int offset) {
		Collection<ICompletionProposal> proposals = new ArrayList<ICompletionProposal>();
		for (AsciidocBlocksAndHeadersProposals proposalElement : AsciidocBlocksAndHeadersProposals.values()) {
			proposals.add(proposalElement.toCompletionProposal(document, offset));
		}
		return proposals;
	}
}
