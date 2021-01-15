package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.parameters.Parameter;

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

    private String oldResponseType;
    private String newResponseType;

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

    public List<ChangedParameter> getChangedParameter() {
        return changedParameter;
    }

    public void setChangedParameter(List<ChangedParameter> changedParameter) {
        this.changedParameter = changedParameter;
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

    public void setSummary(String summary) { this.summary = summary; }

    public void setOldResponseType(String oldResponseType) { this.oldResponseType = oldResponseType; }

    public String getOldResponseType() { return oldResponseType; }

    public void setNewResponseType(String newResponseType) { this.newResponseType = newResponseType; }

    public String getNewResponseType() { return newResponseType; }

    public boolean isDiff() {
        return !addParameters.isEmpty() || !missingParameters.isEmpty() || !changedParameter.isEmpty() || isDiffProp()
                || isDiffConsumes() || isDiffProduces() || isDiffResponseType();
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

    public List<String> getAddConsumes() {
        return this.addConsumes;
    }

    public void setAddConsumes(List<String> increased) {
        this.addConsumes = increased == null ? new ArrayList<>() : increased;
    }

    public List<String> getMissingConsumes() {
        return this.missingConsumes;
    }

    public void setMissingConsumes(List<String> missing) {
        this.missingConsumes = missing == null ? new ArrayList<>() : missing;
    }

    public List<String> getAddProduces() {
        return this.addProduces;
    }

    public void setAddProduces(List<String> increased) {
        this.addProduces = increased == null ? new ArrayList<>() : increased;
    }

    public List<String> getMissingProduces() {
        return this.missingProduces;
    }

    public void setMissingProduces(List<String> missing) {
        this.missingProduces = missing == null ? new ArrayList<>() : missing;
    }

    public Boolean isDiffResponseType() { return this.oldResponseType != this.newResponseType; }
}
