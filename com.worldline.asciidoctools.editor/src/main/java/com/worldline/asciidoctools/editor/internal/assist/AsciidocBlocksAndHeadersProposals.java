/**
 * AsciidocTools by Worldline
 *
 * Copyright (C) 2016 Worldline or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.worldline.asciidoctools.editor.internal.assist;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;

import com.worldline.asciidoctools.editor.internal.Activator;
import com.worldline.asciidoctools.editor.internal.ComparableCompletionProposal;

/**
 * 
 * @author mvanbesien (mvaawl@gmail.com)
 *
 */
public enum AsciidocBlocksAndHeadersProposals {
	VARIABLE_COMPLETION(":: - Add variable", ":: ", 1), 
	TITLE_LEVEL1_COMPLETION("= - Add 1st level title", "= "), 
	TITLE_LEVEL2_COMPLETION("== - Add 2nd level title", "== "),
	TITLE_LEVEL3_COMPLETION("=== - Add 3rd level title","=== "), 
	SEPARATOR_COMPLETION("'''' - Add separator", "\n''''\n\n"), 
	ANCHOR_COMPLETION("[[]] - Add anchor", "[[]]", 2),
	PASSTHROUGH_COMPLETION("++++ - Add passthrough block", "\n++++\n\n++++\n", 6),
	QUOTE_COMPLETION("____ - Add quote", "\n____\n\n____\n", 6),
	LITERAL_COMPLETION(".... - Add literal block", "\n....\n\n....\n", 6), 
	SOURCE_COMPLETION("---- - Add source block", "\n----\n\n----\n", 6), 
	LIST_COMPLETION("* - Add list", "\n\n* \n* \n* \n", 4),
	NUMBERED_LIST_COMPLETION(". - Add numbered list", "\n\n. \n. \n. \n ", 4),
	TABLE_COMPLETION("|=== - Add table", "[]\n|===\n||\n||\n|===", 9);

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

	public ComparableCompletionProposal toCompletionProposal(IDocument document, int offset) {
		return new ComparableCompletionProposal(new CompletionProposal(this.contents, offset, 0, this.cursor,
				Activator.getDefault().getImage("/icons/completion-block.png"), this.message, null, this.message), 1);
	}

	public static Collection<ComparableCompletionProposal> getValidCompletionProposals(IDocument document, int offset) {
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		for (AsciidocBlocksAndHeadersProposals proposalElement : AsciidocBlocksAndHeadersProposals.values()) {
			proposals.add(proposalElement.toCompletionProposal(document, offset));
		}
		return proposals;
	}
}
