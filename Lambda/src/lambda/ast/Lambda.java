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

	public abstract <T, U> T accept(Visitor<T, U> visitor, U param);

	public <T> T accept(SingleVisitor<T> visitor)
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

	public static abstract class SingleVisitor<T> implements Visitor<T, Unit>
	{
		public final T visitAbstract(ASTAbstract abs, Unit param) { return visitAbstract(abs); }
		public final T visitApply(ASTApply app, Unit param) { return visitApply(app); }
		public final T visitLiteral(ASTLiteral literal, Unit param) { return visitLiteral(literal); }
		public final T visitMacro(ASTMacro macro, Unit param) { return visitMacro(macro); }

		public abstract T visitAbstract(ASTAbstract abs);
		public abstract T visitApply(ASTApply app);
		public abstract T visitLiteral(ASTLiteral l);
		public abstract T visitMacro(ASTMacro m);
	}

	public static abstract interface Visitor<T, U>
	{
		public abstract T visitAbstract(ASTAbstract abs, U param);
		public abstract T visitApply(ASTApply app, U param);
		public abstract T visitLiteral(ASTLiteral l, U param);
		public abstract T visitMacro(ASTMacro m, U param);
	}
}
