package com.plexobject.data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * This class defines a data field that may be represented by a column of row in
 * the database
 * 
 * @author shahzad bhatti
 *
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class DataField {
    private final MetaField metaField;
    private final Object value;

    public DataField(MetaField metaField, Object value) {
        this.metaField = metaField;
        this.value = value;
    }

    public MetaField getMetaField() {
        return metaField;
    }

    public Object getValue() {
        return value;
    }

    public boolean isValid() {
        return value != null && value instanceof Exception == false;
    }

    public long getAsLong() {
        return ConversionUtils.getAsLong(metaField, value);
    }

    public double getAsDecimal() {
        return ConversionUtils.getAsDecimal(metaField, value);
    }

    public byte[] getBinary() {
        return ConversionUtils.getBinary(metaField, value);
    }

    public Date getAsDate() {
        return ConversionUtils.getAsDate(metaField, value);
    }

    public long[] getAsLongArray() {
        return ConversionUtils.getAsLongArray(metaField, value);
    }

    public double[] getAsDecimalArray() {
        return ConversionUtils.getAsDecimalArray(metaField, value);
    }

    public Date[] getAsDateArray() {
        return ConversionUtils.getAsDateArray(metaField, value);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((metaField == null) ? 0 : metaField.hashCode());
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
        DataField other = (DataField) obj;
        if (metaField == null) {
            if (other.metaField != null)
                return false;
        } else if (!metaField.equals(other.metaField))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DataField [metaField=" + metaField + ", value=" + value + "]";
    }

}
