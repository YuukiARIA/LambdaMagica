package lambda.ast;

import lambda.Environment;
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

	protected abstract Lambda substitute(IDContext paramIDContext, String paramString, Lambda paramLambda);

	public abstract <T, U> T accept(Visitor<T, U> paramVisitor, U paramU);

	public <T> T accept(SingleVisitor<T> visitor)
	{
		return accept(visitor, Unit.VALUE);
	}

	public String toString()
	{
		return LambdaPrinter.toString(this);
	}

	public static abstract class SingleVisitor<T> implements Visitor<T, Unit>
	{
		public final T visitAbstract(ASTAbstract abs, Unit param)
		{
			return visitAbstract(abs);
		}

		public final T visitApply(ASTApply app, Unit param)
		{
			return visitApply(app);
		}

		public final T visitLiteral(ASTLiteral literal, Unit param)
		{
			return visitLiteral(literal);
		}

		public final T visitMacro(ASTMacro macro, Unit param)
		{
			return visitMacro(macro);
		}

		public abstract T visitAbstract(ASTAbstract paramASTAbstract);

		public abstract T visitApply(ASTApply paramASTApply);

		public abstract T visitLiteral(ASTLiteral paramASTLiteral);

		public abstract T visitMacro(ASTMacro paramASTMacro);
	}

	public static abstract interface Visitor<T, U>
	{
		public abstract T visitAbstract(ASTAbstract paramASTAbstract, U paramU);

		public abstract T visitApply(ASTApply paramASTApply, U paramU);

		public abstract T visitLiteral(ASTLiteral paramASTLiteral, U paramU);

		public abstract T visitMacro(ASTMacro paramASTMacro, U paramU);
	}
}
