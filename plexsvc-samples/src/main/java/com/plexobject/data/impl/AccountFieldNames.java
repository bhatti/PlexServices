package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum AccountFieldNames {
    AccountID("accountID", MetaFieldType.INTEGER), AccountName("accountName",
            MetaFieldType.TEXT), AccountType("accountType", MetaFieldType.TEXT);
    private final MetaField metaField;

    private AccountFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(AccountID.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields.of(AccountName.getMetaField(),
                AccountType.getMetaField());
    }
}
