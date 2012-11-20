package lambda.gui.macroview;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
class UnmodifiableTableModel extends DefaultTableModel
{
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
}
