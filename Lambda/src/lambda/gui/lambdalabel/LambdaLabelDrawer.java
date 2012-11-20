package lambda.gui.lambdalabel;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;

import lambda.gui.lambdalabel.LambdaLabel.AbstractLabel;
import lambda.gui.lambdalabel.LambdaLabel.ApplyLabel;
import lambda.gui.lambdalabel.LambdaLabel.LiteralLabel;
import lambda.gui.lambdalabel.LambdaLabel.MacroLabel;
import lambda.gui.lambdalabel.LambdaLabel.RedexWrapper;
import lambda.gui.lambdalabel.LambdaLabel.Visitor;

public class LambdaLabelDrawer implements Visitor<Boolean>
{
	private Graphics g;
	private FontMetrics fm;
	private Point point;

	public void draw(Graphics g, LambdaLabel lambda)
	{
		this.g = g;
		this.fm = g.getFontMetrics();

		LambdaLabelMetrics.getWidth(g, lambda);

		point = new Point(0, fm.getAscent() + fm.getLeading());
		g.setColor(Color.BLACK);
		lambda.accept(this, false);
	}

	public void draw(Graphics g, LambdaLabel lambda, int h)
	{
		this.g = g;
		this.fm = g.getFontMetrics();

		LambdaLabelMetrics.getWidth(g, lambda);

		point = new Point(0, (h - fm.getHeight()) / 2 + fm.getAscent() + fm.getLeading());
		g.setColor(Color.BLACK);
		lambda.accept(this, false);
	}

	public void draw(Graphics g, LambdaLabel lambda, int x, int y, int h)
	{
		this.g = g;
		this.fm = g.getFontMetrics();

		LambdaLabelMetrics.getWidth(g, lambda);

		point = new Point(x, y + (h - fm.getHeight()) / 2 + fm.getAscent() + fm.getLeading());
		g.setColor(Color.BLACK);
		lambda.accept(this, false);
	}

	public void visit(LiteralLabel l, Boolean paren)
	{
		String s = l.name;
		if (paren) drawString("(");
		drawString(s);
		if (paren) drawString(")");
	}

	public void visit(ApplyLabel app, Boolean paren)
	{
		boolean lpar = app.lexpr.isAbstract();
		boolean rpar = !app.rexpr.isAtomic();

		if (paren)
		{
			drawString("(");
		}
		app.lexpr.accept(this, lpar);
		app.rexpr.accept(this, rpar);
		if (paren)
		{
			drawString(")");
		}
	}

	public void visit(AbstractLabel abs, Boolean paren)
	{
		LambdaLabel e = abs;
		String s = "\\";
		while (e.isAbstract())
		{
			AbstractLabel eAbs = (AbstractLabel)e;
			s += eAbs.name;
			e = eAbs.body;
		}
		s += ".";

		if (paren) drawString("(");
		drawString(s);
		e.accept(this, false);
		if (paren) drawString(")");
	}

	public void visit(MacroLabel macro, Boolean paren)
	{
		String s = "<" + macro.name + ">";
		if (paren) drawString("(");
		drawString(s);
		if (paren) drawString(")");
	}

	public void visit(RedexWrapper wrap, Boolean paren)
	{
		g.setColor(wrap.color);
		g.fillRoundRect(point.x + 1, point.y - (fm.getAscent() + fm.getLeading()) + 1, wrap.getWidth() - 2, fm.getHeight() - 2, 10, 10);
		g.setColor(Color.BLACK);

		point.x += 2;
		wrap.lambda.accept(this, paren);
		point.x += 2;
	}

	private void drawString(String s)
	{
		g.drawString(s, point.x, point.y);
		point.x += fm.stringWidth(s);
	}
	//(\xy.xy)(\xy.(\a.a)y)
}
