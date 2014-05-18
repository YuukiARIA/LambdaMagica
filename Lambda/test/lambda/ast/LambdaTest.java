package lambda.ast;

import org.junit.Test;

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
}
