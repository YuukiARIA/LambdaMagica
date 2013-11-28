package lambda.reduction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IRedexNode;
import lambda.ast.Lambda;
import lambda.ast.VariableCollector;

public class RedexFinder
{
	private static VisitorImpl visitor;
	private static LOVisitor loVisitor;

	public static IRedexNode getLeftMostOuterMostRedex(Lambda lambda, boolean etaEnabled)
	{
		if (loVisitor == null)
		{
			loVisitor = new LOVisitor();
		}
		loVisitor.etaEnabled = etaEnabled;
		return lambda.accept(loVisitor);
	}

	public static boolean isNormalForm(Lambda lambda, boolean etaEnabled)
	{
		return getLeftMostOuterMostRedex(lambda, etaEnabled) == null;
	}

	public static List<IRedexNode> getRedexList(Lambda lambda)
	{
		return getRedexList(lambda, false);
	}

	public static List<IRedexNode> getRedexList(Lambda lambda, boolean enableEta)
	{
		if (visitor == null)
		{
			visitor = new VisitorImpl();
		}
		visitor.etaEnabled = enableEta;
		List<IRedexNode> redexes = new ArrayList<IRedexNode>();
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
				if (abs.name.equals(x.name))
				{
					VariableCollector vc = new VariableCollector(app.lexpr);
					Set<String> fv = vc.getFreeVariables();
					if (!fv.contains(x.name))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private static class VisitorImpl implements Lambda.VisitorP<List<IRedexNode>>
	{
		private boolean etaEnabled;

		public void visit(ASTAbstract abs, List<IRedexNode> redexes)
		{
			if (etaEnabled && isEtaRedex(abs))
			{
				redexes.add(abs);
			}
			abs.e.accept(this, redexes);
		}

		public void visit(ASTApply app, List<IRedexNode> redexes)
		{
			if (isBetaRedex(app))
			{
				redexes.add(app);
			}
			app.lexpr.accept(this, redexes);
			app.rexpr.accept(this, redexes);
		}

		public void visit(ASTLiteral literal, List<IRedexNode> redexes)
		{
		}

		public void visit(ASTMacro macro, List<IRedexNode> redexes)
		{
			redexes.add(macro);
		}
	}

	private static class LOVisitor implements Lambda.VisitorR<IRedexNode>
	{
		private boolean etaEnabled;

		public IRedexNode visit(ASTAbstract abs)
		{
			if (etaEnabled && isEtaRedex(abs))
			{
				return abs;
			}
			return abs.e.accept(this);
		}

		public IRedexNode visit(ASTApply app)
		{
			if (isBetaRedex(app))
			{
				return app;
			}
			IRedexNode redex = app.lexpr.accept(this);
			if (redex == null)
			{
				redex = app.rexpr.accept(this);
			}
			return redex;
		}

		public IRedexNode visit(ASTLiteral literal)
		{
			return null;
		}

		public IRedexNode visit(ASTMacro macro)
		{
			return macro;
		}
	}
}
