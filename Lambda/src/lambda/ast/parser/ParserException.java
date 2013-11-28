package lambda.ast.parser;

@SuppressWarnings("serial")
public class ParserException extends Exception
{
	public final int column;

	public ParserException(String message, Token token)
	{
		super(message + "(near the token " + token + " at column " + token.column + ")");
		this.column = token.column;
	}

	public ParserException(String message, int column)
	{
		super(message + "(at column " + column + ")");
		this.column = column;
	}
}
