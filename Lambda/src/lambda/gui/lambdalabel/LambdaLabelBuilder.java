package lambda.gui.lambdalabel;

import java.awt.Color;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IRedexNode;
import lambda.ast.Lambda;

public class LambdaLabelBuilder
{
	private static final Color COLOR_ABS = new Color(255, 240, 100, 200);
	private static final Color COLOR_APP_L = new Color(255, 200, 200, 200);
	private static final Color COLOR_APP_R = new Color(200, 200, 255, 200);
	private static final Color COLOR_MACRO = new Color(200, 255, 200, 200);

	private IRedexNode redex;
	private ConvertVisitor visitor;

	public LambdaLabelBuilder()
	{
		visitor = new ConvertVisitor();
	}

	public LambdaLabel createLambdaLabel(Lambda lambda, IRedexNode redex)
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
				label = LambdaLabel.wrap(label, COLOR_ABS);
			}
			return label;
		}

		public LambdaLabel visit(ASTApply app)
		{
			LambdaLabel left = app.lexpr.accept(this), right = app.rexpr.accept(this);
			if (app == redex)
			{
				left = LambdaLabel.wrap(left, COLOR_APP_L);
				right = LambdaLabel.wrap(right, COLOR_APP_R);
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
				label = LambdaLabel.wrap(label, COLOR_MACRO);
			}
			return label;
		}
	}
}
