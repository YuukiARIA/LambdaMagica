package lambda.ast.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TokenTest
{
	@Test
	public void equality()
	{
		assertEquals(new Token("aaa", TokenType.ID, 12), new Token("aaa", TokenType.ID, 12));
		assertEquals(new Token("m", TokenType.MACRONAME, 0), new Token("m", TokenType.MACRONAME, 0));
		assertEquals(new Token("\\", TokenType.LAMBDA, 999), new Token("\\", TokenType.LAMBDA, 999));
	}

	@Test
	public void hashCodeEquality()
	{
		assertEquals(new Token("aaa", TokenType.ID, 12).hashCode(), new Token("aaa", TokenType.ID, 12).hashCode());
		assertEquals(new Token("m", TokenType.MACRONAME, 0).hashCode(), new Token("m", TokenType.MACRONAME, 0).hashCode());
		assertEquals(new Token("\\", TokenType.LAMBDA, 999).hashCode(), new Token("\\", TokenType.LAMBDA, 999).hashCode());
	}
}
