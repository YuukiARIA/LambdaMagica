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

	public String toString()
	{
		return "'" + text + "':" + type;
	}
}
