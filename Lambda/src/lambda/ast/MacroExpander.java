package lambda.ast;

import lambda.Environment;

public class MacroExpander extends Lambda.SingleVisitor<Lambda>
{
	private Environment env;

	public MacroExpander(Environment env)
	{
		this.env = env;
	}

	public Lambda expand(Lambda lambda)
	{
		return lambda.accept(this);
	}

	public Lambda visitAbstract(ASTAbstract abs)
	{
		Lambda e = abs.e.accept(this);
		return e == abs.e ? abs : new ASTAbstract(abs.originalName, abs.name, e);
	}

	public Lambda visitApply(ASTApply app)
	{
		Lambda e1 = app.lexpr.accept(this);
		Lambda e2 = app.rexpr.accept(this);
		return e1 == app.lexpr && e2 == app.rexpr ? app : new ASTApply(e1, e2);
	}

	public Lambda visitLiteral(ASTLiteral literal)
	{
		return literal;
	}

	public Lambda visitMacro(ASTMacro macro)
	{
		Lambda l = env.expandMacro(macro.name);
		if (l != null)
		{
			return l;
		}
		System.out.println("- <" + macro.name + "> is undefined");
		return macro;
	}
}
