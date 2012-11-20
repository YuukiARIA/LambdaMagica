package lambda.ast;

import lambda.Environment;
import util.Pair;

public class ASTApply extends Lambda implements IRedex
{
	public final Lambda lexpr;
	public final Lambda rexpr;

	public ASTApply(Lambda lexpr, Lambda rexpr)
	{
		this.lexpr = lexpr;
		this.rexpr = rexpr;
	}

	public boolean isApplication()
	{
		return true;
	}

	public int getPrec()
	{
		return 1;
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env)
	{
		if (lexpr.isAbstraction())
		{
			return new Pair<Boolean, Lambda>(true, lexpr.apply(context, rexpr));
		}

		Pair<Boolean, Lambda> ret = lexpr.betaReduction(context, env);
		if (ret._1)
		{
			return ret.snd(new ASTApply(ret._2, rexpr));
		}

		ret = rexpr.betaReduction(context, env);
		if (ret._1)
		{
			return ret.snd(new ASTApply(lexpr, ret._2));
		}

		return new Pair<Boolean, Lambda>(false, this);
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env, IRedex redex)
	{
		if (this == redex && lexpr.isAbstraction())
		{
			return new Pair<Boolean, Lambda>(true, lexpr.apply(context, rexpr));
		}

		Pair<Boolean, Lambda> ret = lexpr.betaReduction(context, env, redex);
		if (ret._1)
		{
			return ret.snd(new ASTApply(ret._2, rexpr));
		}

		ret = rexpr.betaReduction(context, env, redex);
		if (ret._1)
		{
			return ret.snd(new ASTApply(lexpr, ret._2));
		}

		return new Pair<Boolean, Lambda>(false, this);
	}

	protected Lambda substitute(IDContext context, String name, Lambda lambda)
	{
		Lambda l = lexpr.substitute(context, name, lambda);
		Lambda r = rexpr.substitute(context, name, lambda);
		return l == lexpr && r == rexpr ? this : new ASTApply(l, r);
	}

	public <T, U> T accept(Lambda.Visitor<T, U> visitor, U param)
	{
		return visitor.visitApply(this, param);
	}
}
