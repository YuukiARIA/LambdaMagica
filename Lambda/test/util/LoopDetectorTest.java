package util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;

import static org.junit.Assert.assertThat;

public class LoopDetectorTest
{
	@Test
	public void returnsEmptySetForEmptyGraph()
	{
		Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();
		LoopDetector<Integer> detector = LoopDetector.create(edges);
		assertThat(detector.detectCyclicLoop(), equalTo(Collections.<Integer>emptySet()));
	}

	@Test
	public void returnsEmptySetForAcyclicGraph()
	{
		Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();
		edges.put(0, set(1, 3));
		edges.put(1, set(3));
		edges.put(2, set(0));
		edges.put(3, set(7));
		edges.put(4, set(5));
		edges.put(5, set(6, 7));
		LoopDetector<Integer> detector = LoopDetector.create(edges);
		assertThat(detector.detectCyclicLoop(), equalTo(Collections.<Integer>emptySet()));
	}

	@Test
	public void returnsNodeSetForSelfCyclicGraph()
	{
		Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();
		edges.put(0, set(1, 3));
		edges.put(1, set(3));
		edges.put(2, set(0));
		edges.put(3, set(3));
		edges.put(4, set(5));
		edges.put(5, set(6, 7));
		LoopDetector<Integer> detector = LoopDetector.create(edges);
		assertThat(detector.detectCyclicLoop(), hasItem(3));
	}

	@Test
	public void returnsNodeSetForCyclicGraph()
	{
		Map<Integer, Set<Integer>> edges = new HashMap<Integer, Set<Integer>>();
		edges.put(0, set(1, 3));
		edges.put(1, set(3));
		edges.put(2, set(0));
		edges.put(3, set(4));
		edges.put(4, set(5));
		edges.put(5, set(6, 7, 0));
		LoopDetector<Integer> detector = LoopDetector.create(edges);
		assertThat(detector.detectCyclicLoop(), hasItems(0, 1, 3, 4, 5));
	}

	private static <T> Set<T> set(T ... args)
	{
		return new HashSet<T>(Arrays.asList(args));
	}
}
