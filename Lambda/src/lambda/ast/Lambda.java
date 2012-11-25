package lambda.ast;

import lambda.Environment;
import lambda.ast.parser.Lexer;
import lambda.ast.parser.Parser;
import lambda.ast.parser.ParserException;
import util.Pair;

public abstract class Lambda
{
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

	public abstract int getPrec();

	public abstract Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env);
	public abstract Pair<Boolean, Lambda> betaReduction(IDContext context, Environment env, IRedex redex);

	public Pair<Boolean, Lambda> etaReduction()
	{
		return Pair.of(Boolean.valueOf(false), this);
	}

	protected Lambda apply(IDContext context, Lambda e)
	{
		return this;
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
