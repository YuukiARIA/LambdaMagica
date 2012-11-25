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

	public void accept(Lambda.Visitor visitor)
	{
		visitor.visit(this);
	}

	public <TParam> void accept(Lambda.VisitorP<TParam> visitor, TParam param)
	{
		visitor.visit(this, param);
	}

	public <TRet> TRet accept(Lambda.VisitorR<TRet> visitor)
	{
		return visitor.visit(this);
	}

	public <TRet, TParam> TRet accept(Lambda.VisitorRP<TRet, TParam> visitor, TParam param)
	{
		return visitor.visit(this, param);
	}
}
