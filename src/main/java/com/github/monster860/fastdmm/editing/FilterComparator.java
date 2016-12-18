package com.github.monster860.fastdmm.editing;

import java.util.Comparator;

public class FilterComparator implements Comparator<String> {
	@Override
	public int compare(String o1, String o2) {
		if(o1.startsWith("~"))
			o1 = o1.substring(1);
		if(o2.startsWith("~"))
			o2 = o1.substring(2);
		return o1.compareToIgnoreCase(o2);
	}
}
