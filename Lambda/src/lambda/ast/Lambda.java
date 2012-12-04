package lambda.ast;

import lambda.ast.parser.Lexer;
import lambda.ast.parser.Parser;
import lambda.ast.parser.ParserException;

public abstract class Lambda
{
	public abstract Lambda deepCopy();

	public boolean isMacro()
	{
		return false;
	}

	public boolean isLiteral()
	{
		return false;
	}

	public boolean isApplication()
	{
		return false;
	}

	public boolean isAbstraction()
	{
		return false;
	}

	public boolean isAtomic()
	{
		return isMacro() || isLiteral();
	}

	protected abstract Lambda substitute(IDContext context, String name, Lambda lambda);

	public abstract void accept(Visitor visitor);
	public abstract <TParam> void accept(VisitorP<TParam> visitor, TParam param);
	public abstract <TRet> TRet accept(VisitorR<TRet> visitor);
	public abstract <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param);

	public String toString()
	{
		return LambdaPrinter.toString(this);
	}

	public static Lambda parse(String text) throws ParserException
	{
		Parser parser = new Parser(new Lexer(text));
		return parser.parse();
	}

	public static interface Visitor
	{
		public void visit(ASTAbstract abs);
		public void visit(ASTApply app);
		public void visit(ASTLiteral l);
		public void visit(ASTMacro m);
	}

	public static interface VisitorP<TParam>
	{
		public void visit(ASTAbstract abs, TParam param);
		public void visit(ASTApply app, TParam param);
		public void visit(ASTLiteral l, TParam param);
		public void visit(ASTMacro m, TParam param);
	}

	public static interface VisitorR<TRet>
	{
		public TRet visit(ASTAbstract abs);
		public TRet visit(ASTApply app);
		public TRet visit(ASTLiteral l);
		public TRet visit(ASTMacro m);
	}

	public static interface VisitorRP<TRet, TParam>
	{
		public TRet visit(ASTAbstract abs, TParam param);
		public TRet visit(ASTApply app, TParam param);
		public TRet visit(ASTLiteral l, TParam param);
		public TRet visit(ASTMacro m, TParam param);
	}
}
