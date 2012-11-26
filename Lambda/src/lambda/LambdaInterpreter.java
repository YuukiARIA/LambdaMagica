package lambda;

import java.util.Set;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.Lambda.VisitorR;
import lambda.ast.VariableCollector;

public class LambdaInterpreter
{
	private Lambda sourceLambda;
	private Lambda lambda;
	private boolean isEtaEnabled;
	private boolean isNormal;
	private boolean isCyclic;
	private int stepCount;

	public LambdaInterpreter(Lambda sourceLambda)
	{
		this.sourceLambda = sourceLambda;
		initialize();
	}

	public void initialize()
	{
		lambda = sourceLambda;
		isNormal = false;
		isCyclic = false;
		stepCount = 0;
	}

	public boolean step(Environment env)
	{
		return step(env, null);
	}

	public boolean step(Environment env, IRedex redex)
	{
		if (!isNormal && !isCyclic)
		{
			Reducer.Result ret = Reducer.reduce(lambda, env, redex);
			isCyclic = AlphaComparator.alphaEquiv(lambda, ret.lambda);
			lambda = ret.lambda;
			isNormal = NormalFormChecker.isNormalForm(lambda);
			if (ret.reduced)
			{
				stepCount++;
			}
			return ret.reduced && !isCyclic;
		}
		return false;
	}

	public int getStep()
	{
		return stepCount;
	}

	public boolean isNormal()
	{
		return this.isNormal;
	}

	public boolean isCyclic()
	{
		return !isNormal && isCyclic;
	}

	public boolean isTerminated()
	{
		return (isNormal() || isCyclic()) && (!isEtaEnabled || !isEtaRedex(lambda));
	}

	public Lambda getLambda()
	{
		return lambda;
	}

	private static boolean isEtaRedex(Lambda lambda)
	{
		if (lambda.isAbstraction())
		{
			ASTAbstract abs = (ASTAbstract)lambda;
			if (abs.e.isApplication())
			{
				ASTApply app = (ASTApply)abs.e;
				if (app.rexpr.isLiteral())
				{
					ASTLiteral x = (ASTLiteral)app.rexpr;
					VariableCollector vc = new VariableCollector(app.lexpr);
					Set<String> fv = vc.getFreeVariables();
					if (!fv.contains(x.name))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	private static class NormalFormChecker implements VisitorR<Boolean>
	{
		private static NormalFormChecker visitor;

		public static boolean isNormalForm(Lambda lambda)
		{
			if (visitor == null)
			{
				visitor = new NormalFormChecker();
			}
			return visitor.visit(lambda);
		}

		private boolean visit(Lambda lambda)
		{
			return lambda.accept(this);
		}

		public Boolean visit(ASTAbstract abs)
		{
			return visit(abs.e);
		}

		public Boolean visit(ASTApply app)
		{
			return !app.lexpr.isAbstraction() && visit(app.lexpr) && visit(app.rexpr);
		}

		public Boolean visit(ASTLiteral literal)
		{
			return true;
		}

		public Boolean visit(ASTMacro macro)
		{
			return false;
		}
	}
}
