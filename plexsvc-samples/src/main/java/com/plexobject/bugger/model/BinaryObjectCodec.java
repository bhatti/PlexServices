package com.plexobject.bugger.model;

import java.io.IOException;
import java.util.Map;

import org.msgpack.MessagePack;

import com.plexobject.encode.ObjectCodec;

public class BinaryObjectCodec implements ObjectCodec {
	private final MessagePack msgpack = new MessagePack();

	@Override
	public <T> String encode(T obj) {
		try {
			byte[] bytes = msgpack.write(obj);
			return new String(bytes, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException("Failed to convert " + obj, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T decode(String text, Class<?> type, Map<String, Object> params) {
		try {
			byte[] bytes = text.getBytes("UTF-8");
			return (T) msgpack.read(bytes, type);
		} catch (IOException e) {
			throw new RuntimeException("Failed to convert " + text, e);
		}
	}

}
