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

import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;

/**
 * compare two model
 * 
 * @author Sayi
 * @version
 */
public class ModelDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> changed;

    Map<String, Model> oldDedinitions;
    Map<String, Model> newDedinitions;

    private ModelDiff() {
        increased = new ArrayList<ElProperty>();
        missing = new ArrayList<ElProperty>();
        changed = new ArrayList<ElProperty>();
    }

    public static ModelDiff buildWithDefinition(Map<String, Model> left, Map<String, Model> right) {
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

    public ModelDiff diff(Property leftProperty, Property rightProperty) {
        return this.diff(findModel(leftProperty, oldDedinitions), findModel(rightProperty, newDedinitions));
    }

    private ModelDiff diff(Model leftInputModel, Model rightInputModel, String parentEl, Set<Model> visited) {
        // Stop recursing if both models are null
        // OR either model is already contained in the visiting history
        if ((null == leftInputModel && null == rightInputModel) || visited.contains(leftInputModel)
                || visited.contains(rightInputModel)) {
            return this;
        }
        Model leftModel = isModelReference(leftInputModel) ? findReferenceModel(leftInputModel, oldDedinitions)
                : leftInputModel;
        Model rightModel = isModelReference(rightInputModel) ? findReferenceModel(rightInputModel, newDedinitions)
                : rightInputModel;
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
            Model leftSubModel = findModel(left, oldDedinitions);
            Model rightSubModel = findModel(left, newDedinitions);
            if (leftSubModel != null || rightSubModel != null) {
                diff(leftSubModel, rightSubModel, buildElString(parentEl, key),
                        copyAndAdd(visited, leftModel, rightModel));
            } else if (left != null && right != null && !left.equals(right)) {
                // Add a changed ElProperty if not a Reference
                // Useless
                ElProperty changedMeta = addChangeMetadata(convert2ElProperty(key, parentEl, left), left, right);
                if(changedMeta.isChanged()){
                    changed.add(changedMeta);
                }
            }
        });
        return this;
    }

    private Collection<? extends ElProperty> convert2ElPropertys(Map<String, Property> propMap, String parentEl) {

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

    private ElProperty addChangeMetadata(ElProperty diffProperty, Property left, Property right) {
        diffProperty.setTypeChange(!left.getType().equalsIgnoreCase(right.getType()));
        List<String> leftEnums = enumValues(left);
        List<String> rightEnums = enumValues(right);
        if (!leftEnums.isEmpty() && !rightEnums.isEmpty()) {
            ListDiff<String> enumDiff = ListDiff.diff(leftEnums, rightEnums, (t, enumVal) -> {
                for (String value : t) {
                    if (enumVal.equalsIgnoreCase(value)) { return value; }
                }
                return null;
            });
            diffProperty.setNewEnums(enumDiff.getIncreased() != null && !enumDiff.getIncreased().isEmpty());
            diffProperty.setRemovedEnums(enumDiff.getMissing() != null && !enumDiff.getMissing().isEmpty());
        }
        return diffProperty;
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> copyAndAdd(Set<T> set, T... add) {
        Set<T> newSet = new HashSet<T>(set);
        newSet.addAll(Arrays.asList(add));
        return newSet;
    }

    private List<String> enumValues(Property prop) {
        if (prop instanceof StringProperty && ((StringProperty) prop).getEnum() != null) {
            return ((StringProperty) prop).getEnum();
        } else {
            return new ArrayList<>();
        }
    }

    private Model findModel(Property property, Map<String, Model> modelMap) {
        String modelName = null;
        if (property instanceof RefProperty) {
            modelName = ((RefProperty) property).getSimpleRef();
        } else if (property instanceof ArrayProperty) {
            Property arrayType = ((ArrayProperty) property).getItems();
            if (arrayType instanceof RefProperty) {
                modelName = ((RefProperty) arrayType).getSimpleRef();
            }
        }
        return modelName == null ? null : modelMap.get(modelName);
    }

    private boolean isModelReference(Model model) {
        return model instanceof RefModel || model instanceof ArrayModel;
    }

    private Model findReferenceModel(Model model, Map<String, Model> modelMap) {
        String modelName = null;
        if (model instanceof RefModel) {
            modelName = ((RefModel) model).getSimpleRef();
        } else if (model instanceof ArrayModel) {
            Property arrayType = ((ArrayModel) model).getItems();
            if (arrayType instanceof RefProperty) {
                modelName = ((RefProperty) arrayType).getSimpleRef();
            }
        }
        return modelName == null ? null : modelMap.get(modelName);
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
