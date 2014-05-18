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

	public ASTApply deepCopy()
	{
		return new ASTApply(lexpr.deepCopy(), rexpr.deepCopy());
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

	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (o instanceof ASTApply)
		{
			ASTApply app = (ASTApply)o;
			return lexpr.equals(app.lexpr) && rexpr.equals(app.rexpr);
		}
		return false;
	}

	public int hashCode()
	{
		return 17 * lexpr.hashCode() + rexpr.hashCode();
	}
}
