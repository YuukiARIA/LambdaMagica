package lambda.ast;

public final class NodeDescriptor
{
	public final int depth;
	public final int edge;
	public final Lambda node;

	public NodeDescriptor(int depth, int edge, Lambda node)
	{
		this.depth = depth;
		this.edge = edge;
		this.node = node;
	}

	private boolean equals(NodeDescriptor nd)
	{
		return depth == nd.depth && edge == nd.edge && node == nd.node;
	}

	public boolean equals(Object o)
	{
		return o == this || o instanceof NodeDescriptor && equals((NodeDescriptor)o);
	}

	public int hashCode()
	{
		return 31 * depth ^ 17 * edge ^ node.hashCode();
	}
}
