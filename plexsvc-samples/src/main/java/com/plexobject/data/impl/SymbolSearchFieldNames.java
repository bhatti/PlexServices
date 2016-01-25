package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum SymbolSearchFieldNames {
    SymbolSearchText("symbolSearchText", MetaFieldType.TEXT);
    private final MetaField metaField;

    private SymbolSearchFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(SymbolSearchText.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields.of(SecurityFieldNames.SecuritySymbol.getMetaField(),
                SecurityFieldNames.SecurityName.getMetaField(),
                SecurityFieldNames.SecurityDescription.getMetaField());
    }
}
