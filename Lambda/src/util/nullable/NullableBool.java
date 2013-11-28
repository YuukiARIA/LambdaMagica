package util.nullable;

public abstract class NullableBool
{
	public static final NullableBool NONE = new NullableBool()
	{
		public boolean hasValue()
		{
			return false;
		}

		public boolean value()
		{
			throw new RuntimeException("Illegal operation");
		}

		public String toString()
		{
			return "None";
		}
	};

	public abstract boolean hasValue();

	public abstract boolean value();

	public static NullableBool create(final boolean b)
	{
		return new NullableBool()
		{
			public boolean hasValue()
			{
				return true;
			}

			public boolean value()
			{
				return b;
			}

			public String toString()
			{
				return String.valueOf(b);
			}
		};
	}
}
