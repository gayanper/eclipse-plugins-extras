package org.gap.eclipse.plugins.extras.imports;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

public class Imports {

	private List<String> importList;

	public Imports(String settingString) {
		importList = decodeImports(settingString);
	}

	private List<String> decodeImports(String setting) {
		if (Strings.isNullOrEmpty(setting)) {
			return new ArrayList<>();
		}
		return new ArrayList<>(asList(setting.split(";")));
	}

	private String encodeImports(List<String> imports) {
		return Joiner.on(";").join(imports);
	}

	public void mergeImports(List<String> imports) {
		imports.forEach(i -> {
			if (!importList.contains(i)) {
				importList.add(i);
			}
		});
	}

	public String toSetting() {
		return encodeImports(importList);
	}
}
