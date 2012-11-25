package lambda.ast;

import java.util.HashSet;
import java.util.Set;
import lambda.Environment;
import util.Pair;

public class ASTAbstract extends Lambda implements IRedex
{
	public final String originalName;
	public final String name;
	public final Lambda e;

	public ASTAbstract(String originalName, String name, Lambda e)
	{
		this.originalName = originalName;
		this.name = name;
		this.e = e;
	}

	public boolean isAbstraction()
	{
		return true;
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env)
	{
		IDContext nc = IDContext.deriveContext(context);
		nc.addBoundedName(name);
		Pair<Boolean, Lambda> p = e.betaReduction(nc, env);
		if (p._1)
		{
			return p.snd(new ASTAbstract(originalName, name, p._2));
		}
		return p.snd(this);
	}

	public Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env, IRedex redex)
	{
		IDContext nc = IDContext.deriveContext(context);
		nc.addBoundedName(name);
		Pair<Boolean, Lambda> p = e.betaReduction(nc, env, redex);
		if (p._1)
		{
			return p.snd(new ASTAbstract(originalName, name, p._2));
		}
		return p.snd(this);
	}

	public Pair<Boolean, Lambda> etaReduction()
	{
		if (e instanceof ASTApply)
		{
			ASTApply app = (ASTApply)this.e;
			if (app.rexpr instanceof ASTLiteral)
			{
				ASTLiteral x = (ASTLiteral)app.rexpr;
				VariableCollector vc = new VariableCollector(app.lexpr);
				Set<String> fv = vc.getFreeVariables();
				if (!fv.contains(x.name))
				{
					return Pair.of(true, app.lexpr);
				}
			}
		}
		return Pair.of(false, (Lambda)this);
	}

	protected Lambda apply(IDContext context, Lambda lambda)
	{
		IDContext nc = IDContext.deriveContext(context);
		nc.addBoundedName(name);
		Lambda e2 = e.substitute(nc, name, lambda);
		return e2;
	}

	protected Lambda substitute(IDContext context, String name, Lambda lambda)
	{
		if (this.name.equals(name))
		{
			return this;
		}

		VariableCollector vc = new VariableCollector(lambda);
		Set<String> fv = vc.getFreeVariables();
		ASTAbstract l = this;
		if (!fv.isEmpty())
		{
			l = rename(context, fv);
		}
		IDContext nc = IDContext.deriveContext(context);
		nc.addBoundedName(l.name);
		Lambda e2 = l.e.substitute(nc, name, lambda);
		return l == this && e2 == e ? this : new ASTAbstract(originalName, l.name, e2);
	}

	private ASTAbstract rename(IDContext context, Set<String> fv)
	{
		VariableCollector vc = new VariableCollector(this);
		RenameGenerator renamer = new RenameGenerator(fv);
		Set<String> bv = new HashSet<String>(context.getBoundedNames());
		bv.addAll(vc.getBoundedVariables());
		return (ASTAbstract)renamer.rename(bv, this);
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
