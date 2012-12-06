package lambda.stategraph;

import java.util.Collections;
import java.util.List;

import lambda.Environment;
import lambda.ast.IRedexNode;
import lambda.ast.Lambda;
import lambda.reduction.RedexFinder;
import lambda.serialize.LambdaSerializer;

public class LambdaNode implements IStateNode
{
	public final int depth;
	public final Lambda lambda;

	private String text;
	private short[] data;
	private List<IRedexNode> redexes;

	public LambdaNode(int depth, Lambda lambda)
	{
		this.depth = depth;
		this.lambda = lambda;
		data = LambdaSerializer.serialize(lambda);
		redexes = RedexFinder.getRedexList(lambda, Environment.getEnvironment().getBoolean(Environment.KEY_ETA_REDUCTION));
	}

	public short[] getData()
	{
		return data;
	}

	public String getText()
	{
		if (text == null)
		{
			text = lambda.toString().replace("\\", "λ");
		}
		return text;
	}

	public List<IRedexNode> getRedexes()
	{
		return Collections.unmodifiableList(redexes);
	}

	public boolean isNormalForm()
	{
		return redexes.isEmpty();
	}

	public int hashCode()
	{
		return (31 * data.length) ^ data[0] ^ data[data.length >> 1];
	}

	private boolean equals(LambdaNode n)
	{
		if (data.length != n.data.length) return false;
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] != n.data[i]) return false;
		}
		return true;
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof LambdaNode && equals((LambdaNode)o);
	}

	public String toString()
	{
		return lambda.toString();
	}
}
