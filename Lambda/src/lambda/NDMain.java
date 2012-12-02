package lambda;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import lambda.stategraph.IStateNode;
import lambda.stategraph.InfinityNode;
import lambda.stategraph.LambdaNode;
import lambda.stategraph.gui.GraphNode;
import lambda.stategraph.gui.StateFrame;

public class NDMain
{
	private static Set<IStateNode> states = new HashSet<IStateNode>();
	private static Map<LambdaNode, Set<IStateNode>> edges = new HashMap<LambdaNode, Set<IStateNode>>();

	private static void addEdge(LambdaNode src, IStateNode dest)
	{
		Set<IStateNode> dests = edges.get(src);
		if (dests == null)
		{
			dests = new HashSet<IStateNode>();
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

			if (states.contains(p))
			{
				continue;
			}
			states.add(p);

			List<IRedex> redexes = RedexFinder.getRedexList(p.lambda);
			for (IRedex redex : redexes)
			{
				Reducer.Result ret = Reducer.reduce(p.lambda, env, redex);
				LambdaNode p2 = new LambdaNode(p.depth + 1, ret.lambda);
				if (p.depth + 1 <= maxDepth || states.contains(p2))
				{
					addEdge(p, p2);
					queue.add(p2);
				}
				else
				{
					addEdge(p, InfinityNode.getInstance());
					continue;
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
		Map<IStateNode, GraphNode> gns = new HashMap<IStateNode, GraphNode>();

		Queue<LambdaNode> queue = new LinkedList<LambdaNode>();
		LambdaNode initial = new LambdaNode(0, lambda);
		{
			GraphNode gn = new GraphNode(initial.getText());
			f.addNode(gn);
			gns.put(initial, gn);
			f.setInitialNode(gn);
		}
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
				GraphNode gn = new GraphNode(p.getText());
				f.addNode(gn);
				gns.put(p, gn);
			}

			List<IRedex> redexes = p.getRedexes();
			if (redexes.isEmpty())
			{
				gns.get(p).setAccept(true);
			}

			for (IRedex redex : redexes)
			{
				Result ret = Reducer.reduce(p.lambda, env, redex);

				LambdaNode lambdaNode = new LambdaNode(p.depth + 1, ret.lambda);
				IStateNode p2 = lambdaNode;
				if (lambdaNode.depth <= maxDepth || states.contains(lambdaNode))
				{
					if (!gns.containsKey(lambdaNode))
					{
						GraphNode gn = new GraphNode(lambdaNode.getText());
						if (lambdaNode.getRedexes().isEmpty())
						{
							gn.setAccept(true);
						}
						f.addNode(gn);
						gns.put(lambdaNode, gn);
					}
					queue.add(lambdaNode);
					p2 = lambdaNode;
				}
				else
				{
					InfinityNode infNode = InfinityNode.getInstance();
					if (!gns.containsKey(infNode))
					{
						GraphNode gn = new GraphNode(infNode.getText());
						f.addNode(gn);
						gns.put(infNode, gn);
					}
					p2 = infNode;
				}
				f.addEdge(gns.get(p), gns.get(p2));
			}
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
		Map<IStateNode, Integer> idMap = new HashMap<IStateNode, Integer>();
		int id = 0;
		for (IStateNode node : states)
		{
			idMap.put(node, id++);
		}

		System.out.println("digraph {");
		System.out.println("  ratio=1.0");
		System.out.println("  fontsize=4;");
		for (IStateNode node : states)
		{
			if (node.isNormalForm())
			{
				System.out.printf("  %d [label=\"%s\",peripheries=2];\r\n", idMap.get(node), node.getText());
			}
			else
			{
				System.out.printf("  %d [label=\"%s\"];\r\n", idMap.get(node), node.getText());
			}
			//System.out.printf("  %d [label=\"\"];\r\n", idMap.get(node));
		}
		for (Map.Entry<LambdaNode, Set<IStateNode>> e : edges.entrySet())
		{
			LambdaNode start = e.getKey();
			System.out.printf("  %d->{", idMap.get(start));
			for (IStateNode end : e.getValue())
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

		Map<IStateNode, GraphNode> gns = new HashMap<IStateNode, GraphNode>();
		for (IStateNode node : states)
		{
			GraphNode gn = new GraphNode(node.getText());
			f.addNode(gn);
			gns.put(node, gn);
			if (node.isNormalForm())
			{
				gn.setAccept(true);
			}
		}
		for (Map.Entry<LambdaNode, Set<IStateNode>> e : edges.entrySet())
		{
			LambdaNode start = e.getKey();
			GraphNode[] ns = new GraphNode[e.getValue().size()];
			int i = 0;
			for (IStateNode end : e.getValue())
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
					searchOnLine(lambda, 10);
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
