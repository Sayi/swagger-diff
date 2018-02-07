package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.parameters.Parameter;
import lombok.Data;

@Data
public class ChangedOperation implements Changed {

    private String summary;

    private List<Parameter> addParameters = new ArrayList<Parameter>();
    private List<Parameter> missingParameters = new ArrayList<Parameter>();
    private List<ChangedParameter> changedParameters = new ArrayList<ChangedParameter>();

    private List<ElProperty> addProps = new ArrayList<ElProperty>();
    private List<ElProperty> missingProps = new ArrayList<ElProperty>();
    private List<ElProperty> changedProps = new ArrayList<ElProperty>();

    @Override
    public boolean isDiff() {
        return isDiffProp() || isDiffParam();
    }
    public boolean isDiffProp(){
        return !addProps.isEmpty()
                || !missingProps.isEmpty()
                || !changedProps.isEmpty();
    }
    public boolean isDiffParam(){
        return !addParameters.isEmpty() || !missingParameters.isEmpty()
                || !changedParameters.isEmpty();
    }

    @Override
    public boolean isBackwardsCompatible() {
        if(!missingProps.isEmpty() || !missingParameters.isEmpty() || !changedProps.isEmpty()) {
            return false;
        } else {
            for (ChangedParameter changedParameter : getChangedParameters()) {
                if(!changedParameter.isBackwardsCompatible()) {
                    return false;
                }
            }
            for (Parameter parameter : addParameters) {
                if(parameter.getRequired()) {
                    return false;
                }
            }
        }
        return true;
    }

}
