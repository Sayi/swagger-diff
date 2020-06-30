package com.deepoove.swagger.diff.model;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

import java.util.Map;

public class ChangedEndpoint implements Changed {

    private String pathUrl;

    private Map<HttpMethod, Operation> newOperations;
    private Map<HttpMethod, Operation> missingOperations;

    private Map<HttpMethod, ChangedOperation> changedOperations;

    public Map<HttpMethod, Operation> getNewOperations() {
        return newOperations;
    }

    public void setNewOperations(final Map<HttpMethod, Operation> newOperations) {
        this.newOperations = newOperations;
    }

    public Map<HttpMethod, Operation> getMissingOperations() {
        return missingOperations;
    }

    public void setMissingOperations(
            final Map<HttpMethod, Operation> missingOperations) {
        this.missingOperations = missingOperations;
    }


    public Map<HttpMethod, ChangedOperation> getChangedOperations() {
        return changedOperations;
    }

    public void setChangedOperations(
            final Map<HttpMethod, ChangedOperation> changedOperations) {
        this.changedOperations = changedOperations;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public void setPathUrl(final String pathUrl) {
        this.pathUrl = pathUrl;
    }

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
