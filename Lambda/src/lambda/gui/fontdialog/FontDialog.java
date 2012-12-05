package lambda.gui.fontdialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lambda.gui.fontdialog.event.FontUpdateListener;
import lambda.gui.util.GUIUtils;

@SuppressWarnings("serial")
public class FontDialog extends JDialog
{
	private String initSelectedName;
	private JComboBox familyNames;
	private JSpinner fontSize;
	private JLabel labelFontAddition;
	private int fontAddition;
	private boolean approved;

	private List<FontUpdateListener> fontApplyListeners = new ArrayList<FontUpdateListener>();

	public FontDialog()
	{
		setTitle("Font Setting");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		familyNames = new JComboBox();
		Dimension dim = familyNames.getPreferredSize();
		dim.width = 160;
		familyNames.setPreferredSize(dim);
		familyNames.setMaximumSize(dim);
		familyNames.setMinimumSize(dim);
		familyNames.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispatchFontFamilyChangeEvent();
			}
		});
		add(familyNames);
		for (String name : GUIUtils.getAvailableFontFamilyNames())
		{
			familyNames.addItem(name);
		}
		familyNames.setSelectedItem(initSelectedName);

		fontSize = new JSpinner(new SpinnerNumberModel(12, 5, 120, 1));
		fontSize.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				dispatchFontSizeChangeEvent();
			}
		});
		add(fontSize);

		JPanel panelAddition = new JPanel();
		panelAddition.setBorder(BorderFactory.createTitledBorder("UI Font Size"));
		labelFontAddition = new JLabel("+0");
		panelAddition.add(labelFontAddition);
		JButton buttonInc = new JButton("+");
		buttonInc.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFontAddition(getFontAddition() + 1);
				dispatchUIFontAdditionChangeEvent();
			}
		});
		panelAddition.add(buttonInc);
		JButton buttonDec = new JButton("-");
		buttonDec.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setFontAddition(getFontAddition() - 1);
				dispatchUIFontAdditionChangeEvent();
			}
		});
		panelAddition.add(buttonDec);
		add(panelAddition);

		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				approved = true;
				dispose();
			}
		});
		add(buttonOK);

		JButton buttonCancel = new JButton("Cancel");
		buttonCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		add(buttonCancel);

		GroupLayout gl = new GroupLayout(getContentPane());
		getContentPane().setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(gl.createParallelGroup(Alignment.TRAILING)
			.addGroup(gl.createParallelGroup()
				.addGroup(gl.createSequentialGroup()
					.addComponent(familyNames)
					.addComponent(fontSize)
				)
				.addComponent(panelAddition)
			)
			.addGroup(gl.createSequentialGroup()
				.addComponent(buttonOK)
				.addComponent(buttonCancel)
			)
		);
		gl.setVerticalGroup(gl.createSequentialGroup()
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(familyNames)
				.addComponent(fontSize)
			)
			.addComponent(panelAddition)
			.addGroup(gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(buttonOK)
				.addComponent(buttonCancel)
			)
		);
		gl.linkSize(buttonOK, buttonCancel);

		pack();
	}

	public void setSelectedFont(Font font)
	{
		String family = font.getFamily();
		int size = font.getSize();
		familyNames.setSelectedItem(family);
		fontSize.setValue(size);
	}

	public void setSelectedFamilyName(String family)
	{
		initSelectedName = family;
		familyNames.setSelectedItem(family);
	}

	public void setSelectedFontSize(int size)
	{
		fontSize.setValue(size);
	}

	public void setFontAddition(int addition)
	{
		fontAddition = addition;
		labelFontAddition.setText(String.format("%+d", fontAddition));
	}

	public String getSelectedFamilyName()
	{
		return (String)familyNames.getSelectedItem();
	}

	public int getSelectedFontSize()
	{
		return (Integer)fontSize.getValue();
	}

	public int getFontAddition()
	{
		return fontAddition;
	}

	public boolean isApproved()
	{
		return approved;
	}

	public boolean isCanceled()
	{
		return !approved;
	}

	public boolean showModal()
	{
		approved = false;
		setModalityType(ModalityType.APPLICATION_MODAL);
		setVisible(true);
		return approved;
	}

	public void addFontApplyListener(FontUpdateListener l)
	{
		fontApplyListeners.add(l);
	}

	public void removeFontApplyListener(FontUpdateListener l)
	{
		fontApplyListeners.remove(l);
	}

	private void dispatchFontFamilyChangeEvent()
	{
		for (FontUpdateListener l : fontApplyListeners)
		{
			l.fontFamilyChanged(getSelectedFamilyName());
		}
	}

	private void dispatchFontSizeChangeEvent()
	{
		for (FontUpdateListener l : fontApplyListeners)
		{
			l.fontSizeChanged(getSelectedFontSize());
		}
	}
	private void dispatchUIFontAdditionChangeEvent()
	{
		for (FontUpdateListener l : fontApplyListeners)
		{
			l.uiFontAdditionChanged(getFontAddition());
		}
	}
}
