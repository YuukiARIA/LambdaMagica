package lambda.ast;

import java.util.HashSet;
import java.util.Set;

public class MacroNameCollector
{
	private MacroNameCollector()
	{
	}

	public static Set<String> collectMacroName(Lambda lambda)
	{
		final Set<String> macroNames = new HashSet<String>();
		lambda.accept(new Lambda.Traverser()
		{
			public void onMacro(ASTMacro m)
			{
				macroNames.add(m.name);
			}
		});
		return macroNames;
	}
}
