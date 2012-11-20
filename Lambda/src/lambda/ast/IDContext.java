package lambda.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class IDContext
{
	protected Set<String> boundedNames = new HashSet<String>();
	protected Map<String, Integer> ids = new HashMap<String, Integer>();

	public Set<String> getBoundedNames()
	{
		return Collections.unmodifiableSet(boundedNames);
	}

	public void addBoundedName(String name)
	{
		boundedNames.add(name);
	}

	public boolean isBounded(String name)
	{
		return boundedNames.contains(name);
	}

	public int define(String name)
	{
		int id = generateFreshId();
		ids.put(name, id);
		return id;
	}

	public abstract int find(String paramString);

	protected abstract int generateFreshId();

	public static IDContext createContext()
	{
		return new RootContext();
	}

	public static IDContext deriveContext(IDContext superContext)
	{
		return new SubContext(superContext);
	}

	private static class RootContext extends IDContext
	{
		private int freshId;

		public int find(String name)
		{
			Integer id = ids.get(name);
			if (id == null)
			{
				id = define(name);
			}
			return id;
		}

		protected int generateFreshId()
		{
			return freshId++;
		}
	}

	private static class SubContext extends IDContext
	{
		private final IDContext superContext;

		public SubContext(IDContext superContext)
		{
			this.superContext = superContext;
			boundedNames.addAll(superContext.boundedNames);
		}

		public int find(String name)
		{
			Integer id = ids.get(name);
			if (id == null)
			{
				id = superContext.find(name);
			}
			return id;
		}

		protected int generateFreshId()
		{
			return superContext.generateFreshId();
		}
	}
}
