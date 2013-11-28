package lambda.serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.Lambda;
import lambda.ast.parser.ParserException;

public class LambdaSerializer
{
	private static SerializeVisitor visitor;

	private LambdaSerializer()
	{
	}

	public static short[] serialize(Lambda lambda)
	{
		if (visitor == null)
		{
			visitor = new SerializeVisitor();
		}
		List<Short> data = new ArrayList<Short>();
		visitor.initialize();
		lambda.accept(visitor, data);
		short[] a = new short[data.size()];
		for (int i = 0; i < a.length; i++)
		{
			a[i] = data.get(i);
		}
		return a;
	}

	private static class SerializeVisitor implements Lambda.VisitorRP<Object, List<Short>>
	{
		private static class Context
		{
			private LinkedList<String> names = new LinkedList<String>();

			public void clear()
			{
				names.clear();
			}

			public void pushVariable(String name)
			{
				names.push(name);
			}

			public void popVariable()
			{
				names.pop();
			}

			public short findVariable(String name)
			{
				Iterator<String> it = names.iterator();
				for (short i = 0; it.hasNext(); i++)
				{
					if (it.next().equals(name))
					{
						return (short)(i + 1);
					}
				}
				return -1;
			}
		}

		private static final short LAMBDA_BEGIN = 0x7FFE;
		private static final short LAMBDA_END = 0x7FFF;

		private Context cl = new Context();
		private Map<String, Short> fv = new HashMap<String, Short>();
		private short fvId = 1;

		public void initialize()
		{
			cl.clear();
			fv.clear();
			fvId = 1;
		}

		public Object visit(ASTAbstract abs, List<Short> data)
		{
			cl.pushVariable(abs.name);
			data.add(LAMBDA_BEGIN);
			abs.e.accept(this, data);
			data.add(LAMBDA_END);
			cl.popVariable();
			return null;
		}

		public Object visit(ASTApply app, List<Short> data)
		{
			app.lexpr.accept(this, data);
			app.rexpr.accept(this, data);
			return null;
		}

		public Object visit(ASTLiteral l, List<Short> data)
		{
			data.add(getId(cl, l.name));
			return null;
		}

		public Object visit(ASTMacro m, List<Short> data)
		{
			data.add(getFreeVariableId(m.name));
			return null;
		}

		private short getId(Context c, String name)
		{
			short id = c.findVariable(name);
			if (id != -1)
			{
				return id;
			}
			return getFreeVariableId(name);
		}

		private short getFreeVariableId(String name)
		{
			Short id = fv.get(name);
			if (id == null)
			{
				id = (short)(-(fvId++));
				fv.put(name, id);
			}
			return id;
		}
	}

	public static void main(String[] args)
	{
		System.out.println("SerializeTest");
		Scanner sc = new Scanner(System.in);
		while (true)
		{
			System.out.print("input> ");
			String s = sc.nextLine();
			if (s == null) break;
			s = s.trim();
			if (s.isEmpty()) break;

			try
			{
				Lambda e = Lambda.parse(s);
				System.out.println("e = " + e);
				System.out.println("data: " + Arrays.toString(serialize(e)));
			}
			catch (ParserException e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
}
