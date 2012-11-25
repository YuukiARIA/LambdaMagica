package lambda.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lambda.ast.Lambda.VisitorR;
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

	private static class Collector extends VisitorR<Unit>
	{
		private Set<String> fv = new HashSet<String>();
		private Set<String> bv = new HashSet<String>();

		public Unit visit(ASTAbstract abs)
		{
			bv.add(abs.name);
			abs.e.accept(this);
			fv.remove(abs.name);
			return Unit.VALUE;
		}

		public Unit visit(ASTApply app)
		{
			app.lexpr.accept(this);
			app.rexpr.accept(this);
			return Unit.VALUE;
		}

		public Unit visit(ASTLiteral literal)
		{
			fv.add(literal.name);
			return Unit.VALUE;
		}

		public Unit visit(ASTMacro macro)
		{
			return Unit.VALUE;
		}
	}
}
