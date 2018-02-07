package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.parameters.Parameter;
import lombok.Data;

@Data
public class ChangedParameter implements Changed {

    private List<ElProperty> increased = new ArrayList<ElProperty>();
    private List<ElProperty> missing = new ArrayList<ElProperty>();
    private List<ElProperty> requiredChanges = new ArrayList<ElProperty>();
    private List<ElProperty> typesChanges = new ArrayList<ElProperty>();

    private Parameter leftParameter;
    private Parameter rightParameter;

    private boolean isChangeRequired;
//    private boolean isChangeType; // TODO JLA hard ?
    private boolean isChangeDescription;

    @Override
    public boolean isDiff() {
        return isChangeRequired
                || isChangeDescription
                || !increased.isEmpty()
                || !missing.isEmpty()
                || !requiredChanges.isEmpty()
                || !typesChanges.isEmpty();
    }

    @Override
    public boolean isBackwardsCompatible() {
        boolean isBackwardsCompatible = !isChangeRequired
                && missing.isEmpty()
                && requiredChanges.isEmpty()
                && typesChanges.isEmpty();
        for (ElProperty elProperty : increased) {
            if(elProperty.getProperty() != null && elProperty.getProperty().getRequired()) {
                return false;
            }
        }
        return isBackwardsCompatible;
    }

}