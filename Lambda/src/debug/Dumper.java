package debug;

import java.lang.reflect.Field;

public class Dumper
{
	// disallow to create instance
	private Dumper()
	{
	}

	public static <T> String toString(T obj, Class<? extends T> clazz)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(clazz.getName());
		sb.append('[');
		boolean cont = false;
		for (Field f : clazz.getFields())
		{
			if (cont)
			{
				sb.append(',');
			}
			cont = true;

			sb.append(f.getName());
			sb.append('=');
			try
			{
				sb.append(f.get(obj));
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}
		sb.append(']');
		return sb.toString();
	}
}
