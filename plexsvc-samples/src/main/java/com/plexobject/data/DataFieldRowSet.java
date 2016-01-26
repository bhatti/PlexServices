package com.plexobject.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.plexobject.domain.Preconditions;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DataFieldRowSet {
    private final MetaFields metaFields;
    private final List<DataFieldRow> rows = new ArrayList<>();
    private final transient Map<String, MetaField> metaFieldsByName = new HashMap<>();

    public DataFieldRowSet(MetaFields metaFields) {
        this(metaFields, Arrays.<DataFieldRow> asList());
    }

    public DataFieldRowSet(MetaFields metaFields, DataFieldRow[] rows) {
        this(metaFields, Arrays.asList(rows));
    }

    public DataFieldRowSet(MetaFields metaFields, Collection<DataFieldRow> rows) {
        Preconditions.checkNotNull(metaFields, "metaFields cannot be null");
        Preconditions.checkNotNull(rows, "rows cannot be null");
        this.metaFields = metaFields;
        for (MetaField metaField : metaFields.getMetaFields()) {
            metaFieldsByName.put(metaField.getName(), metaField);
        }
        for (DataFieldRow row : rows) {
            addRow(row);
        }
    }

    public void addDataField(MetaField metaField, Object value, int rowNumber) {
        DataFieldRow row = getOrCreateRow(rowNumber);
        metaFields.addMetaField(metaField);
        metaFieldsByName.put(metaField.getName(), metaField);
        row.addField(value);
    }

    public DataFieldRow getOrCreateRow(int row) {
        while (rows.size() <= row) {
            rows.add(new DataFieldRow());
        }
        return rows.get(row);
    }

    public void addRow(DataFieldRow row) {
        Preconditions.checkNotNull(row, "row cannot be null");
        rows.add(row);
    }

    public MetaFields getMetaFields() {
        return metaFields;
    }

    public int size() {
        return rows.size();
    }

    public List<DataFieldRow> getRows() {
        return rows;
    }

    public boolean hasFieldValue(MetaField metaField, int row) {
        if (rows.size() >= row) {
            return false;
        }
        Integer column = getIndex(metaField);
        if (column == null) {
            return false;
        }
        return rows.get(row).hasFieldValue(column);
    }

    public int getIndex(MetaField metaField) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");

        Integer column = metaFields.getIndex(metaField);
        if (column == null) {
            throw new IllegalStateException("MetaField " + metaField
                    + " doesn't exist");
        }
        return column;
    }

    public Object getField(MetaField metaField, int rowNumber) {
        validateRow(rowNumber);
        DataFieldRow row = rows.get(rowNumber);
        int column = getIndex(metaField);
        if (column >= row.size()) {
            throw new IllegalArgumentException("column " + column + " for "
                    + metaField + " is higher than internal columns "
                    + row.size());
        }
        return row.getField(column);
    }

    private void validateRow(int row) {
        if (row >= rows.size()) {
            throw new IllegalArgumentException("row " + row
                    + " is higher than internal rows " + rows.size());
        }
    }

    public String getFieldAsText(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsText(column);
    }

    public long getFieldAsLong(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsLong(column);
    }

    public double getFieldAsDecimal(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsDecimal(column);
    }

    public byte[] getFieldAsBinary(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsBinary(column);
    }

    public Date getFieldAsDate(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsDate(column);
    }

    public long[] getFieldAsLongArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsLongArray(column);
    }

    public double[] getFieldAsDecimalArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsDecimalArray(column);
    }

    public Date[] getFieldAsDateArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsDateArray(column);
    }

    public String[] getFieldAsTextArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        int column = getIndex(metaField);
        return rows.get(row).getFieldAsTextArray(column);
    }

    public boolean hasFieldValue(String name, int row) {
        return hasFieldValue(getMetaFieldsByName(name), row);
    }

    public int getIndex(String name) {
        return getIndex(getMetaFieldsByName(name));
    }

    public Object getField(String name, int row) {
        return getField(getMetaFieldsByName(name), row);
    }

    public String getFieldAsText(String name, int row) {
        return getFieldAsText(getMetaFieldsByName(name), row);
    }

    public long getFieldAsLong(String name, int row) {
        return getFieldAsLong(getMetaFieldsByName(name), row);
    }

    public double getFieldAsDecimal(String name, int row) {
        return getFieldAsDecimal(getMetaFieldsByName(name), row);
    }

    public byte[] getFieldAsBinary(String name, int row) {
        return getFieldAsBinary(getMetaFieldsByName(name), row);
    }

    public Date getFieldAsDate(String name, int row) {
        return getFieldAsDate(getMetaFieldsByName(name), row);
    }

    public long[] getFieldAsLongArray(String name, int row) {
        return getFieldAsLongArray(getMetaFieldsByName(name), row);
    }

    public double[] getFieldAsDecimalArray(String name, int row) {
        return getFieldAsDecimalArray(getMetaFieldsByName(name), row);
    }

    public Date[] getFieldAsDateArray(String name, int row) {
        return getFieldAsDateArray(getMetaFieldsByName(name), row);
    }

    public String[] getFieldAsTextArray(String name, int row) {
        return getFieldAsTextArray(getMetaFieldsByName(name), row);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows.size(); i++) {
            sb.append("\n\t\trow-" + i + ": " + rows.get(i));
        }
        return "DataFieldRowSet [metaFields=" + metaFields + ", rows=" + sb
                + "]";
    }

    private MetaField getMetaFieldsByName(String name) {
        MetaField field = metaFieldsByName.get(name);
        if (field == null) {
            throw new IllegalArgumentException("meta field with name " + name
                    + " doesn't exists, available " + metaFieldsByName.keySet());
        }
        return field;
    }
}
