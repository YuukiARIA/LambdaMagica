package lambda;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IRedexNode;
import lambda.ast.Lambda;

public class LaTeXStringBuilder
{
	private static VisitorImpl visitor = new VisitorImpl();

	public String build(Lambda lambda)
	{
		return build(lambda, null);
	}

	public String build(Lambda lambda, IRedexNode redex)
	{
		visitor.redex = redex;
		return lambda.accept(visitor);
	}

	private static class VisitorImpl implements Lambda.VisitorR<String>
	{
		private IRedexNode redex;

		public String visit(ASTAbstract abs)
		{
			String s = "\\lambda{";
			Lambda l = abs;
			do
			{
				ASTAbstract a = (ASTAbstract)l;
				s += a.name;
				l = a.e;
			}
			while (l.isAbstraction() && l != redex);
			s += "}.";
			s += l.accept(this);
			if (abs == redex)
			{
				s = addUnderline(s);
			}
			return s;
		}

		public String visit(ASTApply app)
		{
			Lambda l = app.lexpr;
			Lambda r = app.rexpr;
			boolean lpar = l.isAbstraction();
			boolean rpar = r.isAbstraction() || r.isApplication();

			String s1 = l.accept(this);
			if (lpar) s1 = "(" + s1 + ")";

			String s2 = r.accept(this);
			if (rpar) s2 = "(" + s2 + ")";

			if (app == redex)
			{
				s1 = addUnderline(s1);
				s2 = addUnderline(s2);
			}

			return s1 + "\\," + s2;
		}

		public String visit(ASTLiteral l)
		{
			return l.name;
		}

		public String visit(ASTMacro m)
		{
			String s = "\\overline{\\rm\\bf\\strut " + m.name + "}";
			if (m == redex)
			{
				s = addUnderline(s);
			}
			return s;
		}

		private static String addUnderline(String s)
		{
			return "\\underline{\\strut " + s + "}";
		}
	}
}
