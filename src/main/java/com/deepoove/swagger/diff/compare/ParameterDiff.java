package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.deepoove.swagger.diff.model.ChangedParameter;

import io.swagger.models.parameters.Parameter;

public class ParameterDiff {

	private List<Parameter> increased;
	private List<Parameter> missing;
	private List<ChangedParameter> changed;
	
	private ParameterDiff(){}

	public static ParameterDiff diff(List<Parameter> left,
			List<Parameter> right) {
		ParameterDiff instance = new ParameterDiff();
		if (null == left) left = new ArrayList<Parameter>();
		if (null == right) right = new ArrayList<Parameter>();
		
		instance.increased = new ArrayList<Parameter>(right);
		instance.missing = new ArrayList<Parameter>();
		instance.changed = new ArrayList<ChangedParameter>();
		for (Parameter leftPara : left){
			String name = leftPara.getName();
			int index = index(instance.increased, name);
			if (-1 == index){
				instance.missing.add(leftPara);
			}else{
				Parameter rightPara = instance.increased.get(index);
				instance.increased.remove(index);
				
				ChangedParameter changedParameter = new ChangedParameter();
				changedParameter.setLeftParameter(leftPara);
				changedParameter.setRightParameter(rightPara);
				//is requried
				boolean rightRequired = rightPara.getRequired();
				boolean leftRequired = leftPara.getRequired();
				changedParameter.setChangeRequired(leftRequired != rightRequired);
				
				//description
				String description = rightPara.getDescription();
				String oldPescription = leftPara.getDescription();
				if (StringUtils.isBlank(description)) description = "";
				if (StringUtils.isBlank(oldPescription)) oldPescription = "";
				changedParameter.setChangeDescription(!description.equals(oldPescription));
				
				if (changedParameter.isDiff()){
					instance.changed.add(changedParameter);
				}
				
			}
			
		}
		return instance;
	}

	private static int index(List<Parameter> right, String name) {
		int i = 0;
		for (; i < right.size(); i++){
			Parameter para = right.get(i);
			if (name.equals(para.getName())){
				return i;
			} 
		}
		return -1;
	}

	public List<Parameter> getIncreased() {
		return increased;
	}

	public void setIncreased(List<Parameter> increased) {
		this.increased = increased;
	}

	public List<Parameter> getMissing() {
		return missing;
	}

	public void setMissing(List<Parameter> missing) {
		this.missing = missing;
	}

	public List<ChangedParameter> getChanged() {
		return changed;
	}

	public void setChanged(List<ChangedParameter> changed) {
		this.changed = changed;
	}

}
