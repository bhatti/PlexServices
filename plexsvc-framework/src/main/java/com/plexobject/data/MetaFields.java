package com.plexobject.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MetaFields {
    private final Set<MetaField> metaFields = new HashSet<>();

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

    public synchronized void addMetaField(MetaField metaField) {
        if (!metaFields.contains(metaField)) {
            metaFields.add(metaField);
        }
    }

    public synchronized void addMetaFields(MetaFields metaFields) {
        for (MetaField metaField : metaFields.getMetaFields()) {
            addMetaField(metaField);
        }
    }

    public synchronized void removeMetaFields(MetaFields metaFields) {
        for (MetaField metaField : metaFields.getMetaFields()) {
            removeMetaField(metaField);
        }
    }

    public synchronized void removeMetaField(MetaField metaField) {
        metaFields.remove(metaField);
    }

    public synchronized boolean containsAll(MetaFields other) {
        return getMissingCount(other) == 0;
    }

    public synchronized boolean contains(MetaField other) {
        return getMissingCount(MetaFields.of(other)) == 0;
    }

    public synchronized int getMissingCount(MetaFields other) {
        int count = 0;
        for (MetaField field : other.metaFields) {
            if (!metaFields.contains(field)) {
                count++;
            }
        }
        return count;
    }

    public synchronized MetaFields getMissingMetaFields(MetaFields other) {
        Set<MetaField> missingMetaFields = new HashSet<MetaField>();
        for (MetaField field : other.metaFields) {
            if (!metaFields.contains(field)) {
                missingMetaFields.add(field);
            }
        }
        return new MetaFields(missingMetaFields);
    }

    public synchronized Collection<MetaField> getMetaFields() {
        return metaFields;
    }

    public synchronized int size() {
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
            MetaField field = MetaFieldFactory.create(args[i],
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
            if (!metaFields.contains(field)) {
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
