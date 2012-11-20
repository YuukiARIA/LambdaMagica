package lambda.ast;

import java.util.ArrayList;
import java.util.List;

import lambda.ast.Lambda.Visitor;
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

	private static class VisitorImpl implements Visitor<Unit, List<IRedex>>
	{
		public Unit visitAbstract(ASTAbstract abs, List<IRedex> param)
		{
			abs.e.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visitApply(ASTApply app, List<IRedex> param)
		{
			if (app.lexpr.isAbstraction())
			{
				param.add(app);
			}
			app.lexpr.accept(this, param);
			app.rexpr.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visitLiteral(ASTLiteral literal, List<IRedex> param)
		{
			return Unit.VALUE;
		}

		public Unit visitMacro(ASTMacro macro, List<IRedex> param)
		{
			param.add(macro);
			return Unit.VALUE;
		}
	}
}
