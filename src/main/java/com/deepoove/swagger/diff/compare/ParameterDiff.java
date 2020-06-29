package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.deepoove.swagger.diff.model.ChangedParameter;

import io.swagger.models.Model;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

/**
 * compare two parameter
 * 
 * @author Sayi
 * @version
 */
public class ParameterDiff {

    private List<Parameter> increased;
    private List<Parameter> missing;
    private List<ChangedParameter> changed;

    Map<String, Model> oldDedinitions;
    Map<String, Model> newDedinitions;

    private ParameterDiff() {
        this.increased = new ArrayList<Parameter>();
        this.missing = new ArrayList<Parameter>();
        this.changed = new ArrayList<ChangedParameter>();
    }

    public static ParameterDiff buildWithDefinition(Map<String, Model> left,
            Map<String, Model> right) {
        ParameterDiff diff = new ParameterDiff();
        diff.oldDedinitions = left;
        diff.newDedinitions = right;
        return diff;
    }

    public ParameterDiff diff(List<Parameter> left, List<Parameter> right) {
        if (null == left) left = new ArrayList<>();
        if (null == right) right = new ArrayList<>();

        ListDiff<Parameter> paramDiff = ListDiff.diff(left, right, (t, param) -> {
            for (Parameter para : t) {
                if (param.getName().equals(para.getName())) { return para; }
            }
            return null;
        });
        this.increased.addAll(paramDiff.getIncreased());
        this.missing.addAll(paramDiff.getMissing());
        Map<Parameter, Parameter> shared = paramDiff.getShared();
        
        shared.forEach((leftPara, rightPara) -> {
            ChangedParameter changedParameter = new ChangedParameter();
            changedParameter.setLeftParameter(leftPara);
            changedParameter.setRightParameter(rightPara);

            if (leftPara instanceof BodyParameter && rightPara instanceof BodyParameter) {
                BodyParameter leftBodyPara = (BodyParameter) leftPara;
                Model leftSchema = leftBodyPara.getSchema();
                BodyParameter rightBodyPara = (BodyParameter) rightPara;
                Model rightSchema = rightBodyPara.getSchema();
                ModelDiff diff = ModelDiff.buildWithDefinition(oldDedinitions, newDedinitions)
                        .diff(leftSchema, rightSchema, leftPara.getName());
                changedParameter.setIncreased(diff.getIncreased());
                changedParameter.setMissing(diff.getMissing());
                changedParameter.setChanged(diff.getChanged());
            }

            // is requried
            boolean rightRequired = rightPara.getRequired();
            boolean leftRequired = leftPara.getRequired();
            changedParameter.setChangeRequired(leftRequired != rightRequired);

            // description
            String description = rightPara.getDescription();
            String oldPescription = leftPara.getDescription();
            if (StringUtils.isBlank(description)) description = "";
            if (StringUtils.isBlank(oldPescription)) oldPescription = "";
            changedParameter.setChangeDescription(!description.equals(oldPescription));

            if (changedParameter.isDiff()) {
                this.changed.add(changedParameter);
            }

        });

        return this;
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
