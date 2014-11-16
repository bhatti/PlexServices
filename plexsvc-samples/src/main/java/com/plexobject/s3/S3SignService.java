package com.plexobject.s3;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.plexobject.domain.ValidationException;
import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.ServiceConfig;
import com.plexobject.service.ServiceConfig.Protocol;
import com.plexobject.service.ServiceConfig.Method;

@ServiceConfig(protocol = Protocol.HTTP, requestClass = Void.class, endpoint = "/sign_auth", method = Method.GET, codec = CodecType.JSON)
public class S3SignService implements RequestHandler {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private String key;

    public S3SignService(String key) {
        this.key = key;
    }

    @Override
    public void handle(Request request) {
        String toSign = request.getProperty("to_sign");
        ValidationException
                .builder()
                .assertNonNull(toSign, "undefined_to_sign", "to_sign",
                        "to_sign not specified").end();
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
                    HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes());
            request.getResponseDispatcher().send(
                    DatatypeConverter.printBase64Binary(rawHmac));
        } catch (Exception e) {
            request.getResponseDispatcher().send(
                    new SignatureException("Failed to generate HMAC : "
                            + e.getMessage()));
        }
    }
}
