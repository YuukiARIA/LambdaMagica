package lambda.ast;

import lambda.ast.parser.ParserException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LambdaTest
{
	@Test
	public void testEqualsAndHashCode()
	{
		Lambda lambda1 = Lambda.literal("x").app(Lambda.literal("y").app(Lambda.macro("m"))).abs("x").abs("y");
		Lambda lambda2 = Lambda.literal("x").app(Lambda.literal("y").app(Lambda.macro("m"))).abs("x").abs("y");
		assertTrue(lambda1.equals(lambda2));
		assertTrue(lambda2.equals(lambda1));
		assertTrue(lambda1.hashCode() == lambda2.hashCode());
	}

	@Test(expected=ParserException.class)
	public void parseEmpty() throws ParserException
	{
		Lambda.parse("");
	}

	@Test
	public void parseTest() throws ParserException
	{
		Lambda lambda = Lambda.literal("x").app(Lambda.literal("y").app(Lambda.macro("m"))).abs("x").abs("y");
		assertEquals(lambda, Lambda.parse("\\y.\\x.x(y<m>)"));
		assertEquals(lambda, Lambda.parse("\\yx.x(y<m>)"));
		assertEquals(lambda, Lambda.parse("  \\ y x . x ( y <m> )   "));
	}
}
