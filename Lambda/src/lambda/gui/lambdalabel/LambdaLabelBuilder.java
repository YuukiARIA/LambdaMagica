package lambda.gui.lambdalabel;

import java.awt.Color;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IRedex;
import lambda.ast.Lambda;

public class LambdaLabelBuilder
{
	private IRedex redex;
	private ConvertVisitor visitor;

	public LambdaLabelBuilder()
	{
		visitor = new ConvertVisitor();
	}

	public LambdaLabel createLambdaLabel(Lambda lambda, IRedex redex)
	{
		this.redex = redex;
		return lambda.accept(visitor);
	}

	private class ConvertVisitor implements Lambda.VisitorR<LambdaLabel>
	{
		public LambdaLabel visit(ASTAbstract abs)
		{
			LambdaLabel label = LambdaLabel.abs(abs.name, abs.e.accept(this));
			if (abs == redex)
			{
				label = LambdaLabel.wrap(label, new Color(255, 240, 100, 200));
			}
			return label;
		}

		public LambdaLabel visit(ASTApply app)
		{
			LambdaLabel left = app.lexpr.accept(this), right = app.rexpr.accept(this);
			if (app == redex)
			{
				left = LambdaLabel.wrap(left, new Color(255, 220, 220, 200));
				right = LambdaLabel.wrap(right, new Color(220, 220, 255, 200));
			}
			return LambdaLabel.apply(left, right);
		}

		public LambdaLabel visit(ASTLiteral l)
		{
			return LambdaLabel.literal(l.name);
		}

		public LambdaLabel visit(ASTMacro m)
		{
			LambdaLabel label = LambdaLabel.macro(m.name);
			if (m == redex)
			{
				label = LambdaLabel.wrap(label, new Color(220, 255, 220, 200));
			}
			return label;
		}
	}
}
