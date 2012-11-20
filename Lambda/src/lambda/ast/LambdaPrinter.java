package lambda.ast;

public class LambdaPrinter extends Lambda.SingleVisitor<Lambda>
{
	private static final char LAMBDA_CHAR = '\\';
	private StringBuilder buf = new StringBuilder();

	public static String toString(Lambda lambda)
	{
		LambdaPrinter printer = new LambdaPrinter();
		lambda.accept(printer);
		return printer.buf.toString();
	}

	public String makeString(Lambda lambda)
	{
		lambda.accept(this);
		return buf.toString();
	}

	public Lambda visitAbstract(ASTAbstract abs)
	{
		Lambda e = abs;
		buf.append(LAMBDA_CHAR);
		while (e.isAbstraction())
		{
			ASTAbstract eAbs = (ASTAbstract)e;
			buf.append(eAbs.name);
			e = eAbs.e;
		}
		buf.append('.');
		e.accept(this);
		return abs;
	}

	public Lambda visitApply(ASTApply app)
	{
		Lambda l = app.lexpr;
		Lambda r = app.rexpr;
		boolean lpar = l.isAbstraction();
		boolean rpar = !r.isAtomic();

		if (lpar) buf.append('(');
		l.accept(this);
		if (lpar) buf.append(')');

		if (rpar) buf.append('(');
		r.accept(this);
		if (rpar) buf.append(')');

		return app;
	}

	public Lambda visitLiteral(ASTLiteral literal)
	{
		buf.append(literal.name);
		return literal;
	}

	public Lambda visitMacro(ASTMacro macro)
	{
		buf.append('<');
		buf.append(macro.name);
		buf.append('>');
		return macro;
	}
}
