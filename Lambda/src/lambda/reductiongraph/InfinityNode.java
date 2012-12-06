package lambda.reductiongraph;

public class InfinityNode implements IStateNode
{
	private static InfinityNode instance;

	private InfinityNode()
	{
	}

	public String getText()
	{
		return "∞";
	}

	public boolean isNormalForm()
	{
		return false;
	}

	public int hashCode()
	{
		return 0xFFFFFFFF;
	}

	public boolean equals(Object o)
	{
		return o == this;
	}

	public static synchronized InfinityNode getInstance()
	{
		if (instance == null)
		{
			instance = new InfinityNode();
		}
		return instance;
	}
}
