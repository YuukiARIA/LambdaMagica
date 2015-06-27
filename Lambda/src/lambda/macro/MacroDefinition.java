package lambda.macro;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import lambda.ast.Lambda;

public class MacroDefinition
{
	private Map<String, Lambda> macros = new TreeMap<String, Lambda>();

	public void clearMacros()
	{
		macros.clear();
	}

	public Lambda defineMacro(String name, Lambda lambda)
	{
		Lambda previous;
		if (lambda == null)
		{
			previous = macros.remove(name);
		}
		else
		{
			previous = macros.put(name, lambda);
		}
		return previous;
	}

	public Lambda expandMacro(String name)
	{
		Lambda l = macros.get(name);
		return l != null ? l.deepCopy() : null;
	}

	public Map<String, Lambda> getDefinedMacros()
	{
		return Collections.unmodifiableMap(macros);
	}
}
