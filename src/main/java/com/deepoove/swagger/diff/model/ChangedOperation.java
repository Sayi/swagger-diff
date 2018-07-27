package com.deepoove.swagger.diff.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.models.parameters.Parameter;

public class ChangedOperation extends ChangedExtensionGroup implements Changed {

	private String summary;

	private List<Parameter> addParameters = new ArrayList<Parameter>();
	private List<Parameter> missingParameters = new ArrayList<Parameter>();

	private List<ChangedParameter> changedParameter = new ArrayList<ChangedParameter>();

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

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean isDiff() {
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty() || !addProps.isEmpty()
				|| !missingProps.isEmpty() || vendorExtensionsAreDiff();
	}
	public boolean isDiffProp(){
		return !addProps.isEmpty()
				|| !missingProps.isEmpty()
				|| propVendorExtsAreDiff();
	}

	public boolean propVendorExtsAreDiff() {
		boolean accumulator = false;
		for (ElProperty prop : changedProps) {
			accumulator = accumulator || prop.vendorExtensionsAreDiff();
		}
		return accumulator;
	}

	public boolean isDiffParam(){
		return !addParameters.isEmpty() || !missingParameters.isEmpty()
				|| !changedParameter.isEmpty();
	}

}
