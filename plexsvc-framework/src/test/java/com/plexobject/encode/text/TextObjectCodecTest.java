package com.plexobject.encode.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.Employee;
import com.plexobject.encode.ObjectCodec;

public class TextObjectCodecTest {
    static class ObjectWithDefaultCtor {

    }

    static class ObjectWithStringCtor {
        private String name;

        public ObjectWithStringCtor(String name) {
            this.name = name;
        }

        public String toString() {
            return name.toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
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
            ObjectWithStringCtor other = (ObjectWithStringCtor) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    ObjectCodec instance = new TextObjectCodec();

    @Test
    public void testEncodeNull() {
        String text = instance.encode(null);
        assertNull(text);
    }

    @Test
    public void testEncodeString() {
        String text = instance.encode("");
        assertEquals("", text);
    }

    @Test
    public void testEncodeStringBuilder() {
        String text = instance.encode(new StringBuilder());
        assertEquals("", text);
    }

    @Test
    public void testGetType() {
        assertEquals(CodecType.TEXT, instance.getType());
    }

    @Test
    public void testEncodeDecode() {
        ObjectWithStringCtor original = new ObjectWithStringCtor("name");
        String text = instance.encode(original);
        assertEquals("name", text);
        assertEquals(original,
                instance.decode(text, ObjectWithStringCtor.class, null));
    }

    @Test(expected = RuntimeException.class)
    public void testEncodeDecodeDefaultCtor() {
        ObjectWithDefaultCtor original = new ObjectWithDefaultCtor();
        String text = instance.encode(original);
        assertTrue(!"name".equals(text));
        instance.decode(text, ObjectWithDefaultCtor.class, null);
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
