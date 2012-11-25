package lambda.ast;

import java.util.ArrayList;
import java.util.List;

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

	private static class VisitorImpl implements Lambda.VisitorP<List<IRedex>>
	{
		public void visit(ASTAbstract abs, List<IRedex> param)
		{
			abs.e.accept(this, param);
		}

		public void visit(ASTApply app, List<IRedex> param)
		{
			if (app.lexpr.isAbstraction())
			{
				param.add(app);
			}
			app.lexpr.accept(this, param);
			app.rexpr.accept(this, param);
		}

		public void visit(ASTLiteral literal, List<IRedex> param)
		{
		}

		public void visit(ASTMacro macro, List<IRedex> param)
		{
			param.add(macro);
		}
	}
}
