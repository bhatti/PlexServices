package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum OrderFieldNames {
    OrderID("orderID", MetaFieldType.INTEGER), OrderQuantity("orderQuantity",
            MetaFieldType.INTEGER), OrderPrice("orderPrice",
            MetaFieldType.DECIMAL), OrderDescription("orderDescription",
            MetaFieldType.TEXT);
    private final MetaField metaField;

    private OrderFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(OrderID.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields.of(OrderQuantity.getMetaField(),
                OrderPrice.getMetaField(), OrderDescription.getMetaField());
    }
}
