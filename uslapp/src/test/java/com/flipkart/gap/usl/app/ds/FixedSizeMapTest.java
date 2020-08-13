package com.flipkart.gap.usl.app.ds;

import com.flipkart.gap.usl.core.helper.ObjectMapperFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;


/**
 * Created by vinay.lodha on 15/09/17.
 */
public class FixedSizeMapTest {
    @Test
    public void jsonTest() throws IOException {
        FixedSizeMap<String,Integer> list = new FixedSizeMap<>(4, new HashMap<>());
        list.put("1", 1);
        String beforeDeserialization = ObjectMapperFactory.getMapper().writeValueAsString(list);
        Assert.assertEquals("{\"maxCapacity\":4,\"elements\":{\"1\":1}}", beforeDeserialization);
        FixedSizeMap<String, Integer> list1 = ObjectMapperFactory.getMapper().readValue("{\"maxCapacity\":4,\"elements\":{\"1\":1}}", FixedSizeMap.class);
        String afterDeserialization = ObjectMapperFactory.getMapper().writeValueAsString(list1);
        Assert.assertEquals(beforeDeserialization, afterDeserialization);
    }
}