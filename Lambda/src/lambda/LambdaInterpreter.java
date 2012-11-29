package lambda;

import java.util.LinkedList;

import lambda.ast.IRedex;
import lambda.ast.Lambda;

public class LambdaInterpreter
{
	private LinkedList<Lambda> steps = new LinkedList<Lambda>();
	private Lambda sourceLambda;
	private Lambda lambda;
	private boolean isNormal;
	private boolean isCyclic;

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
		steps.clear();
	}

	public boolean step(Environment env)
	{
		return step(env, null);
	}

	public boolean step(Environment env, IRedex redex)
	{
		if (!isCyclic)
		{
			push();
			Reducer.Result ret = Reducer.reduce(lambda, env, redex);
			isCyclic = AlphaComparator.alphaEquiv(lambda, ret.lambda);
			lambda = ret.lambda;
			return ret.reduced;
		}
		return false;
	}

	public void revert()
	{
		pop();
	}

	public boolean isRevertable()
	{
		return !steps.isEmpty();
	}

	public int getStep()
	{
		return steps.size();
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

	private void push()
	{
		steps.push(lambda);
	}

	private void pop()
	{
		if (!steps.isEmpty())
		{
			lambda = steps.pop();
		}
	}
}
