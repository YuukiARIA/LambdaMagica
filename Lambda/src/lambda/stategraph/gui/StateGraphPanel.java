package lambda.stategraph.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class StateGraphPanel extends JPanel
{
	private static class Edge
	{
		public final GraphNode p;
		public final GraphNode q;

		public Edge(GraphNode p, GraphNode q)
		{
			this.p = p;
			this.q = q;
		}
	}

	private static final Object LOCK_EDGES = new Object();

	private static final Color TEXT_BACK_COLOR = new Color(255, 255, 255, 220);
	private static final Stroke LINE_STROKE = new BasicStroke(0.5F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	private static final Stroke EM_LINE_STROKE = new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

	private GraphNode initialNode;
	private GraphNode hoverNode;
	private Set<GraphNode> nodes = new HashSet<GraphNode>();
	private Map<Integer, Set<GraphNode>> depthSlicedNodes = new HashMap<Integer, Set<GraphNode>>();
	private int maxDepth;
	private Map<GraphNode, Set<GraphNode>> edges = new HashMap<GraphNode, Set<GraphNode>>();
	private List<Edge> edgeLines = new ArrayList<Edge>();
	private List<Edge> inEdgeLines = new ArrayList<Edge>();
	private List<Edge> outEdgeLines = new ArrayList<Edge>();
	private boolean structureChanged;
	private boolean hoverNodeChanged;
	private Point translation = new Point();
	private boolean antialias;
	private boolean drawCurve;
	private boolean animationSuspended;

	public StateGraphPanel()
	{
		MouseAdapter m = new MouseAdapter()
		{
			private Point mp;

			public void mousePressed(MouseEvent e)
			{
				mp = e.getPoint();
			}

			public void mouseMoved(MouseEvent e)
			{
				updateHoverNode(e.getX() - translation.x, e.getY() - translation.y);
			}

			public void mouseDragged(MouseEvent e)
			{
				int dx = e.getX() - mp.x, dy = e.getY() - mp.y;
				mp = e.getPoint();
				translation.x += dx;
				translation.y += dy;
				resumeAnimation();
			}
		};
		addMouseListener(m);
		addMouseMotionListener(m);
		addComponentListener(new ComponentAdapter()
		{
			public void componentResized(ComponentEvent e)
			{
				resumeAnimation();
			}
		});

		setIgnoreRepaint(true);
		final Thread thread = new Thread()
		{
			public void run()
			{
				try
				{
					while (true)
					{
						if (animationSuspended)
						{
							suspendAnimation();
						}
						updateFrame();
						repaint();
						Thread.sleep(20);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		};
		thread.setName("AnimationThread");
		thread.setDaemon(true);
		thread.start();

		setAntialias(true);
		setDrawCurve(true);
	}

	public void setAntialias(boolean b)
	{
		antialias = b;
		resumeAnimation();
	}

	public void setDrawCurve(boolean b)
	{
		drawCurve = b;
		resumeAnimation();
	}

	private void suspendAnimation()
	{
		try
		{
			synchronized (this)
			{
				while (animationSuspended)
				{
					notifyAll();
					wait();
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private void resumeAnimation()
	{
		synchronized (this)
		{
			animationSuspended = false;
			notifyAll();
		}
	}

	public void magnifyNodeSize()
	{
		GraphNode.R = GraphNode.R + 1;
		resumeAnimation();
	}

	public void minifyNodeSize()
	{
		GraphNode.R = Math.max(GraphNode.R - 1, 0);
		resumeAnimation();
	}

	public void resetAll()
	{
		GraphNode.R = 8;
		translation.setLocation(0, 0);
		resumeAnimation();
	}

	public void addNode(GraphNode node)
	{
		synchronized (nodes)
		{
			nodes.add(node);
			structureChanged = true;
		}
		resumeAnimation();
	}

	public void setInitialNode(GraphNode node)
	{
		initialNode = node;
		resumeAnimation();
	}

	public void addEdge(GraphNode source, GraphNode sink)
	{
		synchronized (LOCK_EDGES)
		{
			Set<GraphNode> sinks = edges.get(source);
			if (sinks == null)
			{
				sinks = new HashSet<GraphNode>();
				edges.put(source, sinks);
			}
			sinks.add(sink);
			structureChanged = true;
			resumeAnimation();
		}
	}

	public void addEdges(GraphNode source, GraphNode ... sinks)
	{
		for (GraphNode sink : sinks)
		{
			addEdge(source, sink);
		}
	}

	private void updateHoverNode(int x, int y)
	{
		GraphNode h = null;
		synchronized (nodes)
		{
			for (GraphNode n : nodes)
			{
				int nx = n.getX(), ny = n.getY();
				if ((nx - x) * (nx - x) + (ny - y) * (ny - y) <= GraphNode.R * GraphNode.R)
				{
					h = n;
					break;
				}
			}
		}
		setHoverNode(h);
	}

	private synchronized void setHoverNode(GraphNode node)
	{
		if (hoverNode != node)
		{
			hoverNode = node;
			hoverNodeChanged = true;
			resumeAnimation();
		}
	}

	private synchronized GraphNode getHoverNode()
	{
		return hoverNode;
	}

	private void layoutNodes()
	{
		if (initialNode == null) return;

		depthSlicedNodes.clear();
		Set<GraphNode> visited = new HashSet<GraphNode>();
		Queue<GraphNode> queue = new LinkedList<GraphNode>();
		initialNode.setDepth(0);
		queue.add(initialNode);
		maxDepth = 0;
		while (!queue.isEmpty())
		{
			GraphNode n1 = queue.poll();
			if (visited.contains(n1)) continue;
			visited.add(n1);

			maxDepth = Math.max(n1.getDepth(), maxDepth);
			Set<GraphNode> set = depthSlicedNodes.get(n1.getDepth());
			if (set == null)
			{
				set = new HashSet<GraphNode>();
				depthSlicedNodes.put(n1.getDepth(), set);
			}
			set.add(n1);

			Set<GraphNode> nextNodes;
			synchronized (LOCK_EDGES)
			{
				nextNodes = edges.get(n1);
			}
			if (nextNodes != null)
			{
				for (GraphNode n2 : nextNodes)
				{
					if (!visited.contains(n2))
					{
						n2.setDepth(Math.min(n2.getDepth(), n1.getDepth() + 1));
						queue.add(n2);
					}
				}
			}
		}
	}

	private void updateNodeLocation()
	{
		for (Map.Entry<Integer, Set<GraphNode>> e : depthSlicedNodes.entrySet())
		{
			Set<GraphNode> set = e.getValue();
			int n = set.size(), i = 0;
			for (GraphNode node : set)
			{
				node.setX(getWidth() * ++i / (n + 1));
				node.setY(getHeight() * (node.getDepth() + 1) / (maxDepth + 2));
			}
		}
	}

	private void updateEdgeLines()
	{
		GraphNode hn = getHoverNode();
		synchronized (LOCK_EDGES)
		{
			edgeLines.clear();
			outEdgeLines.clear();
			inEdgeLines.clear();
			for (Map.Entry<GraphNode, Set<GraphNode>> e : edges.entrySet())
			{
				GraphNode src = e.getKey();
				for (GraphNode sink : e.getValue())
				{
					Edge edgeLine = new Edge(src, sink);
					if (src == hn)
					{
						outEdgeLines.add(edgeLine);
					}
					else if (sink == hn)
					{
						inEdgeLines.add(edgeLine);
					}
					else
					{
						edgeLines.add(edgeLine);
					}
				}
			}
		}
	}

	private void updateFrame()
	{
		boolean animating = false;
		synchronized (nodes)
		{
			for (GraphNode n : nodes)
			{
				animating = n.update() || animating;
			}
		}
		if (structureChanged)
		{
			layoutNodes();
		}
		updateNodeLocation();
		if (hoverNodeChanged || structureChanged)
		{
			updateEdgeLines();
		}
		if (!animating && !structureChanged && !hoverNodeChanged)
		{
			animationSuspended = true;
		}
		structureChanged = false;
		hoverNodeChanged = false;
	}

	private void render(Graphics2D g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		int trX = translation.x;
		int trY = translation.y;
		g.translate(trX, trY);

		if (antialias)
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}

		g.setStroke(LINE_STROKE);

		synchronized (LOCK_EDGES)
		{
			g.setColor(Color.LIGHT_GRAY);
			drawEdges(g, edgeLines);
		}

		synchronized (nodes)
		{
			for (GraphNode n : nodes)
			{
				n.draw(g, n == initialNode);
			}
		}

		g.setStroke(EM_LINE_STROKE);

		synchronized (LOCK_EDGES)
		{
			g.setColor(Color.BLUE);
			drawEdges(g, inEdgeLines);
			g.setColor(Color.RED);
			drawEdges(g, outEdgeLines);
		}

		GraphNode hn = getHoverNode();
		if (hn != null)
		{
			g.setColor(TEXT_BACK_COLOR);
			FontMetrics fm = g.getFontMetrics();
			int w = fm.stringWidth(hn.getLabel());
			int h = fm.getHeight();
			g.fillRect(hn.getX() + 20, hn.getY() - 8 - h + fm.getDescent(), w, h);
			g.setColor(Color.BLACK);
			g.drawString(hn.getLabel(), hn.getX() + 20, hn.getY() - 8);
		}

		if (antialias)
		{
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		}

		g.translate(-trX, -trY);
	}

	protected void paintComponent(Graphics g)
	{
		render((Graphics2D)g);
	}

	private void drawEdges(Graphics2D g, List<Edge> edges)
	{
		for (Edge edge : edges)
		{
			GraphNode p = edge.p, q = edge.q;
			if (p == q)
			{
				drawSelfCyclicEdge(g, p.getX(), p.getY());
			}
			else
			{
				if (drawCurve)
				{
					drawCurveEdge(g, p.getX(), p.getY(), q.getX(), q.getY());
				}
				else
				{
					drawStraightEdge(g, p.getX(), p.getY(), q.getX(), q.getY());
				}
			}
		}
	}

	private static void drawCurveEdge(Graphics2D g, int x0, int y0, int x1, int y1)
	{
		double a = Math.atan2(y1 - y0, x1 - x0) - Math.PI / 2;
		int l = 30;

		double cx = (x0 + x1) / 2.0 + l * Math.cos(a);
		double cy = (y0 + y1) / 2.0 + l * Math.sin(a);
		double as = Math.atan2(cy - y0, cx - x0);
		double at = Math.atan2(y1 - cy, x1 - cx);
		int r = GraphNode.R + 1;
		double rcosAs = r * Math.cos(as);
		double rsinAs = r * Math.sin(as);
		double rcosAt = r * Math.cos(at);
		double rsinAt = r * Math.sin(at);
		double sX = x0 + rcosAs;
		double sY = y0 + rsinAs;
		double tX = x1 - rcosAt;
		double tY = y1 - rsinAt;
		QuadCurve2D curve = new QuadCurve2D.Double(sX, sY, cx, cy, tX, tY);
		g.draw(curve);
		drawTriangle(g, tX, tY, at);
	}

	private static void drawStraightEdge(Graphics2D g, int x0, int y0, int x1, int y1)
	{
		double a = Math.atan2(y1 - y0, x1 - x0);
		double cosA = Math.cos(a), sinA = Math.sin(a);

		double r = 10;
		double sX = x0 + r * cosA;
		double sY = y0 + r * sinA;
		double tX = x1 - r * cosA;
		double tY = y1 - r * sinA;
		g.drawLine((int)sX, (int)sY, (int)tX, (int)tY);
		drawTriangle(g, tX, tY, a);
	}

	private static void drawSelfCyclicEdge(Graphics2D g, int x, int y)
	{
		int r = GraphNode.R;
		double x1 = x;
		double y1 = y - r;
		double cx1 = x + 4 * r;
		double cy1 = y - 8 * r;
		double cx2 = x + 4 * r;
		double cy2 = y + 8 * r;
		double x2 = x;
		double y2 = y + r;
		CubicCurve2D curve = new CubicCurve2D.Double(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
		g.draw(curve);
		double a = Math.atan2(y2 - cy2, x2 - cx2);
		drawTriangle(g, x, y + r, a);
	}

	private static void drawTriangle(Graphics g, double x, double y, double angle)
	{
		int size = 6;
		Polygon p = new Polygon();
		p.addPoint((int)x, (int)y);
		p.addPoint((int)(x - size * Math.cos(angle + Math.PI / 6)), (int)(y - size * Math.sin(angle + Math.PI / 6)));
		p.addPoint((int)(x - size * Math.cos(angle - Math.PI / 6)), (int)(y - size * Math.sin(angle - Math.PI / 6)));
		g.fillPolygon(p);
	}
}
