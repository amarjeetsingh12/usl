package com.flipkart.gap.usl.core.model.dimension;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by vinay.lodha on 19/06/18.
 */
public class DimensionSpecTest {

    @Test
    public void test() {
        DimensionSpec dimensionSpec1 = new DimensionSpec(Dimension.class, "name", 1);
        DimensionSpec dimensionSpec2 = new DimensionSpec(Dimension.class, "name", 1);
        assertEquals(dimensionSpec1, dimensionSpec2);
    }

}