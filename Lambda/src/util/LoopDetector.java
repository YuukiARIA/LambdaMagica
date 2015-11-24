package util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoopDetector<T>
{
	public interface IGraph<T>
	{
		public Set<T> getNodes();
		public Set<T> getNextNodes(T node);
	}

	protected static class Graph<T> implements IGraph<T>
	{
		private Map<T, Set<T>> edges;

		public Graph()
		{
			this.edges = new HashMap<T, Set<T>>();
		}

		public Graph(Map<T, Set<T>> edges)
		{
			this.edges = edges;
		}

		public Set<T> getNodes()
		{
			return edges.keySet();
		}

		public Set<T> getNextNodes(T node)
		{
			Set<T> nodes = edges.get(node);
			if (nodes == null)
			{
				nodes = Collections.emptySet();
			}
			return nodes;
		}
	}

	private IGraph<T> graph;

	private LoopDetector(Map<T, Set<T>> edges)
	{
		this.graph = new Graph<T>(edges);
	}

	public static <T> LoopDetector<T> create(Map<T, Set<T>> edges)
	{
		return new LoopDetector<T>(edges);
	}

	public Set<T> detectCyclicLoop()
	{
		Set<T> visited = new HashSet<T>();
		for (T node : graph.getNodes())
		{
			visited.clear();
			boolean loop = dfs(graph, node, visited);
			if (loop)
			{
				return visited;
			}
		}
		return Collections.emptySet();
	}

	private static <T> boolean dfs(IGraph<T> g, T node, Set<T> visited)
	{
		if (visited.contains(node))
		{
			return true;
		}
		for (T nextNode : g.getNextNodes(node))
		{
			visited.add(node);
			if (dfs(g, nextNode, visited))
			{
				return true;
			}
			visited.remove(node);
		}
		return false;
	}
}
