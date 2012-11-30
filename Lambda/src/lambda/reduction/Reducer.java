package lambda.reduction;

import java.util.Set;

import lambda.Environment;
import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IDContext;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.VariableCollector;

public class Reducer
{
	private static ReductionVisitor visitor;

	public static Result reduce(Lambda lambda, Environment env, IRedex redex)
	{
		if (lambda == null || env == null || redex == null)
		{
			throw new NullPointerException();
		}

		if (visitor == null)
		{
			visitor = new ReductionVisitor();
		}
		visitor.env = env;
		visitor.redex = redex;
		return visitor.reduce(lambda);
	}

	public static class Result
	{
		public final Lambda lambda;
		public final ReductionRule appliedRule;
		public final boolean reduced;

		public Result(Lambda lambda, ReductionRule appliedRule, boolean reduced)
		{
			this.lambda = lambda;
			this.appliedRule = appliedRule;
			this.reduced = reduced;
		}
	}

	private static class ReductionVisitor implements Lambda.VisitorRP<Lambda, IDContext>
	{
		private Environment env;
		private IRedex redex;
		private ReductionRule appliedRule = ReductionRule.NONE;

		public Lambda visit(ASTAbstract abs, IDContext context)
		{
			if (abs == redex && abs.e instanceof ASTApply)
			{
				ASTApply app = (ASTApply)abs.e;
				if (app.rexpr instanceof ASTLiteral)
				{
					ASTLiteral x = (ASTLiteral)app.rexpr;
					VariableCollector vc = new VariableCollector(app.lexpr);
					Set<String> fv = vc.getFreeVariables();
					if (!fv.contains(x.name))
					{
						appliedRule = ReductionRule.ETA_REDUCTION;
						return app.lexpr;
					}
				}
			}

			IDContext nc = IDContext.deriveContext(context);
			nc.addBoundedName(abs.name);
			Lambda ret = reduce(abs.e, nc);
			if (ret != abs.e)
			{
				return new ASTAbstract(abs.originalName, abs.name, ret);
			}
			return abs;
		}

		public Lambda visit(ASTApply app, IDContext context)
		{
			if (app == redex && app.lexpr.isAbstraction())
			{
				ASTAbstract abs = (ASTAbstract)app.lexpr;
				appliedRule = ReductionRule.BETA_REDUCTION;
				return abs.apply(context, app.rexpr);
			}

			Lambda ret = reduce(app.lexpr, context);
			if (ret != app.lexpr)
			{
				return new ASTApply(ret, app.rexpr);
			}

			ret = reduce(app.rexpr, context);
			if (ret != app.rexpr)
			{
				return new ASTApply(app.lexpr, ret);
			}
			return app;
		}

		public Lambda visit(ASTLiteral l, IDContext context)
		{
			return l;
		}

		public Lambda visit(ASTMacro m, IDContext context)
		{
			if (m == redex)
			{
				Lambda l = env.expandMacro(m.name);
				if (l != null)
				{
					appliedRule = ReductionRule.MACRO_EXPANSION;
					return l;
				}
			}
			return m;
		}

		public Result reduce(Lambda lambda)
		{
			Lambda ret = reduce(lambda, IDContext.createContext());
			return new Result(ret, appliedRule, ret != lambda);
		}

		private Lambda reduce(Lambda lambda, IDContext context)
		{
			return lambda.accept(this, context);
		}
	}
}