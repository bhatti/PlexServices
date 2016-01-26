package com.plexobject.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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
    private final List<Object> fields = new ArrayList<>();

    public DataFieldRow() {
    }

    public DataFieldRow(Object... fields) {
        for (Object field : fields) {
            addField(field);
        }
    }

    public DataFieldRow(Collection<Object> fields) {
        for (Object field : fields) {
            addField(field);
        }
    }

    void addField(Object field) {
        if (field == null) {
            field = new NullObject();
        }
        fields.add(field);
    }

    public int size() {
        return fields.size();
    }

    public List<Object> getFields() {
        return fields;
    }

    public boolean hasFieldValue(int n) {
        Object field = fields.get(n);
        if (field == null || field instanceof NullObject
                || field instanceof InitialValue || field instanceof Exception) {
            return false;
        }
        return true;
    }

    public Object getField(int n) {
        Object field = fields.get(n);
        if (field == null || field instanceof NullObject) {
            throw new IllegalStateException("DataField at index " + n
                    + " doesn't exist");
        }
        if (field instanceof RuntimeException) {
            throw (RuntimeException) field;
        } else if (field instanceof Exception) {
            throw new DataProviderException("Error found retrieving at index "
                    + n, (Exception) field);
        }
        return field;
    }

    public String getFieldAsText(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsText(value);
    }

    public long getFieldAsLong(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsLong(value);
    }

    public double getFieldAsDecimal(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsDecimal(value);
    }

    public byte[] getFieldAsBinary(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsBinary(value);
    }

    public Date getFieldAsDate(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsDate(value);
    }

    public long[] getFieldAsLongArray(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsLongArray(value);
    }

    public double[] getFieldAsDecimalArray(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsDecimalArray(value);
    }

    public Date[] getFieldAsDateArray(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsDateArray(value);
    }

    public String[] getFieldAsTextArray(int n) {
        Object value = getField(n);
        return ConversionUtils.getAsTextArray(value);
    }

    @Override
    public String toString() {
        return "[fields=" + fields + "]";
    }

}
