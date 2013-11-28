package lambda.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lambda.ast.Lambda.VisitorRP;
import util.Pair;

class RenameGenerator implements VisitorRP<Lambda, Pair<Set<String>, Map<String, String>>>
{
	private Set<String> renameTarget;

	public RenameGenerator(Set<String> target)
	{
		renameTarget = target;
	}

	public Lambda rename(Set<String> boundedNames, Lambda lambda)
	{
		return lambda.accept(this, new Pair<Set<String>, Map<String, String>>(new HashSet<String>(boundedNames), new HashMap<String, String>()));
	}

	public Lambda visit(ASTAbstract abs, Pair<Set<String>, Map<String, String>> cxt)
	{
		Set<String> bv = cxt._1;
		Map<String, String> renameMap = cxt._2;
		Set<String> bv2 = new HashSet<String>(bv);
		Map<String, String> renameMap2 = new HashMap<String, String>(renameMap);
		if (renameTarget.contains(abs.name))
		{
			String newName = generateName(bv);
			renameMap2.put(abs.name, newName);
			bv2.add(newName);
			Lambda e = abs.e.accept(this, Pair.of(bv2, renameMap2));
			return new ASTAbstract(abs.originalName, newName, e);
		}
		bv2.add(abs.name);
		Lambda e = abs.e.accept(this, Pair.of(bv2, renameMap2));
		return e == abs.e ? abs : new ASTAbstract(abs.originalName, abs.name, e);
	}

	public Lambda visit(ASTApply app, Pair<Set<String>, Map<String, String>> cxt)
	{
		Lambda l = app.lexpr.accept(this, cxt);
		Lambda r = app.rexpr.accept(this, cxt);
		return l == app.lexpr && r == app.rexpr ? app : new ASTApply(l, r);
	}

	public Lambda visit(ASTLiteral literal, Pair<Set<String>, Map<String, String>> cxt)
	{
		if (cxt._2.containsKey(literal.name))
		{
			return new ASTLiteral(literal.originalName, cxt._2.get(literal.name));
		}
		return literal;
	}

	public Lambda visit(ASTMacro macro, Pair<Set<String>, Map<String, String>> cxt)
	{
		return macro;
	}

	private static String generateName(Set<String> bounded)
	{
		for (char c = 'a'; c <= 'z'; c++)
		{
			String name = Character.toString(c);
			if (!bounded.contains(name)) return name;
		}
		for (char c = 'A'; c <= 'Z'; c++)
		{
			String name = Character.toString(c);
			if (!bounded.contains(name)) return name;
		}
		for (int i = 0; i < 1000000; i++)
		{
			String name = "$" + i;
			if (!bounded.contains(name)) return name;
		}
		throw new RuntimeException("failed to generate name");
	}
}
