package lambda.gui.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.UIManager;

public final class GUIUtils
{
	private static String[] availableFontFamilyNames;

	public static void setVerticalLayout(Container host)
	{
		setVerticalLayout(host, true, true);
	}

	public static void setVerticalLayout(Container host, boolean gap, boolean containerGap)
	{
		setGroupLayout(host, gap, containerGap, true);
	}

	public static void setHorizontalLayout(Container host)
	{
		setHorizontalLayout(host, true, true);
	}

	public static void setHorizontalLayout(Container host, boolean gap, boolean containerGap)
	{
		setGroupLayout(host, gap, containerGap, false);
	}

	private static void setGroupLayout(Container host, boolean gap, boolean containerGap, boolean vertical)
	{
		GroupLayout gl = new GroupLayout(host);
		host.setLayout(gl);
		gl.setAutoCreateGaps(gap);
		gl.setAutoCreateContainerGaps(containerGap);
		SequentialGroup sg = gl.createSequentialGroup();
		ParallelGroup pg = gl.createParallelGroup(vertical ? Alignment.LEADING : Alignment.BASELINE);
		for (Component c : host.getComponents())
		{
			sg = sg.addComponent(c);
			pg = pg.addComponent(c, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		}
		if (vertical)
		{
			gl.setVerticalGroup(sg);
			gl.setHorizontalGroup(pg);
		}
		else
		{
			gl.setVerticalGroup(pg);
			gl.setHorizontalGroup(sg);
		}
	}

	public static void setLookAndFeelToSystem()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void preloadFontNames()
	{
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		availableFontFamilyNames = ge.getAvailableFontFamilyNames();
	}

	public static String[] getAvailableFontFamilyNames()
	{
		if (availableFontFamilyNames == null)
		{
			preloadFontNames();
		}
		return availableFontFamilyNames;
	}
}
