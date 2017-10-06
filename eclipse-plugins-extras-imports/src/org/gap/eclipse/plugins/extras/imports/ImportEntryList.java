package org.gap.eclipse.plugins.extras.imports;

import static java.util.Arrays.asList;

import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ImportEntryList {

	public List<String> entries() {
		return Lists.newArrayList(Iterables.concat(guavaEntries(), javaCollectionEntries(), java8Entries()));
	}

	private List<String> guavaEntries() {
		return asList("com.google.common.collect.Sets.*", "com.google.common.collect.Lists.*",
				"com.google.common.collect.Maps.*", "com.google.common.collect.Iterables.*",
				"com.google.common.collect.Iterators.*", "com.google.common.collect.Multimaps.*",
				"com.google.common.collect.Collections2.*", "com.google.common.base.Strings.*",
				"com.google.common.primitives.Longs.*", "com.google.common.primitives.Ints.*",
				"com.google.common.primitives.Shorts.*", "com.google.common.primitives.Chars.*",
				"com.google.common.primitives.Bytes.*", "com.google.common.primitives.Doubles.*",
				"com.google.common.primitives.Floats.*", "com.google.common.collect.FluentIterable.*",
				"com.google.common.base.Joiner.*", "com.google.common.base.Splitter.*");
	}

	private List<String> javaCollectionEntries() {
		return asList("java.util.Arrays.*", "java.util.Collections.*");
	}

	private List<String> java8Entries() {
		return asList("java.util.stream.StreamSupport.*", "java.util.Spliterators.*");
	}

}
