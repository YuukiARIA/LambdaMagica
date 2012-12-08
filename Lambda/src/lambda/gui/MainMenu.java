package lambda.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import lambda.Environment;
import lambda.gui.fontdialog.FontDialog;
import lambda.gui.fontdialog.event.FontUpdateListener;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar
{
	private MainFrame owner;

	public MainMenu(MainFrame ownerFrame)
	{
		owner = ownerFrame;
		setBorder(null);

		final int MOD_CTRL = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

		JMenu menuFile = new JMenu("File");
		add(menuFile);

		JMenuItem itemLoad = new JMenuItem("Load Macros");
		itemLoad.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, MOD_CTRL));
		itemLoad.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				owner.loadMacroFile();
			}
		});
		menuFile.add(itemLoad);

		menuFile.addSeparator();

		JMenuItem itemExit = new JMenuItem("Exit");
		itemExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				owner.dispose();
			}
		});
		menuFile.add(itemExit);

		JMenu menuView = new JMenu("View");
		add(menuView);
		JMenuItem itemFont = new JMenuItem("Font");
		itemFont.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				final Environment env = Environment.getEnvironment();
				String defaultFamily = env.get(Environment.KEY_GUI_FONT_FAMILY, "");
				int defaultSize = env.getInt(Environment.KEY_GUI_FONT_SIZE, 12);
				int defaultAddition = env.getInt(Environment.KEY_GUI_FONT_ADDITION, 0);
				FontDialog dialog = new FontDialog();
				dialog.setLocationRelativeTo(owner);
				dialog.setSelectedFamilyName(defaultFamily);
				dialog.setSelectedFontSize(defaultSize);
				dialog.setFontAddition(defaultAddition);
				dialog.addFontApplyListener(new FontUpdateListener()
				{
					public void fontFamilyChanged(String fontFamily)
					{
						env.set(Environment.KEY_GUI_FONT_FAMILY, fontFamily);
						Main.reload(env.getGUIFont());
					}

					public void fontSizeChanged(int size)
					{
						env.set(Environment.KEY_GUI_FONT_SIZE, size);
						Main.reload(env.getGUIFont());
					}

					public void uiFontAdditionChanged(int addition)
					{
						env.set(Environment.KEY_GUI_FONT_ADDITION, addition);
						Main.updateUIFontAddition();
					}
				});
				if (!dialog.showModal())
				{
					env.set(Environment.KEY_GUI_FONT_FAMILY, defaultFamily);
					env.set(Environment.KEY_GUI_FONT_SIZE, defaultSize);
					env.set(Environment.KEY_GUI_FONT_ADDITION, defaultAddition);
					Main.reload(env.getGUIFont());
					Main.updateUIFontAddition();
				}
			}
		});
		menuView.add(itemFont);

		JMenu menuAbout = new JMenu("About");
		add(menuAbout);
		JMenuItem itemVersion = new JMenuItem("Version");
		itemVersion.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				showVersion();
			}
		});
		menuAbout.add(itemVersion);
	}

	public void updateUI()
	{
		super.updateUI();
		setBorder(null);
	}

	private void showVersion()
	{
		String s = "";
		s += "Lambda * Magica version " + Environment.APPLICATION_VERSION + "\n";
		s += "Release Date: " + Environment.RELEASE_DATE + "\n";
		s += "Copyright (C) 2011-2012 Yuuki.S All Rights Reserved.";
		JOptionPane.showMessageDialog(owner, s, "Version Information", JOptionPane.INFORMATION_MESSAGE);
	}
}
