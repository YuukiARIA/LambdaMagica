package lambda.gui;

import javax.swing.WindowConstants;

import lambda.gui.util.GUIUtils;

public class Main
{
	public static void main(String[] args)
	{
		GUIUtils.setLookAndFeelToSystem();

		MainFrame f = new MainFrame();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
}
