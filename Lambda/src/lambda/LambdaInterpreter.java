package lambda;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.RedexFinder;
import lambda.reduction.Reducer;
import lambda.reduction.Reducer.Result;
import lambda.reduction.ReductionRule;

public class LambdaInterpreter
{
	public static class State
	{
		public final int stepNumber;
		public final Lambda lambda;
		public final ReductionRule appliedRule;

		private IRedex redex;

		public State(int stepNumber, Lambda lambda, ReductionRule appliedRule)
		{
			this.stepNumber = stepNumber;
			this.lambda = lambda;
			this.appliedRule = appliedRule;
		}

		public IRedex getReducedRedex()
		{
			return redex;
		}
	}

	private LinkedList<State> states = new LinkedList<State>();
	private State currentState;
	private Lambda sourceLambda;
	private boolean isCyclic;
	private boolean terminated;

	public void startInterpretation(Lambda sourceLambda)
	{
		this.sourceLambda = sourceLambda;
		initialize();
	}

	public void initialize()
	{
		isCyclic = false;
		terminated = false;
		states.clear();
		pushState(new State(0, sourceLambda, ReductionRule.NONE));
	}

	public Result step(Environment env, IRedex redex)
	{
		Result result = Reducer.reduce(getLambda(), env, redex);
		isCyclic = AlphaComparator.alphaEquiv(getLambda(), result.lambda);
		ReductionRule rule = result.appliedRule;

		int stepNumber = currentState.stepNumber;
		if (rule == ReductionRule.BETA_REDUCTION || rule == ReductionRule.ETA_REDUCTION)
		{
			stepNumber++;
		}
		currentState.redex = redex;
		pushState(new State(stepNumber, result.lambda, rule));

		return result;
	}

	public void revert()
	{
		pop();
	}

	public boolean isRevertable()
	{
		return states.size() > 1;
	}

	public Lambda getSourceLambda()
	{
		return sourceLambda;
	}

	public int getReductionStepCount()
	{
		return currentState.stepNumber;
	}

	public boolean isNormal()
	{
		boolean etaEnabled = Environment.getEnvironment().getBoolean(Environment.KEY_ETA_REDUCTION);
		return RedexFinder.isNormalForm(getLambda(), etaEnabled);
	}

	public boolean isCyclic()
	{
		return !isNormal() && isCyclic;
	}

	public boolean isTerminated()
	{
		return terminated || isNormal() || isCyclic();
	}

	public void terminate()
	{
		terminated = true;
	}

	public Lambda getLambda()
	{
		return currentState.lambda;
	}

	public List<State> getStates()
	{
		return Collections.unmodifiableList(states);
	}

	private void pushState(State s)
	{
		states.addLast(s);
		currentState = s;
	}

	private void pop()
	{
		if (isRevertable())
		{
			states.removeLast();
			currentState = states.getLast();
		}
	}
}
