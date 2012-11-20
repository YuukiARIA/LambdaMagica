package util;

public final class Unit
{
	public static final Unit VALUE = new Unit();

	private Unit()
	{
	}

	public int hashCode()
	{
		return 0;
	}

	public boolean equals(Object o)
	{
		return o == this;
	}

	public String toString()
	{
		return "Unit";
	}
}
