package com.deepoove.swagger.diff.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.deepoove.swagger.diff.model.ChangedParameter;

import io.swagger.models.Model;
import io.swagger.models.RefModel;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import lombok.Data;

/**
 * compare two parameter
 * @author Sayi
 * @version
 */
@Data
public class ParameterDiff {

    private List<Parameter> increased;
    private List<Parameter> missing;
    private List<ChangedParameter> changed;

    Map<String, Model> oldDedinitions;
    Map<String, Model> newDedinitions;

    private ParameterDiff(){}

    public static ParameterDiff buildWithDefinition(final Map<String, Model> left,
            final Map<String, Model> right) {
        ParameterDiff diff = new ParameterDiff();
        diff.oldDedinitions = left;
        diff.newDedinitions = right;
        return diff;
    }

    public ParameterDiff diff(List<Parameter> left,
            List<Parameter> right) {
        ParameterDiff instance = new ParameterDiff();
        if (null == left) {
            left = new ArrayList<Parameter>();
        }
        if (null == right) {
            right = new ArrayList<Parameter>();
        }

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

                if (leftPara instanceof BodyParameter && rightPara instanceof BodyParameter){
                    BodyParameter leftBodyPara = (BodyParameter)leftPara;
                    Model leftSchema = leftBodyPara.getSchema();
                    BodyParameter rightBodyPara = (BodyParameter)rightPara;
                    Model rightSchema = rightBodyPara.getSchema();
                    if (leftSchema instanceof RefModel && rightSchema instanceof RefModel){
                        String leftRef = ((RefModel) leftSchema).getSimpleRef();
                        String rightRef = ((RefModel) rightSchema).getSimpleRef();
                        Model leftModel = oldDedinitions.get(leftRef);
                        Model rightModel = newDedinitions.get(rightRef);
                        ModelDiff diff = ModelDiff.buildWithDefinition(oldDedinitions, newDedinitions).diff(leftModel, rightModel, name);
                        changedParameter.setIncreased(diff.getIncreased());
                        changedParameter.setMissing(diff.getMissing());
                        changedParameter.setRequiredChanges(diff.getRequiredChanges());
                        changedParameter.setTypesChanges(diff.getTypeChanges());
                    }
                }


                //is required
                boolean rightRequired = rightPara.getRequired();
                boolean leftRequired = leftPara.getRequired();
                changedParameter.setChangeRequired(leftRequired != rightRequired && rightRequired);

                // TODO JLA
//                //is change type
//                if(rightPara.getName().contains("username")){
//                    SerializableParameter s =  (SerializableParameter) rightPara;
//                    System.out.println(rightPara.getDescription());
//                    System.out.println(s.getName());
//                    System.out.println(s.getIn());
//                    System.out.println(s.getType());
//                }
//                if(leftPara.getName().contains("username")){
//                    SerializableParameter s =  (SerializableParameter) leftPara;
//                    System.out.println(leftPara.getDescription());
//                    System.out.println(s.getName());
//                    System.out.println(s.getIn());
//                    System.out.println(s.getType());
//                }

                //description
                String description = rightPara.getDescription();
                String oldPescription = leftPara.getDescription();
                if (StringUtils.isBlank(description)) {
                    description = "";
                }
                if (StringUtils.isBlank(oldPescription)) {
                    oldPescription = "";
                }
                changedParameter.setChangeDescription(!description.equals(oldPescription));

                if (changedParameter.isDiff()){
                    instance.changed.add(changedParameter);
                }

            }

        }
        return instance;
    }

    private static int index(final List<Parameter> right, final String name) {
        int i = 0;
        for (; i < right.size(); i++){
            Parameter para = right.get(i);
            if (name.equals(para.getName())){
                return i;
            }
        }
        return -1;
    }


}
