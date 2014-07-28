package com.plexobject.service.util;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.plexobject.encode.json.JsonObjectCodec;

public class JsonObjectCodecTest {
    public static class Employee {
        Long id;
        String name;

        public Employee() {

        }

        public Employee(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
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
            Employee other = (Employee) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            return true;
        }
    }

    JsonObjectCodec instance = new JsonObjectCodec();

    @Test
    public void testEncodeDecode() {
        Employee original = new Employee(100L, "john");
        String json = instance.encode(original);
        assertEquals("{\"id\":100,\"name\":\"john\"}", json);
        assertEquals(original, instance.decode(json, Employee.class, null));
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
