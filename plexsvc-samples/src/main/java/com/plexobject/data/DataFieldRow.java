package com.plexobject.data;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * This class defines collection of data fields that would be returned or
 * consumed by data provider
 * 
 * @author shahzad bhatti
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DataFieldRow {
    private final Set<DataField> fields = new HashSet<>();
    private final transient Map<String, DataField> fieldsByName = new HashMap<>();

    public DataFieldRow(Set<DataField> fields) {
        for (DataField field : fields) {
            addField(field);
        }
    }

    public static DataFieldRow of(DataField... args) {
        Set<DataField> fields = new HashSet<>();
        for (DataField arg : args) {
            fields.add(arg);
        }
        return new DataFieldRow(fields);
    }

    public Collection<DataField> getFields() {
        return fields;
    }

    public DataField getField(String name) {
        return fieldsByName.get(name);
    }

    public long getValueAsLong(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getAsLong();
    }

    public double getValueAsDecimal(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }

        return field.getAsDecimal();
    }

    public byte[] getValueAsBinary(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getBinary();
    }

    public Date getValueAsDate(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getAsDate();
    }

    public long[] getValueAsLongArray(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getAsLongArray();
    }

    public double[] getValueAsDecimalArray(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getAsDecimalArray();
    }

    public Date[] getValueAsDateArray(String name) {
        DataField field = getField(name);
        if (field == null) {
            throw new IllegalStateException("DataField with name " + name
                    + " doesn't exist");
        }
        return field.getAsDateArray();
    }

    public void addField(String name, MetaFieldType type, Object value) {
        addField(new DataField(new MetaField(name, type), value));
    }

    public void addField(DataField field) {
        fields.add(field);
        fieldsByName.put(field.getMetaField().getName(), field);
    }

    public void removeField(DataField field) {
        fields.remove(field);
        fieldsByName.remove(field.getMetaField().getName());
    }

    @Override
    public String toString() {
        return "DataFields [fields=" + fields + "]";
    }

}
