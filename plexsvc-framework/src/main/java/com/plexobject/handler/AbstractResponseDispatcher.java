package com.plexobject.handler;

import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.service.Interceptor;
import com.plexobject.service.OutgoingInterceptorsLifecycle;
import com.plexobject.util.ExceptionUtils;

public abstract class AbstractResponseDispatcher implements ResponseDispatcher {
    private static final Logger log = Logger
            .getLogger(AbstractResponseDispatcher.class);
    private OutgoingInterceptorsLifecycle outgoingInterceptorsLifecycle;

    public OutgoingInterceptorsLifecycle getOutgoingInterceptorsLifecycle() {
        return outgoingInterceptorsLifecycle;
    }

    public void setOutgoingInterceptorsLifecycle(
            OutgoingInterceptorsLifecycle outgoingInterceptorsLifecycle) {
        this.outgoingInterceptorsLifecycle = outgoingInterceptorsLifecycle;
    }

    /**
     * This method serializes response in text and sends back to client
     */
    @Override
    public final void send(Response response) {
        // execute response interceptors if available
        if (outgoingInterceptorsLifecycle != null
                && outgoingInterceptorsLifecycle.hasResponseInterceptors()) {
            for (Interceptor<Response> interceptor : outgoingInterceptorsLifecycle
                    .getResponseInterceptors()) {
                response = interceptor.intercept(response);
            }
        }
        // nothing to send
        String replyText = encode(response);
        // execute output interceptors if available
        if (outgoingInterceptorsLifecycle != null
                && outgoingInterceptorsLifecycle.hasOutputInterceptors()) {

            for (Interceptor<String> interceptor : outgoingInterceptorsLifecycle
                    .getOutputInterceptors()) {
                replyText = interceptor.intercept(replyText);
            }
        }
        doSend(response, replyText);
    }

    protected String encode(Response response) {
        Object payload = response.getPayload();
        String replyText = null;
        if (payload instanceof Exception) {
            log.warn("Error received " + payload);
            Map<String, Object> resp = ExceptionUtils
                    .toErrors((Exception) payload);
            replyText = ObjectCodecFactory.getInstance()
                    .getObjectCodec(response.getCodecType()).encode(resp);
            for (String name : Response.HEADER_PROPERTIES) {
                Object value = resp.get(name);
                if (value != null) {
                    response.setProperty(name, value);
                }
            }
        } else {
            replyText = response.getPayload() instanceof String ? (String) response
                    .getPayload() : ObjectCodecFactory.getInstance()
                    .getObjectCodec(response.getCodecType())
                    .encode(response.getPayload());
        }
        return replyText;
    }

    protected void doSend(Response response, String payload) {

    }

}
