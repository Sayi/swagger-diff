package com.deepoove.swagger.diff.model;

import io.swagger.models.properties.Property;

import java.util.List;

/**
 * property with expression Language grammar
 * 
 * @author Sayi
 * @version
 */
public class ElProperty {

    private String el;

    private Property property;

    // optional change metadata
    private String newTypeChange;
    private List<String> newEnums;
    private List<String> removedEnums;

    private String metadataChanged;

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public String getEl() {
        return el;
    }

    public void setEl(String el) {
        this.el = el;
    }

    public String getNewTypeChange() {
        return newTypeChange;
    }

    public void setNewTypeChange(String typeChange) {
        newTypeChange = newTypeChange;
    }

    public List<String> getNewEnums() {
        return newEnums;
    }

    public void setNewEnums(List<String> newEnums) {
        this.newEnums = newEnums;
    }

    public void metadataChanged(String changes) {
        this.metadataChanged = changes;
    }

    public String metadataChanged() {
        return this.metadataChanged;
    }

    public List<String> getRemovedEnums() {
        return removedEnums;
    }

    public void setRemovedEnums(List<String> removedEnums) {
        this.removedEnums = removedEnums;
    }
}
