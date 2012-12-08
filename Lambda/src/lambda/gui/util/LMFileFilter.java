package lambda.gui.util;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class LMFileFilter extends FileFilter
{
	private static LMFileFilter instance;

	public String getDescription()
	{
		return "lambda macro definition file (*.lm.txt)";
	}

	public boolean accept(File file)
	{
		return file.isDirectory()
			|| file.isFile() && file.getName().endsWith(".lm.txt");
	}

	public static synchronized LMFileFilter getInstance()
	{
		return instance != null ? instance : (instance = new LMFileFilter());
	}
}
