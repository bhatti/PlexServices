package com.plexobject.encode.xml;

import java.io.StringReader;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;
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
        xstream.registerConverter(new MapEntryConverter());
    }

    @Override
    public <T> String encode(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return xstream.toXML(obj);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encode " + obj, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(type);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader in = new StringReader(text);
            if (text != null && text.length() > 0) {
                T obj = (T) jaxbUnmarshaller.unmarshal(in);
                populateProperties(params, obj);
                return obj;
            } else {
                return propertyDecode(params, type);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode " + text, e);
        }

    }

    @Override
    public CodecType getType() {
        return CodecType.XML;
    }

}
