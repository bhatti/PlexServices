package com.plexobject.bridge.eb;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.plexobject.bridge.eb.EventBusToJmsBridgeTest.TestUser;
import com.plexobject.encode.CodecType;

public class EventBusToJmsEntryTest {
    @Test
    public void testCreateEventBusToJmsEntryJmsToEB() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL,
                "queue://{scope}-query-user-service-queue", "query-user-channel",
                TestUser.class.getName(), 1, 0);
        assertEquals(CodecType.JSON, entry.getCodecType());
        assertEquals(EventBusToJmsEntry.Type.JMS_TO_EB_CHANNEL, entry.getType());
        assertEquals("queue://{scope}-query-user-service-queue",
                entry.getSource());
        assertEquals("query-user-channel", entry.getTarget());
        assertEquals(TestUser.class.getName(), entry.getRequestType());
        assertEquals(TestUser.class, entry.getRequestTypeClass());
    }

    @Test
    public void testCreateEventBusToJmsEntryEbToJms() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry(CodecType.JSON,
                EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, "create-user",
                "queue://{scope}-assign-bugreport-service-queue",
                TestUser.class.getName(), 1, 0);
        assertEquals(CodecType.JSON, entry.getCodecType());
        assertEquals(EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, entry.getType());
        assertEquals("create-user", entry.getSource());
        assertEquals("queue://{scope}-assign-bugreport-service-queue",
                entry.getTarget());
        assertEquals(TestUser.class.getName(), entry.getRequestType());
        assertEquals(TestUser.class, entry.getRequestTypeClass());
    }

    @Test
    public void testGetSetRequestType() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry();
        entry.setRequestType(TestUser.class.getName());
        assertEquals(TestUser.class.getName(), entry.getRequestType());
    }

    @Test
    public void testGetSetTarget() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry();
        entry.setTarget("queue://{scope}-assign-bugreport-service-queue");
        assertEquals("queue://{scope}-assign-bugreport-service-queue",
                entry.getTarget());
    }

    @Test
    public void testGetSetSource() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry();
        entry.setSource("create-user");
        assertEquals("create-user", entry.getSource());
    }

    @Test
    public void testGetSetType() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry();
        entry.setType(EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS);
        assertEquals(EventBusToJmsEntry.Type.EB_CHANNEL_TO_JMS, entry.getType());
    }

    @Test
    public void testGetSetCodecType() throws Exception {
        EventBusToJmsEntry entry = new EventBusToJmsEntry();
        entry.setCodecType(CodecType.JSON);
        assertEquals(CodecType.JSON, entry.getCodecType());
    }
}
