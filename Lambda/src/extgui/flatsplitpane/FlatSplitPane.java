package extgui.flatsplitpane;

import javax.swing.BorderFactory;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;

@SuppressWarnings("serial")
public class FlatSplitPane extends JSplitPane
{
	public void updateUI()
	{
		super.updateUI();
		setUI(new FlatSplitPaneUI());
	}
}

class FlatSplitPaneUI extends BasicSplitPaneUI
{
	private static BasicSplitPaneDivider divider;

	public BasicSplitPaneDivider createDefaultDivider()
	{
		if (divider == null)
		{
			divider = new EmptyDivider(this);
		}
		return divider;
	}

	protected void installDefaults()
	{
		super.installDefaults();
		splitPane.setBorder(null);
	}

	@SuppressWarnings("serial")
	private static class EmptyDivider extends BasicSplitPaneDivider
	{
		public EmptyDivider(BasicSplitPaneUI ui)
		{
			super(ui);
			super.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		}

		public void setBorder(Border b)
		{
		}
	}
}
