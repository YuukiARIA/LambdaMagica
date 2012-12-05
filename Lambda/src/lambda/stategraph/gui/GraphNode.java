package lambda.stategraph.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class GraphNode
{
	private static final Color INITIAL_COLOR = new Color(100, 100, 255);
	private static final Color ACCEPT_COLOR = new Color(255, 100, 100);
	private static final Color INFINITY_COLOR = new Color(200, 200, 200);
	private static final Color NODE_COLOR = new Color(100, 255, 100);

	public static int R = 8;

	private String label;
	private int depth = Short.MAX_VALUE;
	private Point2D.Double location = new Point2D.Double();
	private Point2D.Double destination = new Point2D.Double();
	private boolean accept;
	private boolean infinity;
	private boolean animating;

	public GraphNode(String label)
	{
		this.label = label;
	}

	public int getDepth()
	{
		return depth;
	}

	public void setDepth(int d)
	{
		depth = d;
	}

	public String getLabel()
	{
		return label;
	}

	public void setInfinity(boolean b)
	{
		infinity = b;
	}

	public boolean isAccept()
	{
		return accept;
	}

	public void setAccept(boolean b)
	{
		accept = b;
	}

	public void setLocation(int x, int y)
	{
		location.x = x;
		location.y = y;
		animating = true;
	}

	public void setDestination(int x, int y)
	{
		destination.x = x;
		destination.y = y;
		animating = true;
	}

	public void setX(int x)
	{
		destination.x = x;
		animating = true;
	}

	public void setY(int y)
	{
		destination.y = y;
		animating = true;
	}

	public int getX()
	{
		return (int)location.x;
	}

	public int getY()
	{
		return (int)location.y;
	}

	public boolean update()
	{
		if (animating)
		{
			location.x += 0.2 * (destination.x - location.x);
			location.y += 0.2 * (destination.y - location.y);
			if (location.distanceSq(destination) < 0.1)
			{
				location.setLocation(destination);
				animating = false;
			}
		}
		return animating;
	}

	public void draw(Graphics g, boolean initial)
	{
		if (infinity)
		{
			g.setColor(INFINITY_COLOR);
		}
		else if (isAccept())
		{
			g.setColor(ACCEPT_COLOR);
		}
		else if (initial)
		{
			g.setColor(INITIAL_COLOR);
		}
		else
		{
			g.setColor(NODE_COLOR);
		}
		g.fillOval(getX() - R, getY() - R, 2 * R, 2 * R);

		if (R > 4)
		{
			g.setColor(Color.BLACK);
			g.drawOval(getX() - R, getY() - R, 2 * R, 2 * R);

			if (isAccept())
			{
				int r = Math.max(R - 2, 0);
				g.drawOval(getX() - r, getY() - r, 2 * r, 2 * r);
			}
		}
	}
}
