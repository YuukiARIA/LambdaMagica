package lambda.ast;

import lambda.Environment;
import lambda.ast.parser.Lexer;
import lambda.ast.parser.Parser;
import lambda.ast.parser.ParserException;
import util.Pair;
import util.Unit;

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
	public abstract <T, U> T accept(VisitorRP<T, U> visitor, U param);

	public <T> T accept(VisitorR<T> visitor)
	{
		return accept(visitor, Unit.VALUE);
	}

	public String toString()
	{
		return LambdaPrinter.toString(this);
	}

	public static Lambda parse(String text) throws ParserException
	{
		Parser parser = new Parser(new Lexer(text));
		return parser.parse();
	}

	public static abstract class VisitorR<T> implements VisitorRP<T, Unit>
	{
		public final T visit(ASTAbstract abs, Unit param) { return visit(abs); }
		public final T visit(ASTApply app, Unit param) { return visit(app); }
		public final T visit(ASTLiteral literal, Unit param) { return visit(literal); }
		public final T visit(ASTMacro macro, Unit param) { return visit(macro); }

		public abstract T visit(ASTAbstract abs);
		public abstract T visit(ASTApply app);
		public abstract T visit(ASTLiteral l);
		public abstract T visit(ASTMacro m);
	}

	public static interface VisitorRP<T, U>
	{
		public T visit(ASTAbstract abs, U param);
		public T visit(ASTApply app, U param);
		public T visit(ASTLiteral l, U param);
		public T visit(ASTMacro m, U param);
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
}
