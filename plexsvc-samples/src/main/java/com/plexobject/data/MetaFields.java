package com.plexobject.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MetaFields {
    private final Set<MetaField> metaFields;

    public MetaFields(Set<MetaField> metaFields) {
        this.metaFields = metaFields;
    }

    public static MetaFields of(MetaField... args) {
        Set<MetaField> metaFields = new HashSet<>();
        for (MetaField arg : args) {
            metaFields.add(arg);
        }
        return new MetaFields(metaFields);
    }

    public Collection<MetaField> getMetaFields() {
        return metaFields;
    }

    public void addMetaField(String name, MetaFieldType type) {
        addMetaField(new MetaField(name, type));
    }

    public void addMetaField(MetaField field) {
        metaFields.add(field);
    }

    public void removeMetaField(MetaField field) {
        metaFields.remove(field);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((metaFields == null) ? 0 : metaFields.hashCode());
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
        if (metaFields == null) {
            if (other.metaFields != null) {
                return false;
            }
        } else if (metaFields.size() != other.metaFields.size()) {
            return false;
        }
        return metaFields.containsAll(other.metaFields);
    }

}
