package com.deepoove.swagger.diff.model;

import io.swagger.models.properties.Property;

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
    private boolean isTypeChange;
    private boolean newEnums;
    private boolean removedEnums;

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

    public boolean isTypeChange() {
        return isTypeChange;
    }

    public void setTypeChange(boolean typeChange) {
        isTypeChange = typeChange;
    }

    public boolean isNewEnums() {
        return newEnums;
    }

    public void setNewEnums(boolean newEnums) {
        this.newEnums = newEnums;
    }

    public boolean isRemovedEnums() {
        return removedEnums;
    }

    public void setRemovedEnums(boolean removedEnums) {
        this.removedEnums = removedEnums;
    }
}
