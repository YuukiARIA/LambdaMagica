package lambda.ast;

import java.util.HashSet;
import java.util.Set;
import util.Unit;

public class NameGenerator
{
	private static Collector collector;

	public static Set<String> getNames(Lambda lambda)
	{
		if (collector == null)
		{
			collector = new Collector();
		}
		Set<String> names = new HashSet<String>();
		lambda.accept(collector, names);
		return names;
	}

	private static class Collector implements Lambda.Visitor<Unit, Set<String>>
	{
		public Unit visitAbstract(ASTAbstract abs, Set<String> param)
		{
			abs.e.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visitApply(ASTApply app, Set<String> param)
		{
			app.lexpr.accept(this, param);
			app.rexpr.accept(this, param);
			return Unit.VALUE;
		}

		public Unit visitLiteral(ASTLiteral literal, Set<String> param)
		{
			param.add(literal.name);
			return Unit.VALUE;
		}

		public Unit visitMacro(ASTMacro macro, Set<String> param)
		{
			return Unit.VALUE;
		}
	}
}
