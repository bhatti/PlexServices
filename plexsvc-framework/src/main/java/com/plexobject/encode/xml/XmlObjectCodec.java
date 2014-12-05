package com.plexobject.encode.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;

/**
 * This class implements XML marshaling/unmarshaling support
 * 
 * @author shahzad bhatti
 *
 */
public class XmlObjectCodec extends AbstractObjectCodec {
    @Override
    public <T> String encode(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            JAXBContext contextObj = JAXBContext.newInstance(obj.getClass());

            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter out = new StringWriter();
            marshallerObj.marshal(obj, out);
            return out.toString();
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
