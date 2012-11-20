package lambda.ast;

import lambda.ast.Lambda.SingleVisitor;

public class LambdaCloner
{
	private static VisitorImpl visitor;

	public static Lambda copy(Lambda lambda)
	{
		if (visitor == null)
		{
			visitor = new VisitorImpl();
		}
		return lambda.accept(visitor);
	}

	private static class VisitorImpl extends SingleVisitor<Lambda>
	{
		public Lambda visitAbstract(ASTAbstract abs)
		{
			return new ASTAbstract(abs.originalName, abs.name, copy(abs.e));
		}

		public Lambda visitApply(ASTApply app)
		{
			return new ASTApply(copy(app.lexpr), copy(app.rexpr));
		}

		public Lambda visitLiteral(ASTLiteral literal)
		{
			return new ASTLiteral(literal.originalName, literal.name);
		}

		public Lambda visitMacro(ASTMacro macro)
		{
			return new ASTMacro(macro.name);
		}
	}
}
