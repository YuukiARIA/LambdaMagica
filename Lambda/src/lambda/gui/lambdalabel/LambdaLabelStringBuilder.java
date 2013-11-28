package lambda.gui.lambdalabel;

import lambda.gui.lambdalabel.LambdaLabel.AbstractLabel;
import lambda.gui.lambdalabel.LambdaLabel.ApplyLabel;
import lambda.gui.lambdalabel.LambdaLabel.LiteralLabel;
import lambda.gui.lambdalabel.LambdaLabel.MacroLabel;
import lambda.gui.lambdalabel.LambdaLabel.RedexWrapper;
import lambda.gui.lambdalabel.LambdaLabel.VisitorP;

public class LambdaLabelStringBuilder
{
	private static VisitorImpl visitor = new VisitorImpl();

	public static String toString(LambdaLabel lambda)
	{
		StringBuilder sb = new StringBuilder();
		lambda.accept(visitor, sb);
		return sb.toString();
	}

	private static class VisitorImpl implements VisitorP<StringBuilder>
	{
		public void visit(LiteralLabel l, StringBuilder sb)
		{
			sb.append(l.name);
		}

		public void visit(ApplyLabel app, StringBuilder sb)
		{
			boolean lpar = app.lexpr.isParenRequiredInAppLeft();
			boolean rpar = app.rexpr.isParenRequiredInAppRight();

			if (lpar) sb.append('(');
			app.lexpr.accept(this, sb);
			if (lpar) sb.append(')');

			if (rpar) sb.append('(');
			app.rexpr.accept(this, sb);
			if (rpar) sb.append(')');
		}

		public void visit(AbstractLabel abs, StringBuilder sb)
		{
			LambdaLabel e = abs;
			sb.append('\\');
			while (e.isAbstract())
			{
				AbstractLabel eAbs = (AbstractLabel)e;
				sb.append(eAbs.name);
				e = eAbs.body;
			}
			sb.append('.');
			e.accept(this, sb);
		}

		public void visit(MacroLabel macro, StringBuilder sb)
		{
			sb.append('<');
			sb.append(macro.name);
			sb.append('>');
		}

		public void visit(RedexWrapper wrap, StringBuilder sb)
		{
			wrap.lambda.accept(this, sb);
		}
	}
}
