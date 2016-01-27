package com.plexobject.data;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class MetaField implements Comparable<MetaField> {
    private final String name;
    private final MetaFieldType type;

    public MetaField(String name, MetaFieldType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public MetaFieldType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        MetaField other = (MetaField) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return name; // "[" + name + "/" + type + "]";
    }

    @Override
    public int compareTo(MetaField other) {
        return name.compareTo(other.name);
    }
}
