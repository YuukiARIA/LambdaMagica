package lambda.ast;

import java.util.ArrayList;
import java.util.List;

import lambda.ast.Lambda.VisitorRP;
import util.Unit;

public class RedexFinder
{
	private static VisitorImpl visitor;

	public static List<IRedex> getRedexList(Lambda lambda)
	{
		if (visitor == null)
		{
			visitor = new VisitorImpl();
		}
		List<IRedex> redexes = new ArrayList<IRedex>();
		lambda.accept(visitor, redexes);
		return redexes;
	}

	private static class VisitorImpl implements VisitorRP<Unit, List<IRedex>>
	{
		public Unit visit(ASTAbstract abs, List<IRedex> param)
		{
			abs.e.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visit(ASTApply app, List<IRedex> param)
		{
			if (app.lexpr.isAbstraction())
			{
				param.add(app);
			}
			app.lexpr.accept(this, param);
			app.rexpr.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visit(ASTLiteral literal, List<IRedex> param)
		{
			return Unit.VALUE;
		}

		public Unit visit(ASTMacro macro, List<IRedex> param)
		{
			param.add(macro);
			return Unit.VALUE;
		}
	}
}
