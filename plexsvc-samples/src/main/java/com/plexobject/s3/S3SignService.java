package com.plexobject.s3;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.plexobject.encode.CodecType;
import com.plexobject.handler.Request;
import com.plexobject.handler.RequestHandler;
import com.plexobject.service.Protocol;
import com.plexobject.service.RequestMethod;
import com.plexobject.service.ServiceConfig;
import com.plexobject.validation.Field;
import com.plexobject.validation.RequiredFields;

@ServiceConfig(protocol = Protocol.HTTP, endpoint = "/sign_auth", method = RequestMethod.GET, codec = CodecType.JSON)
@RequiredFields({ @Field(name = "to_sign") })
public class S3SignService implements RequestHandler {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private String key;

    public S3SignService(String key) {
        this.key = key;
    }

    @Override
    public void handle(Request request) {
        String toSign = request.getStringProperty("to_sign");
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
                    HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes());
            request.getResponse().setPayload(
                    DatatypeConverter.printBase64Binary(rawHmac));
        } catch (Exception e) {
            request.getResponse().setPayload(
                    new SignatureException("Failed to generate HMAC : "
                            + e.getMessage()));
        }
    }
}
