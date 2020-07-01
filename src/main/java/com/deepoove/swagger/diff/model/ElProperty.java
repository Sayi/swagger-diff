package com.deepoove.swagger.diff.model;

import io.swagger.models.properties.Property;
import lombok.Data;

/**
 * property with expression Language grammar
 *
 * @author Sayi
 * @version
 */
@Data
public class ElProperty {

    private String el;

    private Property property;

    // optional change metadata
    private boolean isTypeChange;
    private boolean newEnums;
    private boolean removedEnums;
    private boolean isBecomeRequired;

}
