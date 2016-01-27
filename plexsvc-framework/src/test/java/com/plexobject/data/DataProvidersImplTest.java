package com.plexobject.data;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DataProvidersImplTest {
    public static class P extends BaseProvider {
        public P(String[] requestFields, String... responseFields) {
            super(metaFrom(requestFields), metaFrom(responseFields));
        }

        @Override
        public void produce(final DataFieldRowSet requestRowSet,
                final DataFieldRowSet responseRowSet,
                final DataConfiguration config) throws DataProviderException {
            System.out.println("===========producing " + this);
            for (int i = 0; i < requestRowSet.size(); i++) {
                for (MetaField metaField : getRequestFields().getMetaFields()) {
                    requestRowSet.getFieldAsText(metaField.getName(), i);
                }
                for (MetaField metaField : ImmutableList.copyOf(responseRowSet
                        .getMetaFields().getMetaFields())) {
                    if (getResponseFields().contains(metaField)) {
                        responseRowSet.addDataField(metaField,
                                metaField.getName() + "-value", i);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "P(" + getRequestFields() + "): " + getResponseFields();
        }

    }

    private final DataProvidersImpl dataProviders = new DataProvidersImpl();

    @Before
    public void setUp() throws Exception {
        dataProviders.register(new P(new String[] { "X" }, "B", "F"));
        dataProviders.register(new P(new String[] { "A" }, "B", "C", "D"));
        dataProviders.register(new P(new String[] { "B", "D" }, "E", "F", "G"));
        dataProviders.register(new P(new String[] { "B", "F" }, "H", "I", "J"));
        dataProviders.register(new P(new String[] { "L", "M" }, "N", "O", "P"));
        dataProviders.register(new P(new String[] { "B", "F", "L" }, "Q", "R",
                "S"));
        dataProviders.register(new P(new String[] { "H", "N" }, "V", "W", "X"));
        dataProviders.register(new P(new String[] { "L", "H" }, "V", "W"));
        dataProviders.register(new P(new String[] { "F" }, "G"));
        dataProviders.register(new P(new String[] { "V", "W" }, "Z"));
    }

    @Test
    public void testGetDataProviders() {
        String[] datapoints = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "O", "P", "Q", "R", "S", "V", "W", "X", "Z" };
        for (String dp : datapoints) {
            Collection<DataProvider> providers = dataProviders
                    .getDataProviders(metaFrom("A", "K", "L", "M"),
                            metaFrom(dp));
            System.out.println(dp + " => " + providers);
        }
    }

    @Test
    public void testGetMissingMetaFields() {
        MetaFields first = metaFrom("A", "B", "C");
        MetaFields second = metaFrom("A", "B", "C");
        MetaFields third = metaFrom("A", "B", "C", "D");
        MetaFields fourth = metaFrom("X", "Y");

        assert first.containsAll(second);
        assert first.contains(second.getMetaFields().get(0));
        assert first.getMissingCount(second) == 0;
        assert first.getMissingCount(third) == 1;
        assert first.getMissingCount(fourth) == 2;
        assert second.getMissingCount(third) == 1;

        System.out.println("0: " + first.getMissingMetaFields(second));
        System.out.println("1: " + first.getMissingMetaFields(third));
        System.out.println("2: " + first.getMissingMetaFields(fourth));
        System.out.println("1: " + second.getMissingMetaFields(third));
        long started = System.currentTimeMillis();
        long elapsed = System.currentTimeMillis() - started;
        System.out.println("ALL DONE " + elapsed);
    }

    @Test
    public void testProduce() {
        DataFieldRowSet request = rowsetFrom(true, "A");
        DataFieldRowSet response = rowsetFrom(false, "E", "F", "H");
        dataProviders.produce(request, response, new DataConfiguration());
        System.out.println("RRR " + request + "\n\t" + response);
        assertEquals(1, response.size());
        assertEquals("E-value", response.getFieldAsText("E", 0));
        assertEquals("F-value", response.getFieldAsText("F", 0));
        assertEquals("H-value", response.getFieldAsText("H", 0));

    }

    @Test
    public void testSimpleGetDataProviders() {
        // P1: X -> B, F
        // P2: A -> B, C, D
        // P3: B, D -> E, F, G
        // P4: B, F -> H, I, J
        // So Given A, we want to get E, F, H, which means we should look
        // E, which can be get metaFrom P3, but it requires B, D
        // B can be get metaFrom P1 and P2, but we will need X or A
        //
        Collection<DataProvider> providers = dataProviders.getDataProviders(
                metaFrom("A"), metaFrom("E", "F", "H"));
        System.out.println(providers);
    }

    static MetaFields metaFrom(String... args) {
        MetaFields metaFields = new MetaFields();
        for (String arg : args) {
            metaFields.addMetaField(new MetaField(arg, MetaFieldType.TEXT));
        }
        return metaFields;
    }

    static DataFieldRowSet rowsetFrom(boolean addData, String... args) {
        MetaFields metaFields = metaFrom(args);
        DataFieldRowSet rowset = new DataFieldRowSet(metaFields);
        if (addData) {
            DataFieldRow row = new DataFieldRow((Object[]) args);
            rowset.addRow(row);
        }
        return rowset;
    }
}
