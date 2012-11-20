package lambda.gui.util;

import javax.swing.UIManager;

public final class GUIUtils
{
	public static void setLookAndFeelToSystem()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception localException)
		{
		}
	}
}
