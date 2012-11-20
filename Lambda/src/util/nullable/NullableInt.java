package util.nullable;

public abstract class NullableInt
{
	public static final NullableInt NONE = new NullableInt()
	{
		public boolean hasValue()
		{
			return false;
		}

		public int value()
		{
			throw new RuntimeException("Illegal operation");
		}

		public String toString()
		{
			return "None";
		}
	};

	public abstract boolean hasValue();

	public abstract int value();

	public static NullableInt create(final int n)
	{
		return new NullableInt()
		{
			public boolean hasValue()
			{
				return true;
			}

			public int value()
			{
				return n;
			}

			public String toString()
			{
				return String.valueOf(n);
			}
		};
	}
}
