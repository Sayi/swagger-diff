package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.deepoove.swagger.diff.model.ElProperty;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;

public class PropertyDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> changed;

    Map<String, Model> oldDedinitions;
    Map<String, Model> newDedinitions;

    private PropertyDiff() {
        increased = new ArrayList<ElProperty>();
        missing = new ArrayList<ElProperty>();
        changed = new ArrayList<ElProperty>();
    }

    public static PropertyDiff buildWithDefinition(Map<String, Model> left,
                                                   Map<String, Model> right) {
        PropertyDiff diff = new PropertyDiff();
        diff.oldDedinitions = left;
        diff.newDedinitions = right;
        return diff;
    }

    public PropertyDiff diff(Property left, Property right) {
        if ((null == left || isRef(left)) && (null == right || isRef(right))) {
            Model leftModel = null == left ? null : oldDedinitions.get(getRefName(left));
            Model rightModel = null == right ? null : newDedinitions.get(getRefName(right));
            ModelDiff diff = ModelDiff
                    .buildWithDefinition(oldDedinitions, newDedinitions)
                    .diff(leftModel, rightModel);
            increased.addAll(diff.getIncreased());
            missing.addAll(diff.getMissing());
            changed.addAll(diff.getChanged());
        }
        return this;
    }

    private boolean isRef(Property property) {
        return (property instanceof ArrayProperty && ((ArrayProperty) property).getItems() instanceof RefProperty);
    }

    private String getRefName(Property property) {
        return (isRef(property)) ? ((RefProperty) ((ArrayProperty) property).getItems()).getSimpleRef() : null;
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
