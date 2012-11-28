package lambda.gui.lambdalabel;

import java.awt.Color;

public abstract class LambdaLabel
{
	private int width;

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public abstract boolean isParenRequiredInAppLeft();
	public abstract boolean isParenRequiredInAppRight();
	public abstract boolean isAbstract();
	public abstract boolean isAtomic();

	public abstract <T> void accept(VisitorP<T> visitor, T param);
	public abstract <T> T accept(VisitorR<T> visitor);
	public abstract <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param);

	public static LiteralLabel literal(String name)
	{
		return new LiteralLabel(name);
	}

	public static ApplyLabel apply(LambdaLabel x, LambdaLabel y)
	{
		return new ApplyLabel(x, y);
	}

	public static AbstractLabel abs(String name, LambdaLabel e)
	{
		return new AbstractLabel(name, e);
	}

	public static MacroLabel macro(String name)
	{
		return new MacroLabel(name);
	}

	public static RedexWrapper wrap(LambdaLabel l, Color c)
	{
		return new RedexWrapper(l, c);
	}

	public static interface VisitorP<T>
	{
		public void visit(LiteralLabel l, T param);
		public void visit(ApplyLabel app, T param);
		public void visit(AbstractLabel abs, T param);
		public void visit(MacroLabel macro, T param);
		public void visit(RedexWrapper wrap, T param);
	}

	public static interface VisitorR<T>
	{
		public T visit(LiteralLabel l);
		public T visit(ApplyLabel app);
		public T visit(AbstractLabel abs);
		public T visit(MacroLabel macro);
		public T visit(RedexWrapper wrap);
	}

	public static interface VisitorRP<TRet, TParam>
	{
		public TRet visit(LiteralLabel l, TParam param);
		public TRet visit(ApplyLabel app, TParam param);
		public TRet visit(AbstractLabel abs, TParam param);
		public TRet visit(MacroLabel macro, TParam param);
		public TRet visit(RedexWrapper wrap, TParam param);
	}

	public static class LiteralLabel extends LambdaLabel
	{
		public final String name;

		public LiteralLabel(String name)
		{
			this.name = name;
		}

		public boolean isParenRequiredInAppLeft()
		{
			return false;
		}

		public boolean isParenRequiredInAppRight()
		{
			return false;
		}

		public boolean isAbstract() { return false; }
		public boolean isAtomic() { return true; }

		public <T> void accept(VisitorP<T> visitor, T param) { visitor.visit(this, param); }
		public <T> T accept(VisitorR<T> visitor) { return visitor.visit(this); }
		public <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param) { return visitor.visit(this, param); }
	}

	public static class ApplyLabel extends LambdaLabel
	{
		public final LambdaLabel lexpr;
		public final LambdaLabel rexpr;

		public ApplyLabel(LambdaLabel x, LambdaLabel y)
		{
			this.lexpr = x;
			this.rexpr = y;
		}

		public boolean isParenRequiredInAppLeft()
		{
			return false;
		}

		public boolean isParenRequiredInAppRight()
		{
			return true;
		}

		public boolean isAbstract() { return false; }
		public boolean isAtomic() { return false; }

		public <T> void accept(VisitorP<T> visitor, T param) { visitor.visit(this, param); }
		public <T> T accept(VisitorR<T> visitor) { return visitor.visit(this); }
		public <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param) { return visitor.visit(this, param); }
	}

	public static class AbstractLabel extends LambdaLabel
	{
		public final String name;
		public final LambdaLabel body;

		public AbstractLabel(String name, LambdaLabel body)
		{
			this.name = name;
			this.body = body;
		}

		public boolean isParenRequiredInAppLeft()
		{
			return true;
		}

		public boolean isParenRequiredInAppRight()
		{
			return true;
		}

		public boolean isAbstract() { return true; }
		public boolean isAtomic() { return false; }

		public <T> void accept(VisitorP<T> visitor, T param) { visitor.visit(this, param); }
		public <T> T accept(VisitorR<T> visitor) { return visitor.visit(this); }
		public <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param) { return visitor.visit(this, param); }
	}

	public static class MacroLabel extends LambdaLabel
	{
		public final String name;

		public MacroLabel(String name)
		{
			this.name = name;
		}

		public boolean isParenRequiredInAppLeft()
		{
			return false;
		}

		public boolean isParenRequiredInAppRight()
		{
			return false;
		}

		public boolean isAbstract() { return false; }
		public boolean isAtomic() { return true; }

		public <T> void accept(VisitorP<T> visitor, T param) { visitor.visit(this, param); }
		public <T> T accept(VisitorR<T> visitor) { return visitor.visit(this); }
		public <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param) { return visitor.visit(this, param); }
	}

	public static class RedexWrapper extends LambdaLabel
	{
		public final LambdaLabel lambda;
		public final Color color;

		public RedexWrapper(LambdaLabel lambda, Color color)
		{
			this.lambda = lambda;
			this.color = color;
		}

		public boolean isParenRequiredInAppLeft()
		{
			return lambda.isParenRequiredInAppLeft();
		}

		public boolean isParenRequiredInAppRight()
		{
			return lambda.isParenRequiredInAppRight();
		}

		public boolean isAbstract() { return false; }
		public boolean isAtomic() { return lambda.isAtomic(); }

		public <T> void accept(VisitorP<T> visitor, T param) { visitor.visit(this, param); }
		public <T> T accept(VisitorR<T> visitor) { return visitor.visit(this); }
		public <TRet, TParam> TRet accept(VisitorRP<TRet, TParam> visitor, TParam param) { return visitor.visit(this, param); }
	}
}
