package com.deepoove.swagger.diff.compare;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import com.deepoove.swagger.diff.model.ElProperty;

import com.google.common.base.Joiner;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.properties.*;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
            Model rightSubModel = findModel(right, newDedinitions);
            if (leftSubModel != null || rightSubModel != null) {
                diff(leftSubModel, rightSubModel, buildElString(parentEl, key),
                        copyAndAdd(visited, leftModel, rightModel));
            } else if (left != null && right != null && !left.equals(right)) {
                // Add a changed ElProperty if not a Reference
                // Useless
                changed.add(addChangeMetadata(convert2ElProperty(key, parentEl, left), left, right));
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
        if(!left.getType().equalsIgnoreCase(right.getType())) {
            diffProperty.setNewTypeChange(right.getType());
        }

        List<String> leftEnums = enumValues(left);
        List<String> rightEnums = enumValues(right);
        addEnums(diffProperty, leftEnums, rightEnums);

        List<String> changes = new ArrayList<>();
        if(!StringUtils.equals(left.getType(), right.getType())) {
            return diffProperty;
        } else if (left instanceof AbstractNumericProperty) {
            AbstractNumericProperty leftP = (AbstractNumericProperty) left;
            AbstractNumericProperty rightP = (AbstractNumericProperty) right;


            addNumericChanges("maximum", leftP, rightP, AbstractNumericProperty::getMaximum, AbstractNumericProperty::getExclusiveMaximum, changes);
            addNumericChanges("minimum", leftP, rightP, AbstractNumericProperty::getMinimum, AbstractNumericProperty::getExclusiveMinimum, changes);
        } else if(left instanceof StringProperty) {
            StringProperty leftP = (StringProperty) left;
            StringProperty rightP = (StringProperty) right;

            addChanges("maximum length", leftP, rightP, StringProperty::getMaxLength, changes);
            addChanges("minimum length", leftP, rightP, StringProperty::getMinLength, changes);
            addChanges("default", leftP, rightP, StringProperty::getDefault, changes);
            addChanges("pattern", leftP, rightP, StringProperty::getPattern, changes);
        }

        addChanges("format", left, right, Property::getFormat, changes);
        addChanges("example", left, right, Property::getExample, changes);
        addChanges("allow empty value", left, right, Property::getAllowEmptyValue, changes);
        addChanges("readonly", left, right, Property::getReadOnly, changes);
        addChanges("required", left, right, Property::getRequired, changes);
        addChanges("description", left, right, Property::getDescription, changes);

        diffProperty.metadataChanged(Joiner.on(". ").join(changes));
        return diffProperty;
    }

    public static void addEnums(ElProperty diffProperty, List<String> leftEnums, List<String> rightEnums) {
        if (!leftEnums.isEmpty() && !rightEnums.isEmpty()) {
            ListDiff<String> enumDiff = ListDiff.diff(leftEnums, rightEnums, (t, enumVal) -> {
                for (String value : t) {
                    if (enumVal.equalsIgnoreCase(value)) { return value; }
                }
                return null;
            });
            diffProperty.setNewEnums(enumDiff.getIncreased());
            diffProperty.setRemovedEnums(enumDiff.getMissing());
        }
    }

    public static <T> void addNumericChanges(String description, T left, T right, Function<T, BigDecimal> getter,
                                   Function<T, Boolean> exclusive, List<String> changes) {
        if(!Objects.equals(getter.apply(left), getter.apply(right))) {
            if(getter.apply(left) == null) {
                changes.add(String.format("Added %s %s %s", BooleanUtils.isTrue(exclusive.apply(right)) ? "exclusive" : "nonexclusive", description, getter.apply(right)));
            } else if (getter.apply(right) == null) {
                changes.add(String.format("Removed %s", description));
            } else {
                changes.add(String.format("Changed %s from '%s' -> '%s'", description, getter.apply(left), getter.apply(right)));
            }
        } else if(!Objects.equals(exclusive.apply(left), exclusive.apply(right))) {
            changes.add(String.format("Changed %s to %s", description, BooleanUtils.isTrue(exclusive.apply(right)) ? "exclusive" : "nonexlusive"));
        }
    }

    public static <PROP> void addChanges(String description, PROP left, PROP right, Function<PROP, Object> getter, List<String> changes) {
        if(!Objects.equals(getter.apply(left), getter.apply(right))) {
            if (getter.apply(left) == null) {
                changes.add(String.format("Added %s %s", description, getter.apply(right)));
            } else if (getter.apply(right) == null) {
                changes.add(String.format("Removed %s", description));
            } else {
                changes.add(String.format("Changed %s from '%s' -> '%s'", description, getter.apply(left), getter.apply(right)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> copyAndAdd(Set<T> set, T... add) {
        Set<T> newSet = new HashSet<T>(set);
        newSet.addAll(Arrays.asList(add));
        return newSet;
    }

    private List<String> enumValues(Property prop) {
        Property finalProp = prop;
        if(prop instanceof ArrayProperty) {
            finalProp = ((ArrayProperty) prop).getItems();
        }

        if (finalProp instanceof StringProperty && ((StringProperty) finalProp).getEnum() != null) {
            return ((StringProperty) finalProp).getEnum();
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
