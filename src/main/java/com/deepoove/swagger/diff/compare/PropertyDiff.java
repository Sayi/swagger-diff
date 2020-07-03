package com.deepoove.swagger.diff.compare;

import com.deepoove.swagger.diff.model.ElProperty;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PropertyDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> changed;

    Map<String, Model> oldDefinitions;
    Map<String, Model> newDefinitions;

    private PropertyDiff() {
        increased = new ArrayList<>();
        missing = new ArrayList<>();
        changed = new ArrayList<>();
    }

    public static PropertyDiff buildWithDefinition(Map<String, Model> left, Map<String, Model> right) {
        PropertyDiff diff = new PropertyDiff();
        diff.oldDefinitions = left;
        diff.newDefinitions = right;
        return diff;
    }

    public PropertyDiff diff(Property left, Property right) {
        ModelDiff diff = ModelDiff.buildWithDefinition(oldDefinitions, newDefinitions).diff(left, right);
        increased.addAll(diff.getIncreased());
        missing.addAll(diff.getMissing());
        changed.addAll(diff.getChanged());
        return this;
    }
}
