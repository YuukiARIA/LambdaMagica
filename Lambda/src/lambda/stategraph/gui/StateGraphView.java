package lambda.stategraph.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import lambda.ast.Lambda;
import lambda.stategraph.StateSearcher;
import lambda.stategraph.event.SearchEndListener;


@SuppressWarnings("serial")
public class StateGraphView extends JPanel
{
	private StateGraphPanel graphPanel;
	private JButton buttonStart;
	private JButton buttonStop;
	private JSpinner spinnerMaxDepth;

	private StateSearcher searcher;

	public StateGraphView()
	{
		setLayout(new BorderLayout());

		graphPanel = new StateGraphPanel();
		add(graphPanel, BorderLayout.CENTER);

		buttonStart = new JButton("start");
		buttonStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispathActionEvent();
			}
		});
		buttonStop = new JButton("stop");
		buttonStop.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (searcher != null)
				{
					searcher.abort();
				}
			}
		});
		buttonStop.setEnabled(false);

		spinnerMaxDepth = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));

		final JCheckBox checkAntialias = new JCheckBox("antialias");
		checkAntialias.setSelected(false);
		checkAntialias.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				graphPanel.setAntialias(checkAntialias.isSelected());
			}
		});

		final JCheckBox checkCurve = new JCheckBox("curve");
		checkCurve.setSelected(true);
		checkCurve.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				graphPanel.setDrawCurve(checkCurve.isSelected());
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(buttonStart);
		buttonPanel.add(buttonStop);
		buttonPanel.add(spinnerMaxDepth);
		buttonPanel.add(checkAntialias);
		buttonPanel.add(checkCurve);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public void setStartButtonEnabled(boolean b)
	{
		buttonStart.setEnabled(b);
	}

	public void setStopButtonEnabled(boolean b)
	{
		buttonStop.setEnabled(b);
	}

	public void startSearch(Lambda lambda)
	{
		buttonStart.setEnabled(false);
		buttonStop.setEnabled(true);

		graphPanel.clearGraph();

		int maxDepth = (Integer)spinnerMaxDepth.getValue();
		searcher = new StateSearcher(graphPanel, lambda, maxDepth);
		searcher.addSearchEndListener(new SearchEndListener()
		{
			public void searchEnded()
			{
				buttonStart.setEnabled(true);
				buttonStop.setEnabled(false);
				searcher = null;
			}
		});
		searcher.startSearch();
	}

	public void addActionListener(ActionListener listener)
	{
		listenerList.add(ActionListener.class, listener);
	}

	public void removeActionListener(ActionListener listener)
	{
		listenerList.remove(ActionListener.class, listener);
	}

	private void dispathActionEvent()
	{
		ActionEvent event = new ActionEvent(this, 0, "start");
		for (ActionListener l : listenerList.getListeners(ActionListener.class))
		{
			l.actionPerformed(event);
		}
	}
}
