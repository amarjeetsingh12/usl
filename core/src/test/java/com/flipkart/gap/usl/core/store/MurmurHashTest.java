package com.flipkart.gap.usl.core.store;

import com.flipkart.gap.usl.core.store.dimension.hbase.MurmurHash;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ankesh.maheshwari on 25/11/16.
 */
public class MurmurHashTest {
    @Test(expected = IllegalArgumentException.class)
    public void murmurHashTest1WithException() {
        new MurmurHash(0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void murmurHashTest2WithException() {
        new MurmurHash(-1, 4);
    }

    @Test
    public void murmurHashTestGetPrefix() {
        MurmurHash murmurHash = new MurmurHash(40, 0);
        String s1 = new String(murmurHash.getHashPrefix("ABCDEFGHI".getBytes()));
        String s2 = new String(murmurHash.getHashPrefix("ABCDEFGHI".getBytes()));
        Assert.assertEquals(s1, s2);
    }

    @Test
    public void murmurHashTestDefaultPartition() {
        MurmurHash murmurHash = new MurmurHash(40, 10);
        String s1 = Bytes.toString(murmurHash.getHashPrefix(null));
        Assert.assertEquals(s1, Integer.toString(10));
    }
}
