package com.plexobject.handler;

import java.util.Map;

import org.apache.log4j.Logger;

import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodec;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.service.Interceptor;
import com.plexobject.service.OutgoingInterceptorsLifecycle;
import com.plexobject.util.ExceptionUtils;

public abstract class AbstractResponseDispatcher implements ResponseDispatcher {
    private static final Logger logger = Logger
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
        Object encodedReply = encode(response);
        // execute output interceptors if available
        if (outgoingInterceptorsLifecycle != null
                && outgoingInterceptorsLifecycle.hasOutputInterceptors()) {
            BasePayload payload = response;
            payload.setContents(encodedReply);
            for (Interceptor<BasePayload<String>> interceptor : outgoingInterceptorsLifecycle
                    .getOutputInterceptors()) {
                payload = interceptor.intercept(payload);
            }
            encodedReply = payload.getContents();
        }

        doSend(response, encodedReply);
    }

    protected Object encode(Response response) {
        Object payload = response.getContents();

        if (response.getCodecType() == CodecType.SERVICE_SPECIFIC) {
            if (response.getContents() instanceof byte[]) {
                return (byte[]) response.getContents();
            } else if (response.getContents() instanceof String) {
                return response.getContentsAs();
            }
        } else if (payload instanceof Exception) {
            logger.warn("PLEXSVC " + getClass().getSimpleName()
                    + " Error received " + payload);
            Map<String, Object> resp = ExceptionUtils
                    .toErrors((Exception) payload);
            for (String name : Response.HEADER_PROPERTIES) {
                Object value = resp.get(name);
                if (value != null) {
                    response.setProperty(name, value);
                }
            }
            return ObjectCodecFactory.getInstance()
                    .getObjectCodec(response.getCodecType()).encode(resp);
        } else if (response.getContents() instanceof String) {
            return response.getContentsAs();
        }
        ObjectCodec codec = ObjectCodecFactory.getInstance().getObjectCodec(
                response.getCodecType());
        if (codec == null) {
            throw new IllegalArgumentException("Codec not supported for "
                    + response.getCodecType());
        }
        return codec.encode(response.getContentsAs());
    }

    protected void doSend(Response response, Object encodedReply) {

    }

}
