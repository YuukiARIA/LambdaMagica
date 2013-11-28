package lambda;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class Environment
{
	public static final String APPLICATION_VERSION = "3.82";
	public static final String RELEASE_DATE = "2013-11-26";

	public static final String PROPERTY_FILENAME = "properties.txt";

	public static final String KEY_CONTINUE_STEPS = "continue_steps";
	public static final String KEY_PRINT_STEP = "print_step";
	public static final String KEY_TRACE = "trace";
	public static final String KEY_SHORT = "short";
	public static final String KEY_DATA_CONV = "data_abstraction";
	public static final String KEY_AUTO = "auto";
	public static final String KEY_ETA_REDUCTION = "eta_reduction";
	public static final String KEY_GUI_FONT_FAMILY = "gui.fontfamily";
	public static final String KEY_GUI_FONT_SIZE = "gui.fontsize";
	public static final String KEY_GUI_FONT_ADDITION = "gui.ui.font.addition";
	public static final String KEY_PRINT_BETA_ETA = "print_beta_eta";
	public static final String KEY_LAST_DIRECTORY = "gui.load.lastdir";

	private static Environment instance;

	private Map<String, String> entries = new TreeMap<String, String>();

	private Font guiFont;

	private Environment()
	{
		set(KEY_CONTINUE_STEPS, 500);
		set(KEY_TRACE, false);
		set(KEY_SHORT, true);
		set(KEY_DATA_CONV, false);
		set(KEY_AUTO, false);
		set(KEY_ETA_REDUCTION, false);
		set(KEY_GUI_FONT_FAMILY, Font.DIALOG_INPUT);
		set(KEY_GUI_FONT_SIZE, 12);
		set(KEY_GUI_FONT_ADDITION, 0);
		set(KEY_PRINT_BETA_ETA, true);
		set(KEY_LAST_DIRECTORY, ".");
	}

	public String get(String key, String defval)
	{
		String value = entries.get(key);
		if (value == null)
		{
			value = defval;
			set(key, value);
		}
		return value;
	}

	public int getInt(String key, int defval)
	{
		int value;
		try
		{
			value = Integer.parseInt(entries.get(key));
		}
		catch (NumberFormatException e)
		{
			value = defval;
			set(key, value);
		}
		return value;
	}

	public boolean getBoolean(String key)
	{
		String value = entries.get(key);
		if (value != null)
		{
			return Boolean.parseBoolean(value);
		}
		set(key, false);
		return false;
	}

	public <T> void set(String key, T value)
	{
		entries.put(key, value.toString());
	}

	public Font getGUIFont()
	{
		String family = get(KEY_GUI_FONT_FAMILY, Font.DIALOG_INPUT);
		int size = getInt(KEY_GUI_FONT_SIZE, 12);
		if (guiFont == null || !guiFont.getFamily().equals(family) || guiFont.getSize() != size)
		{
			guiFont = new Font(family, Font.PLAIN, size);
		}
		return guiFont;
	}

	public static Environment getEnvironment()
	{
		if (instance == null)
		{
			createEnvironment();
		}
		return instance;
	}

	private static void createEnvironment()
	{
		instance = new Environment();
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(PROPERTY_FILENAME)));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] kv = line.split("\\s*=\\s*");
				if (kv.length == 2)
				{
					instance.entries.put(kv[0].trim(), kv[1].trim());
				}
			}
			reader.close();
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				instance.save();
			}
		});
	}

	private void save()
	{
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(PROPERTY_FILENAME))));
			for (Map.Entry<String, String> e : entries.entrySet())
			{
				out.println(e.getKey() + "=" + e.getValue());
			}
			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
