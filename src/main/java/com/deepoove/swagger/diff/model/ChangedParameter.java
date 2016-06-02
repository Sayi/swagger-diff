package com.deepoove.swagger.diff.model;

import io.swagger.models.parameters.Parameter;

public class ChangedParameter implements Changed {

	private Parameter leftParameter;
	private Parameter rightParameter;

	private boolean isChangeRequired;
	// private boolean isChangeType;
	private boolean isChangeDescription;

	public boolean isChangeRequired() {
		return isChangeRequired;
	}

	public void setChangeRequired(boolean isChangeRequired) {
		this.isChangeRequired = isChangeRequired;
	}

	public boolean isChangeDescription() {
		return isChangeDescription;
	}

	public void setChangeDescription(boolean isChangeDescription) {
		this.isChangeDescription = isChangeDescription;
	}

	public Parameter getLeftParameter() {
		return leftParameter;
	}

	public void setLeftParameter(Parameter leftParameter) {
		this.leftParameter = leftParameter;
	}

	public Parameter getRightParameter() {
		return rightParameter;
	}

	public void setRightParameter(Parameter rightParameter) {
		this.rightParameter = rightParameter;
	}

	public boolean isDiff() {
		return isChangeRequired || isChangeDescription;
	}

}
