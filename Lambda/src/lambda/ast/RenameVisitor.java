package lambda.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lambda.ast.Lambda.VisitorRP;

public class RenameVisitor implements VisitorRP<Lambda, Map<String, String>>
{
	private Lambda lambda;
	private Set<String> bounded = new HashSet<String>();

	public RenameVisitor(Lambda lambda)
	{
		this.lambda = lambda;
	}

	public Lambda rename()
	{
		return lambda.accept(this, new HashMap<String, String>());
	}

	public Lambda visit(ASTAbstract abs, Map<String, String> renameMap)
	{
		Map<String, String> map = new HashMap<String, String>(renameMap);
		ASTAbstract abs2;
		if (bounded.contains(abs.originalName))
		{
			abs2 = abs;
		}
		else
		{
			bounded.add(abs.originalName);
			map.put(abs.name, abs.originalName);
			abs2 = new ASTAbstract(abs.originalName, abs.originalName, abs.e);
		}
		Lambda e = abs2.e.accept(this, map);
		bounded.remove(abs.originalName);
		return e == abs2.e ? abs2 : new ASTAbstract(abs2.originalName, abs2.name, e);
	}

	public Lambda visit(ASTApply app, Map<String, String> renameMap)
	{
		Lambda l = app.lexpr.accept(this, renameMap);
		Lambda r = app.rexpr.accept(this, renameMap);
		return l == app.lexpr && r == app.rexpr ? app : new ASTApply(l, r);
	}

	public Lambda visit(ASTLiteral literal, Map<String, String> renameMap)
	{
		if (renameMap.containsKey(literal.name))
		{
			return new ASTLiteral(literal.originalName, renameMap.get(literal.name));
		}
		return literal;
	}

	public Lambda visit(ASTMacro macro, Map<String, String> renameMap)
	{
		return macro;
	}
}
