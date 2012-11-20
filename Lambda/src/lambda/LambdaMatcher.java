package lambda;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.Lambda;
import lambda.ast.Lambda.Visitor;

public class LambdaMatcher
{
	private static MatchingVisitor visitor;

	public static boolean structuralEquivalent(Lambda e1, Lambda e2)
	{
		if (visitor == null)
		{
			visitor = new MatchingVisitor();
		}
		return e1.accept(visitor, e2);
	}

	private static class MatchingVisitor implements Visitor<Boolean, Lambda>
	{
		public Boolean visitAbstract(ASTAbstract abs, Lambda param)
		{
			if (param.getClass() == ASTAbstract.class)
			{
				ASTAbstract abs2 = (ASTAbstract)param;
				return abs.e.accept(this, abs2.e);
			}
			return false;
		}

		public Boolean visitApply(ASTApply app, Lambda param)
		{
			if (param.getClass() == ASTApply.class)
			{
				ASTApply app2 = (ASTApply)param;
				return app.lexpr.accept(this, app2.lexpr) && app.rexpr.accept(this, app2.rexpr);
			}
			return false;
		}

		public Boolean visitLiteral(ASTLiteral literal, Lambda param)
		{
			return param.getClass() == ASTLiteral.class;
		}

		public Boolean visitMacro(ASTMacro macro, Lambda param)
		{
			if (param.getClass() == ASTMacro.class)
			{
				ASTMacro macro2 = (ASTMacro)param;
				return macro.name.equals(macro2.name);
			}
			return false;
		}
	}
}
