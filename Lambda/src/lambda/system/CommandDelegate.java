package lambda.system;

public abstract interface CommandDelegate
{
	public abstract void commandInvoked(String[] args);
}
