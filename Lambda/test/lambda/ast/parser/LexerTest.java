package lambda.ast.parser;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LexerTest
{
	@Test
	public void lexIdent()
	{
		expectTokens("c", TokenType.ID);
		expectTokens("C", TokenType.ID);
		expectTokens("a b c A B C", TokenType.ID, TokenType.ID, TokenType.ID, TokenType.ID, TokenType.ID, TokenType.ID);
	}

	@Test
	public void lexLambda()
	{
		expectTokens("\\", TokenType.LAMBDA);
		expectTokens("\u00A5", TokenType.LAMBDA);
	}

	@Test
	public void lexSymbols()
	{
		expectTokens("().", TokenType.LPAR, TokenType.RPAR, TokenType.DOT);
	}

	@Test
	public void lexMacroName()
	{
		expectTokens("<m>", TokenType.MACRONAME);
		expectTokens("<macro>", TokenType.MACRONAME);
	}

	@Test
	public void lexEmpty()
	{
		expectTokens("");
		expectTokens("     ");
		expectTokens("\t\t\r\r  \n\n\n");
	}

	@Test
	public void macroNameText()
	{
		checkMacroNameText("m");
		checkMacroNameText("macro");
		checkMacroNameText("123");
		checkMacroNameText("aaa222");
	}

	private void checkMacroNameText(String name)
	{
		try
		{
			Token t = lexWord("<" + name + ">");
			assertEquals(name, t.text);
		}
		catch (LexerException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void lexEnds()
	{
		Lexer lexer = new Lexer("");
		assertTrue(lexer.isEnd());
		try
		{
			assertEquals(TokenType.END, lexer.nextToken().type);
			assertEquals(TokenType.END, lexer.nextToken().type);
		}
		catch (LexerException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	private static void expectTokens(String s, TokenType ... types)
	{
		Lexer lexer = new Lexer(s);
		try
		{
			Token token = null;
			int i = 0;
			while ((token = lexer.nextToken()).type != TokenType.END)
			{
				if (i < types.length)
				{
					assertEquals(types[i++], token.type);
				}
				else
				{
					fail();
				}
			}
		}
		catch (LexerException e)
		{
			e.printStackTrace();
			fail();
		}
	}

	private static Token lexWord(String s) throws LexerException
	{
		Lexer lexer = new Lexer(s);
		Token t = lexer.nextToken();
		assertTrue(lexer.isEnd());
		return t;
	}
}
