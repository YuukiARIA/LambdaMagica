package lambda.stategraph.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;

public class GraphNode
{
	private static final int R = 8;

	private String label;
	private int depth = Short.MAX_VALUE;
	private Point2D.Double location = new Point2D.Double();
	private Point2D.Double destination = new Point2D.Double();
	private boolean accept;

	public GraphNode()
	{
	}

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
	}

	public void setDestination(int x, int y)
	{
		destination.x = x;
		destination.y = y;
	}

	public void setX(int x)
	{
		destination.x = x;
	}

	public void setY(int y)
	{
		destination.y = y;
	}

	public int getX()
	{
		return (int)location.x;
	}

	public int getY()
	{
		return (int)location.y;
	}

	public void update()
	{
		location.x += 0.1 * (destination.x - location.x);
		location.y += 0.1 * (destination.y - location.y);
	}

	public void draw(Graphics g, boolean initial, boolean hover)
	{
		if (label != null && hover)
		{
			//g.drawLine(x, y, x + 20, y - 8);
			//g.setColor(new Color(255, 255, 255, 220));
			//FontMetrics fm = g.getFontMetrics();
			//int w = fm.stringWidth(label);
			//int h = fm.getHeight();
			//g.fillRect(x + 20, y - 8 - h + fm.getDescent(), w, h);
			g.setColor(Color.BLACK);
			g.drawString(label, getX() + 20, getY() - 8);
		}

		if (isAccept())
		{
			g.setColor(new Color(255, 200, 200));
		}
		else if (initial)
		{
			g.setColor(new Color(200, 200, 255));
		}
		else
		{
			g.setColor(Color.WHITE);
		}
		g.fillOval(getX() - R, getY() - R, 2 * R, 2 * R);

		g.setColor(Color.BLACK);
		g.drawOval(getX() - R, getY() - R, 2 * R, 2 * R);

		if (isAccept())
		{
			int r = Math.max(R - 2, 0);
			g.drawOval(getX() - r, getY() - r, 2 * r, 2 * r);
		}
	}
}
