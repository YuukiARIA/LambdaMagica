package lambda.gui.fontdialog.event;

import java.util.EventListener;

public interface FontApplyListener extends EventListener
{
	public void applied(String fontFamily, int size);
}
