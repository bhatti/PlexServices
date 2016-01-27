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

    public synchronized void addDataField(MetaField metaField, Object value,
            int rowNumber) {
        DataFieldRow row = getOrCreateRow(rowNumber);
        if (row == null) {
            throw new RuntimeException("XXX " + metaField + ", " + value
                    + ", row-num " + rowNumber + ", rows " + rows.size()
                    + ", rows " + rows + ", meta " + metaFields);
        }
        addMetaField(metaField);
        //
        row.addField(metaField, value);
    }

    public synchronized DataFieldRow getOrCreateRow(int row) {
        while (row >= rows.size()) {
            addRow(new DataFieldRow());
        }
        return rows.get(row);
    }

    public synchronized void addRow(DataFieldRow row) {
        Preconditions.checkNotNull(row, "row cannot be null");
        rows.add(row);
    }

    public MetaFields getMetaFields() {
        return metaFields;
    }

    public synchronized int size() {
        return rows.size();
    }

    public synchronized List<DataFieldRow> getRows() {
        return rows;
    }

    public synchronized boolean hasFieldValue(MetaField metaField, int row) {
        if (row >= rows.size()) {
            return false;
        }
        return rows.get(row).hasFieldValue(metaField);
    }

    public synchronized Object getField(MetaField metaField, int rowNumber) {
        validateRow(rowNumber);
        DataFieldRow row = rows.get(rowNumber);
        return row.getValue(metaField);
    }

    private void validateRow(int row) {
        if (row >= rows.size()) {
            throw new IllegalArgumentException("row " + row
                    + " is higher than internal rows " + rows.size());
        }
    }

    public String getValueAsText(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsText(metaField);
    }

    public long getValueAsLong(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsLong(metaField);
    }

    public double getValueAsDecimal(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsDecimal(metaField);
    }

    public byte[] getValueAsBinary(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsBinary(metaField);
    }

    public Date getValueAsDate(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsDate(metaField);
    }

    public long[] getValueAsLongArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsLongArray(metaField);
    }

    public double[] getValueAsDecimalArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsDecimalArray(metaField);
    }

    public Date[] getValueAsDateArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsDateArray(metaField);
    }

    public String[] getValueAsTextArray(MetaField metaField, int row) {
        Preconditions.checkNotNull(metaField, "metaField cannot be null");
        validateRow(row);
        return rows.get(row).getValueAsTextArray(metaField);
    }

    public boolean hasFieldValue(String name, int row) {
        return hasFieldValue(getMetaFieldsByName(name), row);
    }

    public Object getField(String name, int row) {
        return getField(getMetaFieldsByName(name), row);
    }

    public String getValueAsText(String name, int row) {
        return getValueAsText(getMetaFieldsByName(name), row);
    }

    public long getValueAsLong(String name, int row) {
        return getValueAsLong(getMetaFieldsByName(name), row);
    }

    public double getValueAsDecimal(String name, int row) {
        return getValueAsDecimal(getMetaFieldsByName(name), row);
    }

    public byte[] getValueAsBinary(String name, int row) {
        return getValueAsBinary(getMetaFieldsByName(name), row);
    }

    public Date getValueAsDate(String name, int row) {
        return getValueAsDate(getMetaFieldsByName(name), row);
    }

    public long[] getValueAsLongArray(String name, int row) {
        return getValueAsLongArray(getMetaFieldsByName(name), row);
    }

    public double[] getValueAsDecimalArray(String name, int row) {
        return getValueAsDecimalArray(getMetaFieldsByName(name), row);
    }

    public Date[] getValueAsDateArray(String name, int row) {
        return getValueAsDateArray(getMetaFieldsByName(name), row);
    }

    public String[] getValueAsTextArray(String name, int row) {
        return getValueAsTextArray(getMetaFieldsByName(name), row);
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

    private synchronized void addMetaField(MetaField metaField) {
        if (!metaFieldsByName.containsKey(metaField.getName())) {
            metaFields.addMetaField(metaField);
            metaFieldsByName.put(metaField.getName(), metaField);
        }
    }

}
