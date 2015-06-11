package com.plexobject.encode.xml;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.EncodingException;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;
import com.plexobject.validation.ValidationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * This class implements XML marshaling/unmarshaling support
 * 
 * @author shahzad bhatti
 *
 */
public class XmlObjectCodec extends AbstractObjectCodec {
    private final XStream xstream = new XStream(new StaxDriver());

    public static class MapEntryConverter implements Converter {
        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class clazz) {
            return AbstractMap.class.isAssignableFrom(clazz);
        }

        @SuppressWarnings("rawtypes")
        public void marshal(Object value, HierarchicalStreamWriter writer,
                MarshallingContext context) {

            AbstractMap map = (AbstractMap) value;
            for (Object obj : map.entrySet()) {
                Map.Entry entry = (Map.Entry) obj;
                writer.startNode(entry.getKey().toString());
                writer.setValue(entry.getValue().toString());
                writer.endNode();
            }

        }

        public Object unmarshal(HierarchicalStreamReader reader,
                UnmarshallingContext context) {
            Map<String, String> map = new HashMap<String, String>();

            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String key = reader.getNodeName();
                String value = reader.getValue();
                map.put(key, value);

                reader.moveUp();
            }

            return map;
        }
    }

    public XmlObjectCodec() {
        xstream.alias("error", ValidationException.Error.class);
        xstream.processAnnotations(new Class[] { Request.class, Response.class });
        xstream.registerConverter(new MapEntryConverter());
        //xstream.alias("items", Map.class);
        // NamedMapConverter namedMapConverter = new NamedMapConverter(
        // xstream.getMapper(), "attr", "description", String.class,
        // "value", String.class);
        // xstream.registerConverter(namedMapConverter);
    }

    public void processAnnotations(Class<?>... klasses) {
        xstream.processAnnotations(klasses);
    }

    @Override
    public <T> String encode(T obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        } else if (obj instanceof CharSequence) {
            return obj.toString();
        }
        try {
            return xstream.toXML(obj);
        } catch (Exception e) {
            throw new EncodingException("Failed to encode " + obj, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
        if (type == null) {
            return null;
        }
        try {
            if (text != null && text.length() > 0) {
                T obj = (T) xstream.fromXML(text);
                populateProperties(params, obj);
                return obj;
            } else {
                return propertyDecode(params, type);
            }

        } catch (Exception e) {
            throw new EncodingException("Failed to decode " + text, e);
        }

    }

    @Override
    public CodecType getType() {
        return CodecType.XML;
    }

}
