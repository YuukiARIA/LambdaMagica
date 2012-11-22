package lambda.gui.fontdialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lambda.gui.fontdialog.event.FontApplyListener;

@SuppressWarnings("serial")
public class FontDialog extends JDialog
{
	private String initSelectedName;
	private JComboBox familyNames;
	private JSpinner fontSize;
	private boolean approved;

	private List<FontApplyListener> fontApplyListeners = new ArrayList<FontApplyListener>();

	public FontDialog()
	{
		setTitle("Font");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

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
				dispatchApplyEvent();
			}
		});
		add(familyNames);
		SwingWorker<String[], Void> worker = new SwingWorker<String[], Void>()
		{
			protected void done()
			{
				try
				{
					for (String name : get())
					{
						familyNames.addItem(name);
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
				}
				familyNames.setSelectedItem(initSelectedName);
			}

			protected String[] doInBackground() throws Exception
			{
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				return ge.getAvailableFontFamilyNames();
			}
		};
		worker.execute();

		fontSize = new JSpinner(new SpinnerNumberModel(12, 5, 120, 1));
		fontSize.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				dispatchApplyEvent();
			}
		});
		add(fontSize);

		JButton buttonOK = new JButton("OK");
		buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				approved = true;
				dispatchApplyEvent();
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
			.addGroup(gl.createSequentialGroup()
				.addComponent(familyNames)
				.addComponent(fontSize)
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

	public String getSelectedFamilyName()
	{
		return (String)familyNames.getSelectedItem();
	}

	public int getSelectedFontSize()
	{
		return (Integer)fontSize.getValue();
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

	public void addFontApplyListener(FontApplyListener l)
	{
		fontApplyListeners.add(l);
	}

	public void removeFontApplyListener(FontApplyListener l)
	{
		fontApplyListeners.remove(l);
	}

	private void dispatchApplyEvent()
	{
		for (FontApplyListener l : fontApplyListeners)
		{
			l.applied(getSelectedFamilyName(), getSelectedFontSize());
		}
	}
}
