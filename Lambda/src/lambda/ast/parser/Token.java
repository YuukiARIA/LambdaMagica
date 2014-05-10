package lambda.ast.parser;

final class Token
{
	public final int column;
	public final String text;
	public final TokenType type;

	public Token(String text, TokenType type, int column)
	{
		this.text = text;
		this.type = type;
		this.column = column;
	}

	public boolean equals(Object o)
	{
		if (o == this) return true;
		if (o instanceof Token)
		{
			Token t = (Token)o;
			return column == t.column && text.equals(t.text) && type == t.type;
		}
		return false;
	}

	public int hashCode()
	{
		return 17 * (column ^ 31 * text.hashCode()) ^ type.hashCode();
	}

	public String toString()
	{
		return "'" + text + "':" + type;
	}
}
