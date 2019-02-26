package org.gap.eclipse.plugins.extras.codemining.recursive;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.manipulation.SharedASTProviderCore;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class RecursiveCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
			monitor.isCanceled();
			ITextEditor textEditor = super.getAdapter(ITextEditor.class);
			ITypeRoot unit = EditorUtility.getEditorInputJavaElement(textEditor, false);

			if (unit == null) {
				return null;
			}
			CompilationUnit cu = SharedASTProviderCore.getAST(unit, SharedASTProviderCore.WAIT_YES, null);
			MiningASTVisitor visitor = new MiningASTVisitor(this, viewer.getDocument());
			cu.accept(visitor);
			monitor.isCanceled();
			return visitor.getMinings();
		});
	}

}
