package lambda.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import lambda.Environment;

@SuppressWarnings("serial")
public class MainMenu extends JMenuBar
{
	private JFrame owner;

	public MainMenu(JFrame ownerFrame)
	{
		owner = ownerFrame;

		JMenu menuFile = new JMenu("File");
		add(menuFile);
		JMenuItem itemExit = new JMenuItem("Exit");
		itemExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				owner.dispose();
			}
		});
		menuFile.add(itemExit);

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

	private void showVersion()
	{
		String s = "";
		s += "Lambda * Magica version " + Environment.APPLICATION_VERSION + "\n";
		s += "Release Date: 2012-11-21";
		JOptionPane.showMessageDialog(owner, s, "Version Information", JOptionPane.INFORMATION_MESSAGE);
	}
}
