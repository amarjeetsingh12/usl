package com.flipkart.gap.usl.app.dimension;

import com.flipkart.gap.usl.core.registry.DimensionRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by vinay.lodha on 13/07/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DimensionRegistry.class)
public class DimensionRegistryTest {

    @Test
    public void getTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DimensionRegistry dimensionRegistry = new DimensionRegistry();
        Method method = DimensionRegistry.class.getDeclaredMethod("setInternalEventMap", String.class);
        method.setAccessible(true);
        method.invoke(dimensionRegistry, "com.flipkart.gap.usl.app.dimension");
    }

}
