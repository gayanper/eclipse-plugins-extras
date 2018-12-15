package org.gap.eclipse.plugins.extras.fixes.asist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.gap.eclipse.plugins.extras.fixes.PluginActivator;

@SuppressWarnings("restriction")
public class SearchStaticAssistProcessor implements IQuickAssistProcessor {
	private static final String SEARCH_STATIC_ID = "org.gap.eclipse.plugins.extras.fixes.asist.SearchStaticAssistProcessor.assist"; //$NON-NLS-1$ ;

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		return isApplicable(context);
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		if (!isApplicable(context))
			return null;
		return findProposals(context, false);
	}

	private IJavaCompletionProposal[] findProposals(IInvocationContext context, boolean evalOnly) {
		ASTNode node = context.getCoveringNode();
		if (node instanceof SimpleName) {
			Optional<String> expectedType = extractExpectedType((SimpleName) node);

			SearchPattern fieldPattern = SearchPattern.createPattern(((SimpleName) node).getIdentifier(),
					IJavaSearchConstants.FIELD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);
			SearchPattern methodPattern = SearchPattern.createPattern(((SimpleName) node).getIdentifier(),
					IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_EXACT_MATCH);

			SearchEngine engine = new SearchEngine();
			List<IMember> staticMatches = new ArrayList<>();
			try {
				engine.search(SearchPattern.createOrPattern(fieldPattern, methodPattern),
						new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, createSearchScope(),
						new SearchRequestor() {

							@Override
							public void acceptSearchMatch(SearchMatch match) throws CoreException {
								Object element = match.getElement();
								if (element instanceof IMember) {
									IMember member = (IMember) element;
									if ((member.getFlags() & Flags.AccStatic) == Flags.AccStatic) {
										if (expectedType.isPresent()) {
											if (expectedType.get().equals(memberType(member))) {
												staticMatches.add(member);
											}
										} else {
											staticMatches.add(member);
										}
									}
								}
							}
						}, new NullProgressMonitor());

				List<IJavaCompletionProposal> proposals = new ArrayList<>(staticMatches.size());
				for (IMember member : staticMatches) {
					if (member instanceof IField) {
						proposals.add(createFieldProposal(context, node, (IField) member));
					} else if (member instanceof IMethod) {
						proposals.add(createMethodProposal(context, node, (IMethod) member));
					}
				}
				return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
			} catch (CoreException e) {
				PluginActivator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
			}

		}
		return null;
	}

	private String memberType(IMember member) throws JavaModelException {
		if ((member instanceof IMethod)) {
			return ((IMethod) member).getReturnType();
		} else if (member instanceof IField) {
			String rawSignature = ((IField) member).getTypeSignature();
			String typeSignature = Signature.toString(rawSignature);
			if (!typeSignature.contains(".") && (rawSignature.startsWith("Q") || rawSignature.startsWith("L"))) {
				return "java.lang." + typeSignature;
			} else {
				return typeSignature;
			}
		}
		return "";
	}

	private Optional<String> extractExpectedType(SimpleName node) {
		if (node.getParent() instanceof MethodInvocation) {
			MethodInvocation invocation = (MethodInvocation) node.getParent();
			@SuppressWarnings("unchecked")
			List<ASTNode> args = invocation.arguments();
			int argIndex = 0;
			for (int i = 0; i < args.size(); i++) {
				if (args.get(i).equals(node)) {
					argIndex = i;
					break;
				}
			}

			IMethodBinding binding = invocation.resolveMethodBinding();
			if (binding != null) {
				ITypeBinding[] parameterTypes = binding.getParameterTypes();
				if (parameterTypes.length < argIndex) {
					if (binding.isVarargs()) {
						String qualifiedName = parameterTypes[parameterTypes.length - 1].getComponentType()
								.getQualifiedName();
						if (!"java.lang.Object".equals(qualifiedName)) {
							return Optional.ofNullable(qualifiedName);
						}
					}
					return Optional.empty();
				} else {
					if (binding.isVarargs() && parameterTypes[argIndex].isArray()) {
						String qualifiedName = parameterTypes[argIndex].getComponentType().getQualifiedName();
						if (!"java.lang.Object".equals(qualifiedName)) {
							return Optional.ofNullable(qualifiedName);
						}
					} else {
						return Optional.ofNullable(parameterTypes[argIndex].getTypeDeclaration().getQualifiedName());
					}
				}
			}
		}
		return Optional.empty();
	}

	private static IJavaSearchScope createSearchScope() throws JavaModelException {
		IJavaProject[] projects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects();
		return SearchEngine.createJavaSearchScope(projects, IJavaSearchScope.SOURCES);
	}

	private IJavaCompletionProposal createFieldProposal(IInvocationContext context, ASTNode astNode, IField field) {
		final ICompilationUnit cu = context.getCompilationUnit();
		final AST ast = astNode.getAST();
		final ASTRewrite rewrite = ASTRewrite.create(ast);

		ASTRewriteCorrectionProposal proposal = new ASTRewriteCorrectionProposal(
				"Replace with " + qualifiedName(field), cu, rewrite, 0,
				JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		proposal.setCommandId(SEARCH_STATIC_ID);
		FieldAccess fieldAccess = ast.newFieldAccess();
		fieldAccess.setExpression(ast.newName(field.getDeclaringType().getFullyQualifiedName()));
		fieldAccess.setName(ast.newSimpleName(field.getElementName()));
		rewrite.replace(astNode, fieldAccess, null);

		return proposal;
	}

	private IJavaCompletionProposal createMethodProposal(IInvocationContext context, ASTNode astNode, IMethod method) {
		final ICompilationUnit cu = context.getCompilationUnit();
		final AST ast = astNode.getAST();
		final ASTRewrite rewrite = ASTRewrite.create(ast);

		ASTRewriteCorrectionProposal proposal = new ASTRewriteCorrectionProposal(
				"Replace with " + qualifiedName(method) + "()", cu, rewrite, 0,
				JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE));
		proposal.setCommandId(SEARCH_STATIC_ID);
		MethodInvocation invocation = ast.newMethodInvocation();
		invocation.setExpression(ast.newName(method.getDeclaringType().getFullyQualifiedName()));
		invocation.setName(ast.newSimpleName(method.getElementName()));
		rewrite.replace(astNode, invocation, null);
		
		return proposal;
	}

	private String qualifiedName(IMember member) {
		return member.getDeclaringType().getFullyQualifiedName() + "." + member.getElementName();
	}

	private boolean isApplicable(IInvocationContext context) {
		return context.getCoveringNode() instanceof SimpleName;
	}
}
