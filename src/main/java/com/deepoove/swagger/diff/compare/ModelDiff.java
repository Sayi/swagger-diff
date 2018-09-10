package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.deepoove.swagger.diff.model.ElProperty;
import com.google.common.collect.ImmutableMap;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

/**
 * compare two model
 * @author Sayi
 * @version 
 */
public class ModelDiff {

	private List<ElProperty> increased;
	private List<ElProperty> missing;
	private List<ElProperty> changed;
	private int count = 0;

	Map<String, Model> oldDedinitions;
	Map<String, Model> newDedinitions;

	private ModelDiff() {
		increased = new ArrayList<ElProperty>();
		missing = new ArrayList<ElProperty>();
		changed = new ArrayList<ElProperty>();
	}

	public static ModelDiff buildWithDefinition(Map<String, Model> left,
			Map<String, Model> right) {
		ModelDiff diff = new ModelDiff();
		diff.oldDedinitions = left;
		diff.newDedinitions = right;
		return diff;
	}

	public ModelDiff diff(Model leftModel, Model rightModel) {
		return this.diff(leftModel, rightModel, null, new HashSet<String>());
	}

	public ModelDiff diff(Model leftModel, Model rightModel, String parentEl) {
		return this.diff(leftModel, rightModel, parentEl, new HashSet<String>());
	}

	private ModelDiff diff(Model leftModel, Model rightModel, String parentEl, Set<String> visited) {
		if ((null == leftModel && null == rightModel)) return this;
		Map<String, Property> leftProperties = null == leftModel ? null : leftModel.getProperties();
		Map<String, Property> rightProperties = null == rightModel ? null : rightModel.getProperties();
		MapKeyDiff<String, Property> propertyDiff = MapKeyDiff.diff(leftProperties, rightProperties);
		Map<String, Property> increasedProp = propertyDiff.getIncreased();
		Map<String, Property> missingProp = propertyDiff.getMissing();

		increased.addAll(asElProperties(increasedProp, parentEl, false, new HashSet<String>()));
		missing.addAll(asElProperties(missingProp, parentEl, true, new HashSet<String>()));

		List<String> sharedKey = propertyDiff.getSharedKey();
		for (String key : sharedKey) {
			Property left = leftProperties.get(key);
			Property right = rightProperties.get(key);

			if (RefProperty.class.isInstance(left) && RefProperty.class.isInstance(right)) {
				String leftRef = ((RefProperty) left).getSimpleRef();
				String rightRef = ((RefProperty) right).getSimpleRef();

				if (!visited.contains(leftRef) && !visited.contains(rightRef)) {
					count += 1;
					diff(oldDedinitions.get(leftRef), newDedinitions.get(rightRef),
							null == parentEl ? key : (parentEl + "." + key),
							copyAndAdd(visited, leftRef, rightRef));
				}
			} else if (!left.equals(right)) {
				changed.add(asElProperty(key, left, parentEl));
			}
		}
		return this;
	}

	private ElProperty asElProperty(String propName, Property prop, String parentEl) {
		return new ArrayList<ElProperty>(asElProperties(ImmutableMap.of(propName, prop), parentEl, true, new HashSet<String>())).get(0);
	}

	private Collection<? extends ElProperty> asElProperties(
			Map<String, Property> propMap, String parentEl, boolean isLeft, Set<String> visited) {
		List<ElProperty> result = new ArrayList<ElProperty>();
		if (null == propMap) return result;
		for (Entry<String, Property> entry : propMap.entrySet()) {
			String propName = entry.getKey();
			Property property = entry.getValue();
			if (property instanceof RefProperty) {
				String ref = ((RefProperty) property).getSimpleRef();
				Model model = isLeft ? oldDedinitions.get(ref)
						: newDedinitions.get(ref);
				if (model != null && !visited.contains(ref)) {
					Map<String, Property> properties = model.getProperties();
					result.addAll(
							asElProperties(properties,
									null == parentEl ? propName
											: (parentEl + "." + propName),
									isLeft, copyAndAdd(visited, ref)));
					return result;
				}
			}
			result.add(buildElProperty(propName, parentEl, property));
		}
		return result;
	}

	private ElProperty buildElProperty(String propName, String parentEl, Property property) {
		ElProperty pWithPath = new ElProperty();
		pWithPath.setProperty(property);
		pWithPath.setEl(null == parentEl ? propName
				: (parentEl + "." + propName));
		return pWithPath;
	}

	private <T> Set<T> copyAndAdd(Set<T> set, T... add) {
		Set<T> newSet = new HashSet<T>(set);
		newSet.addAll(Arrays.asList(add));
		return newSet;
	}

	public List<ElProperty> getIncreased() {
		return increased;
	}

	public void setIncreased(List<ElProperty> increased) {
		this.increased = increased;
	}

	public List<ElProperty> getMissing() {
		return missing;
	}

	public void setMissing(List<ElProperty> missing) {
		this.missing = missing;
	}

	public List<ElProperty> getChanged() {
		return changed;
	}

	public void setChanged(List<ElProperty> changed) {
		this.changed = changed;
	}
}
