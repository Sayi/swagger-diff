package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.model.ElProperty;

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

	Map<String, Model> oldDedinitions;
	Map<String, Model> newDedinitions;

	private ModelDiff() {
		increased = new ArrayList<ElProperty>();
		missing = new ArrayList<ElProperty>();
	}

	public static ModelDiff buildWithDefinition(Map<String, Model> left,
			Map<String, Model> right) {
		ModelDiff diff = new ModelDiff();
		diff.oldDedinitions = left;
		diff.newDedinitions = right;
		return diff;
	}

	public ModelDiff diff(Model leftModel, Model rightModel) {
		return this.diff(leftModel, rightModel, null);
	}

	public ModelDiff diff(Model leftModel, Model rightModel, String parentEl) {
		if (null == leftModel && null == rightModel) return this;
		Map<String, Property> leftProperties = null == leftModel ? null : leftModel.getProperties();
		Map<String, Property> rightProperties = null == rightModel ? null : rightModel.getProperties();
		MapKeyDiff<String, Property> propertyDiff = MapKeyDiff.diff(leftProperties, rightProperties);
		Map<String, Property> increasedProp = propertyDiff.getIncreased();
		Map<String, Property> missingProp = propertyDiff.getMissing();

		increased.addAll(convert2ElPropertys(increasedProp, parentEl, false));
		missing.addAll(convert2ElPropertys(missingProp, parentEl, true));

		List<String> sharedKey = propertyDiff.getSharedKey();
		for (String key : sharedKey) {
			Property left = leftProperties.get(key);
			Property right = rightProperties.get(key);
			if (left instanceof RefProperty
					&& right instanceof RefProperty) {
				String leftRef = ((RefProperty) left).getSimpleRef();
				String rightRef = ((RefProperty) right).getSimpleRef();
				diff(oldDedinitions.get(leftRef),
						newDedinitions.get(rightRef),
						null == parentEl ? key : (parentEl + "." + key));
			}
		}
		return this;
	}

	private Collection<? extends ElProperty> convert2ElPropertys(
			Map<String, Property> propMap, String parentEl, boolean isLeft) {
		List<ElProperty> result = new ArrayList<ElProperty>();
		if (null == propMap) return result;
		for (Entry<String, Property> entry : propMap.entrySet()) {
			String propName = entry.getKey();
			Property property = entry.getValue();
			if (property instanceof RefProperty) {
				String ref = ((RefProperty) property).getSimpleRef();
				Model model = isLeft ? oldDedinitions.get(ref)
						: newDedinitions.get(ref);
				if (model != null) {
					Map<String, Property> properties = model.getProperties();
					result.addAll(
							convert2ElPropertys(properties,
									null == parentEl ? propName
											: (parentEl + "." + propName),
									isLeft));
				}
			} else {
				ElProperty pWithPath = new ElProperty();
				pWithPath.setProperty(property);
				pWithPath.setEl(null == parentEl ? propName
						: (parentEl + "." + propName));
				result.add(pWithPath);
			}
		}
		return result;
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

}
