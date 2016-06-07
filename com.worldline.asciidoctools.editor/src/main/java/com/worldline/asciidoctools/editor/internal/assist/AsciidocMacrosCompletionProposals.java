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
public enum AsciidocMacrosCompletionProposals {
	LINK_COMPLETION("link:[] - Add link", "link:[]", "link:", 5), IMAGE_COMPLETION("image:[] - Add image", "image:[]", "image:",
			6), MAILTO_COMPLETION("mailto:[] - Add mailto link", "mailto:[]", "mailto:", 7);

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

	public ComparableCompletionProposal toCompletionProposal(IDocument document, int offset, int replacement, int priority) {
		return new ComparableCompletionProposal(new CompletionProposal(this.contents.substring(replacement), offset, 0, this.cursor - replacement,
				Activator.getDefault().getImage("/icons/completion-block.png"), this.message, null, this.message), priority);
	}

	public ComparableCompletionProposal toCompletionProposal(IDocument document, int offset) {
		return toCompletionProposal(document, offset, 0, 0);
	}

	public static Collection<ComparableCompletionProposal> getValidCompletionProposals(IDocument document, int offset, String start) {
		Collection<ComparableCompletionProposal> proposals = new ArrayList<ComparableCompletionProposal>();
		if ("".equals(start) || (start != null && start.trim().endsWith("|"))) {
			for (AsciidocMacrosCompletionProposals proposalElement : AsciidocMacrosCompletionProposals.values()) {
				proposals.add(proposalElement.toCompletionProposal(document, offset));
			}
		} else {
			for (AsciidocMacrosCompletionProposals proposalElement : AsciidocMacrosCompletionProposals.values()) {
				if (proposalElement.trigger.startsWith(start)) {
					proposals.add(proposalElement.toCompletionProposal(document, offset, start.length(), start.length()));
				}
			}
		}
		return proposals;
	}
}
