package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.deepoove.swagger.diff.model.ElProperty;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class RefModelPropertiesDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> changed;

    Map<String, Model> novaDefinitions;

    private RefModelPropertiesDiff() {
        increased = new ArrayList<>();
        missing = new ArrayList<>();
        changed = new ArrayList<>();
    }

    public static RefModelPropertiesDiff buildWithDefinition(Map<String, Model> novaDefinitions) {
        RefModelPropertiesDiff diff = new RefModelPropertiesDiff();
        diff.novaDefinitions = novaDefinitions;
        return diff;
    }

    public RefModelPropertiesDiff diff(Property ezModel, Property novaModel) {
        return this.diff(ezModel, novaModel, null, new HashSet<>());
    }

    public RefModelPropertiesDiff diff(Property ezModel, Property novaModel, String parentEl) {
        return this.diff(ezModel, novaModel, parentEl, new HashSet<>());
    }

    private RefModelPropertiesDiff diff(Property ezModel, Property novaModel, String parentEl, Set<Property> visited) {
        // Stop recursing if both models are null
        // OR either model is already contained in the visiting history
        if ((null == ezModel && null == novaModel) || visited.contains(ezModel) || visited.contains(novaModel)) {
            return this;
        }
        Map<String, Property> ezProperties = unwrapEZProperties(ezModel);
        Map<String, Property> novaProperties = unwrapNovaProperties(novaModel);

        // remove odd props
        preprocessProps(ezProperties, novaProperties);

        // Diff the properties
        return diff(ezModel, novaModel, parentEl, visited, ezProperties, novaProperties);
    }

    public RefModelPropertiesDiff diff(Map<String, Property> ezProperties, Map<String, Property> novaProperties) {
        return diff(null, null, null, new HashSet<>(), ezProperties, novaProperties);
    }

    private RefModelPropertiesDiff diff(Property ezModel, Property novaModel, String parentEl, Set<Property> visited, Map<String, Property> ezProperties, Map<String, Property> novaProperties) {
        MapKeyDiff<String, Property> propertyDiff = MapKeyDiff.diff(ezProperties, novaProperties);

        increased.addAll(convert2ElPropertys(propertyDiff.getIncreased(), parentEl));
        missing.addAll(convert2ElPropertys(propertyDiff.getMissing(), parentEl));

        // Recursively find the diff between properties
        List<String> sharedKey = propertyDiff.getSharedKey();
        sharedKey.stream().forEach((key) -> {
            Property ez = ezProperties.get(key);
            Property nova = novaProperties.get(key);

            if (nova instanceof RefProperty) {
                diff(ez, nova,
                        buildElString(parentEl, key),
                        copyAndAdd(visited, ezModel, novaModel));
            }
            else if (nova instanceof ArrayProperty) {//hack because found in set
                diff(ez, nova,
                        buildElString(parentEl, key),
                        new HashSet<>());
            }
            else if (ez != null && nova != null && !ez.getType().equals(nova.getType())) {
                // for now comparing only type
                changed.add(convert2ElProperty(key, parentEl, ez));
            }
        });
        return this;
    }

    private Map<String, Property> unwrapNovaProperties(Property novaModel) {
        if (novaModel instanceof RefProperty) {
            return novaDefinitions.get(((RefProperty) novaModel).getSimpleRef()).getProperties();
        }
        if (novaModel instanceof ArrayProperty) {
            Property items = ((ArrayProperty) novaModel).getItems();
            if (items instanceof RefProperty) {
                return novaDefinitions.get(((RefProperty) items).getSimpleRef()).getProperties();
            }
        }
        return null;
    }

    private void preprocessProps(Map<String, Property> ezProperties, Map<String, Property> novaProperties) {
        if (novaProperties != null) {
            novaProperties.remove("self");
        }
        if (ezProperties != null) {
            ezProperties.remove("_messages");
            ezProperties.remove("_links");
            ObjectProperty embedded = (ObjectProperty) ezProperties.remove("_embedded");
            if (embedded != null) {
                ezProperties.put("entities", embedded.getProperties().get("entities"));
            }
        }
    }

    private Map<String, Property> unwrapEZProperties(Property ezModel) {
        if (ezModel instanceof ObjectProperty) {
            return ((ObjectProperty) ezModel).getProperties();
        }
        if (ezModel instanceof ArrayProperty) {
            Property items = ((ArrayProperty) ezModel).getItems();
            if (items instanceof ObjectProperty) {
                return ((ObjectProperty) items).getProperties();
            }
        }
        return null;
    }

    static Collection<? extends ElProperty> convert2ElPropertys(
            Map<String, Property> propMap, String parentEl) {

        List<ElProperty> result = new ArrayList<ElProperty>();
        if (null == propMap) return result;

        for (Map.Entry<String, Property> entry : propMap.entrySet()) {
            // TODO Recursively get the properties
            result.add(convert2ElProperty(entry.getKey(), parentEl, entry.getValue()));
        }
        return result;
    }

    private static String buildElString(String parentEl, String propName) {
        return null == parentEl ? propName : (parentEl + "." + propName);
    }

    private static ElProperty convert2ElProperty(String propName, String parentEl, Property property) {
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
