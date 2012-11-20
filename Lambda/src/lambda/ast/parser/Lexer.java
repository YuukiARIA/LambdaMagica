package lambda.ast.parser;

public class Lexer
{
	private char[] cs;
	private int column;

	public Lexer(String text)
	{
		cs = text.toCharArray();
		column = 0;
	}

	public Token nextToken() throws LexerException
	{
		skipWhitespaces();

		if (isEnd())
		{
			return new Token("$END", TokenType.END, column);
		}

		char c = peek();
		if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'))
		{
			int col = column;
			succ();
			return new Token(Character.toString(c), TokenType.ID, col);
		}
		else if (c == '<')
		{
			int col = column;
			succ();
			c = peek();
			StringBuilder buf = new StringBuilder();
			while (!isEnd() && c != '>')
			{
				buf.append(c);
				succ();
				c = peek();
			}
			if (c == '>')
			{
				succ();
				return new Token(buf.toString(), TokenType.MACRONAME, col);
			}
			throw new LexerException("Missing '>'.", column);
		}
		else
		{
			int col = column;
			succ();
			switch (c)
			{
			case '\\': return new Token("\\", TokenType.LAMBDA, col);
			case '.':  return new Token(".", TokenType.DOT, col);
			case '(':  return new Token("(", TokenType.LPAR, col);
			case ')':  return new Token(")", TokenType.RPAR, col);
			}
			throw new LexerException("Illegal character '" + c + "'.", col);
		}
	}

	public boolean isEnd()
	{
		return column >= cs.length;
	}

	private void skipWhitespaces()
	{
		while (!isEnd() && Character.isWhitespace(peek())) succ();
	}

	private char peek()
	{
		return column < cs.length ? cs[column] : '$';
	}

	private void succ()
	{
		column++;
	}
}
