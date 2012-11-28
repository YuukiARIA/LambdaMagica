package lambda.gui.lambdalabel;

import java.awt.FontMetrics;
import java.awt.Graphics;

import lambda.gui.lambdalabel.LambdaLabel.AbstractLabel;
import lambda.gui.lambdalabel.LambdaLabel.ApplyLabel;
import lambda.gui.lambdalabel.LambdaLabel.LiteralLabel;
import lambda.gui.lambdalabel.LambdaLabel.MacroLabel;
import lambda.gui.lambdalabel.LambdaLabel.RedexWrapper;
import lambda.gui.lambdalabel.LambdaLabel.VisitorRP;

public class LambdaLabelMetrics
{
	private static MeasureVisitor visitor;

	public static int getWidth(Graphics g, LambdaLabel lambdaLabel)
	{
		if (visitor == null)
		{
			visitor = new MeasureVisitor(2);
		}
		return visitor.calculateSize(g.getFontMetrics(), lambdaLabel);
	}

	private static class MeasureVisitor implements VisitorRP<Integer, Boolean>
	{
		private FontMetrics fm;
		private int padding;

		public MeasureVisitor(int padding)
		{
			this.padding = padding;
		}

		public int calculateSize(FontMetrics fm, LambdaLabel lambdaLabel)
		{
			this.fm = fm;
			return lambdaLabel.accept(this, false);
		}

		public Integer visit(LiteralLabel l, Boolean paren)
		{
			int w = getParenedWidth(l.name, paren);
			l.setWidth(w);
			return l.getWidth();
		}

		public Integer visit(ApplyLabel app, Boolean paren)
		{
			boolean lpar = app.lexpr.isParenRequiredInAppLeft();
			boolean rpar = app.rexpr.isParenRequiredInAppRight();
			int width = app.lexpr.accept(this, lpar) + app.rexpr.accept(this, rpar);
			if (paren)
			{
				width += getParenWidth();
			}
			app.setWidth(width);
			return width;
		}

		public Integer visit(AbstractLabel abs, Boolean paren)
		{
			LambdaLabel e = abs;
			int width = fm.charWidth('\\');
			while (e.isAbstract())
			{
				AbstractLabel eAbs = (AbstractLabel)e;
				width += fm.stringWidth(eAbs.name);
				e = eAbs.body;
			}
			width += fm.charWidth('.');
			width += e.accept(this, false);
			if (paren)
			{
				width += getParenWidth();
			}
			abs.setWidth(width);
			return abs.getWidth();
		}

		public Integer visit(MacroLabel macro, Boolean paren)
		{
			int w = getParenedWidth("<" + macro.name + ">", paren);
			macro.setWidth(w);
			return macro.getWidth();
		}

		public Integer visit(RedexWrapper wrap, Boolean paren)
		{
			int w = wrap.lambda.accept(this, paren);
			wrap.setWidth(w + 2 * padding);
			return wrap.getWidth();
		}

		private int getParenWidth()
		{
			return fm.charWidth('(') + fm.charWidth(')');
		}

		private int getParenedWidth(String s, boolean paren)
		{
			int w = getWidth(s);
			if (paren)
			{
				w += getParenWidth();
			}
			return w;
		}

		private int getWidth(String s)
		{
			return fm.stringWidth(s);
		}
	}
}
