package lambda;

import java.util.Set;

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
		if (visitor == null)
		{
			visitor = new ReductionVisitor();
		}
		visitor.env = env;
		visitor.redex = redex;
		return visitor.reduce(lambda, IDContext.createContext());
	}

	public static class Result
	{
		public final Lambda lambda;
		public final boolean reduced;

		public Result(Lambda lambda, boolean reduced)
		{
			this.lambda = lambda;
			this.reduced = reduced;
		}
	}

	private static class ReductionVisitor implements Lambda.VisitorRP<Result, IDContext>
	{
		private Environment env;
		private IRedex redex;

		public Result visit(ASTAbstract abs, IDContext context)
		{
			if (isRedex(abs) && abs.e instanceof ASTApply)
			{
				ASTApply app = (ASTApply)abs.e;
				if (app.rexpr instanceof ASTLiteral)
				{
					ASTLiteral x = (ASTLiteral)app.rexpr;
					VariableCollector vc = new VariableCollector(app.lexpr);
					Set<String> fv = vc.getFreeVariables();
					if (!fv.contains(x.name))
					{
						return new Result(app.lexpr, true);
					}
				}
			}

			IDContext nc = IDContext.deriveContext(context);
			nc.addBoundedName(abs.name);
			Result ret = reduce(abs.e, nc);
			if (ret.reduced)
			{
				return new Result(new ASTAbstract(abs.originalName, abs.name, ret.lambda), true);
			}
			return new Result(abs, false);
		}

		public Result visit(ASTApply app, IDContext context)
		{
			if (isRedex(app) && app.lexpr.isAbstraction())
			{
				ASTAbstract abs = (ASTAbstract)app.lexpr;
				return new Result(abs.apply(context, app.rexpr), true);
			}

			Result ret = reduce(app.lexpr, context);
			if (ret.reduced)
			{
				return new Result(new ASTApply(ret.lambda, app.rexpr), true);
			}

			ret = reduce(app.rexpr, context);
			if (ret.reduced)
			{
				return new Result(new ASTApply(app.lexpr, ret.lambda), true);
			}

			return new Result(app, false);
		}

		public Result visit(ASTLiteral l, IDContext context)
		{
			return new Result(l, false);
		}

		public Result visit(ASTMacro m, IDContext context)
		{
			if (isRedex(m))
			{
				Lambda l = env.expandMacro(m.name);
				if (l != null)
				{
					return new Result(l, true);
				}
			}
			return new Result(m, false);
		}

		private Result reduce(Lambda lambda, IDContext context)
		{
			return lambda.accept(this, context);
		}

		private boolean isRedex(IRedex r)
		{
			return redex == null || redex == r;
		}
	}
}
