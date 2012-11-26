package lambda.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RedexFinder
{
	private static VisitorImpl visitor;
	private static LOVisitor loVisitor;

	public static IRedex getLeftMostOuterMostRedex(Lambda lambda, boolean etaEnabled)
	{
		if (loVisitor == null)
		{
			loVisitor = new LOVisitor();
		}
		loVisitor.etaEnabled = etaEnabled;
		return lambda.accept(loVisitor);
	}

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
		visitor.etaEnabled = enableEta;
		List<IRedex> redexes = new ArrayList<IRedex>();
		lambda.accept(visitor, redexes);
		return redexes;
	}

	private static boolean isBetaRedex(ASTApply app)
	{
		return app.lexpr.isAbstraction();
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

	private static class VisitorImpl implements Lambda.VisitorP<List<IRedex>>
	{
		private boolean etaEnabled;

		public void visit(ASTAbstract abs, List<IRedex> redexes)
		{
			if (etaEnabled && isEtaRedex(abs))
			{
				redexes.add(abs);
			}
			abs.e.accept(this, redexes);
		}

		public void visit(ASTApply app, List<IRedex> redexes)
		{
			if (isBetaRedex(app))
			{
				redexes.add(app);
			}
			app.lexpr.accept(this, redexes);
			app.rexpr.accept(this, redexes);
		}

		public void visit(ASTLiteral literal, List<IRedex> redexes)
		{
		}

		public void visit(ASTMacro macro, List<IRedex> redexes)
		{
			redexes.add(macro);
		}
	}

	private static class LOVisitor implements Lambda.VisitorR<IRedex>
	{
		private boolean etaEnabled;

		public IRedex visit(ASTAbstract abs)
		{
			if (etaEnabled && isEtaRedex(abs))
			{
				return abs;
			}
			return abs.e.accept(this);
		}

		public IRedex visit(ASTApply app)
		{
			if (isBetaRedex(app))
			{
				return app;
			}
			IRedex redex = app.lexpr.accept(this);
			if (redex == null)
			{
				redex = app.rexpr.accept(this);
			}
			return redex;
		}

		public IRedex visit(ASTLiteral literal)
		{
			return null;
		}

		public IRedex visit(ASTMacro macro)
		{
			return macro;
		}
	}
}
