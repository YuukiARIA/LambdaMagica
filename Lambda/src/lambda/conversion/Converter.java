package lambda.conversion;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.Lambda;
import util.nullable.NullableBool;
import util.nullable.NullableInt;

public final class Converter
{
	public static NullableInt toNat(Lambda lambda)
	{
		Lambda e = lambda;

		if (!e.isAbstraction())
		{
			return NullableInt.NONE;
		}

		String s = ((ASTAbstract)e).name;
		e = ((ASTAbstract)e).e;

		if (!e.isAbstraction())
		{
			return NullableInt.NONE;
		}

		String z = ((ASTAbstract)e).name;
		e = ((ASTAbstract)e).e;

		int value = 0;
		while (e.isApplication())
		{
			ASTApply app = (ASTApply)e;
			if (!app.lexpr.isLiteral()) return NullableInt.NONE;
			if (!((ASTLiteral)app.lexpr).name.equals(s)) return NullableInt.NONE;
			e = app.rexpr;
			value++;
		}

		if (!e.isLiteral())
		{
			return NullableInt.NONE;
		}
		ASTLiteral l = (ASTLiteral)e;
		return l.name.equals(z) ? NullableInt.create(value) : NullableInt.NONE;
	}

	public static NullableBool toBool(Lambda lambda)
	{
		Lambda e = lambda;

		if (!e.isAbstraction())
		{
			return NullableBool.NONE;
		}

		String t = ((ASTAbstract)e).name;
		e = ((ASTAbstract)e).e;

		if (!e.isAbstraction())
		{
			return NullableBool.NONE;
		}

		String f = ((ASTAbstract)e).name;
		e = ((ASTAbstract)e).e;

		if (!e.isLiteral())
		{
			return NullableBool.NONE;
		}

		ASTLiteral x = (ASTLiteral)e;
		if (x.name.equals(t))
		{
			return NullableBool.create(true);
		}
		if (x.name.equals(f))
		{
			return NullableBool.create(false);
		}

		return NullableBool.NONE;
	}
}
