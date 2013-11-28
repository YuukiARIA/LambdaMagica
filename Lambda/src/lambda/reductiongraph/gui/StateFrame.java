package lambda.reductiongraph.gui;

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

@SuppressWarnings("serial")
public class StateFrame extends JFrame
{
	private DirectedGraphPanel gp;

	public StateFrame()
	{
		setSize(600, 600);
		gp = new DirectedGraphPanel();
		add(gp);


		int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		InputMap im = gp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mod), "min");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, mod), "min");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, mod), "mag");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, mod), "mag");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, mod), "mag");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, mod), "reset");
		gp.getActionMap().put("min", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				gp.minifyNodeSize();
			}
		});
		gp.getActionMap().put("mag", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				gp.magnifyNodeSize();
			}
		});
		gp.getActionMap().put("reset", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				gp.resetAll();
			}
		});

		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				//gp.startDrawing();
			}
		});
	}

	public void addNode(GraphNode node)
	{
		gp.addNode(node);
	}

	public void setInitialNode(GraphNode node)
	{
		gp.setInitialNode(node);
	}

	public void addEdges(GraphNode source, GraphNode ... sinks)
	{
		gp.addEdges(source, sinks);
	}

	public void paint(Graphics g)
	{
	}
}
