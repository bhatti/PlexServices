package com.plexobject.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetaFieldFactory {
    private static Map<String, MetaField> fieldsCache = new ConcurrentHashMap<>();

    public static synchronized MetaField create(String name, MetaFieldType type) {
        MetaField field = fieldsCache.get(name);
        if (field != null) {
            if (field.getType() == type) {
                return field;
            }
            throw new IllegalArgumentException(
                    "Duplicate meta field with different type found " + field
                            + ", new type " + type);
        }
        field = new MetaField(name, type);
        return field;
    }
}
