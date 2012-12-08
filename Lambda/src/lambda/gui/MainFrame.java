package lambda.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lambda.Environment;
import lambda.LaTeXStringBuilder;
import lambda.LambdaInterpreter;
import lambda.LambdaInterpreter.State;
import lambda.ast.IRedexNode;
import lambda.ast.Lambda;
import lambda.ast.MacroExpander;
import lambda.ast.VariableCollector;
import lambda.ast.parser.ParserException;
import lambda.conversion.Converter;
import lambda.gui.macroview.MacroDefinitionView;
import lambda.gui.util.GUIUtils;
import lambda.gui.util.LMFileFilter;
import lambda.macro.MacroDefinition;
import lambda.reduction.RedexFinder;
import lambda.reduction.Reducer.Result;
import lambda.reduction.ReductionRule;
import lambda.reductiongraph.gui.ReductionGraphView;
import lambda.system.CommandDelegate;
import lambda.system.CommandProcessor;
import util.nullable.NullableBool;
import util.nullable.NullableInt;
import extgui.LineEditor;
import extgui.flatsplitpane.FlatSplitPane;

@SuppressWarnings("serial")
public class MainFrame extends JFrame
{
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("[HH:mm:ss]");

	private JTabbedPane tabbedPane;

	private LineEditor inputField;
	private JButton buttonStep;

	private JCheckBox checkPrintStep;
	private JCheckBox checkShort;
	private JCheckBox checkEtaEnabled;
	private JCheckBox checkDataConv;
	private JCheckBox checkAuto;
	private JCheckBox checkTraceInAuto;
	private JCheckBox checkPrintBetaEta;

	private JSpinner spinnerMaxSteps;
	private JButton buttonResume;

	private JButton buttonLaTeX;
	private JButton buttonStop;
	private JButton buttonClear;

	private RedexView redexView;
	private JTextArea output;
	private JTextArea systemOutput;
	private MacroDefinitionView macroView;

	private JFileChooser fileChooser;

	private Environment env = Environment.getEnvironment();
	private MacroDefinition macros = new MacroDefinition();
	private final CommandProcessor commands = new CommandProcessor();
	private final LambdaInterpreter interpreter = new LambdaInterpreter();

	private boolean autoRunning;
	private AutoRunningThread thread;

