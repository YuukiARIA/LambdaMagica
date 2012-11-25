package lambda.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedexFinder
{
	private static VisitorImpl visitor;

	public static List<IRedex> getRedexList(Lambda lambda)
	{
		return getRedexList(lambda, false);
	}

	public static List<IRedex> getRedexList(Lambda lambda, boolean enableEta)
	{
		if (visitor == null)
		{
			visitor = new VisitorImpl();
		}
		visitor.enableEta = enableEta;
		List<IRedex> redexes = new ArrayList<IRedex>();
		lambda.accept(visitor, redexes);
		return redexes;
	}

	private static class VisitorImpl implements Lambda.VisitorP<List<IRedex>>
	{
		private boolean enableEta;

		public void visit(ASTAbstract abs, List<IRedex> param)
		{
			if (enableEta && isEtaRedex(abs))
			{
				param.add(abs);
			}
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

		private static boolean isEtaRedex(ASTAbstract abs)
		{
			if (abs.e.isApplication())
			{
				ASTApply app = (ASTApply)abs.e;
				if (app.rexpr.isLiteral())
				{
					ASTLiteral x = (ASTLiteral)app.rexpr;
					VariableCollector vc = new VariableCollector(app.lexpr);
					Set<String> fv = vc.getFreeVariables();
					if (!fv.contains(x.name))
					{
						return true;
					}
				}
			}
			return false;
		}
	}
}
