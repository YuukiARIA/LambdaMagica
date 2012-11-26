package lambda.gui.util;

import java.awt.GraphicsEnvironment;

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

	public static void preloadFontNames()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.getAvailableFontFamilyNames();
			}
		};
		thread.setName("PreloadThread");
		thread.setDaemon(true);
		thread.start();
	}
}
