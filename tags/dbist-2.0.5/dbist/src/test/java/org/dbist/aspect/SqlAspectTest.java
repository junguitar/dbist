package org.dbist.aspect;

import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.common.util.ValueUtils;

import org.apache.commons.collections.ComparatorUtils;
import org.junit.Test;

public class SqlAspectTest {

	private static final Comparator<String> COMPARATOR_REVERSED = ComparatorUtils.reversedComparator(ComparatorUtils.naturalComparator());
	@Test
	public void test() {
		Set<String> sample = ValueUtils.toSet("a", "aa", "a_", "a0");
		Set<String> keySet = new TreeSet<String>(COMPARATOR_REVERSED);
		keySet.addAll(sample);
		for (String key : keySet)
			System.out.print(key + " ");
	}

}
