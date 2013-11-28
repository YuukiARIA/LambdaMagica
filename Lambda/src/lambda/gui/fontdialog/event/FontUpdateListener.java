package lambda.gui.fontdialog.event;

public interface FontUpdateListener
{
	public void fontFamilyChanged(String family);
	public void fontSizeChanged(int size);
	public void uiFontAdditionChanged(int addition);
}
