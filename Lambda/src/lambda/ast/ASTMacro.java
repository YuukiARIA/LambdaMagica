package lambda.ast;

import lambda.Environment;
import util.Pair;

public class ASTMacro extends Lambda implements IRedex
{
	public final String name;

	public ASTMacro(String name)
	{
		this.name = name;
	}

	public boolean isMacro()
	{
		return true;
	}

	public int getPrec()
	{
		return 0;
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env)
	{
		MacroExpander expander = new MacroExpander(env);
		Lambda l = expander.expand(this);
		return Pair.of(l != this, l);
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env, IRedex redex)
	{
		if (this == redex)
		{
			return betaReduction(context, env);
		}
		else
		{
			return Pair.of(false, (Lambda)this);
		}
	}

	protected Lambda substitute(IDContext context, String name, Lambda lambda)
	{
		return this;
	}

	public <T, U> T accept(Lambda.Visitor<T, U> visitor, U param)
	{
		return visitor.visitMacro(this, param);
	}
}
