package com.flipkart.gap.usl.core.retentionPolicyVisitor;

import com.flipkart.gap.usl.core.model.dimension.DimensionCollection;
import com.flipkart.gap.usl.core.model.dimension.SizeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.SizeNTimeBasedRetentionPolicy;
import com.flipkart.gap.usl.core.model.dimension.TimeBasedRetentionPolicy;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by vinay.lodha on 13/02/18.
 */
public class ListRetentionPolicyVisitorTest {


    private static List<Element> getList(long timestamp) {
        List<Element> elements = new ArrayList<>();
        elements.add(createElement("A", timestamp - TimeUnit.DAYS.toMillis( 5)));
        elements.add(createElement("B", timestamp - TimeUnit.DAYS.toMillis( 0)));
        elements.add(createElement("C", timestamp - TimeUnit.DAYS.toMillis( 4)));
        elements.add(createElement("D", timestamp - TimeUnit.DAYS.toMillis( 2)));
        elements.add(createElement("E", timestamp - TimeUnit.DAYS.toMillis( 1)));
        elements.add(createElement("F", timestamp - TimeUnit.DAYS.toMillis( 3)));
        return elements;
    }

    private static List<Element> getSameTimestampElements(long timestamp) {
        List<Element> elements = new ArrayList<>();
        elements.add(createElement("A", timestamp));
        elements.add(createElement("B", timestamp));
        elements.add(createElement("C", timestamp));
        elements.add(createElement("D", timestamp));
        elements.add(createElement("E", timestamp));
        elements.add(createElement("F", timestamp));
        return elements;
    }

    private static Element createElement(String dummy, long timestamp) {
        Element element = new Element(dummy);
        element.setUpdated(timestamp);
        element.setCreated(timestamp);
        return element;
    }

    @Test
    public void visit() throws Exception {
        ListRetentionPolicyVisitor<Element> visitor = new ListRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        List<Element> list = getList(timeStamp);

        visitor.visit(new SizeNTimeBasedRetentionPolicy(4, 2), list);
        assertEquals(2, list.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list.get(1).getUpdated());

        List<Element> list2 = getList(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(4, 6), list2);
        assertEquals(4, list2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list2.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list2.get(2).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), list2.get(3).getUpdated());

        List<Element> list3 = getList(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(10, 3), list3);
        assertEquals(3, list3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list3.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list3.get(2).getUpdated());

        List<Element> list4 = getSameTimestampElements(timeStamp);
        visitor.visit(new SizeNTimeBasedRetentionPolicy(10, 3), list4);
        assertEquals(3, list4.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(0).getUpdated());
        assertEquals("D", list4.get(0).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(1).getUpdated());
        assertEquals("E", list4.get(1).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(2).getUpdated());
        assertEquals("F", list4.get(2).dummy);

        List<Element> list5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new SizeNTimeBasedRetentionPolicy(1, 3), list5);
        assertEquals(0, list5.size());

    }

    @Test
    public void visit1() throws Exception {
        ListRetentionPolicyVisitor<Element> visitor = new ListRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        List<Element> list = getList(timeStamp);

        visitor.visit(new SizeBasedRetentionPolicy(2), list);
        assertEquals(2, list.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list.get(1).getUpdated());

        List<Element> list2 = getList(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(4), list2);
        assertEquals(4, list2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list2.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list2.get(2).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), list2.get(3).getUpdated());

        List<Element> list3 = getList(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(3), list3);
        assertEquals(3, list3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list3.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list3.get(2).getUpdated());

        List<Element> list4 = getSameTimestampElements(timeStamp);
        visitor.visit(new SizeBasedRetentionPolicy(3), list4);
        assertEquals(3, list4.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(0).getUpdated());
        assertEquals("D", list4.get(0).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(1).getUpdated());
        assertEquals("E", list4.get(1).dummy);
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list4.get(2).getUpdated());
        assertEquals("F", list4.get(2).dummy);

        List<Element> list5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new SizeBasedRetentionPolicy(1), list5);
        assertEquals(1, list5.size());
        assertEquals("F", list5.get(0).dummy);
    }

    @Test
    public void visit2() throws Exception {
        ListRetentionPolicyVisitor<Element> visitor = new ListRetentionPolicyVisitor<>();
        long timeStamp = System.currentTimeMillis() - 10L;
        List<Element> list = getList(timeStamp);

        visitor.visit(new TimeBasedRetentionPolicy(2), list);
        assertEquals(2, list.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list.get(1).getUpdated());

        List<Element> list2 = getList(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(4), list2);
        assertEquals(4, list2.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list2.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list2.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list2.get(2).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(3), list2.get(3).getUpdated());

        List<Element> list3 = getList(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(3), list3);
        assertEquals(3, list3.size());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(0), list3.get(0).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(2), list3.get(1).getUpdated());
        assertEquals(timeStamp - TimeUnit.DAYS.toMillis(1), list3.get(2).getUpdated());

        List<Element> list4 = getSameTimestampElements(timeStamp);
        visitor.visit(new TimeBasedRetentionPolicy(2), list4);
        assertEquals(6, list4.size());
        assertEquals("A", list4.get(0).dummy);
        assertEquals("B", list4.get(1).dummy);
        assertEquals("C", list4.get(2).dummy);
        assertEquals("D", list4.get(3).dummy);
        assertEquals("E", list4.get(4).dummy);
        assertEquals("F", list4.get(5).dummy);

        List<Element> list5 = getSameTimestampElements(timeStamp - TimeUnit.DAYS.toMillis(2));
        visitor.visit(new TimeBasedRetentionPolicy(1), list5);
        assertEquals(0, list5.size());
    }

    private static class Element extends DimensionCollection.DimensionElement {
        private String dummy;
        Element(String dummy) {
            this.dummy = dummy;
        }
    }
}