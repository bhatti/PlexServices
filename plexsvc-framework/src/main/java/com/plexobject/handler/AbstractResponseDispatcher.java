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
            for (Interceptor<BasePayload<Object>> interceptor : outgoingInterceptorsLifecycle
                    .getOutputInterceptors()) {
                payload = interceptor.intercept(payload);
            }
            encodedReply = payload.getContents();
        }

        doSend(response, encodedReply);
    }

    protected Object encode(Response response) {
        Object payload = response.getContents();
        CodecType codecType = response.getCodecType();
        if (codecType == CodecType.SERVICE_SPECIFIC) {
            if (payload instanceof byte[]) {
                return (byte[]) payload;
            } else if (payload instanceof String) {
                return payload;
            } else {
                logger.warn("PLEXSVC Unknown content " + payload);
                codecType = CodecType.JSON;
            }
        }
        if (payload instanceof Throwable) {
            logger.warn("PLEXSVC " + getClass().getSimpleName()
                    + " Error received " + payload, (Throwable) payload);
            Map<String, Object> resp = ExceptionUtils
                    .toErrors((Throwable) payload);
            for (String name : Response.HEADER_PROPERTIES) {
                Object value = resp.get(name);
                if (value != null) {
                    response.setProperty(name, value);
                }
            }
            return ObjectCodecFactory.getInstance().getObjectCodec(codecType)
                    .encode(resp);
        } else if (payload instanceof String) {
            return payload;
        }
        ObjectCodec codec = ObjectCodecFactory.getInstance().getObjectCodec(
                codecType);
        if (codec == null) {
            throw new IllegalArgumentException("Codec not supported for "
                    + response.getCodecType());
        }
        return codec.encode(response.getContentsAs());
    }

    protected void doSend(Response response, Object encodedReply) {

    }

}
