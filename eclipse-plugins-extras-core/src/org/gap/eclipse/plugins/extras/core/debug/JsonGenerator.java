package org.gap.eclipse.plugins.extras.core.debug;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonGenerator {
	private Set<String> terminalTypes = ImmutableSet.of("java.lang.String");
	
	public JsonObject construct(IJavaVariable variable) throws CoreException {
		JsonObject jsonObject = new JsonObject();
		populateJson(variable, jsonObject);
		return jsonObject;
	}

	
	private void populateJson(IJavaVariable variable, JsonObject jsonObject) throws CoreException {
		final IValue value = logicalValue(variable.getValue());
		if(isTerminalType(value)) {
			jsonObject.addProperty(variable.getName(), value.getValueString());
		} else if(isArray(value)) {
			final IJavaValue[] values = ((IJavaArray) value).getValues();
			jsonObject.add(variable.getName(), createJsonArray(values));
		} else {
			final JsonObject innerObject = new JsonObject();
			jsonObject.add(variable.getName(), innerObject);
			for (IVariable subVariable : value.getVariables()) {
				populateJson((IJavaVariable) subVariable, innerObject);
			}
		}
	}
	
	private IValue logicalValue(IValue value) throws CoreException {
		ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
		if (types.length > 0) {
			ILogicalStructureType type = DebugPlugin.getDefaultStructureType(types);
			if (type != null) {
				return type.getLogicalStructure(value);
			}
		}

		return value;
	}

	private JsonElement createJsonArray(IJavaValue[] values) throws CoreException {
		JsonArray array = new JsonArray();
		for (IJavaValue value : values) {
			if (isTerminalType(value)) {
				array.add(value.getValueString());
			} else if (isArray(value)) {
				array.add(createJsonArray(((IJavaArray)value).getValues()));
			} else {
				JsonObject innerObject = new JsonObject();
				array.add(innerObject);
				for (IVariable variable : value.getVariables()) {
					populateJson((IJavaVariable) variable, innerObject);
				}

			}
		}

		return array;
	}

	private boolean isTerminalType(IValue value) throws DebugException {
		if(value instanceof IJavaPrimitiveValue) {
			return true;
		}else {
			return terminalTypes.contains(value.getReferenceTypeName());
		}
	}
	
	private boolean isArray(IValue value) {
		return value instanceof IJavaArray;
	}
}
