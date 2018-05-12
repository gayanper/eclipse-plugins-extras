package org.gap.eclipse.plugins.extras.fixes.asist;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;

public class ASTHelper {
	public static BodyDeclaration findParentBodyDeclaration(ASTNode node) {
		while ((node != null) && (!(node instanceof BodyDeclaration))) {
			node = node.getParent();
		}
		return (BodyDeclaration) node;
	}

}
