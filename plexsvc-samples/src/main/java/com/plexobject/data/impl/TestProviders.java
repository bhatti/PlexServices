package com.plexobject.data.impl;

import java.util.Arrays;
import java.util.Collection;

import com.plexobject.data.DataConfiguration;
import com.plexobject.data.DataFieldRow;
import com.plexobject.data.DataFieldRowSet;
import com.plexobject.data.DataProvider;
import com.plexobject.data.DataProviderException;
import com.plexobject.data.DataProviders;
import com.plexobject.data.DataProvidersImpl;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaFields;

public class TestProviders {
    public static class P extends BaseProvider {
        public P(String[] requestFields, String... responseFields) {
            super(metaFrom(requestFields), metaFrom(responseFields));
        }

        @Override
        public void produce(final DataFieldRowSet requestRowSet,
                final DataFieldRowSet responseRowSet,
                final DataConfiguration config) throws DataProviderException {
            for (int i = 0; i < requestRowSet.size(); i++) {
                for (MetaField metaField : getRequestFields().getMetaFields()) {
                    requestRowSet.getFieldAsText(metaField.getName(), i);
                }
                for (MetaField metaField : responseRowSet.getMetaFields()
                        .getMetaFields()) {
                    responseRowSet.addDataField(metaField, metaField.getName()
                            + "-value", i);
                }
            }
        }

        @Override
        public String toString() {
            return "P(" + getRequestFields() + "): " + getResponseFields();
        }

    }

    private final DataProviders dataProviders = new DataProvidersImpl();

    public TestProviders() {
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

        // A => [], K => [], L => [], M => [], T => [], U => [], Y => []
        // B => [P([A]): [B, C, D], P([X]): [B, F]]
        // C => [P([A]): [B, C, D]]
        // D => [P([A]): [B, C, D]]
        // E => [P([B, D]): [E, F, G]]
        // F => [P([B, D]): [E, F, G], P([X]): [B, F]]
        // G => [P([B, D]): [E, F, G], P([F]): [G]]
        // H => [P([B, F]): [H, I, J]]
        // I => [P([B, F]): [H, I, J]]
        // J => [P([B, F]): [H, I, J]]
        // N => [P([L, M]): [N, O, P]]
        // O => [P([L, M]): [N, O, P]]
        // P => [P([L, M]): [N, O, P]]
        // Q => [P([B, F, L]): [Q, R, S]]
        // R => [P([B, F, L]): [Q, R, S]]
        // S => [P([B, F, L]): [Q, R, S]]
        // V => [P([H, N]): [V, W, X], P([L, H]): [V, W]]
        // W => [P([H, N]): [V, W, X], P([L, H]): [V, W]]
        // X => [P([H, N]): [V, W, X]]
        // Z => [P([V, W]): [Z]]
    }

    public void runAll() {
        String[] datapoints = { "A", "B", "C", "D", "E", "F", "G", "H", "I",
                "J", "K", "O", "P", "Q", "R", "S", "V", "W", "X", "Z" }; // "L",
                                                                         // "M","N","T",
                                                                         // "U","Y",
        for (String dp : datapoints) {
            Collection<DataProvider> providers = dataProviders
                    .getDataProviders(metaFrom("A", "K", "L", "M"),
                            metaFrom(dp));
            System.out.println(dp + " => " + providers);
        }
    }

    public void run2() {
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
    }

    public void run() {
        DataFieldRowSet response = rowsetFrom("E", "F", "H");
        dataProviders.produce(rowsetFrom("A"), response,
                new DataConfiguration());
        System.out.println(response);

    }

    public void run1() {
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

    static DataFieldRowSet rowsetFrom(String... args) {
        MetaFields metaFields = metaFrom(args);
        DataFieldRowSet rowset = new DataFieldRowSet(metaFields);
        rowset.addRow(new DataFieldRow(Arrays.asList(args)));
        return rowset;
    }

    public static void main(String[] args) {
        new TestProviders().run();
    }
}
