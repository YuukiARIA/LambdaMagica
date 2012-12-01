package lambda.stategraph.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.QuadCurve2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
class GraphPanel extends JPanel
{
	private GraphNode initialNode;
	private GraphNode hoverNode;
	private Set<GraphNode> nodes = new HashSet<GraphNode>();
	private Map<GraphNode, Set<GraphNode>> edges = new HashMap<GraphNode, Set<GraphNode>>();
	
	public GraphPanel()
	{
		addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseMoved(MouseEvent e)
			{
				int px = e.getX(), py = e.getY();
				GraphNode h = null;
				for (GraphNode n : nodes)
				{
					int x = n.getX(), y = n.getY();
					if ((x - px) * (x - px) + (y - py) * (y - py) <= 10 * 10)
					{
						h = n;
						break;
					}
				}
				if (hoverNode != h)
				{
					hoverNode = h;
					repaint();
				}
			}
			
			public void mouseDragged(MouseEvent e)
			{
			}
		});

		Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						updateFrame();
						repaint();
						Thread.sleep(30);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public synchronized void addNode(GraphNode node)
	{
		nodes.add(node);
		node.setLocation(getWidth() / 2, getHeight() / 2);
	}

	public void setInitialNode(GraphNode node)
	{
		initialNode = node;
	}

	public synchronized void addEdge(GraphNode source, GraphNode sink)
	{
		Set<GraphNode> sinks = edges.get(source);
		if (sinks == null)
		{
			sinks = new HashSet<GraphNode>();
			edges.put(source, sinks);
		}
		sinks.add(sink);
	}

	public void addEdge(GraphNode source, GraphNode ... sinks)
	{
		for (GraphNode sink : sinks)
		{
			addEdge(source, sink);
		}
	}

	public void layoutNodes()
	{
		if (initialNode == null) return;

		Map<Integer, Set<GraphNode>> slices = new HashMap<Integer, Set<GraphNode>>();
		Set<GraphNode> visited = new HashSet<GraphNode>();
		Queue<GraphNode> queue = new LinkedList<GraphNode>();
		initialNode.setDepth(0);
		queue.add(initialNode);
		int maxDepth = 0;
		while (!queue.isEmpty())
		{
			GraphNode n1 = queue.poll();
			if (visited.contains(n1)) continue;
			visited.add(n1);

			maxDepth = Math.max(n1.getDepth(), maxDepth);
			Set<GraphNode> set = slices.get(n1.getDepth());
			if (set == null)
			{
				set = new HashSet<GraphNode>();
				slices.put(n1.getDepth(), set);
			}
			set.add(n1);

			if (edges.containsKey(n1))
			{
				for (GraphNode n2 : edges.get(n1))
				{
					if (!visited.contains(n2))
					{
						n2.setDepth(Math.min(n2.getDepth(), n1.getDepth() + 1));
						queue.add(n2);
					}
				}
			}
		}

		for (Map.Entry<Integer, Set<GraphNode>> e : slices.entrySet())
		{
			Set<GraphNode> set = e.getValue();
			int n = set.size(), i = 0;
			for (GraphNode node : set)
			{
				//node.setX(getWidth() * (node.getDepth() + 1) / (maxDepth + 2));
				//node.setY(getHeight() * ++i / (n + 1));
				node.setX(getWidth() * ++i / (n + 1));
				node.setY(getHeight() * (node.getDepth() + 1) / (maxDepth + 2));
			}
		}
	}

	private void updateFrame()
	{
		for (GraphNode n : nodes)
		{
			n.update();
		}
	}

	protected synchronized void paintComponent(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		layoutNodes();
		for (Map.Entry<GraphNode, Set<GraphNode>> e : edges.entrySet())
		{
			GraphNode src = e.getKey();
			for (GraphNode sink : e.getValue())
			{
				if (src == hoverNode || sink == hoverNode)
				{
					continue;
				}
				g.setColor(Color.LIGHT_GRAY);
				drawCurveEdge((Graphics2D)g, src.getX(), src.getY(), sink.getX(), sink.getY());
			}
		}
		for (Map.Entry<GraphNode, Set<GraphNode>> e : edges.entrySet())
		{
			GraphNode src = e.getKey();
			for (GraphNode sink : e.getValue())
			{
				if (src == hoverNode)
				{
					g.setColor(Color.BLUE);
				}
				else if (sink == hoverNode)
				{
					g.setColor(Color.RED);
				}
				else
				{
					continue;
				}
				drawCurveEdge((Graphics2D)g, src.getX(), src.getY(), sink.getX(), sink.getY());
			}
		}

		for (GraphNode n : nodes)
		{
			n.draw(g, n == initialNode, n == hoverNode);
		}
	}

	private void drawCurveEdge(Graphics2D g, int x0, int y0, int x1, int y1)
	{
		//g.drawLine(x0, y0, x1, y1);

		double a = Math.atan2(y1 - y0, x1 - x0) - Math.PI / 2;
		int l = 50;
		//g.drawLine(px, py, px + (int)(l * Math.cos(a)), py + (int)(l * Math.sin(a)));

		double cx = (x0 + x1) / 2.0 + l * Math.cos(a);
		double cy = (y0 + y1) / 2.0 + l * Math.sin(a);
		double as = Math.atan2(cy - y0, cx - x0);
		double at = Math.atan2(y1 - cy, x1 - cx);
		double r = 10;
		double sX = x0 + r * Math.cos(as);
		double sY = y0 + r * Math.sin(as);
		double tX = x1 - r * Math.cos(at);
		double tY = y1 - r * Math.sin(at);
		QuadCurve2D curve = new QuadCurve2D.Double(sX, sY, cx, cy, tX, tY);
		g.draw(curve);
		drawTriangle(g, tX, tY, at);
	}
	
	private void drawStraightEdge(Graphics2D g, int x0, int y0, int x1, int y1)
	{
		double a = Math.atan2(y1 - y0, x1 - x0);

		double r = 10;
		double sX = x0 + r * Math.cos(a);
		double sY = y0 + r * Math.sin(a);
		double tX = x1 - r * Math.cos(a);
		double tY = y1 - r * Math.sin(a);
		g.drawLine((int)sX, (int)sY, (int)tX, (int)tY);
		drawTriangle(g, tX, tY, a);
	}
	
	private void drawTriangle(Graphics g, double x, double y, double angle)
	{
		int size = 6;
		Polygon p = new Polygon();
		p.addPoint((int)x, (int)y);
		p.addPoint((int)(x - size * Math.cos(angle + Math.PI / 6)), (int)(y - size * Math.sin(angle + Math.PI / 6)));
		p.addPoint((int)(x - size * Math.cos(angle - Math.PI / 6)), (int)(y - size * Math.sin(angle - Math.PI / 6)));
		g.fillPolygon(p);
	}
}

@SuppressWarnings("serial")
public class StateFrame extends JFrame
{
	private GraphPanel gp;

	public StateFrame()
	{
		setSize(600, 600);
		gp = new GraphPanel();
		add(gp);
	}

	public void addNode(GraphNode node)
	{
		gp.addNode(node);
	}

	public void setInitialNode(GraphNode node)
	{
		gp.setInitialNode(node);
	}

	public void addEdge(GraphNode source, GraphNode ... sinks)
	{
		gp.addEdge(source, sinks);
	}
}
