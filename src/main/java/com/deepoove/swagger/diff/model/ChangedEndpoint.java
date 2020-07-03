package com.deepoove.swagger.diff.model;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import lombok.Data;

import java.util.Map;

@Data
public class ChangedEndpoint implements Changed {

    private String pathUrl;

    private Map<HttpMethod, Operation> newOperations;
    private Map<HttpMethod, Operation> missingOperations;

    private Map<HttpMethod, ChangedOperation> changedOperations;

    @Override
    public boolean isDiff() {
//		newOperations.isEmpty()
//		|| !missingOperations.isEmpty()
//		||
        return !changedOperations.isEmpty();
    }

    @Override
    public boolean isBackwardsCompatible() {
        if (!missingOperations.isEmpty()) {
            return false;
        } else {
            for (ChangedOperation changedOperation : changedOperations.values()) {
                if (!changedOperation.isBackwardsCompatible()) {
                    return false;
                }
            }
        }
        return true;
    }

}
