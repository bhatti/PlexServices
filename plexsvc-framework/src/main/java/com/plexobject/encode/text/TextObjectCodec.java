package com.plexobject.encode.text;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.plexobject.encode.AbstractObjectCodec;
import com.plexobject.encode.CodecType;

public class TextObjectCodec extends AbstractObjectCodec {
	@Override
	public CodecType getType() {
		return CodecType.TEXT;
	}

	@Override
	public <T> String encode(T obj) {
		if (obj instanceof String) {
			return (String) obj;
		} else if (obj != null) {
			return obj.toString();
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
		if (text != null && text.length() > 0) {
			try {
				Constructor<?> ctor = type.getConstructor(String.class);
				if (ctor != null) {
					T obj = (T) ctor.newInstance(text);
					populateProperties(params, obj);
					return obj;
				}
				throw new RuntimeException(
				        "Failed to find string constructor for " + type
				                + " with " + text);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException("Failed to instantiate " + type
				        + " with " + text, e);
			}
		} else {
			return propertyDecode(params, type);
		}
	}
}
