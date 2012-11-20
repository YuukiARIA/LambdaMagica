package lambda.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lambda.ast.Lambda.SingleVisitor;
import util.Unit;

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

	private static class Collector extends SingleVisitor<Unit>
	{
		private Set<String> fv = new HashSet<String>();
		private Set<String> bv = new HashSet<String>();

		public Unit visitAbstract(ASTAbstract abs)
		{
			bv.add(abs.name);
			abs.e.accept(this);
			fv.remove(abs.name);
			return Unit.VALUE;
		}

		public Unit visitApply(ASTApply app)
		{
			app.lexpr.accept(this);
			app.rexpr.accept(this);
			return Unit.VALUE;
		}

		public Unit visitLiteral(ASTLiteral literal)
		{
			fv.add(literal.name);
			return Unit.VALUE;
		}

		public Unit visitMacro(ASTMacro macro)
		{
			return Unit.VALUE;
		}
	}
}
