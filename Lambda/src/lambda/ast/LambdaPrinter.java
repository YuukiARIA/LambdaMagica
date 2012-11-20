package lambda.ast;

public class LambdaPrinter extends Lambda.SingleVisitor<Lambda>
{
	private static final char LAMBDA_CHAR = '\\';
	private boolean highlightRedex;
	private IRedex redex;
	private StringBuilder buf = new StringBuilder();

	public LambdaPrinter()
	{
		this(false, null);
	}

	public LambdaPrinter(boolean highlightRedex, IRedex redex)
	{
		this.highlightRedex = highlightRedex;
		this.redex = redex;
	}

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

		if (highlightRedex && app == redex) buf.append("<font color=\"#ff5555\">");

		if (lpar) buf.append('(');
		l.accept(this);
		if (lpar) buf.append(')');

		if (highlightRedex && app == redex) buf.append("</font><font color=\"#5555ff\">");

		if (rpar) buf.append('(');
		r.accept(this);
		if (rpar) buf.append(')');

		if (highlightRedex && app == redex) buf.append("</font>");
		return app;
	}

	public Lambda visitLiteral(ASTLiteral literal)
	{
		buf.append(literal.name);
		return literal;
	}

	public Lambda visitMacro(ASTMacro macro)
	{
		if (highlightRedex)
		{
			if (macro == redex)
			{
				buf.append("<font color=\"#55ff55\">");
			}
			buf.append("&lt;");
		}
		else
		{
			buf.append('<');
		}
		buf.append(macro.name);
		if (highlightRedex)
		{
			buf.append("&gt;");
			if (macro == redex)
			{
				buf.append("</font>");
			}
		}
		else
		{
			buf.append('>');
		}
		return macro;
	}
}
