package org.gap.eclipse.plugins.extras.codemining.recursive;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class RecursiveCodeMining extends LineHeaderCodeMining {

	public RecursiveCodeMining(MethodInvocation invocation, IDocument document, ICodeMiningProvider provider)
			throws BadLocationException, JavaModelException {
		super(getLineNumber(invocation, document), document, provider);
		setLabel("Recursion");
	}


	private static int getLineNumber(MethodInvocation invocation, IDocument document)
			throws JavaModelException, BadLocationException {
		return document.getLineOfOffset(invocation.getStartPosition());
	}


}
