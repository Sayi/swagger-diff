package com.deepoove.swagger.diff.compare;

import com.deepoove.swagger.diff.model.ElProperty;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.Data;

import java.util.*;
import java.util.Map.Entry;

/**
 * compare two model
 *
 * @author Sayi
 */
@Data
public class ModelDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> changed;
    private List<ElProperty> requiredChanges = new ArrayList<ElProperty>();
    private List<ElProperty> typeChanges = new ArrayList<ElProperty>();

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
        return this.diff(leftModel, rightModel, null, new HashSet<Model>());
    }

    public ModelDiff diff(Model leftModel, Model rightModel, String parentEl) {
        return this.diff(leftModel, rightModel, parentEl, new HashSet<Model>());
    }

    private ModelDiff diff(Model leftModel, Model rightModel, String parentEl, Set<Model> visited) {
        // Stop recursing if both models are null
        // OR either model is already contained in the visiting history
        if ((null == leftModel && null == rightModel) || visited.contains(leftModel) || visited.contains(rightModel)) {
            return this;
        }
        Map<String, Property> leftProperties = null == leftModel ? null : leftModel.getProperties();
        Map<String, Property> rightProperties = null == rightModel ? null : rightModel.getProperties();

        // Diff the properties
        MapKeyDiff<String, Property> propertyDiff = MapKeyDiff.diff(leftProperties, rightProperties);

        increased.addAll(convert2ElPropertys(propertyDiff.getIncreased(), parentEl));
        missing.addAll(convert2ElPropertys(propertyDiff.getMissing(), parentEl));

        // Recursively find the diff between properties
        List<String> sharedKey = propertyDiff.getSharedKey();
        sharedKey.stream().forEach((key) -> {
            Property left = leftProperties.get(key);
            Property right = rightProperties.get(key);

            if ((left instanceof RefProperty) && (right instanceof RefProperty)) {
                String leftRef = ((RefProperty) left).getSimpleRef();
                String rightRef = ((RefProperty) right).getSimpleRef();

                diff(oldDedinitions.get(leftRef), newDedinitions.get(rightRef),
                        buildElString(parentEl, key),
                        copyAndAdd(visited, leftModel, rightModel));

            } else if (left != null && right != null && !left.equals(right)) {
                // Add a changed ElProperty if not a Reference
                // Useless
                changed.add(convert2ElProperty(key, parentEl, left));
            }
        });
        return this;
    }

    private Collection<? extends ElProperty> convert2ElPropertys(
            Map<String, Property> propMap, String parentEl) {

        List<ElProperty> result = new ArrayList<ElProperty>();
        if (null == propMap) return result;

        for (Entry<String, Property> entry : propMap.entrySet()) {
            // TODO Recursively get the properties
            result.add(convert2ElProperty(entry.getKey(), parentEl, entry.getValue()));
        }
        return result;
    }

    private String buildElString(String parentEl, String propName) {
        return null == parentEl ? propName : (parentEl + "." + propName);
    }

    private ElProperty convert2ElProperty(String propName, String parentEl, Property property) {
        ElProperty pWithPath = new ElProperty();
        pWithPath.setProperty(property);
        pWithPath.setEl(buildElString(parentEl, propName));
        return pWithPath;
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> copyAndAdd(Set<T> set, T... add) {
        Set<T> newSet = new HashSet<T>(set);
        newSet.addAll(Arrays.asList(add));
        return newSet;
    }

}
