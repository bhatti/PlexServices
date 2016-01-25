package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum UserFieldNames {
    UserID("userID", MetaFieldType.INTEGER), UserName("userName",
            MetaFieldType.TEXT), AccountIDs("accountIDs",
            MetaFieldType.COLLECTION_INTEGER);
    private final MetaField metaField;

    private UserFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(UserID.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields
                .of(UserName.getMetaField(), AccountIDs.getMetaField());
    }
}
