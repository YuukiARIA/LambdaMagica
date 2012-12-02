package lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.WindowConstants;

import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.RedexFinder;
import lambda.ast.parser.ParserException;
import lambda.reduction.Reducer;
import lambda.reduction.Reducer.Result;
import lambda.serialize.LambdaSerializer;
import lambda.stategraph.gui.GraphNode;
import lambda.stategraph.gui.StateFrame;

class LambdaNode
{
	public final int depth;
	public final Lambda lambda;
	public boolean normal;
	private short[] data;
	private List<IRedex> redexes;

	public LambdaNode(int depth, Lambda lambda)
	{
		this.depth = depth;
		this.lambda = lambda;
		if (lambda != null) data = LambdaSerializer.serialize(lambda);
		else data = new short[] { 0x7FFD };

		if (lambda != null)
		{
			redexes = RedexFinder.getRedexList(lambda);
		}
	}

	public short[] getData()
	{
		return data;
	}

	public List<IRedex> getRedexes()
	{
		return Collections.unmodifiableList(redexes);
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

public class NDMain
{
	private static LambdaNode NODE_INF;
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

			if (states.contains(p))
			{
				continue;
			}
			states.add(p);

			List<IRedex> redexes = RedexFinder.getRedexList(l);
			if (redexes.isEmpty())
			{
				p.normal = true;
			}
			else
			{
				for (IRedex redex : redexes)
				{
					Reducer.Result ret = Reducer.reduce(l, env, redex);
					LambdaNode p2 = new LambdaNode(p.depth + 1, ret.lambda);
					if (p.depth + 1 <= maxDepth || states.contains(p2))
					{
						addEdge(p, p2);
						queue.add(p2);
					}
					else
					{
						if (NODE_INF == null)
						{
							NODE_INF = new LambdaNode(0, null);
							states.add(NODE_INF);
						}
						addEdge(p, NODE_INF);
						System.out.println("limit");
						continue;
					}
				}
			}
		}
		System.out.println("states = " + states.size());
	}

	private static void searchOnLine(Lambda lambda, int maxDepth)
	{
		StateFrame f = new StateFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setVisible(true);

		Environment env = Environment.getEnvironment();
		Map<LambdaNode, GraphNode> gns = new HashMap<LambdaNode, GraphNode>();

		Queue<LambdaNode> queue = new LinkedList<LambdaNode>();
		LambdaNode initial = new LambdaNode(0, lambda);
		queue.add(initial);
		while (!queue.isEmpty())
		{
			LambdaNode p = queue.poll();

			if (states.contains(p))
			{
				continue;
			}
			states.add(p);

			if (!gns.containsKey(p))
			{
				String label = p.lambda.toString().replace("\\", "λ");
				GraphNode gn = new GraphNode(label);
				f.addNode(gn);
				gns.put(p, gn);
			}

			if (p.depth == 0)
			{
				f.setInitialNode(gns.get(p));
			}

			List<IRedex> redexes = p.getRedexes();
			if (redexes.isEmpty())
			{
				p.normal = true;
				gns.get(p).setAccept(true);
			}

			for (IRedex redex : redexes)
			{
				Result ret = Reducer.reduce(p.lambda, env, redex);
				LambdaNode p2 = new LambdaNode(p.depth + 1, ret.lambda);
				if (p.depth + 1 <= maxDepth)
				{
					if (!gns.containsKey(p2))
					{
						String label = p2.lambda.toString().replace("\\", "λ");
						GraphNode gn = new GraphNode(label);
						if (p2.getRedexes().isEmpty())
						{
							p2.normal = true;
							gn.setAccept(true);
						}
						f.addNode(gn);
						gns.put(p2, gn);
					}
					queue.add(p2);
				}
				else
				{
					if (NODE_INF == null)
					{
						NODE_INF = new LambdaNode(0, null);
						states.add(NODE_INF);
						GraphNode gn = new GraphNode("∞");
						f.addNode(gn);
						gns.put(NODE_INF, gn);
					}
					p2 = NODE_INF;
				}
				addEdge(p, p2);
				f.addEdge(gns.get(p), gns.get(p2));
			}
			//f.repaint();
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
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
		System.out.println("  ratio=1.0");
		System.out.println("  fontsize=4;");
		for (LambdaNode node : states)
		{
			String label;
			if (node != NODE_INF)
			{
				label = node.lambda.toString().replace("\\", "λ");
			}
			else
			{
				label = "∞";
			}
			if (node.normal)
			{
				System.out.printf("  %d [label=\"%s\",peripheries=2];\r\n", idMap.get(node), label);
			}
			else
			{
				System.out.printf("  %d [label=\"%s\"];\r\n", idMap.get(node), label);
			}
			//System.out.printf("  %d [label=\"\"];\r\n", idMap.get(node));
		}
		for (Map.Entry<LambdaNode, Set<LambdaNode>> e : edges.entrySet())
		{
			LambdaNode start = e.getKey();
			if (start == NODE_INF) continue;
			System.out.printf("  %d->{", idMap.get(start));
			for (LambdaNode end : e.getValue())
			{
				System.out.printf(" %d", idMap.get(end));
			}
			System.out.println(" };");
		}
		System.out.println("}");
	}

	private static void outputGraphFrame()
	{
		StateFrame f = new StateFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		Map<LambdaNode, GraphNode> gns = new HashMap<LambdaNode, GraphNode>();
		for (LambdaNode node : states)
		{
			String label;
			if (node != NODE_INF)
			{
				label = node.lambda.toString().replace("\\", "λ");
			}
			else
			{
				label = "∞";
			}
			
			GraphNode gn = new GraphNode(label);
			f.addNode(gn);
			gns.put(node, gn);
			
			if (node.normal)
			{
				gn.setAccept(true);
			}
			
			if (node != NODE_INF && node.depth == 0)
			{
				f.setInitialNode(gn);
			}
		}
		for (Map.Entry<LambdaNode, Set<LambdaNode>> e : edges.entrySet())
		{
			LambdaNode start = e.getKey();
			if (start == NODE_INF) continue;
			
			GraphNode[] ns = new GraphNode[e.getValue().size()];
			int i = 0;
			for (LambdaNode end : e.getValue())
			{
				ns[i++] = gns.get(end);
			}
			f.addEdge(gns.get(start), ns);
		}
		
		f.setVisible(true);
	}

	// (\tf.f)(\f.(\x.f(xx))(\x.f(xx)))stop
	// (\xy.x(\tf.t)y)(\tf.t)((\p.p(\tf.f)(\tf.t))((\xy.xy(\tf.f))(\tf.t)(\tf.f)))
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
					//System.out.println("input = " + lambda);
					searchOnLine(lambda, 100);
					//outputGraphFrame();
					//outputGraph();
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
