package com.plexobject.domain;

import org.junit.Test;

public class PreconditionsTest {
    @Test
    public void testCheckEmptyValid() throws Exception {
        Preconditions.checkEmpty("x", "will not throw error");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckEmptyNull() throws Exception {
        Preconditions.checkEmpty(null, "will throw error");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckEmpty() throws Exception {
        Preconditions.checkEmpty("", "will throw error");
    }

    @Test
    public void testCheckNotNullValid() throws Exception {
        Preconditions.checkNotNull("x", "will not throw error");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotNullError() throws Exception {
        Preconditions.checkNotNull(null, "will throw error");
    }

    @Test
    public void testCheckArgument() throws Exception {
        Preconditions.checkArgument(true, "will not throw error");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckArgumentFailed() throws Exception {
        Preconditions.checkArgument(false, "will throw error");
    }

    @Test
    public void testCheckArgumentObject() throws Exception {
        Preconditions.checkArgument("", "will not throw error");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckArgumentObjectFailed() throws Exception {
        Preconditions.checkArgument(null, "will throw error");
    }
}
