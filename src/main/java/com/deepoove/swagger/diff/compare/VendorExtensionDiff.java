package com.deepoove.swagger.diff.compare;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.deepoove.swagger.diff.model.ChangedExtensionGroup;

import io.swagger.models.Info;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;

public class VendorExtensionDiff {

  private boolean withExts;

  public VendorExtensionDiff(boolean withExts) {
    this.withExts = withExts;
  }

  public ChangedExtensionGroup diff(Parameter left, Parameter right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Operation left, Operation right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Swagger left, Swagger right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Info left, Info right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Path left, Path right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Response left, Response right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(Tag left , Tag right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  public ChangedExtensionGroup diff(SecuritySchemeDefinition left, SecuritySchemeDefinition right) {
    return diff(left.getVendorExtensions(), right.getVendorExtensions());
  }

  private ChangedExtensionGroup diff(Map<String, Object> oldExts, Map<String, Object> newExts) {
    ChangedExtensionGroup group = new ChangedExtensionGroup();
    if (withExts) {
      MapDiff<String, Object> mapDiff = MapDiff.diff(oldExts, newExts);
      group.setMissingVendorExtensions(mapDiff.getMissing());
      group.setChangedVendorExtensions(mapDiff.getChanged());
      group.setIncreasedVendorExtensions(mapDiff.getIncreased());
    }
    return group;
  }

  public ChangedExtensionGroup diffTagGroup(Map<String, Tag> left, Map<String, Tag> right) {
    MapDiff<String, Tag> responseDiff = MapDiff.diff(left, right);
    ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
    for (Entry<String, Pair<Tag, Tag>> entry : responseDiff.getChanged().entrySet()) {
      String code = entry.getKey();
      Tag oldVal = entry.getValue().getLeft();
      Tag newVal = entry.getValue().getRight();
      responseGroup.putSubGroup(code, diff(oldVal, newVal));
    }
    return responseGroup;
  }

  public ChangedExtensionGroup diffSecGroup(Map<String, SecuritySchemeDefinition> left, Map<String, SecuritySchemeDefinition> right) {
    MapDiff<String, SecuritySchemeDefinition> responseDiff = MapDiff.diff(left, right);
    ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
    for (Entry<String, Pair<SecuritySchemeDefinition, SecuritySchemeDefinition>> entry : responseDiff.getChanged().entrySet()) {
      String code = entry.getKey();
      SecuritySchemeDefinition oldVal = entry.getValue().getLeft();
      SecuritySchemeDefinition newVal = entry.getValue().getRight();
      responseGroup.putSubGroup(code, diff(oldVal, newVal));
    }
    return responseGroup;
  }

  public ChangedExtensionGroup diffResGroup(Map<String, Response> left, Map<String, Response> right) {
    MapDiff<String, Response> responseDiff = MapDiff.diff(left, right);
    ChangedExtensionGroup responseGroup = new ChangedExtensionGroup();
    for (Entry<String, Pair<Response, Response>> entry : responseDiff.getChanged().entrySet()) {
      String code = entry.getKey();
      Response oldVal = entry.getValue().getLeft();
      Response newVal = entry.getValue().getRight();
      responseGroup.putSubGroup(code, diff(oldVal, newVal));
    }
    return responseGroup;
  }
}
