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

	public static void setVerticalLayout(Container host, Component ... components)
	{
		GroupLayout gl = new GroupLayout(host);
		host.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		SequentialGroup sg = gl.createSequentialGroup();
		ParallelGroup pg = gl.createParallelGroup(Alignment.LEADING);
		for (Component c : components)
		{
			sg = sg.addComponent(c);
			pg = pg.addComponent(c, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
		}
		gl.setVerticalGroup(sg);
		gl.setHorizontalGroup(pg);
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
