package com.plexobject.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MetaFields {
    private final List<MetaField> metaFields = new ArrayList<>();
    private transient final Map<MetaField, Integer> metaFieldsLookup = new HashMap<>();

    public MetaFields() {
    }

    public MetaFields(MetaField[] metaFields) {
        this(Arrays.asList(metaFields));
    }

    public MetaFields(Collection<MetaField> metaFields) {
        for (MetaField metaField : metaFields) {
            addMetaField(metaField);
        }
    }

    public void addMetaField(MetaField metaField) {
        if (!metaFieldsLookup.containsKey(metaField)) {
            metaFields.add(metaField);
            metaFieldsLookup.put(metaField, metaFieldsLookup.size());
        }
    }

    public void addMetaFields(MetaFields metaFields) {
        for (MetaField metaField : metaFields.getMetaFields()) {
            addMetaField(metaField);
        }
    }

    public Integer getIndex(MetaField field) {
        return metaFieldsLookup.get(field);
    }

    public boolean containsAll(MetaFields other) {
        return getMissingCount(other) == 0;
    }

    public boolean contains(MetaField other) {
        return getMissingCount(MetaFields.of(other)) == 0;
    }

    public int getMissingCount(MetaFields other) {
        int count = 0;
        for (MetaField field : other.metaFields) {
            if (metaFieldsLookup.get(field) == null) {
                count++;
            }
        }
        return count;
    }

    public MetaFields getMissingMetaFields(MetaFields other) {
        Set<MetaField> missingMetaFields = new HashSet<MetaField>();
        for (MetaField field : other.metaFields) {
            if (metaFieldsLookup.get(field) == null) {
                missingMetaFields.add(field);
            }
        }
        return new MetaFields(missingMetaFields);
    }

    public List<MetaField> getMetaFields() {
        return metaFields;
    }

    public int size() {
        return metaFields.size();
    }

    public static MetaFields of(MetaField... args) {
        final MetaFields metaFields = new MetaFields();
        for (MetaField arg : args) {
            metaFields.addMetaField(arg);
        }
        return metaFields;
    }

    public static MetaFields of(String... args) {
        final MetaFields metaFields = new MetaFields();
        for (int i = 0; i < args.length; i += 2) {
            MetaField field = new MetaField(args[i],
                    MetaFieldType.valueOf(args[i + 1].toString()));
            metaFields.addMetaField(field);
        }
        return metaFields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        for (MetaField field : metaFields) {
            result = prime * result + field.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MetaFields other = (MetaFields) obj;
        if (metaFields.size() != other.metaFields.size()) {
            return false;
        }
        for (MetaField field : other.metaFields) {
            if (metaFieldsLookup.get(field) == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return metaFields.toString();
    }

}
