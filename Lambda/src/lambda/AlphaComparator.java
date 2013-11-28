package lambda;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;

import lambda.ast.ASTAbstract;
import lambda.ast.ASTApply;
import lambda.ast.ASTLiteral;
import lambda.ast.ASTMacro;
import lambda.ast.Lambda;
import lambda.ast.parser.ParserException;

public class AlphaComparator
{
	private static AlphaVisitor visitor = new AlphaVisitor();

	public static boolean alphaEquiv(Lambda lambda1, Lambda lambda2)
	{
		visitor.initialize();
		return lambda1.accept(visitor, lambda2);
	}

	private static class AlphaVisitor implements Lambda.VisitorRP<Boolean, Lambda>
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

			public int findVariable(String name)
			{
				Iterator<String> it = names.iterator();
				for (int i = 0; it.hasNext(); i++)
				{
					if (it.next().equals(name))
					{
						return i + 1;
					}
				}
				return -1;
			}
		}

		private Context cl = new Context();
		private Context cr = new Context();
		private Map<String, Integer> fv = new HashMap<String, Integer>();
		private int fvId = 1;

		public void initialize()
		{
			cl.clear();
			cr.clear();
			fv.clear();
			fvId = 1;
		}

		public Boolean visit(ASTAbstract abs, Lambda lambda)
		{
			if (lambda instanceof ASTAbstract)
			{
				ASTAbstract abs2 = (ASTAbstract)lambda;
				push(abs.name, abs2.name);
				boolean ret = abs.e.accept(this, abs2.e);
				pop();
				return ret;
			}
			return false;
		}

		public Boolean visit(ASTApply app1, Lambda lambda)
		{
			if (lambda instanceof ASTApply)
			{
				ASTApply app2 = (ASTApply)lambda;
				return app1.lexpr.accept(this, app2.lexpr) && app1.rexpr.accept(this, app2.rexpr);
			}
			return false;
		}

		public Boolean visit(ASTLiteral l1, Lambda lambda)
		{
			if (lambda instanceof ASTLiteral)
			{
				ASTLiteral l2 = (ASTLiteral)lambda;
				return getId(cl, l1.name) == getId(cr, l2.name);
			}
			return false;
		}

		public Boolean visit(ASTMacro m1, Lambda lambda)
		{
			if (lambda instanceof ASTMacro)
			{
				ASTMacro m2 = (ASTMacro)lambda;
				return getFreeVariableId(m1.name) == getFreeVariableId(m2.name);
			}
			return false;
		}

		private void push(String name1, String name2)
		{
			cl.pushVariable(name1);
			cr.pushVariable(name2);
		}

		private void pop()
		{
			cl.popVariable();
			cr.popVariable();
		}

		private int getId(Context c, String name)
		{
			int id = c.findVariable(name);
			if (id != -1)
			{
				return id;
			}
			return getFreeVariableId(name);
		}

		private int getFreeVariableId(String name)
		{
			Integer id = fv.get(name);
			if (id == null)
			{
				id = -(fvId++);
				fv.put(name, id);
			}
			return id;
		}
	}

	public static void main(String[] args)
	{
		System.out.println("AlpahEquivTest");
		Scanner sc = new Scanner(System.in);
		while (true)
		{
			System.out.print("input e1> ");
			String s1 = sc.nextLine();
			if (s1 == null) break;
			s1 = s1.trim();
			if (s1.isEmpty()) break;

			System.out.print("input e2> ");
			String s2 = sc.nextLine();
			if (s2 == null) break;
			s2 = s2.trim();
			if (s2.isEmpty()) break;

			try
			{
				Lambda e1 = Lambda.parse(s1);
				Lambda e2 = Lambda.parse(s2);
				System.out.println("e1 = " + e1);
				System.out.println("e2 = " + e2);
				System.out.println("structural equiv: " + LambdaMatcher.structuralEquivalent(e1, e2));
				System.out.println("     alpha equiv: " + AlphaComparator.alphaEquiv(e1, e2));
			}
			catch (ParserException e)
			{
				System.out.println(e.getMessage());
			}
		}
	}
}
