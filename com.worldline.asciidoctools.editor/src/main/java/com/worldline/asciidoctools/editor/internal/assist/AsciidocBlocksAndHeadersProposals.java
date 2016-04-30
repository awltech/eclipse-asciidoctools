package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

import com.worldline.asciidoctools.editor.internal.Activator;

/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public enum AsciidocBlocksAndHeadersProposals {
	VARIABLE_COMPLETION("New variable", ":: ", 1),
	TITLE_LEVEL1_COMPLETION("New 1st level title", "= "),
	TITLE_LEVEL2_COMPLETION("New 2nd level title", "== "),
	TITLE_LEVEL3_COMPLETION("New 3rd level title", "=== "),
	SEPARATOR_COMPLETION("New separator", "\n''''\n\n"),
	ANCHOR_COMPLETION("New anchor", "[[]]", 2),
	PASSTHROUGH_COMPLETION("New passthrough block", "\n++++\n\n++++\n", 6),
	QUOTE_COMPLETION("New quote", "\n____\n\n____\n", 6),
	LITERAL_COMPLETION("New literal block", "\n....\n\n....\n", 6),
	SOURCE_COMPLETION("New source block", "\n----\n\n----\n", 6),
	LIST_COMPLETION("New list", "\n\n* \n* \n* \n", 4),
	NUMBERED_LIST_COMPLETION("New numbered list","\n\n. \n. \n. \n ", 4),
	TABLE_COMPLETION("New table", "[]\n|===\n||\n||\n|===", 9)
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
		return new CompletionProposal(this.contents, offset, 0, this.cursor, Activator.getDefault().getImage("/icons/completion-block.png"),
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
