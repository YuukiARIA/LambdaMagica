package util;

public final class Pair<T, U>
{
	public final T _1;
	public final U _2;

	public Pair(T fst, U snd)
	{
		_1 = fst;
		_2 = snd;
	}

	public T fst()
	{
		return this._1;
	}

	public U snd()
	{
		return this._2;
	}

	public Pair<T, U> fst(T _1)
	{
		return of(_1, _2);
	}

	public Pair<T, U> snd(U _2)
	{
		return of(_1, _2);
	}

	public int hashCode()
	{
		return _1.hashCode() ^ _2.hashCode();
	}

	public boolean equals(Pair<?, ?> p)
	{
		return _1.equals(p._1) && _2.equals(p._2);
	}

	public boolean equals(Object o)
	{
		return (o == this) || (((o instanceof Pair)) && (equals((Pair<?, ?>)o)));
	}

	public static <T, U> Pair<T, U> of(T fst, U snd)
	{
		return new Pair<T, U>(fst, snd);
	}
}
