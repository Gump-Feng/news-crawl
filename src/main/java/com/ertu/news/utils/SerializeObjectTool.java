package com.ertu.news.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeObjectTool {
	// 序列化对象
	public static byte[] serialize(Object object) {
		ObjectOutputStream oos = null;
		byte[] bytes = null;
		try {
			// 序列化
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			bytes = baos.toByteArray();
		} catch (Exception e) {
		}
		return bytes;
	}

	// 反序列化
	public static Object unserialize(byte[] bytes) {
		ByteArrayInputStream bais = null;
		Object readObject = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			readObject = ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return readObject;
	}
}