	public MainFrame()
	{
		setTitle("Lambda * Magica " + Environment.APPLICATION_VERSION);
		setJMenuBar(new MainMenu(this));

		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel inputPanel = new JPanel(new BorderLayout());
		inputField = new LineEditor();
		inputField.setFont(env.getGUIFont());
		inputField.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				processInput();
			}
		});
		inputPanel.add(inputField, BorderLayout.CENTER);

		buttonStep = new JButton("step");
		buttonStep.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				processInput();
			}
		});
		inputPanel.add(buttonStep, BorderLayout.EAST);

		leftPanel.add(inputPanel, BorderLayout.NORTH);

		JPanel buttonPanel = new JPanel();

		output = new JTextArea();
		output.setEditable(false);
		output.setFont(env.getGUIFont());

		systemOutput = new JTextArea();
		systemOutput.setEditable(false);
		systemOutput.setFont(env.getGUIFont());

		final JSplitPane split = new FlatSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		split.setTopComponent(new JScrollPane(output));
		split.setBottomComponent(new JScrollPane(systemOutput));
		leftPanel.add(split, BorderLayout.CENTER);

		JPanel pAuto = createAutoModeConfigurationPanel();
		buttonPanel.add(pAuto);

		JPanel pReduction = new JPanel();
		pReduction.setLayout(new BoxLayout(pReduction, BoxLayout.Y_AXIS));
		pReduction.setBorder(BorderFactory.createTitledBorder("Reduction"));
		checkEtaEnabled = createOptionCheckBox(Environment.KEY_ETA_REDUCTION, "enable eta-reduction");
		pReduction.add(checkEtaEnabled);
		buttonPanel.add(pReduction);

		JPanel pPrinting = new JPanel();
		pPrinting.setLayout(new BoxLayout(pPrinting, BoxLayout.Y_AXIS));
		pPrinting.setBorder(BorderFactory.createTitledBorder("Printing"));
		checkPrintStep = createOptionCheckBox(Environment.KEY_PRINT_STEP, "print step");
		checkPrintBetaEta = createOptionCheckBox(Environment.KEY_PRINT_BETA_ETA, "print beta/eta");
		checkShort = createOptionCheckBox(Environment.KEY_SHORT, "short printing");
		checkDataConv = createOptionCheckBox(Environment.KEY_DATA_CONV, "show nat/bool data");
		pPrinting.add(checkPrintStep);
		pPrinting.add(checkPrintBetaEta);
		pPrinting.add(checkShort);
		pPrinting.add(checkDataConv);
		buttonPanel.add(pPrinting);

		buttonLaTeX = new JButton("generate LaTeX source");
		buttonLaTeX.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				generateLaTeX();
			}
		});
		buttonPanel.add(buttonLaTeX);

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

		GUIUtils.setVerticalLayout(buttonPanel);

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("General", buttonPanel);

		redexView = new RedexView();
		redexView.setBackground(Color.WHITE);
		redexView.setFont(env.getGUIFont());
		redexView.setMargin(5, 5, 5, 5);
		redexView.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stepNormal();
			}
		});
		tabbedPane.add("Redex", new JScrollPane(redexView));

		macroView = new MacroDefinitionView();
		macroView.setFont(env.getGUIFont());
		tabbedPane.addTab("Macros", macroView);

		final ReductionGraphView sgView = new ReductionGraphView();
		sgView.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String s = inputField.getText().trim();
				if (!s.isEmpty())
				{
					output.setText("");
					inputField.saveHistory();
					inputField.setText("");
					try
					{
						Lambda lambda = Lambda.parse(s);
						MacroExpander expander = new MacroExpander(macros);
						lambda = expander.expand(lambda, true);
						sgView.startSearch(lambda);
					}
					catch (ParserException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});
		tabbedPane.addTab("ReductionGraph", sgView);

		final JSplitPane sp = new FlatSplitPane();
		sp.setContinuousLayout(true);
		sp.setLeftComponent(leftPanel);
		sp.setRightComponent(tabbedPane);
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e)
			{
				sp.setDividerLocation(0.7);
				sp.setResizeWeight(0.5);
				split.setDividerLocation(0.8);
				split.setResizeWeight(0.9);
			}
		});
		sp.setDividerLocation(Short.MAX_VALUE);
		add(sp);
		setSize(700, 600);

		setupAcceleration();

		initializeCommands();
	}

	public void loadMacroFile()
	{
		if (fileChooser == null)
		{
			File cd = new File(env.get(Environment.KEY_LAST_DIRECTORY, "."));
			fileChooser = new JFileChooser(cd);
			fileChooser.setDialogTitle("Load Macro File");
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setAcceptAllFileFilterUsed(true);
			fileChooser.setFileFilter(LMFileFilter.getInstance());
		}
		int ret = fileChooser.showOpenDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			loadFile(file);
		}
		env.set(Environment.KEY_LAST_DIRECTORY, fileChooser.getCurrentDirectory().getPath());
	}

	private JPanel createAutoModeConfigurationPanel()
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Auto Mode"));
		checkAuto = createOptionCheckBox(Environment.KEY_AUTO, "auto reduction");
		checkAuto.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				updateAutoModePanel();
			}
		});
		checkTraceInAuto = createOptionCheckBox(Environment.KEY_TRACE, "show trace in auto mode");

		JPanel stepPanel = new JPanel();
		spinnerMaxSteps = new JSpinner(new SpinnerNumberModel(env.getInt(Environment.KEY_CONTINUE_STEPS, 100), 0, 1000, 1));
		spinnerMaxSteps.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				env.set(Environment.KEY_CONTINUE_STEPS, spinnerMaxSteps.getValue());
			}
		});
		stepPanel.add(new JLabel("step limit:"));
		stepPanel.add(spinnerMaxSteps);
		GUIUtils.setHorizontalLayout(stepPanel, true, true);

		buttonResume = new JButton("continue");
		buttonResume.setEnabled(false);
		buttonResume.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (autoRunning && thread != null)
				{
					thread.resumeAuto();
				}
			}
		});
		buttonStop = new JButton("stop");
		buttonStop.setEnabled(false);
		buttonStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (autoRunning && thread != null)
				{
					stopAuto();
				}
			}
		});
		p.add(checkAuto);
		p.add(checkTraceInAuto);
		p.add(stepPanel);
		p.add(buttonResume);
		p.add(buttonStop);
		GUIUtils.setVerticalLayout(p);
		updateAutoModePanel();
		return p;
	}

	private void updateAutoModePanel()
	{
		boolean b = checkAuto.isSelected();
		checkTraceInAuto.setEnabled(b);
		spinnerMaxSteps.setEnabled(b);
	}

	private void setupAcceleration()
	{
		int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		String keyInc = "font.size.increase";
		String keyDec = "font.size.decrease";
		String keyDef = "font.size.default";
		String keyRev = "revert";

		InputMap imap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, mod), keyInc);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, mod), keyInc);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, mod), keyInc);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mod), keyDec);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, mod), keyDec);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, mod), keyDef);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), keyRev);

		ActionMap amap = getRootPane().getActionMap();
		amap.put(keyInc, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				Environment env = Environment.getEnvironment();
				int size = env.getInt(Environment.KEY_GUI_FONT_SIZE, 12);
				if (size < 120)
				{
					env.set(Environment.KEY_GUI_FONT_SIZE, size + 1);
					setCodeFont(env.getGUIFont());
				}
			}
		});
		amap.put(keyDec, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				Environment env = Environment.getEnvironment();
				int size = env.getInt(Environment.KEY_GUI_FONT_SIZE, 12);
				if (5 < size)
				{
					env.set(Environment.KEY_GUI_FONT_SIZE, size - 1);
					setCodeFont(env.getGUIFont());
				}
			}
		});
		amap.put(keyDef, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				Environment env = Environment.getEnvironment();
				env.set(Environment.KEY_GUI_FONT_SIZE, 12);
				setCodeFont(env.getGUIFont());
			}
		});
		amap.put(keyRev, new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!autoRunning)
				{
					stepBackward();
				}
			}
		});
	}

	private JCheckBox createOptionCheckBox(final String key, String text)
	{
		final JCheckBox checkBox = new JCheckBox(text);
		checkBox.setSelected(env.getBoolean(key));
		checkBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				env.set(key, checkBox.isSelected());
			}
		});
		return checkBox;
	}

	public void setCodeFont(Font font)
	{
		inputField.setFont(font);
		output.setFont(font);
		systemOutput.setFont(font);
		redexView.setFont(font);
		macroView.setFont(font);
	}

	private void processInput()
	{
		String s = inputField.getText().trim();
		if (!s.isEmpty())
		{
			output.setText("");
			inputField.saveHistory();
			inputField.setText("");
			start(s);
		}
		else
		{
			stepNormal();
		}
	}

	private void start(String text)
	{
		if (autoRunning) return;

		text = text.trim();
		if (text.startsWith(":"))
		{
			String[] a = text.split("\\s+");
			String cmd = a[0];
			String[] params = Arrays.copyOfRange(a, 1, a.length);
			if (!commands.invokeCommand(cmd, params))
			{
				println("- unknown command " + cmd);
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

	private IRedexNode getDefaultRedex()
	{
		boolean eta = env.getBoolean(Environment.KEY_ETA_REDUCTION);
		return RedexFinder.getLeftMostOuterMostRedex(interpreter.getLambda(), eta);
	}

	private IRedexNode getSelectedRedex()
	{
		IRedexNode redex = redexView.getSelectedRedex();
		return redex != null ? redex : getDefaultRedex();
	}

	private boolean stepReduction(boolean auto)
	{
		if (interpreter.isTerminated())
		{
			return false;
		}

		IRedexNode redex = auto ? getDefaultRedex() : getSelectedRedex();
		if (redex == null)
		{
			return false;
		}

		boolean succeeded = true;
		Result result = interpreter.step(macros, redex);
		if (result.reduced)
		{
			if (!auto || env.getBoolean(Environment.KEY_TRACE))
			{
				StringBuilder sb = new StringBuilder();

				if (env.getBoolean(Environment.KEY_PRINT_STEP))
				{
					if (result.appliedRule == ReductionRule.MACRO_EXPANSION)
					{
						sb.append("  -: ");
					}
					else
					{
						sb.append(String.format("%3d: ", interpreter.getReductionStepCount()));
					}
				}

				if (env.getBoolean(Environment.KEY_PRINT_BETA_ETA))
				{
					switch (result.appliedRule)
					{
					case BETA_REDUCTION:
						sb.append("β");
						break;
					case ETA_REDUCTION:
						sb.append("η");
						break;
					case MACRO_EXPANSION:
						sb.append(" ");
						break;
					default:
						break;
					}
				}
				switch (result.appliedRule)
				{
				case BETA_REDUCTION:
				case ETA_REDUCTION:
					sb.append(" --> ");
					break;
				case MACRO_EXPANSION:
					sb.append("   = ");
					break;
				default:
					break;
				}

				String s = interpreter.getLambda().toString();
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
			if (checkIfCycled() || checkIfNormalForm())
			{
				reductionTerminated();
				succeeded = false;
			}
		}
		else
		{
			reductionTerminated();
			succeeded = false;
		}
		return succeeded;
	}

	private boolean stepNormal()
	{
		boolean ret = stepReduction(false);
		updateRedexView(interpreter.getLambda());
		return ret;
	}

	private synchronized boolean stepAuto()
	{
		return stepReduction(true);
	}

	private void stepBackward()
	{
		if (!autoRunning && !interpreter.isTerminated() && interpreter.isRevertable())
		{
			interpreter.revert();
			deleteLine();
			updateRedexView(interpreter.getLambda());
		}
	}

	private void showConvertedData(Lambda lambda)
	{
		NullableInt natValue = Converter.toNat(lambda);
		if (natValue.hasValue())
		{
			println("  = " + natValue + " (as nat)");
		}
		NullableBool boolValue = Converter.toBool(lambda);
		if (boolValue.hasValue())
		{
			println("  = " + boolValue + " (as bool)");
		}
	}

	private boolean checkIfNormalForm()
	{
		Lambda lambda = interpreter.getLambda();
		boolean etaEnabled = env.getBoolean(Environment.KEY_ETA_REDUCTION);
		if (RedexFinder.isNormalForm(lambda, etaEnabled))
		{
			printEndState(lambda, "(normal form)");
			return true;
		}
		return false;
	}

	private boolean checkIfCycled()
	{
		if (interpreter.isCyclic())
		{
			printEndState(interpreter.getLambda(), "(cyclic reduction)");
			return true;
		}
		return false;
	}

	private void printEndState(Lambda lambda, String label)
	{
		MacroExpander expander = new MacroExpander(macros);
		lambda = expander.expand(lambda, true);
		println("==> " + lambda + "    " + label);
		if (env.getBoolean(Environment.KEY_DATA_CONV))
		{
			showConvertedData(lambda);
		}
	}

	private void reductionTerminated()
	{
		buttonStop.setEnabled(false);
	}

	private void updateRedexView(Lambda lambda)
	{
		redexView.setRedexes(lambda);
		redexView.revalidate();
		redexView.repaint();
	}

	private void defineMacro(String name, String expr)
	{
		try
		{
			Lambda lambda = Lambda.parse(expr);
			macros.defineMacro(name, lambda);
			macroView.addMacro(name, lambda);
			println(String.format("- <%s> is defined as %s", name, lambda));

			VariableCollector vc = new VariableCollector(lambda);
			Set<String> fv = vc.getFreeVariables();
			if (!fv.isEmpty())
			{
				println(String.format("- Warning: <%s> contains free variables %s", name, fv));
			}
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
			Lambda lambda = Lambda.parse(line);

			println(lambda.toString());

			interpreter.startInterpretation(lambda);

			if (checkIfNormalForm())
			{
				reductionTerminated();
			}
			else
			{
				if (!env.getBoolean(Environment.KEY_AUTO))
				{
					tabbedPane.setSelectedIndex(1);
					buttonStep.requestFocus();
					updateRedexView(lambda);
				}
				else
				{
					startAuto();
				}
			}
		}
	}

	private void startAuto()
	{
		autoRunning = true;
		buttonStop.setEnabled(true);
		buttonStep.setEnabled(false);
		thread = new AutoRunningThread();
		thread.start();
	}

	private void stopAuto()
	{
		if (thread != null)
		{
			interpreter.terminate();
			autoRunning = false;
			thread.resumeAuto();
			try
			{
				thread.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			printSystemMessage("Stopped.");
		}
	}

	private void loadFile(File file)
	{
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)));

			println("- load '" + file.getName() + "'");
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
			println("- cannot open file \"" + file.getPath() + "\"");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadFile(String path)
	{
		File file = new File(path);
		if (!file.exists() && !path.endsWith(".lm.txt"))
		{
			file = new File(path + ".lm.txt");
		}
		loadFile(file);
	}

	private void clearMacros()
	{
		macros.clearMacros();
		macroView.clearList();
		println("- macros were cleared.");
	}

	private void initializeCommands()
	{
		commands.add(":q", new CommandDelegate()
		{
			public void commandInvoked(String[] params)
			{
				dispose();
			}
		});
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
						expr += s + " ";
					}
					try
					{
						Lambda lambda = Lambda.parse(expr);
						MacroExpander expander = new MacroExpander(macros);
						println(expander.expand(lambda, true).toString());
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
				println("- :f <expr>  - expand all macros and show expression.");
				println("- :l <path>  - load lines from a text file.");
				println("- :c         - clear all macros.");
				println("- :pwd       - print working directory.");
				println("- :q         - quit interpreter.");
			}
		});
	}

	private void generateLaTeX()
	{
		StringBuilder buf = new StringBuilder();
		LaTeXStringBuilder builder = new LaTeXStringBuilder();

		buf.append("\\begin{eqnarray*}\n");
		for (State s : interpreter.getStates())
		{
			switch (s.appliedRule)
			{
			case NONE:
				buf.append("&& ");
				break;
			case BETA_REDUCTION:
				buf.append("&\\longrightarrow_\\beta& ");
				break;
			case ETA_REDUCTION:
				buf.append("&\\longrightarrow_\\eta& ");
				break;
			case MACRO_EXPANSION:
				buf.append("&=& ");
				break;
			}
			buf.append(builder.build(s.lambda, s.getReducedRedex()));
			buf.append(" \\\\\n");
		}
		buf.append("\\end{eqnarray*}\n");

		SimpleTextDialog dialog = new SimpleTextDialog();
		dialog.setTextAreaFont(output.getFont());
		dialog.setText(buf.toString());
		dialog.setVisible(true);
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

	private synchronized void printSystemMessage(String text)
	{
		String s = DATE_FORMAT.format(new Date()) + " " + text;
		systemOutput.append(s);
		systemOutput.append("\n");
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				systemOutput.setCaretPosition(systemOutput.getText().length());
			}
		});
	}

	private synchronized void deleteLine()
	{
		String text = output.getText().trim();
		int i = text.lastIndexOf('\n');
		if (i != -1)
		{
			text = text.substring(0, i + 1);
			output.setText(text);
		}
	}

	private class AutoRunningThread extends Thread
	{
		private int nextLimit;
		private boolean noLimit;
		private boolean suspended;

		public AutoRunningThread()
		{
			setName("AutoRunningThread");
			setDaemon(true);
			updateLimit();
		}

		public synchronized void resumeAuto()
		{
			printSystemMessage("Resumed.");
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					buttonResume.setEnabled(false);
				}
			});
			updateLimit();
			suspended = false;
			notifyAll();
		}

		public void run()
		{
			try
			{
				while (autoRunning)
				{
					if (!stepAuto())
					{
						break;
					}
					if (isLimited())
					{
						printSystemMessage(interpreter.getReductionStepCount() + " steps done. Continue?");
						suspendAuto();
					}
				}
			}
			catch (StackOverflowError e)
			{
				println("Fatal Error: generated too large structure");
			}
			finally
			{
				finalizeAuto();
			}
		}

		private void updateLimit()
		{
			int limit = env.getInt(Environment.KEY_CONTINUE_STEPS, 100);
			if (noLimit)
			{
				if (limit >= 0)
				{
					nextLimit = interpreter.getReductionStepCount() + limit;
					noLimit = false;
				}
			}
			else
			{
				if (limit == 0)
				{
					noLimit = true;
				}
				else
				{
					nextLimit += limit;
				}
			}
		}

		private boolean isLimited()
		{
			return !noLimit && interpreter.getReductionStepCount() >= nextLimit;
		}

		private synchronized void suspendAuto()
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					buttonResume.setEnabled(true);
				}
			});
			suspended = true;
			try
			{
				while (suspended)
				{
					notifyAll();
					wait();
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		private void finalizeAuto()
		{
			autoRunning = false;
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					buttonResume.setEnabled(false);
					buttonStop.setEnabled(false);
					buttonStep.setEnabled(true);
				}
			});
			thread = null;
		}
	}
}
