package lambda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import lambda.ast.Lambda;

public class Environment
{
	public static final String KEY_CONTINUE_STEPS = "continue_steps";
	public static final String KEY_TRACE = "trace";
	public static final String KEY_SHORT = "short";
	public static final String KEY_DATA_CONV = "data_abstraction";

	private String fileName;
	private Map<String, Lambda> macros = new TreeMap<String, Lambda>();
	private Map<String, String> entries = new TreeMap<String, String>();

	private Environment()
	{
		set("continue_steps", 500);
		set("trace", false);
		set("short", true);
		set("data_abstraction", false);
	}

	public void defineMacro(String name, Lambda lambda)
	{
		macros.put(name, lambda);
	}

	public Lambda expandMacro(String name)
	{
		return macros.get(name);
	}

	public void dumpMacros()
	{
		for (Map.Entry<String, Lambda> ent : macros.entrySet())
		{
			echoMacro(ent.getKey(), ent.getValue());
		}
	}

	public Map<String, Lambda> getDefinedMacros()
	{
		return Collections.unmodifiableMap(macros);
	}

	private static void echoMacro(String name, Lambda lambda)
	{
		System.out.println(getMacroString(name, "=", lambda));
	}

	private static String getMacroString(String name, String connector, Lambda lambda)
	{
		return String.format("- <%s> %s %s", name, connector, lambda);
	}

	public void clearMacros()
	{
		macros.clear();
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

	public static Environment load(String fileName)
	{
		final Environment env = new Environment();
		env.fileName = fileName;
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));
			String line;
			while ((line = reader.readLine()) != null)
			{
				String[] kv = line.split("\\s*=\\s*");
				if (kv.length == 2)
				{
					env.entries.put(kv[0].trim(), kv[1].trim());
				}
			}

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
				env.save();
			}
		});
		return env;
	}

	public void save()
	{
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(fileName))));
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
