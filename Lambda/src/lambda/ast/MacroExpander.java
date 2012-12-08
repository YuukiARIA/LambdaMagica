package lambda.ast;

import lambda.macro.MacroDefinition;

public class MacroExpander implements Lambda.VisitorR<Lambda>
{
	private MacroDefinition macroDef;
	private boolean recursive;
	private boolean unexpanded;

	public MacroExpander(MacroDefinition macroDef)
	{
		this.macroDef = macroDef;
	}

	public Lambda expand(Lambda lambda)
	{
		return expand(lambda, false);
	}

	public Lambda expand(Lambda lambda, boolean recursive)
	{
		this.recursive = recursive;
		this.unexpanded = false;
		return lambda.accept(this);
	}

	public boolean isSucceeded()
	{
		return !unexpanded;
	}

	public Lambda visit(ASTAbstract abs)
	{
		Lambda e = abs.e.accept(this);
		return e == abs.e ? abs : new ASTAbstract(abs.originalName, abs.name, e);
	}

	public Lambda visit(ASTApply app)
	{
		Lambda e1 = app.lexpr.accept(this);
		Lambda e2 = app.rexpr.accept(this);
		return e1 == app.lexpr && e2 == app.rexpr ? app : new ASTApply(e1, e2);
	}

	public Lambda visit(ASTLiteral literal)
	{
		return literal;
	}

	public Lambda visit(ASTMacro macro)
	{
		Lambda l = macroDef.expandMacro(macro.name);
		if (l != null)
		{
			return recursive ? l.accept(this) : l;
		}
		System.out.println("- <" + macro.name + "> is undefined");
		unexpanded = true;
		return macro;
	}
}
