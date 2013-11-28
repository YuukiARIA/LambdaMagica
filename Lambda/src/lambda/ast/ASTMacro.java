package lambda.ast;

public class ASTMacro extends Lambda implements IRedexNode
{
	public final String name;

	public ASTMacro(String name)
	{
		this.name = name;
	}

	public ASTMacro deepCopy()
	{
		return new ASTMacro(name);
	}

	public boolean isMacro()
	{
		return true;
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
