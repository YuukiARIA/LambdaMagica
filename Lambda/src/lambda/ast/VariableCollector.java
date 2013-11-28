package lambda.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VariableCollector
{
	private Collector collector;

	public VariableCollector(Lambda lambda)
	{
		collector = new Collector();
		lambda.accept(collector);
	}

	public Set<String> getFreeVariables()
	{
		return Collections.unmodifiableSet(collector.fv);
	}

	public Set<String> getBoundedVariables()
	{
		return Collections.unmodifiableSet(collector.bv);
	}

	private static class Collector implements Lambda.Visitor
	{
		private Set<String> fv = new HashSet<String>();
		private Set<String> bv = new HashSet<String>();

		public void visit(ASTAbstract abs)
		{
			bv.add(abs.name);
			abs.e.accept(this);
			fv.remove(abs.name);
		}

		public void visit(ASTApply app)
		{
			app.lexpr.accept(this);
			app.rexpr.accept(this);
		}

		public void visit(ASTLiteral literal)
		{
			fv.add(literal.name);
		}

		public void visit(ASTMacro macro)
		{
		}
	}
}
