package lambda.ast.parser;

@SuppressWarnings("serial")
public class LexerException extends Exception
{
	public final int column;

	public LexerException(String message, int column)
	{
		super(message);
		this.column = column;
	}
}
