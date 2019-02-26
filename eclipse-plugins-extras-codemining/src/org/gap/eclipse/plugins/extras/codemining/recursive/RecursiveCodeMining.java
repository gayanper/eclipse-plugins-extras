package org.gap.eclipse.plugins.extras.codemining.recursive;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;

public class RecursiveCodeMining extends LineHeaderCodeMining {

	public RecursiveCodeMining(IMethodBinding binding, IDocument document, ICodeMiningProvider provider)
			throws BadLocationException, JavaModelException {
		super(getLineNumber(binding, document), document, provider);
		setLabel("Recursive");
	}


	private static int getLineNumber(IMethodBinding binding, IDocument document)
			throws JavaModelException, BadLocationException {
		ISourceRange r = ((ISourceReference) binding.getJavaElement()).getNameRange();
		int offset = r.getOffset();
		return document.getLineOfOffset(offset);
	}


}
