package org.gap.eclipse.plugins.extras.codemining.recursive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.gap.eclipse.plugins.extras.codemining.PluginActivator;

class MiningASTVisitor extends ASTVisitor {
	private List<ICodeMining> minings = new ArrayList<ICodeMining>();

	private Optional<IMethodBinding> currentMethod = Optional.empty();

	private ICodeMiningProvider provider;

	private IDocument document;

	public MiningASTVisitor(ICodeMiningProvider provider, IDocument document) {
		super();
		this.provider = provider;
		this.document = document;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		currentMethod = Optional.of(node.resolveBinding());
		return super.visit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		currentMethod = Optional.empty();
		super.endVisit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if(currentMethod.isPresent()) {
			if (node.resolveMethodBinding().equals(currentMethod.get())) {
				try {
					minings.add(new RecursiveCodeMining(node, document, provider));
				} catch (JavaModelException | BadLocationException e) {
					PluginActivator.getDefault().error("Failed to mine recursive methods", e);
				}
			}
		}
		
		return super.visit(node);
	}

	public List<ICodeMining> getMinings() {
		return minings;
	}
}
