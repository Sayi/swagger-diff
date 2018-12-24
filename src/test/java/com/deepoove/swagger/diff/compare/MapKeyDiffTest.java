package com.deepoove.swagger.diff.compare;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MapKeyDiffTest {

    @Test
    public void test() {
        Map<String, String> left = ImmutableMap.of("/auth/test", "test",
                "/test1", "test1",
                "/test2/auth", "test2");
        Map<String, String> right = ImmutableMap.of("/test", "test",
                "/test1", "test1");
        MapKeyDiff<String, String> diff = MapKeyDiff.diff(left, right);
        System.out.println(diff.getSharedKey());
        System.out.println(diff.getIncreased());
        System.out.println(diff.getMissing());
    }
}
