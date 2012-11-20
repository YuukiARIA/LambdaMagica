package lambda.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import lambda.Environment;
import lambda.LambdaInterpreter;
import lambda.ast.ASTAbstract;
import lambda.ast.IRedex;
import lambda.ast.Lambda;
import lambda.ast.MacroExpander;
import lambda.ast.RedexFinder;
import lambda.ast.parser.Lexer;
import lambda.ast.parser.Parser;
import lambda.ast.parser.ParserException;
import lambda.gui.lambdalabel.LambdaLabel;
import lambda.gui.lambdalabel.LambdaLabelBuilder;
import lambda.gui.macroview.MacroDefinitionView;
import lambda.gui.util.GUIUtils;
import lambda.system.CommandDelegate;
import lambda.system.CommandProcessor;

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	private JTextField inputField;
	private JButton buttonStep;
	private JButton buttonClear;

	private JCheckBox checkAuto;
	private JCheckBox checkTraceInAuto;
	private JButton buttonStop;

	private RedexView redexView;
	private JTextArea output;
	private MacroDefinitionView macroView;

	private Environment env = Environment.load("properties.txt");
	private final CommandProcessor commands = new CommandProcessor();
	private LambdaInterpreter interpreter;

	private boolean autoRunning;
	private Thread thread;

	public MainFrame()
	{
		setTitle("Lambda Magica");

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputField = new JTextField();
		inputField.setFont(new Font("Consolas", Font.PLAIN, 12));
		inputField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String s = inputField.getText().trim();
				if (!s.isEmpty())
				{
					output.setText("");
					inputField.setText("");
					start(s);
				}
			}
		});
		inputPanel.add(inputField, BorderLayout.CENTER);

		buttonStep = new JButton("step");
		buttonStep.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				step();
			}
		});
		buttonStep.setEnabled(false);
		inputPanel.add(buttonStep, BorderLayout.EAST);

		leftPanel.add(inputPanel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();

		output = new JTextArea();
		output.setEditable(false);
		output.setFont(new Font("Consolas", Font.PLAIN, 12));
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

		JButton buttonClearMacros = new JButton("clear macros");
		buttonClearMacros.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				clearMacros();
			}
		});
		buttonPanel.add(buttonClearMacros);

		checkAuto = new JCheckBox("auto reduction");
		buttonPanel.add(checkAuto);

		checkTraceInAuto = new JCheckBox("show trace in auto mode");
		buttonPanel.add(checkTraceInAuto);

		buttonStop = new JButton("stop");
		buttonStop.setEnabled(false);
		buttonStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (thread != null)
				{
					autoRunning = false;
					try
					{
						thread.join();
					}
					catch (InterruptedException ex)
					{
						ex.printStackTrace();
					}
					println("- STOPPED.");
					buttonStop.setEnabled(false);
					thread = null;
				}
			}
		});
		buttonPanel.add(buttonStop);

		GroupLayout gl = new GroupLayout(buttonPanel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup()
			.addComponent(checkAuto)
			.addComponent(checkTraceInAuto)
			.addComponent(buttonStop)
			.addComponent(buttonClear)
			.addComponent(buttonClearMacros)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addComponent(checkAuto)
			.addComponent(checkTraceInAuto)
			.addComponent(buttonStop)
			.addComponent(buttonClear)
			.addComponent(buttonClearMacros)
		);
		gl.linkSize(SwingConstants.HORIZONTAL, checkAuto, checkTraceInAuto, buttonStop, buttonClear, buttonClearMacros);
		buttonPanel.setLayout(gl);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General", buttonPanel);

		redexView = new RedexView();
		redexView.setBackground(Color.WHITE);
		redexView.setFont(new Font(Font.DIALOG_INPUT, Font.PLAIN, 12));
		redexView.setMargin(5, 5, 5, 5);
		tabbedPane.add("Redex", new JScrollPane(redexView));

		macroView = new MacroDefinitionView();
		tabbedPane.addTab("Macros", macroView);

		final JSplitPane sp = new JSplitPane();
		sp.setContinuousLayout(true);
		sp.setLeftComponent(leftPanel);
		sp.setRightComponent(tabbedPane);
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				sp.setDividerLocation(0.7);
				sp.setResizeWeight(0.5);
			}
		});
		add(sp);
		setSize(600, 500);

		initializeCommands();
	}

	private void start(String text)
	{
		if (autoRunning) return;

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

	private boolean stepReduction()
	{
		interpreter.step(env);
		return interpreter.isNormal() || interpreter.isCyclic();
	}

	private void step()
	{
		if (interpreter == null) return;

		if (!interpreter.isNormal())
		{
			boolean terminated = stepReduction();
			Lambda lambda = interpreter.getLambda();
			StringBuilder sb = new StringBuilder();
			if (!terminated)
			{
				String s = lambda.toString();
				if (env.getBoolean(Environment.KEY_SHORT) && s.length() > 75)
				{
					sb.append(s.substring(0, 35));
					sb.append(" ... ");
					sb.append(s.substring(s.length() - 35, s.length()));
				}
				else
				{
					sb.append(s);
				}
			}
			else
			{
				MacroExpander expander = new MacroExpander(env);
				lambda = expander.expand(lambda);
				sb.append(lambda.toString());
				if (interpreter.isNormal())
				{
					sb.append("    (normal form)");
				}
				else if (interpreter.isCyclic())
				{
					sb.append("    (cyclic reduction)");
				}
				buttonStep.setEnabled(false);
				buttonStop.setEnabled(false);
			}
			println("--> " + sb.toString());

			updateRedexView();
		}
	}

	private void updateRedexView()
	{
		if (interpreter == null) return;

		Lambda lambda = interpreter.getLambda();
		List<IRedex> redexes = RedexFinder.getRedexList(lambda);
		LambdaLabelBuilder builder = new LambdaLabelBuilder();
		redexView.clearLabels();
		for (IRedex redex : redexes)
		{
			final LambdaLabel label = builder.createLambdaLabel(lambda, redex);
			redexView.addLabel(label);
		}
		redexView.revalidate();
		redexView.repaint();
	}

	private void defineMacro(String name, String expr)
	{
		try
		{
			Lambda lambda = parseExpression(expr);
			env.defineMacro(name, lambda);
			macroView.addMacro(name, lambda);
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
		if (line.indexOf('=') != -1)
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
		if (line.indexOf('=') != -1)
		{
			readMacro(line);
		}
		else
		{
			Lambda lambda = parseExpression(line);

			println(lambda.toString());
			ASTAbstract.varid = 0;

			interpreter = new LambdaInterpreter(lambda);

			if (!checkAuto.isSelected())
			{
				updateRedexView();
				buttonStep.setEnabled(true);
				buttonStep.requestFocus();
			}
			else
			{
				startAuto();
			}
		}
	}

	private void startAuto()
	{
		buttonStop.setEnabled(true);
		thread = new Thread()
		{
			public void run()
			{
				try
				{
					while (autoRunning)
					{
						boolean terminated = stepReduction();
						Lambda lambda = interpreter.getLambda();
						if (!terminated && checkTraceInAuto.isSelected())
						{
							StringBuilder sb = new StringBuilder("--> ");
							String s = lambda.toString();
							if (env.getBoolean(Environment.KEY_SHORT) && s.length() > 75)
							{
								sb.append(s.substring(0, 35));
								sb.append(" ... ");
								sb.append(s.substring(s.length() - 35, s.length()));
							}
							else
							{
								sb.append(s);
							}
							println(sb.toString());
						}
						else if (terminated)
						{
							MacroExpander expander = new MacroExpander(env);
							lambda = expander.expand(lambda);
	
							StringBuilder sb = new StringBuilder("--> ");
							sb.append(lambda.toString());
							if (interpreter.isNormal())
							{
								sb.append("    (normal form)");
							}
							else if (interpreter.isCyclic())
							{
								sb.append("    (cyclic reduction)");
							}
							println(sb.toString());
							autoRunning = false;
						}
					}
				}
				finally
				{
					buttonStep.setEnabled(false);
					buttonStop.setEnabled(false);
					autoRunning = false;
					thread = null;
				}
			}
		};
		thread.setName("AutoRunningThread");
		thread.setDaemon(true);
		autoRunning = true;
		thread.start();
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
				readMacro(line);
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

	private void clearMacros()
	{
		env.clearMacros();
		macroView.clearList();
		println("- macros were cleared.");
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
					try
					{
						Lambda lambda = parseExpression(expr);
						MacroExpander expander = new MacroExpander(env);
						println(expander.expand(lambda).toString());
					}
					catch (ParserException e)
					{
						println(e.getMessage());
					}
				}
			}
		});
		commands.add(":c", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				clearMacros();
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
				println("- :c         - clear all macros.");
				println("- :pwd       - print working directory.");
				println("- :q         - quit interpreter.");
			}
		});
	}

	private synchronized void println(String line)
	{
		output.append(line);
		output.append(System.getProperty("line.separator"));
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				output.setCaretPosition(output.getText().length());
			}
		});
	}

	private static Lambda parseExpression(String s) throws ParserException
	{
		Parser parser = new Parser(new Lexer(s));
		return parser.parse();
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
