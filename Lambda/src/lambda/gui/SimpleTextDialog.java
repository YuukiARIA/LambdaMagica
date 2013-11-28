package lambda.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

@SuppressWarnings("serial")
public class SimpleTextDialog extends JDialog
{
	private JTextArea textArea;

	public SimpleTextDialog()
	{
		setTitle("Text");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setAlwaysOnTop(true);
		setSize(400, 300);
		setLocationRelativeTo(null);

		textArea = new JTextArea();
		add(new JScrollPane(textArea), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton buttonCopy = new JButton("copy all");
		buttonCopy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				textArea.selectAll();
				textArea.copy();
			}
		});
		buttonPanel.add(buttonCopy);
		JButton buttonClose = new JButton("close");
		buttonClose.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		});
		buttonPanel.add(buttonClose);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setText(String text)
	{
		textArea.setText(text);
	}

	public void setTextAreaFont(Font font)
	{
		textArea.setFont(font);
	}
}
