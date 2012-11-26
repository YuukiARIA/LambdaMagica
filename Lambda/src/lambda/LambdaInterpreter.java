package lambda;

import lambda.ast.IRedex;
import lambda.ast.Lambda;

public class LambdaInterpreter
{
	private Lambda sourceLambda;
	private Lambda lambda;
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
		if (!isCyclic)
		{
			Reducer.Result ret = Reducer.reduce(lambda, env, redex);
			isCyclic = AlphaComparator.alphaEquiv(lambda, ret.lambda);
			lambda = ret.lambda;
			if (ret.reduced)
			{
				stepCount++;
			}
			return ret.reduced;
		}
		return false;
	}

	public int getStep()
	{
		return stepCount;
	}

	public boolean isNormal()
	{
		return isNormal;
	}

	public boolean isCyclic()
	{
		return !isNormal && isCyclic;
	}

	public boolean isTerminated()
	{
		return isNormal() || isCyclic();
	}

	public Lambda getLambda()
	{
		return lambda;
	}
}
