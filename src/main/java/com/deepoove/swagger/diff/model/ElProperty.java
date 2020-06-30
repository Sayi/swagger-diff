package com.deepoove.swagger.diff.model;

import io.swagger.models.properties.Property;
import lombok.Data;

/**
 * property with expression Language grammar
 *
 * @author Sayi
 */
@Data
public class ElProperty {

    private String el;

    private Property property;

}
