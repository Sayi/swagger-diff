package com.deepoove.swagger.diff.compare;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.QueryParameter;

public class ListDiffTest {

    @Test
    public void testDiffIsNotEncreasedWhenTypeIsChanged() {
        QueryParameter parameter1 = new QueryParameter();
        parameter1.setName("test");
        parameter1.setType("int");
        QueryParameter parameter2 = new QueryParameter();
        parameter2.setName("test");
        parameter2.setType("int32");
        List<Parameter> left = ImmutableList.of(parameter1);
        List<Parameter> right = ImmutableList.of(parameter2);
        ListDiff<Parameter> diff = ListDiff.diff(left, right, (t, param) -> {
            for (Parameter para : t) {
                if (param.getName().equals(para.getName())) { return para; }
            }
            return null;
        });
        assertNotNull(diff);
        assertThat(diff.getIncreased().size(), is(0));
    }
}
