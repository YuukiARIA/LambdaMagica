package lambda.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import lambda.Environment;
import lambda.LambdaInterpreter;
import lambda.ast.ASTAbstract;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.LambdaPrinter;
import lambda.ast.MacroExpander;
import lambda.ast.RedexFinder;
import lambda.ast.parser.Lexer;
import lambda.ast.parser.Parser;
import lambda.ast.parser.ParserException;
import lambda.gui.lambdalabel.LambdaLabel;
import lambda.gui.lambdalabel.LambdaLabelBuilder;
import lambda.gui.lambdalabel.LambdaLabelDrawer;
import lambda.gui.lambdalabel.LambdaLabelMetrics;
import lambda.gui.util.GUIUtils;
import lambda.system.CommandDelegate;
import lambda.system.CommandProcessor;

@SuppressWarnings("serial")
class RedexPanel extends JPanel
{
	private Insets margin = new Insets(0, 0, 0, 0);
	private List<LambdaLabel> labels = new ArrayList<LambdaLabel>();
	private int height = 20;
	private int maxWidth;
	private int hoverIndex = -1;
	private int selectedIndex = -1;

	public RedexPanel()
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

		g.translate(margin.left, margin.top);

		if (hoverIndex != -1)
		{
			g.setColor(new Color(200, 200, 255));
			g.fillRect(0, hoverIndex * height, getWidth(), height);
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
			g.setColor(new Color(40, 40, 255));
			g.drawRect(0, selectedIndex * height, getWidth(), height);
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

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	private JTextField input;
	private JButton buttonStep;
	private JButton buttonClear;
	private JButton buttonRedex;
	private RedexPanel redexPanel;
	private JTextArea output;
	private Environment env = Environment.load("properties.txt");
	private final CommandProcessor commands = new CommandProcessor();
	private LambdaInterpreter interpreter;

	public MainFrame()
	{
		setTitle("Lambda Magica");

		JPanel leftPanel = new JPanel(new BorderLayout());

		input = new JTextField();
		input.setFont(new Font("Consolas", Font.PLAIN, 12));
		input.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String s = input.getText().trim();
				if (!s.isEmpty())
				{
					start(s);
					input.setText("");
				}
			}
		});
		leftPanel.add(input, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 1, 2, 2));

		buttonStep = new JButton("step");
		buttonStep.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				step();
			}
		});
		buttonPanel.add(buttonStep);

		output = new JTextArea();
		output.setFont(new Font("Consolas", Font.PLAIN, 12));
		JSplitPane split = new JSplitPane();
		split.setLeftComponent(new JScrollPane());
		leftPanel.add(new JScrollPane(output), BorderLayout.CENTER);

		buttonClear = new JButton("clear output");
		buttonClear.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				output.setText("");
			}
		});
		buttonPanel.add(buttonClear);

		buttonRedex = new JButton("Redex");
		buttonRedex.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateRedexView();
			}
		});
		buttonPanel.add(buttonRedex);

		redexPanel = new RedexPanel();
		redexPanel.setBackground(Color.WHITE);
		redexPanel.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
		redexPanel.setMargin(5, 5, 5, 5);
		buttonPanel.add(new JScrollPane(redexPanel));

		final JSplitPane sp = new JSplitPane();
		sp.setLeftComponent(leftPanel);
		sp.setRightComponent(buttonPanel);
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				sp.setDividerLocation(0.5);
				sp.setResizeWeight(0.5);
			}
		});
		add(sp);
		setSize(400, 300);

		initializeCommands();
	}

	private void start(String text)
	{
		text = text.trim();
		if (text.startsWith(":"))
		{
			if (text.startsWith(":q"))
			{
				dispose();
			}
			else
			{
				commands.invokeCommand(text);
			}
		}
		else if (!text.isEmpty())
		{
			try
			{
				evalLine(text);
			}
			catch (ParserException e)
			{
				String s = "";
				for (int i = 0; i < e.column; i++)
				{
					s = s + ' ';
				}
				println(text);
				println(s + '^');
				println("- " + e.getMessage());
			}
		}
	}

	private void step()
	{
		if (interpreter != null && !interpreter.isNormal())
		{
			interpreter.step(env);
			Lambda lambda = interpreter.getLambda();
			String s;
			if (!interpreter.isNormal() && !interpreter.isCyclic())
			{
				s = lambda.toString();
				if (env.getBoolean("short") && s.length() > 75)
				{
					s = s.substring(0, 35) + " ... " + s.substring(s.length() - 35, s.length());
				}
			}
			else
			{
				MacroExpander expander = new MacroExpander(env);
				lambda = expander.expand(lambda);
				s = lambda.toString();
				if (interpreter.isNormal())
				{
					s = s + "    (normal form)";
				}
				else if (interpreter.isCyclic())
				{
					s = s + "    (cyclic reduction)";
				}
			}
			println("--> " + s);

			updateRedexView();
		}
	}

	private void showRedex()
	{
		if (interpreter == null) return;

		Lambda lambda = interpreter.getLambda();
		List<IRedex> redexes = RedexFinder.getRedexList(lambda);
		String s = "";
		for (IRedex redex : redexes)
		{
			LambdaPrinter printer = new LambdaPrinter(true, redex);
			s = s + printer.makeString(lambda) + "<br/>";
		}
		s = "<html>" + s + "</html>";
		JLabel label = new JLabel(s);
		label.setOpaque(true);
		label.setBackground(Color.WHITE);
		label.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
		JDialog dialog = new JDialog();
		dialog.add(new JScrollPane(label));
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	private void updateRedexView()
	{
		if (interpreter == null) return;

		Lambda lambda = interpreter.getLambda();
		List<IRedex> redexes = RedexFinder.getRedexList(lambda);
		LambdaLabelBuilder builder = new LambdaLabelBuilder();
		redexPanel.clearLabels();
		for (IRedex redex : redexes)
		{
			final LambdaLabel label = builder.createLambdaLabel(lambda, redex);
			redexPanel.addLabel(label);
		}
		redexPanel.revalidate();
		redexPanel.repaint();
	}

	private void defineMacro(String name, String expr)
	{
		Parser parser = new Parser(new Lexer(expr));
		try
		{
			Lambda lambda = parser.parse();
			env.defineMacro(name, lambda);
			String s = String.format("- <%s> is defined as %s", name, lambda);
			println(s);
		}
		catch (ParserException e)
		{
			println("- " + e.getMessage());
		}
	}

	private void readMacro(String line)
	{
		if (line.contains("="))
		{
			String[] v = line.split("\\s*=\\s*");
			if (v.length == 2)
			{
				defineMacro(v[0], v[1]);
			}
			else
			{
				println("- Invalid expression: " + line);
			}
		}
	}

	private void evalLine(String line) throws ParserException
	{
		if (line.contains("="))
		{
			readMacro(line);
		}
		else
		{
			Parser parser = new Parser(new Lexer(line));
			Lambda lambda = parser.parse();

			println(lambda.toString());
			ASTAbstract.varid = 0;

			interpreter = new LambdaInterpreter(lambda);

			updateRedexView();
			buttonStep.requestFocus();
		}
	}

	private void loadFile(String path)
	{
		if (!path.endsWith(".lm.txt"))
		{
			path = path + ".lm.txt";
		}

		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(path)));

			println("- load '" + path + "'");
			String line;
			while ((line = reader.readLine()) != null)
			{
				int c = line.indexOf('#');
				if (c != -1) line = line.substring(0, c);
				line = line.trim();
				if (line.isEmpty()) continue;
				try
				{
					evalLine(line);
				}
				catch (ParserException e)
				{
					println("- " + e.getMessage());
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
			println("- cannot open file \"" + path + "\"");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void initializeCommands()
	{
		commands.add(":l", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				if (params.length == 0)
				{
					File cd = new File(".");
					for (File file : cd.listFiles())
					{
						if (file.isFile() && file.getName().endsWith(".lm.txt"))
						{
							loadFile(file.getName());
						}
					}
				}
				else
				{
					for (String fileName : params)
					{
						loadFile(fileName);
					}
				}
			}
		});
		commands.add(":f", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				if (params.length >= 1)
				{
					String expr = "";
					for (String s : params)
					{
						expr = expr + s + " ";
					}
					Parser parser = new Parser(new Lexer(expr));
					MacroExpander expander = new MacroExpander(env);
					try
					{
						println(expander.expand(parser.parse()).toString());
					}
					catch (ParserException e)
					{
						println(e.getMessage());
					}
				}
			}
		});
		commands.add(":s", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				if (params.length >= 1)
				{
					try
					{
						env.set("continue_steps", Integer.parseInt(params[0]));
					}
					catch (NumberFormatException e)
					{
						println("- Illegal number format: " + params[0]);
					}
				}
			}
		});
		commands.add(":t", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				boolean b = false;
				if (params.length >= 1)
				{
					b = params[0].equals("on");
				}
				env.set("trace", b);
				println("- set trace " + (b ? "on" : "off"));
			}
		});
		commands.add(":c", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				env.clearMacros();
				println("- macros were cleared.");
			}
		});
		commands.add(":m", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				for (Map.Entry<String, Lambda> e : env.getDefinedMacros().entrySet())
				{
					String s = String.format("- <%s> = %s", e.getKey(), e.getValue());
					println(s);
				}
			}
		});
		commands.add(":pwd", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				println(new File("").getAbsolutePath());
			}
		});
		commands.add(":?", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				println("- :?         - show this help.");
				println("- :f <expr>  - expand macros and show expression.");
				println("- :l <path>  - load lines from a text file.");
				println("- :s <n>     - set the number of continuation steps.");
				println("- :t [on]    - set trace mode. ");
				println("- :m         - list defined macros.");
				println("- :c         - clear all macros.");
				println("- :pwd       - print working directory.");
				println("- :q         - quit interpreter.");
			}
		});
	}

	private void println(String line)
	{
		output.append(line);
		output.append(System.getProperty("line.separator"));
		output.setCaretPosition(output.getText().length());
	}

	public static void main(String[] args)
	{
		GUIUtils.setLookAndFeelToSystem();

		MainFrame f = new MainFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
