package com.deepoove.swagger.diff.model;

import io.swagger.models.parameters.Parameter;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChangedOperation implements Changed {

    private String summary;

    private List<Parameter> addParameters = new ArrayList<Parameter>();
    private List<Parameter> missingParameters = new ArrayList<Parameter>();

    private List<ChangedParameter> changedParameter = new ArrayList<ChangedParameter>();

    private List<ElProperty> addProps = new ArrayList<ElProperty>();
    private List<ElProperty> missingProps = new ArrayList<ElProperty>();
    private List<ElProperty> changedProps = new ArrayList<ElProperty>();
    private List<String> addConsumes = new ArrayList<>();
    private List<String> missingConsumes = new ArrayList<>();
    private List<String> addProduces = new ArrayList<>();
    private List<String> missingProduces = new ArrayList<>();

    public boolean isDiff() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty() || !changedParameter.isEmpty() || isDiffProp()
                || isDiffConsumes() || isDiffProduces();
    }

    @Override
    public boolean isBackwardsCompatible() {
        if (!missingProps.isEmpty() || !missingParameters.isEmpty() || !changedProps.isEmpty()
                || isDiffConsumes() || isDiffProduces()) {
            return false;
        } else {
            for (ChangedParameter changedParameter : getChangedParameter()) {
                if (!changedParameter.isBackwardsCompatible()) {
                    return false;
                }
            }
            for (Parameter parameter : addParameters) {
                if (parameter.getRequired()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isDiffProp() {
        return !addProps.isEmpty() || !missingProps.isEmpty() || !changedProps.isEmpty();
    }

    public boolean isDiffParam() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty() || !changedParameter.isEmpty();
    }

    public boolean isDiffConsumes() {
        return !addConsumes.isEmpty() || !missingConsumes.isEmpty();
    }

    public boolean isDiffProduces() {
        return !addProduces.isEmpty() || !missingProduces.isEmpty();
    }

    public void setAddConsumes(List<String> increased) {
        this.addConsumes = increased == null ? new ArrayList<>() : increased;
    }


    public void setMissingConsumes(List<String> missing) {
        this.missingConsumes = missing == null ? new ArrayList<>() : missing;
    }


    public void setAddProduces(List<String> increased) {
        this.addProduces = increased == null ? new ArrayList<>() : increased;
    }


    public void setMissingProduces(List<String> missing) {
        this.missingProduces = missing == null ? new ArrayList<>() : missing;
    }
}
