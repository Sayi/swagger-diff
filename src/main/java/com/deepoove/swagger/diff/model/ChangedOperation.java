package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.parameters.Parameter;

public class ChangedOperation implements Changed {

	private String summary;

	private List<Parameter> addParameters = new ArrayList<Parameter>();
	private List<Parameter> missingParameters = new ArrayList<Parameter>();

	private List<ChangedParameter> changedParameter = new ArrayList<ChangedParameter>();

	private List<ResponseProperty> addProps = new ArrayList<ResponseProperty>();
	private List<ResponseProperty> missingProps = new ArrayList<ResponseProperty>();

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

	public List<ResponseProperty> getAddProps() {
		return addProps;
	}

	public void setAddProps(List<ResponseProperty> addProps) {
		this.addProps = addProps;
	}

	public List<ResponseProperty> getMissingProps() {
		return missingProps;
	}

	public void setMissingProps(List<ResponseProperty> missingProps) {
		this.missingProps = missingProps;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean isDiff() {
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty() || !addProps.isEmpty()
				|| !missingProps.isEmpty();
	}
	public boolean isDiffProp(){
		return !addProps.isEmpty()
				|| !missingProps.isEmpty();
	}
	public boolean isDiffParam(){
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty();
	}

}
