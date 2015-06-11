package com.plexobject.service;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.domain.Constants;
import com.plexobject.encode.CodecType;
import com.plexobject.encode.ObjectCodecFactory;
import com.plexobject.handler.AbstractResponseDispatcher;
import com.plexobject.handler.Request;
import com.plexobject.handler.Response;

public class InterceptorAwareRequestBuilder extends Request.Builder {
    public static Request buildRequest(Protocol protocol, Method method,
            String endpoint, Map<String, Object> params,
            Map<String, Object> headers, String input, CodecType codecType,
            Class<?> payloadClass, AbstractResponseDispatcher dispatcher,
            InterceptorsLifecycle interceptors) {
        String sessionId = (String) params.get(Constants.SESSION_ID);

        // check if input request overrided the codec using Accept header
        codecType = CodecType.fromAcceptHeader(
                (String) params.get(Constants.ACCEPT), codecType);

        Response response = new Response(new HashMap<String, Object>(),
                new HashMap<String, Object>(), null);
        response.setCodecType(codecType);
        if (interceptors != null) {
            // apply input interceptors
            if (interceptors.hasInputInterceptors()) {
                for (Interceptor<String> interceptor : interceptors
                        .getInputInterceptors()) {
                    input = interceptor.intercept(input);
                }
            }
        }
        // decode text input into object
        // Note that we may not know payloadClas at this time so we will try it
        // if available, otherwise
        // we will do it in ServiceInvocationHelper
        Object payload = payloadClass != null && payloadClass != Void.class ? ObjectCodecFactory
                .getInstance().getObjectCodec(codecType)
                .decode(input, payloadClass, params)
                : input;

        // build request
        Request req = Request.builder().setProtocol(protocol).setMethod(method)
                .setEndpoint(endpoint).setProperties(params)
                .setHeaders(headers).setCodecType(codecType)
                .setPayload(payload).setSessionId(sessionId)
                .setResponse(response).setResponseDispatcher(dispatcher)
                .build();
        return req;
    }
}
