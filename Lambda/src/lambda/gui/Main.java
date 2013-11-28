package lambda.gui;

import java.awt.Font;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import lambda.Environment;
import lambda.gui.util.GUIUtils;

public class Main
{
	private static MainFrame mainFrame;
	private static Map<String, Integer> defaultSizes = null;

	public static void reload(Font font)
	{
		mainFrame.setCodeFont(font);
	}

	public static void updateUIFontAddition()
	{
		if (defaultSizes == null)
		{
			defaultSizes = new HashMap<String, Integer>();
			for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet())
			{
				String key = entry.getKey().toString();
				if (key.toLowerCase().endsWith("font"))
				{
					Font font = UIManager.getFont(key);
					defaultSizes.put(key, font.getSize());
				}
			}
		}

		int a = Environment.getEnvironment().getInt(Environment.KEY_GUI_FONT_ADDITION, 0);
		for (Map.Entry<String, Integer> entry : defaultSizes.entrySet())
		{
			String key = entry.getKey();
			Font font = UIManager.getFont(key);
			float size = entry.getValue() + a;
			if (size > 0)
			{
				UIManager.put(key, new FontUIResource(font.deriveFont(size)));
			}
		}

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					for (Window window : Window.getWindows())
					{
						SwingUtilities.updateComponentTreeUI(window);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	public static void main(String[] args)
	{
		GUIUtils.preloadFontNames();
		GUIUtils.setLookAndFeelToSystem();

		updateUIFontAddition();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				mainFrame = new MainFrame();
				mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				mainFrame.setLocationRelativeTo(null);
				mainFrame.setVisible(true);
			}
		});
	}
}
