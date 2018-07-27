package com.deepoove.swagger.diff.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

public class ChangedExtensionGroup {
  protected Map<String, Object> increasedVendorExtensions = new LinkedHashMap<String, Object>();
  protected Map<String, Object> missingVendorExtensions = new LinkedHashMap<String, Object>();
  protected Map<String, Pair<Object, Object>> changedVendorExtensions = new LinkedHashMap<String, Pair<Object, Object>>();
  protected Map<String, ChangedExtensionGroup> changedSubGroups = new LinkedHashMap<String, ChangedExtensionGroup>();

  public boolean vendorExtensionsAreDiffShallow() {
    return !(increasedVendorExtensions.isEmpty()
        && changedVendorExtensions.isEmpty()
        && missingVendorExtensions.isEmpty());
  }

  public boolean vendorExtensionsAreDiff() {
    return vendorExtensionsAreDiffShallow()
        || subVendorExtensionsAreDiff();
  }

  private boolean subVendorExtensionsAreDiff() {
    boolean accumulator = false;
    for (ChangedExtensionGroup subgroup : changedSubGroups.values()) {
      accumulator = accumulator || subgroup.vendorExtensionsAreDiff();
    }
    return accumulator;
  }

  public Map<String, Object> getIncreasedVendorExtensions() {
    return increasedVendorExtensions;
  }

  public void setIncreasedVendorExtensions(Map<String, Object> increasedVendorExtensions) {
    this.increasedVendorExtensions = increasedVendorExtensions;
  }

  public Map<String, Object> getMissingVendorExtensions() {
    return missingVendorExtensions;
  }

  public void setMissingVendorExtensions(Map<String, Object> missingVendorExtensions) {
    this.missingVendorExtensions = missingVendorExtensions;
  }

  public Map<String, Pair<Object, Object>> getChangedVendorExtensions() {
    return changedVendorExtensions;
  }

  public void setChangedVendorExtensions(Map<String, Pair<Object, Object>> changedVendorExtensions) {
    this.changedVendorExtensions = changedVendorExtensions;
  }

  public Map<String, ChangedExtensionGroup> getChangedSubGroups() {
    return changedSubGroups;
  }

  public boolean hasSubGroup(String key) {
    return changedSubGroups.containsKey(key);
  }

  public ChangedExtensionGroup getSubGroup(String key) {
    return changedSubGroups.get(key);
  }

  public void putSubGroup(String key, ChangedExtensionGroup group) {
    changedSubGroups.put(key, group);
  }

  public void setVendorExtsFromGroup(ChangedExtensionGroup newDiffs) {
    this.increasedVendorExtensions = newDiffs.getIncreasedVendorExtensions();
    this.missingVendorExtensions = newDiffs.getMissingVendorExtensions();
    this.changedVendorExtensions = newDiffs.getChangedVendorExtensions();
  }
}
