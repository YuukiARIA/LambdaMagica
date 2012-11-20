package lambda.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lambda.gui.lambdalabel.LambdaLabel;
import lambda.gui.lambdalabel.LambdaLabelDrawer;
import lambda.gui.lambdalabel.LambdaLabelMetrics;

@SuppressWarnings("serial")
public class RedexView extends JPanel
{
	private static final Color HOVER_BACK_COLOR = new Color(220, 220, 255);
	private static final Color SELECTION_RECT_COLOR = new Color(40, 40, 255);

	private Insets margin = new Insets(0, 0, 0, 0);
	private List<LambdaLabel> labels = new ArrayList<LambdaLabel>();
	private int height = 20;
	private int maxWidth;
	private int hoverIndex = -1;
	private int selectedIndex = -1;

	public RedexView()
	{
		MouseHandler mh = new MouseHandler();
		addMouseListener(mh);
		addMouseMotionListener(mh);
	}

	public void setMargin(int top, int left, int bottom, int right)
	{
		margin.set(top, left, bottom, right);
	}

	public void clearLabels()
	{
		labels.clear();
		maxWidth = 0;
		hoverIndex = -1;
		selectedIndex = -1;
	}

	public void addLabel(LambdaLabel l)
	{
		labels.add(l);
		maxWidth = Math.max(maxWidth, LambdaLabelMetrics.getWidth(getGraphics(), l));

		int w = maxWidth + margin.left + margin.right;
		int h = height * labels.size() + margin.top + margin.bottom;
		setPreferredSize(new Dimension(w, h));
	}

	public int getSelectedIndex()
	{
		return selectedIndex;
	}

	public void setSelectedIndex(int index)
	{
		if (0 <= index && index < labels.size())
		{
			selectedIndex = index;
			repaint();
		}
	}

	protected void paintComponent(Graphics g)
	{
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());

		if (labels.isEmpty())
		{
			String s = "No Redexes";
			FontMetrics fm = g.getFontMetrics();
			int sw = fm.stringWidth(s);
			int sh = fm.getHeight();
			g.setColor(Color.LIGHT_GRAY);
			g.drawString(s, (getWidth() - sw) / 2, (getHeight() - sh) / 2 + fm.getLeading() + fm.getAscent());
			return;
		}

		g.translate(margin.left, margin.top);

		int width = getWidth() - (margin.left + margin.right);

		if (hoverIndex != -1)
		{
			g.setColor(HOVER_BACK_COLOR);
			g.fillRect(0, hoverIndex * height, width, height);
		}

		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

		LambdaLabelDrawer drawer = new LambdaLabelDrawer();
		for (int i = 0; i < labels.size(); i++)
		{
			drawer.draw(g, labels.get(i), 0, i * height, height);
		}

		if (selectedIndex != -1)
		{
			g.setColor(SELECTION_RECT_COLOR);
			g.drawRect(0, selectedIndex * height, width, height);
		}

		g.translate(-margin.left, -margin.top);
	}

	private class MouseHandler extends MouseAdapter
	{
		public void mouseMoved(MouseEvent e)
		{
			hoverIndex = (e.getY() - margin.top) / height;
			if (hoverIndex < 0 || labels.size() <= hoverIndex)
			{
				hoverIndex = -1;
			}
			repaint();
		}

		public void mouseExited(MouseEvent e)
		{
			hoverIndex = -1;
			repaint();
		}

		public void mousePressed(MouseEvent e)
		{
			selectedIndex = hoverIndex;
			repaint();
		}
	}
}
