package com.deepoove.swagger.diff.model;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.deepoove.swagger.diff.compare.MapDiff;

public class ChangedVendorExtensionGroup {
  protected Map<String, Object> increasedVendorExtensions = new LinkedHashMap<String, Object>();
  protected Map<String, Object> missingVendorExtensions = new LinkedHashMap<String, Object>();
  protected Map<String, Pair<Object, Object>> changedVendorExtensions = new LinkedHashMap<String, Pair<Object, Object>>();
  protected Map<String, ChangedVendorExtensionGroup> changedSubGroups = new LinkedHashMap<String, ChangedVendorExtensionGroup>();

  public boolean vendorExtensionsAreDiff() {
    return !increasedVendorExtensions.isEmpty()
        || !changedVendorExtensions.isEmpty()
        || !missingVendorExtensions.isEmpty()
        || subVendorExtensionsAreDiff();
  }

  private boolean subVendorExtensionsAreDiff() {
    boolean accumulator = false;
    for (ChangedVendorExtensionGroup subgroup : changedSubGroups.values()) {
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

  public Map<String, ChangedVendorExtensionGroup> getChangedSubGroups() {
    return changedSubGroups;
  }

  public void setVendorExtsFromGroup(ChangedVendorExtensionGroup newDiffs) {
    this.increasedVendorExtensions = newDiffs.getIncreasedVendorExtensions();
    this.missingVendorExtensions = newDiffs.getMissingVendorExtensions();
    this.changedVendorExtensions = newDiffs.getChangedVendorExtensions();
  }
}
