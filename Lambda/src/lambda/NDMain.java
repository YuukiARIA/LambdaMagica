package lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import lambda.ast.IDContext;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.RedexFinder;
import lambda.ast.parser.ParserException;
import lambda.serialize.LambdaSerializer;
import util.Pair;

class LambdaNode
{
	public final int depth;
	public final Lambda lambda;
	private short[] data;

	public LambdaNode(int depth, Lambda lambda)
	{
		this.depth = depth;
		this.lambda = lambda;
		data = LambdaSerializer.serialize(lambda);
	}

	public short[] getData()
	{
		return data;
	}

	public int hashCode()
	{
		return (31 * data.length) ^ data[0] ^ data[data.length - 1];
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

public class NDMain
{
	private static Set<LambdaNode> states = new HashSet<LambdaNode>();
	private static Map<LambdaNode, Set<LambdaNode>> edges = new HashMap<LambdaNode, Set<LambdaNode>>();

	private static void addEdge(LambdaNode src, LambdaNode dest)
	{
		Set<LambdaNode> dests = edges.get(src);
		if (dests == null)
		{
			dests = new HashSet<LambdaNode>();
			edges.put(src, dests);
		}
		dests.add(dest);
	}

	private static void search(Lambda lambda, int maxDepth)
	{
		Environment env = Environment.getEnvironment();

		Queue<LambdaNode> queue = new LinkedList<LambdaNode>();
		queue.add(new LambdaNode(0, lambda));
		while (!queue.isEmpty())
		{
			LambdaNode p = queue.poll();
			Lambda l = p.lambda;

			if (p.depth >= maxDepth || states.contains(p))
			{
				continue;
			}
			states.add(p);

			for (IRedex redex : RedexFinder.getRedexList(l))
			{
				Pair<Boolean, Lambda> ret = l.betaReduction(IDContext.createContext(), env, redex);
				LambdaNode p2 = new LambdaNode(p.depth + 1, ret._2);
				addEdge(p, p2);
				queue.add(p2);
			}
		}
		System.out.println("states = " + states.size());
	}

	private static void outputGraph()
	{
		Map<LambdaNode, Integer> idMap = new HashMap<LambdaNode, Integer>();
		int id = 0;
		for (LambdaNode node : states)
		{
			idMap.put(node, id++);
		}

		System.out.println("digraph {");
		for (LambdaNode node : states)
		{
			String label = node.lambda.toString().replace("\\", "λ");
			//System.out.printf("  %d [label=\"%s\"];\r\n", idMap.get(node), label);
			System.out.printf("  %d [label=\"\"];\r\n", idMap.get(node));
		}
		for (Map.Entry<LambdaNode, Set<LambdaNode>> e : edges.entrySet())
		{
			LambdaNode start = e.getKey();
			System.out.printf("  %d->{", idMap.get(start));
			for (LambdaNode end : e.getValue())
			{
				System.out.printf(" %d", idMap.get(end));
			}
			System.out.println(" };");
		}
		System.out.println("}");
	}

	public static void main(String[] args)
	{
		System.out.println("ND mode");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try
		{
			while ((line = reader.readLine()) != null)
			{
				try
				{
					Lambda lambda = Lambda.parse(line);
					System.out.println("input = " + lambda);
					search(lambda, 1000);
					outputGraph();
				}
				catch (ParserException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
