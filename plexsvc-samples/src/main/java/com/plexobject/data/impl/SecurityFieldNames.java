package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum SecurityFieldNames {
    SecurityID("securityID", MetaFieldType.INTEGER), SecuritySymbol(
            "securitySymbol", MetaFieldType.TEXT), SecurityName("securityName",
            MetaFieldType.TEXT), SecurityDescription("securityDescription",
            MetaFieldType.TEXT);
    private final MetaField metaField;

    private SecurityFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(SecurityID.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields
                .of(SecuritySymbol.getMetaField(), SecurityName.getMetaField(),
                        SecurityDescription.getMetaField());
    }
}
