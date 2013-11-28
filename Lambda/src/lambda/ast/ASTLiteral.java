package lambda.ast;

public class ASTLiteral extends Lambda
{
	public final String originalName;
	public final String name;

	public ASTLiteral(String name)
	{
		this(name, name);
	}

	public ASTLiteral(String originalName, String name)
	{
		this.originalName = originalName;
		this.name = name;
	}

	public ASTLiteral deepCopy()
	{
		return new ASTLiteral(originalName, name);
	}

	public boolean isLiteral()
	{
		return true;
	}

	protected Lambda substitute(IDContext context, String name, Lambda lambda)
	{
		return name.equals(this.name) ? lambda.deepCopy() : this;
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
