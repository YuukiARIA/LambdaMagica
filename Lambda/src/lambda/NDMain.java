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

import lambda.ast.IRedexNode;
import lambda.ast.Lambda;
import lambda.ast.parser.ParserException;
import lambda.macro.MacroDefinition;
import lambda.reduction.RedexFinder;
import lambda.reduction.Reducer;
import lambda.reduction.Reducer.Result;
import lambda.stategraph.IStateNode;
import lambda.stategraph.InfinityNode;
import lambda.stategraph.LambdaNode;
import lambda.stategraph.gui.GraphNode;
import lambda.stategraph.gui.StateFrame;

public class NDMain
{
	private static final MacroDefinition MACRO_DEF = new MacroDefinition();

	private static Set<IStateNode> states = new HashSet<IStateNode>();
	private static Map<LambdaNode, Set<IStateNode>> edges = new HashMap<LambdaNode, Set<IStateNode>>();
	private static Map<IStateNode, GraphNode> nodeMapping = new HashMap<IStateNode, GraphNode>();
	private static boolean createdNew;

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

	private static GraphNode addGraphNode(StateFrame f, IStateNode node)
	{
		GraphNode gn = nodeMapping.get(node);
		if (gn == null)
		{
			gn = new GraphNode(node.getText());
			nodeMapping.put(node, gn);
			f.addNode(gn);
			createdNew = true;
		}
		else
		{
			createdNew = false;
		}
		return gn;
	}

	private static void search(Lambda lambda, int maxDepth)
	{
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

			List<IRedexNode> redexes = RedexFinder.getRedexList(p.lambda);
			for (IRedexNode redex : redexes)
			{
				Reducer.Result ret = Reducer.reduce(p.lambda, MACRO_DEF, redex);
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

		nodeMapping.clear();

		Queue<LambdaNode> queue = new LinkedList<LambdaNode>();
		LambdaNode initial = new LambdaNode(0, lambda);
		GraphNode gnInitial = addGraphNode(f, initial);
		f.setInitialNode(gnInitial);
		queue.add(initial);
		while (!queue.isEmpty())
		{
			LambdaNode p1 = queue.poll();

			if (states.contains(p1))
			{
				continue;
			}
			states.add(p1);

			GraphNode gn1 = addGraphNode(f, p1);

			List<IRedexNode> redexes = p1.getRedexes();
			if (redexes.isEmpty())
			{
				gn1.setAccept(true);
			}

			for (IRedexNode redex : redexes)
			{
				Result ret = Reducer.reduce(p1.lambda, MACRO_DEF, redex);

				LambdaNode lambdaNode = new LambdaNode(p1.depth + 1, ret.lambda);
				GraphNode gn2;
				if (lambdaNode.depth <= maxDepth || states.contains(lambdaNode))
				{
					gn2 = addGraphNode(f, lambdaNode);
					if (lambdaNode.getRedexes().isEmpty())
					{
						gn2.setAccept(true);
					}
					if (createdNew)
					{
						gn2.setLocation(gn1.getX(), gn1.getY());
						gn2.setDestination(gn1.getX(), gn1.getY());
					}
					queue.add(lambdaNode);
				}
				else
				{
					InfinityNode infNode = InfinityNode.getInstance();
					gn2 = addGraphNode(f, infNode);
					gn2.setInfinity(true);
				}
				f.addEdges(gn1, gn2);
			}
			try
			{
				Thread.sleep(50);
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
			f.addEdges(gns.get(start), ns);
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
