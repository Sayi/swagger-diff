package com.deepoove.swagger.diff.compare;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;


public class MapDiff<K, V> {
  private Map<K, V> increased = new LinkedHashMap<K, V>();
  private Map<K, V> missing = new LinkedHashMap<K, V>();
  private Map<K, Pair<V, V>> changed = new LinkedHashMap<K, Pair<V, V>>();


  private MapDiff() {

  }

  public static <K, V> MapDiff<K, V> diff(Map<K, V> mapLeft,
                                          Map<K, V> mapRight) {
    MapDiff instance = new MapDiff();
    if (null == mapLeft && null == mapRight) return instance;

    if (null == mapLeft) {
      instance.increased = mapRight;
      return instance;
    }

    if (null == mapRight) {
      instance.missing = mapLeft;
      return instance;
    }

    instance.increased.putAll(mapRight);

    for (Entry<K, V> entry : mapLeft.entrySet()) {
      K leftKey = entry.getKey();
      V leftValue = entry.getValue();

      if (mapRight.containsKey(leftKey)) {
        instance.increased.remove(leftKey);

        if (!(mapRight.get(leftKey).getClass().isInstance(leftValue) && mapRight.get(leftKey).equals(leftValue))) {
          instance.changed.put(leftKey, Pair.of(leftValue, mapRight.get(leftKey)));
        }
      } else {
        instance.missing.put(leftKey, leftValue);
      }
    }
    return instance;
  }

  public Map<K, V> getIncreased() {
    return increased;
  }

  public Map<K, V> getMissing() {
    return missing;
  }

  public Map<K, Pair<V, V>> getChanged() {
    return changed;
  }
}
