package lambda.macro;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lambda.ast.Lambda;
import lambda.ast.MacroNameCollector;
import util.LoopDetector;

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

	public Lambda getDefinition(String name)
	{
		return macros.get(name);
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

	public Map<String, Set<String>> getMacroDependencies()
	{
		Map<String, Set<String>> dependencies = new HashMap<String, Set<String>>();
		for (Map.Entry<String, Lambda> entry : macros.entrySet())
		{
			Set<String> usedNames = MacroNameCollector.collectMacroName(entry.getValue());
			dependencies.put(entry.getKey(), usedNames);
		}
		return dependencies;
	}

	public Set<String> detectRecursiveDefinitions()
	{
		LoopDetector<String> detector = LoopDetector.create(getMacroDependencies());
		return detector.detectCyclicLoop();
	}
}
