package com.flipkart.gap.usl.core.retentionPolicyVisitor;

import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.SizeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.SizeNTimeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.TimeBasedRetentionPolicy;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by vinay.lodha on 13/02/18.
 */
public class MapRetentionPolicyVisitorTest {

    @Test
    public void visit() throws Exception {
        MapRetentionPolicyVisitor<Integer,Element> visitor = new MapRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        Map<Integer,Element> map = getMap(timeStamp);

        visitor.visit(new SizeNTimeBasedRetentionPolicy(4, 2), map);
        assertEquals(2, map.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map.get(4).getUpdated());

        Map<Integer,Element> map2 = getMap(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(4, 6), map2);
        assertEquals(4, map2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map2.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map2.get(4).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), map2.get(5).getUpdated());

        Map<Integer,Element> map3 = getMap(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(10, 3), map3);
        assertEquals(3, map3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map3.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map3.get(4).getUpdated());

        Map<Integer,Element> map4 = getSameTimestampElements(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(10, 3), map4);
        assertEquals(3, map4.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(3).getUpdated());
        assertEquals("D", map4.get(3).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(4).getUpdated());
        assertEquals("E", map4.get(4).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(5).getUpdated());
        assertEquals("F", map4.get(5).dummy);

        Map<Integer,Element> map5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new SizeNTimeBasedRetentionPolicy(1, 3), map5);
        assertEquals(0, map5.size());

    }

    @Test
    public void visit1() throws Exception {
        MapRetentionPolicyVisitor<Integer,Element> visitor = new MapRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        Map<Integer,Element> map = getMap(timeStamp);

        visitor.visit(new SizeBasedRetentionPolicy(2), map);
        assertEquals(2, map.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map.get(4).getUpdated());

        Map<Integer,Element> map2 = getMap(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(4), map2);
        assertEquals(4, map2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map2.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map2.get(4).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), map2.get(5).getUpdated());

        Map<Integer,Element> map3 = getMap(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(3), map3);
        assertEquals(3, map3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map3.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map3.get(4).getUpdated());

        Map<Integer,Element> map4 = getSameTimestampElements(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(3), map4);
        assertEquals(3, map4.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(3).getUpdated());
        assertEquals("D", map4.get(3).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(4).getUpdated());
        assertEquals("E", map4.get(4).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map4.get(5).getUpdated());
        assertEquals("F", map4.get(5).dummy);

        Map<Integer,Element> map5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new SizeBasedRetentionPolicy(1), map5);
        assertEquals(1, map5.size());
        assertEquals("F", map5.get(5).dummy);
    }

    @Test
    public void visit2() throws Exception {
        MapRetentionPolicyVisitor<Integer,Element> visitor = new MapRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        Map<Integer,Element> map = getMap(timeStamp);

        visitor.visit(new TimeBasedRetentionPolicy(2), map);
        assertEquals(2, map.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map.get(4).getUpdated());

        Map<Integer,Element> map2 = getMap(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(4), map2);
        assertEquals(4, map2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map2.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map2.get(4).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), map2.get(5).getUpdated());

        Map<Integer,Element> map3 = getMap(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(3), map3);
        assertEquals(3, map3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), map3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), map3.get(3).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), map3.get(4).getUpdated());

        Map<Integer,Element> map4 = getSameTimestampElements(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(2), map4);
        assertEquals(6, map4.size());
        assertEquals("A", map4.get(0).dummy);
        assertEquals("B", map4.get(1).dummy);
        assertEquals("C", map4.get(2).dummy);
        assertEquals("D", map4.get(3).dummy);
        assertEquals("E", map4.get(4).dummy);
        assertEquals("F", map4.get(5).dummy);

        Map<Integer,Element> map5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new TimeBasedRetentionPolicy(1), map5);
        assertEquals(0, map5.size());
    }

    private static class Element extends DimensionCollection.DimensionElement {
        private String dummy;
        Element(String dummy) {
            this.dummy = dummy;
        }
    }

    private static Map<Integer, Element> getMap(long timestamp) {
        Map<Integer, Element> elements = new HashMap<>();
        elements.put(0, createElement("A", timestamp - TimeUnit.DAYS.toMillis( 5)));
        elements.put(1, createElement("B", timestamp - TimeUnit.DAYS.toMillis( 0)));
        elements.put(2, createElement("C", timestamp - TimeUnit.DAYS.toMillis( 4)));
        elements.put(3, createElement("D", timestamp - TimeUnit.DAYS.toMillis( 2)));
        elements.put(4, createElement("E", timestamp - TimeUnit.DAYS.toMillis( 1)));
        elements.put(5, createElement("F", timestamp - TimeUnit.DAYS.toMillis( 3)));
        return elements;
    }

    private static Map<Integer, Element>  getSameTimestampElements(long timestamp) {
        Map<Integer, Element> elements = new HashMap<>();
        elements.put(0, createElement("A", timestamp ));
        elements.put(1, createElement("B", timestamp ));
        elements.put(2, createElement("C", timestamp ));
        elements.put(3, createElement("D", timestamp ));
        elements.put(4, createElement("E", timestamp ));
        elements.put(5, createElement("F", timestamp ));
        return elements;
    }

    private static Element createElement(String dummy, long timestamp) {
        Element element = new Element(dummy);
        element.setUpdated(timestamp);
        element.setCreated(timestamp);
        return element;
    }

}