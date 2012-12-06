package lambda.reductiongraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.event.EventListenerList;

import lambda.ast.IRedexNode;
import lambda.ast.Lambda;
import lambda.macro.MacroDefinition;
import lambda.reduction.Reducer;
import lambda.reduction.Reducer.Result;
import lambda.reductiongraph.event.SearchEndListener;
import lambda.reductiongraph.gui.GraphNode;
import lambda.reductiongraph.gui.DirectedGraphPanel;

public class StateSearcher
{
	private static final MacroDefinition EMPTY_MACRO_DEF = new MacroDefinition();

	private DirectedGraphPanel graphPanel;
	private Lambda lambda;
	private int maxDepth;
	private Map<IStateNode, GraphNode> nodeMapping = new HashMap<IStateNode, GraphNode>();
	private boolean createdNew;
	private int states;
	private EventListenerList listeners = new EventListenerList();
	private Thread thread;

	public StateSearcher(DirectedGraphPanel graphPanel, Lambda lambda, int maxDepth)
	{
		this.graphPanel = graphPanel;
		this.lambda = lambda;
		this.maxDepth = maxDepth;
	}

	public int getStateCount()
	{
		return states;
	}

	public void startSearch()
	{
		thread = new SearchThread();
		thread.setName("SearchThread");
		thread.setDaemon(true);
		thread.start();
	}

	public void abort()
	{
		if (thread != null)
		{
			thread.interrupt();
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			thread = null;
		}
	}

	public void addSearchEndListener(SearchEndListener l)
	{
		listeners.add(SearchEndListener.class, l);
	}

	private void dispatchSearchEndEvent()
	{
		for (SearchEndListener l : listeners.getListeners(SearchEndListener.class))
		{
			l.searchEnded();
		}
	}

	private void searchOnLine() throws InterruptedException
	{
		nodeMapping.clear();

		Set<LambdaNode> visited = new HashSet<LambdaNode>();
		Queue<LambdaNode> queue = new LinkedList<LambdaNode>();
		LambdaNode initial = new LambdaNode(0, lambda);
		GraphNode gnInitial = addGraphNode(initial);
		graphPanel.setInitialNode(gnInitial);
		queue.add(initial);
		while (!queue.isEmpty())
		{
			LambdaNode p1 = queue.poll();

			if (visited.contains(p1))
			{
				continue;
			}
			visited.add(p1);

			GraphNode gn1 = addGraphNode(p1);

			List<IRedexNode> redexes = p1.getRedexes();
			if (redexes.isEmpty())
			{
				gn1.setAccept(true);
			}

			for (IRedexNode redex : redexes)
			{
				Result ret = Reducer.reduce(p1.lambda, EMPTY_MACRO_DEF, redex);

				LambdaNode lambdaNode = new LambdaNode(p1.depth + 1, ret.lambda);
				GraphNode gn2;
				if (lambdaNode.depth <= maxDepth || visited.contains(lambdaNode))
				{
					gn2 = addGraphNode(lambdaNode);
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
					gn2 = addGraphNode(infNode);
					gn2.setInfinity(true);
				}
				graphPanel.addEdge(gn1, gn2);
			}
			Thread.sleep(50);
		}
		states = visited.size();
	}

	private GraphNode addGraphNode(IStateNode node)
	{
		GraphNode gn = nodeMapping.get(node);
		if (gn == null)
		{
			gn = new GraphNode(node.getText());
			nodeMapping.put(node, gn);
			graphPanel.addNode(gn);
			createdNew = true;
		}
		else
		{
			createdNew = false;
		}
		return gn;
	}

	private class SearchThread extends Thread
	{
		public void run()
		{
			try
			{
				searchOnLine();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			finally
			{
				dispatchSearchEndEvent();
			}
		}
	}
}
