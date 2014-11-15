package com.plexobject.encode.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.plexobject.bridge.eb.EventBusToJmsEntry;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.Employee;

public class JsonObjectCodecTest {
    private static class Obj {
        @SuppressWarnings("unused")
		private String name;
    }

    JsonObjectCodec instance = new JsonObjectCodec();

    @Test
    public void testEncodeNull() {
        String json = instance.encode(null);
        assertNull(json);
    }

    @Test
    public void testDecodeNull() {
        Object obj = instance.jsonDecode(null, null);
        assertNull(obj);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeBadJson() {
        instance.jsonDecode("[{]}", null);
    }

    @Test
    public void testEncodeString() {
        String json = instance.encode("");
        assertEquals("", json);
    }

    @Test
    public void testEncodeStringBuilder() {
        String json = instance.encode(new StringBuilder());
        assertEquals("", json);
    }

    @Test
    public void testEncodePrivateObject() {
        String json = instance.encode(new Obj());
        assertEquals("{}", json);
    }

    @Test
    public void testGetType() {
        assertEquals(CodecType.JSON, instance.getType());
    }

    @Test
    public void testDecodeArray() {
        String mappingJson = "[{\"codecType\":\"JSON\",\"path\":\"/projects/{projectId}/bugreports/{id}/assign\",\"method\":\"POST\",\"destination\":\"queue:{scope}-assign-bugreport-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects/{projectId}/bugreports\",\"method\":\"GET\",\"destination\":\"queue:{scope}-query-project-bugreport-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/users\",\"method\":\"GET\",\"destination\":\"queue:{scope}-query-user-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects\",\"method\":\"GET\",\"destination\":\"queue:{s    cope}-query-projects-service\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/bugreports\",\"method\":\"GET\",\"destination\":\"queue:{scope}-bugreports-service-queue\",\"timeoutSecs\":5},{\"c    odecType\":\"JSON\",\"path\":\"/projects/{id}/membership/add\",\"method\":\"POST\",\"destination\":\"queue:{scope}-add-project-member-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects/{id}/membership/remove\",\"method\":\"POST\",\"destination\":\"queue:{scope}-remove-project-member-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects/{projectId}/bugreports\",\"method\":\"POST\",\"destination\":\"queue:{scope}-create-bugreport-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/users\",\"method\":\"POST\",\"destination\":\"queue:{scope}-create-user-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects\",\"method\":\"POST\",\"destination\":\"queue:{scope}-create-projects-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/users/{id}\",\"method\":\"POST\",\"destination\":\"queue:{scope}-update-user-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/users/{id}/delete\",\"method\":\"POST\",\"destination\":\"queue:{scope}-delete-user-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects/{id}\",\"method\":\"POST\",\"destination\":\"queue:{scope}-update-project-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/projects/{projectId}/bugreports/{id}\",\"method\":\"POST\",\"destination\":\"queue:{scope}-update-bugreport-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/login\",\"method\":\"POST\",\"destination\":\"queue:{scope}-login-service-queue\",\"timeoutSecs\":5},{\"codecType\":\"JSON\",\"path\":\"/logs\",\"method\":\"POST\",\"destination\":\"queue:{scope}-log-service-queue\",\"asynchronous\":true}]";
        Collection<EventBusToJmsEntry> entries = new JsonObjectCodec().decode(
                mappingJson, new TypeReference<List<EventBusToJmsEntry>>() {
                });
        assertEquals(16, entries.size());
    }

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
