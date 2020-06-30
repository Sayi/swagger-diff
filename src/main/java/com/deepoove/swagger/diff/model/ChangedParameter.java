package com.deepoove.swagger.diff.model;

import io.swagger.models.parameters.Parameter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChangedParameter implements Changed {

    private List<ElProperty> increased = new ArrayList<>();
    private List<ElProperty> missing = new ArrayList<>();
    private List<ElProperty> changed = new ArrayList<>();
    private List<ElProperty> typesChanges = new ArrayList<>();
    private List<ElProperty> requiredChanges = new ArrayList<>();

    private Parameter leftParameter;
    private Parameter rightParameter;

    private boolean isChangeRequired;
    private boolean isChangeDescription;
    private boolean isChangeType;

    @Override
    public boolean isDiff() {
        return isChangeRequired
                || isChangeDescription
                || !increased.isEmpty()
                || !missing.isEmpty()
                || isChangeType
                || !requiredChanges.isEmpty()
                || !typesChanges.isEmpty();
    }

    @Override
    public boolean isBackwardsCompatible() {
        boolean isBackwardsCompatible = !isChangeRequired
                && !isChangeType
                && missing.isEmpty()
                && requiredChanges.isEmpty()
                && typesChanges.isEmpty();
        for (ElProperty elProperty : increased) {
            if (elProperty.getProperty() != null && elProperty.getProperty().getRequired()) {
                return false;
            }
        }
        return isBackwardsCompatible;
    }
}
