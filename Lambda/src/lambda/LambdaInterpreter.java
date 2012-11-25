package lambda;

import java.util.Set;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.IDContext;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.Lambda.VisitorR;
import lambda.ast.VariableCollector;
import util.Pair;

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
		if (!isNormal && !isCyclic)
		{
			if (isEtaEnabled)
			{
				Pair<Boolean, Lambda> ret = lambda.etaReduction();
				if (ret._1)
				{
					lambda = ret._2;
					isNormal = NormalFormChecker.isNormalForm(lambda);
					return true;
				}
			}
			Pair<Boolean, Lambda> ret = lambda.betaReduction(IDContext.createContext(), env);
			isCyclic = LambdaMatcher.structuralEquivalent(lambda, ret._2);
			lambda = ret._2;
			isNormal = NormalFormChecker.isNormalForm(lambda);
			if (ret._1)
			{
				stepCount++;
			}
			return ret._1 && !isCyclic;
		}
		return false;
	}

	public boolean step(Environment env, IRedex redex)
	{
		if (!isNormal && !isCyclic)
		{
			if (isEtaEnabled)
			{
				Pair<Boolean, Lambda> ret = lambda.etaReduction();
				if (ret._1)
				{
					lambda = ret._2;
					isNormal = NormalFormChecker.isNormalForm(lambda);
					return true;
				}
			}
			Pair<Boolean, Lambda> ret = lambda.betaReduction(IDContext.createContext(), env, redex);
			isCyclic = LambdaMatcher.structuralEquivalent(lambda, ret._2);
			lambda = ret._2;
			isNormal = NormalFormChecker.isNormalForm(lambda);
			if (ret._1)
			{
				stepCount++;
			}
			return ret._1 && !isCyclic;
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

	private static class NormalFormChecker extends VisitorR<Boolean>
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
