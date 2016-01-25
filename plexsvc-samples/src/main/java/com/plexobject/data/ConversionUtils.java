package com.plexobject.data;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;

public class ConversionUtils {
    public static long getAsLong(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.valueOf((String) value);
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static double getAsDecimal(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            return Double.valueOf((String) value);
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static byte[] getBinary(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof String) {
            return ((String) value).getBytes();
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static Date getAsDate(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Date) {
            return (Date) value;
        } else if (value instanceof Number) {
            return new Date(((Number) value).longValue());
        } else if (value instanceof String) {
            return new Date(Long.valueOf((String) value));
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static long[] getAsLongArray(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) (value);
            long[] values = new long[collection.size()];
            int i = 0;
            for (Object obj : collection) {
                values[i++] = ConversionUtils.getAsLong(metaField, obj);
            }
            return values;
        } else if (value instanceof Array) {
            Array array = (Array) value;
            long[] values = new long[Array.getLength(array)];
            for (int i = 0; i < values.length; i++) {
                values[i++] = ConversionUtils.getAsLong(metaField,
                        Array.get(array, i));
            }
            return values;
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static double[] getAsDecimalArray(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) (value);
            double[] values = new double[collection.size()];
            int i = 0;
            for (Object obj : collection) {
                values[i++] = ConversionUtils.getAsDecimal(metaField, obj);
            }
            return values;
        } else if (value instanceof Array) {
            Array array = (Array) value;
            double[] values = new double[Array.getLength(array)];
            for (int i = 0; i < values.length; i++) {
                values[i++] = ConversionUtils.getAsDecimal(metaField,
                        Array.get(array, i));
            }
            return values;
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }

    public static Date[] getAsDateArray(MetaField metaField, Object value) {
        if (value instanceof RuntimeException) {
            throw (RuntimeException) value;
        } else if (value instanceof Exception) {
            throw new DataProviderException("error found retrieving "
                    + metaField, (Exception) value);
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) (value);
            Date[] values = new Date[collection.size()];
            int i = 0;
            for (Object obj : collection) {
                values[i++] = ConversionUtils.getAsDate(metaField, obj);
            }
            return values;
        } else if (value instanceof Array) {
            Array array = (Array) value;
            Date[] values = new Date[Array.getLength(array)];
            for (int i = 0; i < values.length; i++) {
                values[i++] = ConversionUtils.getAsDate(metaField,
                        Array.get(array, i));
            }
            return values;
        }
        throw new DataProviderException("unexpected type found retrieving "
                + metaField + ", value " + value);
    }
}
