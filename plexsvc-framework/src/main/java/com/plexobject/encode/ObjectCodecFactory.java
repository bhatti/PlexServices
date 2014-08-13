package com.plexobject.encode;

import java.util.HashMap;
import java.util.Map;

import com.plexobject.encode.json.JsonObjectCodec;
import com.plexobject.encode.text.TextObjectCodec;

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
		encoders.put(CodecType.TEXT, new TextObjectCodec());
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
