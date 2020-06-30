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

    private List<ChangedParameter> changedParameters = new ArrayList<ChangedParameter>();

    private List<ElProperty> addProps = new ArrayList<ElProperty>();

    private List<ElProperty> missingProps = new ArrayList<ElProperty>();

    private List<ElProperty> changedProps = new ArrayList<ElProperty>();

    public List<Parameter> getAddParameters() {
        return addParameters;
    }

    public void setAddParameters(List<Parameter> addParameters) {
        this.addParameters = addParameters;
    }

    public List<Parameter> getMissingParameters() {
        return missingParameters;
    }

    public void setMissingParameters(List<Parameter> missingParameters) {
        this.missingParameters = missingParameters;
    }

    public List<ChangedParameter> getChangedParameters() {
        return changedParameters;
    }

    public void setChangedParameters(List<ChangedParameter> changedParameters) {
        this.changedParameters = changedParameters;
    }

    public List<ElProperty> getAddProps() {
        return addProps;
    }

    public void setAddProps(List<ElProperty> addProps) {
        this.addProps = addProps;
    }

    public List<ElProperty> getMissingProps() {
        return missingProps;
    }

    public void setMissingProps(List<ElProperty> missingProps) {
        this.missingProps = missingProps;
    }

    public List<ElProperty> getChangedProps() {
        return changedProps;
    }

    public void setChangedProps(List<ElProperty> changedProps) {
        this.changedProps = changedProps;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isDiff() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty()
                || !changedParameters.isEmpty() || isDiffProp();
    }

    public boolean isDiffProp() {
        return !addProps.isEmpty()
                || !missingProps.isEmpty()
                || !changedProps.isEmpty();
    }

    public boolean isDiffParam() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty()
                || !changedParameters.isEmpty();
    }

    @Override
    public boolean isBackwardsCompatible() {
        if (!missingProps.isEmpty() || !missingParameters.isEmpty() || !changedProps.isEmpty()) {
            return false;
        } else {
            for (ChangedParameter changedParameter : getChangedParameters()) {
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
}
