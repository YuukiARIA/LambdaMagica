package lambda.ast;

public class ASTApply extends Lambda implements IRedexNode
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

	protected Lambda substitute(IDContext context, String name, Lambda lambda)
	{
		Lambda l = lexpr.substitute(context, name, lambda);
		Lambda r = rexpr.substitute(context, name, lambda);
		return l == lexpr && r == rexpr ? this : new ASTApply(l, r);
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
