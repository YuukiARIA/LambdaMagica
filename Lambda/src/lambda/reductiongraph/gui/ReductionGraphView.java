package lambda.reductiongraph.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import lambda.ast.Lambda;
import lambda.gui.util.GUIUtils;
import lambda.reductiongraph.StateSearcher;
import lambda.reductiongraph.event.SearchEndListener;

@SuppressWarnings("serial")
public class ReductionGraphView extends JPanel
{
	private DirectedGraphPanel graphPanel;
	private JButton buttonStart;
	private JButton buttonStop;
	private JSpinner spinnerMaxDepth;

	private StateSearcher searcher;
	private int storedNodeCount;

	public ReductionGraphView()
	{
		setLayout(new BorderLayout());

		graphPanel = new DirectedGraphPanel();
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

		final JCheckBox checkMultiEdge = new JCheckBox("multi-edge");
		checkMultiEdge.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				graphPanel.setMultipleEdges(checkMultiEdge.isSelected());
			}
		});

		JPanel p1 = new JPanel();
		p1.add(buttonStart);
		p1.add(buttonStop);
		p1.add(new JLabel("max depth:"));
		p1.add(spinnerMaxDepth);
		JPanel p2 = new JPanel();
		p2.add(checkAntialias);
		p2.add(checkCurve);
		p2.add(checkMultiEdge);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(p1);
		buttonPanel.add(p2);
		GUIUtils.setVerticalLayout(buttonPanel, false, false);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public int getStoredNodeCount()
	{
		return storedNodeCount;
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
				storedNodeCount = searcher.getStateCount();
				dispathSearchEndEvent();
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

	public void addSearchEndListener(SearchEndListener l)
	{
		listenerList.add(SearchEndListener.class, l);
	}

	public void removeSearchEndListener(SearchEndListener l)
	{
		listenerList.remove(SearchEndListener.class, l);
	}

	private void dispathSearchEndEvent()
	{
		for (SearchEndListener l : listenerList.getListeners(SearchEndListener.class))
		{
			l.searchEnded();
		}
	}
}
