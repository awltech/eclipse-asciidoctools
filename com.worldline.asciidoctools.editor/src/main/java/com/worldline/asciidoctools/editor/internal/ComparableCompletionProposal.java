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
