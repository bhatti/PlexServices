package com.plexobject.encode.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Before;
import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.Employee;
import com.plexobject.encode.EncodingException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class XmlObjectCodecTest {
    @XStreamAlias("Obj")
    static class Obj {
        @XStreamAlias("Name")
        @XStreamAsAttribute
        private String name;
        @SuppressWarnings("deprecation")
        @XStreamAlias("Date")
        @XStreamAsAttribute
        private Date date = new Date(2015 - 1900, 0, 1);

        Obj() {
        }

        Obj(String name) {
            this.name = name;
        }
    }

    XmlObjectCodec instance = new XmlObjectCodec();

    @Before
    public void setUp() throws Exception {
        instance.processAnnotations(Obj.class, Employee.class);
    }

    @Test
    public void testEncodeNull() {
        String xml = instance.encode(null);
        assertNull(xml);
    }

    @Test(expected = EncodingException.class)
    public void testEncodeBadClass() {
        String xml = instance.encode(this);
        assertNull(xml);
    }

    @Test
    public void testDecodeNull() {
        Object obj = instance.decode(null, null, null);
        assertNull(obj);
    }

    @Test(expected = EncodingException.class)
    public void testDecodeBadXml() {
        Object obj = instance.decode("[x]", Employee.class, null);
        assertNull(obj);
    }

    @Test
    public void testDecodeNullType() {
        instance.decode("[{]}", null, null);
    }

    @Test
    public void testEncodeString() {
        String xml = instance.encode("");
        assertEquals("", xml);
    }

    @Test
    public void testEncodeStringBuilder() {
        String xml = instance.encode(new StringBuilder());
        assertEquals("", xml);
    }

    @Test
    public void testEncodePrivateObject() {
        String xml = instance.encode(new Obj("name"));
        assertEquals(
                "<?xml version=\"1.0\" ?><Obj Name=\"name\" Date=\"2015-01-01 08:00:00.0 UTC\"></Obj>",
                xml);
    }

    @Test
    public void testGetType() {
        assertEquals(CodecType.XML, instance.getType());
    }

    @Test
    public void testEncodeDecode() {
        Employee original = new Employee(100L, "john");
        String xml = instance.encode(original);
        assertEquals(
                "<?xml version=\"1.0\" ?><Obj ID=\"100\" Name=\"john\"></Obj>",
                xml);
        assertEquals(original, instance.decode(xml, Employee.class, null));
    }

    @Test
    public void testEncodeDecodeParams() throws Exception {
        Employee original = new Employee(100L, "john");
        Map<String, Object> objectAsMap = new HashMap<>();
        for (Map.Entry<String, String> e : BeanUtils.describe(original)
                .entrySet()) {
            objectAsMap.put(e.getKey(), e.getValue());
        }
        assertEquals(original,
                instance.decode(null, Employee.class, objectAsMap));
    }

}
