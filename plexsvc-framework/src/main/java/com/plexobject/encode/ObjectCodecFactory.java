package com.plexobject.encode;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.encode.binary.BinaryObjectCodec;
import com.plexobject.encode.gson.GsonObjectCodec;
import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.text.TextObjectCodec;
import com.plexobject.encode.xml.XmlObjectCodec;

/**
 * This factory class provides implementation of code based on type
 * 
 * @author shahzad bhatti
 *
 */
public class ObjectCodecFactory {
    private Map<CodecType, ObjectCodec> encoders = new HashMap<>();
    private static final ObjectCodecFactory INSTANCE = new ObjectCodecFactory();

    private ObjectCodecFactory() {
        encoders.put(CodecType.JSON, new JsonObjectCodec());
        encoders.put(CodecType.GSON, new GsonObjectCodec());
        encoders.put(CodecType.TEXT, new TextObjectCodec());
        encoders.put(CodecType.BINARY, new BinaryObjectCodec());
        encoders.put(CodecType.XML, new XmlObjectCodec());
    }

    public static ObjectCodecFactory getInstance() {
        return INSTANCE;
    }

    public void registerEncoder(CodecType type, ObjectCodec codec) {
        encoders.put(type, codec);
    }

    public ObjectCodec getObjectCodec(CodecType type) {
        ObjectCodec codec = encoders.get(type);
        if (codec == null) {
            throw new IllegalArgumentException("Unsupported codec " + type);
        }
        return codec;
    }
}
