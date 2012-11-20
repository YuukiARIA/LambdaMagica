package lambda.ast;

import lambda.ast.Lambda.Visitor;
import util.Pair;

public class Substituter
{
	private VisitorImpl visitor;
	private int varid;

	public Substituter()
	{
		visitor = new VisitorImpl();
	}

	public Lambda substitute(Lambda lambda, String name, Lambda e)
	{
		return visitor.visit(lambda, Pair.of(name, e));
	}

	private class VisitorImpl implements Visitor<Lambda, Pair<String, Lambda>>
	{
		private VisitorImpl()
		{
		}

		private Lambda visit(Lambda l, Pair<String, Lambda> param)
		{
			return l.accept(this, param);
		}

		public Lambda visitAbstract(ASTAbstract abs, Pair<String, Lambda> param)
		{
			String v = "$" + Substituter.this.varid++;
			ASTLiteral fresh = new ASTLiteral(abs.originalName, v);
			Lambda e = visit(abs.e, new Pair<String, Lambda>(abs.name, fresh));
			e = visit(e, param);
			return new ASTAbstract(abs.originalName, v, e);
		}

		public Lambda visitApply(ASTApply app, Pair<String, Lambda> param)
		{
			Lambda l = visit(app.lexpr, param);
			Lambda r = visit(app.rexpr, param);
			return l == app.lexpr && r == app.rexpr ? app : new ASTApply(l, r);
		}

		public Lambda visitLiteral(ASTLiteral literal, Pair<String, Lambda> param)
		{
			String name = param._1;
			return name.equals(literal.name) ? param._2 : literal;
		}

		public Lambda visitMacro(ASTMacro macro, Pair<String, Lambda> param)
		{
			return macro;
		}
	}
}
