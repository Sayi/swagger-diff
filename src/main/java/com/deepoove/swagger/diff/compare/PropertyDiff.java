package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.deepoove.swagger.diff.model.ResponseProperty;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class PropertyDiff {

	private List<ResponseProperty> increased;
	private List<ResponseProperty> missing;

	Map<String, Model> oldDedinitions;
	Map<String, Model> newDedinitions;

	private PropertyDiff() {
		increased = new ArrayList<ResponseProperty>();
		missing = new ArrayList<ResponseProperty>();
	}

	public static PropertyDiff buildWithDefinition(Map<String, Model> left,
			Map<String, Model> right) {
		PropertyDiff diff = new PropertyDiff();
		diff.oldDedinitions = left;
		diff.newDedinitions = right;
		return diff;

	}

	public PropertyDiff diff(Property left, Property right) {
		return this.diff(left, right, null);
	}
	public PropertyDiff diff(Property left, Property right, String parentEl) {
		if (left instanceof RefProperty && right instanceof RefProperty) {
			String leftRef = ((RefProperty) left).getSimpleRef();
			String rightRef = ((RefProperty) right).getSimpleRef();
			Model leftModel = oldDedinitions.get(leftRef);
			Model rightModel = newDedinitions.get(rightRef);
			if (null != leftModel && null != rightModel) {
				Map<String, Property> leftProperties = leftModel
						.getProperties();
				Map<String, Property> rightProperties = rightModel
						.getProperties();
				MapKeyDiff<String, Property> propertyDiff = MapKeyDiff
						.diff(leftProperties, rightProperties);
				Map<String, Property> increasedProp = propertyDiff
						.getIncreased();
				Map<String, Property> missingProp = propertyDiff.getMissing();

				increased.addAll(
						convert2ResponsePropertys(increasedProp, parentEl, false));
				missing.addAll(
						convert2ResponsePropertys(missingProp, parentEl, true));

				List<String> sharedKey = propertyDiff.getSharedKey();
				for (String key : sharedKey) {
					diff(leftProperties.get(key), rightProperties.get(key), null == parentEl ? key : (parentEl + "." + key));
				}
			}

		}
		return this;
	}

	private Collection<? extends ResponseProperty> convert2ResponsePropertys(
			Map<String, Property> propMap, String parentEl, boolean isLeft) {
		List<ResponseProperty> result = new ArrayList<ResponseProperty>();
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
							convert2ResponsePropertys(properties,
									null == parentEl ? propName
											: (parentEl + "." + propName),
									isLeft));
				}
			} else {
				ResponseProperty pWithPath = new ResponseProperty();
				pWithPath.setProperty(property);
				pWithPath.setEl(null == parentEl ? propName
						: (parentEl + "." + propName));
				result.add(pWithPath);
			}
		}
		return result;
	}

	public List<ResponseProperty> getIncreased() {
		return increased;
	}

	public void setIncreased(List<ResponseProperty> increased) {
		this.increased = increased;
	}

	public List<ResponseProperty> getMissing() {
		return missing;
	}

	public void setMissing(List<ResponseProperty> missing) {
		this.missing = missing;
	}

}
