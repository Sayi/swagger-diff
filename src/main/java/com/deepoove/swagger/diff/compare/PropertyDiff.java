package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.deepoove.swagger.diff.model.ElProperty;

import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import lombok.Data;

@Data
public class PropertyDiff {

    private List<ElProperty> increased;
    private List<ElProperty> missing;
    private List<ElProperty> typeChanges;

    Map<String, Model> oldDedinitions;
    Map<String, Model> newDedinitions;

    private PropertyDiff() {
        increased = new ArrayList<ElProperty>();
        missing = new ArrayList<ElProperty>();
        typeChanges = new ArrayList<ElProperty>();
    }

    public static PropertyDiff buildWithDefinition(final Map<String, Model> left,
            final Map<String, Model> right) {
        PropertyDiff diff = new PropertyDiff();
        diff.oldDedinitions = left;
        diff.newDedinitions = right;
        return diff;
    }

    public PropertyDiff diff(final Property left, final Property right) {
        if ((null == left || left instanceof RefProperty) && (null == right || right instanceof RefProperty)) {
            Model leftModel = null == left ? null : oldDedinitions.get(((RefProperty) left).getSimpleRef());
            Model rightModel = null == right ? null : newDedinitions.get(((RefProperty) right).getSimpleRef());
            ModelDiff diff = ModelDiff
                    .buildWithDefinition(oldDedinitions, newDedinitions)
                    .diff(leftModel, rightModel);
            increased.addAll(diff.getIncreased());
            missing.addAll(diff.getMissing());
            typeChanges.addAll(diff.getTypeChanges());
        }
        return this;
    }

}
