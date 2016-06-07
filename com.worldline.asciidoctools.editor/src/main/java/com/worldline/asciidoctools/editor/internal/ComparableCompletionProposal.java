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
package com.worldline.asciidoctools.editor.internal;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class ComparableCompletionProposal implements ICompletionProposal, Comparable<ComparableCompletionProposal> {

	private final ICompletionProposal original;

	private final int priority;

	public ComparableCompletionProposal(ICompletionProposal original, int priority) {
		this.original = original;
		this.priority = priority;
	}

	@Override
	public void apply(IDocument document) {
		this.original.apply(document);
	}

	@Override
	public Point getSelection(IDocument document) {
		return this.original.getSelection(document);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return this.original.getAdditionalProposalInfo();
	}

	@Override
	public String getDisplayString() {
		return this.original.getDisplayString();
	}

	@Override
	public Image getImage() {
		return this.original.getImage();
	}

	@Override
	public IContextInformation getContextInformation() {
		return this.original.getContextInformation();
	}

	@Override
	public int compareTo(ComparableCompletionProposal o) {
		return o.priority != this.priority ? o.priority - this.priority : this.getDisplayString().compareTo(o.getDisplayString());
	}

}
