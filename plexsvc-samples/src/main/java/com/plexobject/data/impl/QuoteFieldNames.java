package com.plexobject.data.impl;

import com.plexobject.data.MetaFieldType;
import com.plexobject.data.MetaField;
import com.plexobject.data.MetaFields;

public enum QuoteFieldNames {
    QuoteBid("quoteBid", MetaFieldType.DECIMAL), QuoteAsk("quoteAsk",
            MetaFieldType.DECIMAL), QuoteVolume("quoteVolume",
            MetaFieldType.INTEGER);
    private final MetaField metaField;

    private QuoteFieldNames(String name, MetaFieldType type) {
        this.metaField = new MetaField(name, type);
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public static MetaFields getInputFields() {
        return MetaFields.of(SecurityFieldNames.SecuritySymbol.getMetaField());
    }

    public static MetaFields getOutputFields() {
        return MetaFields.of(QuoteBid.getMetaField(), QuoteAsk.getMetaField(),
                QuoteVolume.getMetaField());
    }
}
