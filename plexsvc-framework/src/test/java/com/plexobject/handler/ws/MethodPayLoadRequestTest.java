package com.plexobject.handler.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.NettyRequest;
import com.plexobject.handler.Request;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;

public class MethodPayLoadRequestTest {

    @Test
    public void testGetMethodNameAndPayload() throws Exception {
        String[] stringPayloads = { "{get:'   '}", "{get:  { }  }",
                "{'get':'myid'}", "{  \"get\"\n\t:'myid  \n' \t}",
                "  {' get' : \"myid\"  } \n\t", "{\"get':'myid' \n }\t" };
        String[] stringResult = { "", "{ }", "myid", "myid", "myid", "myid" };
        String[] intPayloads = { "{'get':2}", "{  'get'\n\t:3 \t}",
                "  {' get' : 4  } \n\t", "{'get':12345 \n }\t" };
        String[] intResult = { "2", "3", "4", "12345" };
        String[] objPayloads = { "{'  get' : { 'name' : 'myid'} }",
                "{'  get' : {\"name\" : 2 } }" };
        String[] objResults = { "{ 'name' : 'myid'}", "{\"name\" : 2 }" };
        String[] badPayloads = { "my text", " 345 " };
        String[] badResult = { "my text", "345" };
        Map<String, Object> properties = new HashMap<>();
        for (int i = 0; i < stringPayloads.length; i++) {
            Request request = newRequest(stringPayloads[i], properties);
            MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                    .getMethodNameAndPayloads(request, null);
            assertEquals(1, methodPayLoadRequest.requests.size());
            MethodPayLoadInfo info = methodPayLoadRequest.requests.get(0);
            assertEquals("get", info.method);
            assertEquals(stringResult[i], info.payload);
        }

        for (int i = 0; i < intPayloads.length; i++) {
            Request request = newRequest(intPayloads[i], properties);
            MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                    .getMethodNameAndPayloads(request, null);
            assertEquals(1, methodPayLoadRequest.requests.size());
            MethodPayLoadInfo info = methodPayLoadRequest.requests.get(0);
            assertEquals("get", info.method);
            assertEquals(intResult[i], info.payload);
        }
        for (int i = 0; i < objPayloads.length; i++) {
            Request request = newRequest(objPayloads[i], properties);
            MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                    .getMethodNameAndPayloads(request, null);
            assertEquals(1, methodPayLoadRequest.requests.size());
            MethodPayLoadInfo info = methodPayLoadRequest.requests.get(0);
            assertEquals("get", info.method);
            assertEquals(objResults[i], info.payload);
        }
        for (int i = 0; i < badPayloads.length; i++) {
            Request request = newRequest(badPayloads[i], properties);
            try {
                MethodPayLoadRequest.getMethodNameAndPayloads(request, null);
                fail("should have failed");
            } catch (IllegalArgumentException e) {
            }
        }
        //
        properties.put("methodName", "get");
        for (int i = 0; i < badPayloads.length; i++) {
            Request request = newRequest(badPayloads[i], properties);
            MethodPayLoadRequest methodPayLoadRequest = MethodPayLoadRequest
                    .getMethodNameAndPayloads(request, null);
            assertEquals(1, methodPayLoadRequest.requests.size());
            MethodPayLoadInfo info = methodPayLoadRequest.requests.get(0);
            assertEquals("get", info.method);
            assertEquals(badResult[i], info.payload);
        }

    }

    private static Request newRequest(String payload,
            Map<String, Object> properties) {
        Request request = NettyRequest.builder().setProtocol(Protocol.HTTP)
                .setMethod(RequestMethod.GET).setEndpoint("/w")
                .setProperties(properties).setHeaders(properties)
                .setCodecType(CodecType.JSON).setContents(payload)
                .setResponseDispatcher(new AbstractResponseDispatcher() {
                }).build();
        return request;
    }}
