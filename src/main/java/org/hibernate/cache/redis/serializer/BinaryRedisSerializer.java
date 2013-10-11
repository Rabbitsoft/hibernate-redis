/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hibernate.cache.redis.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializer for Redis Key or Value.
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class BinaryRedisSerializer<T> implements RedisSerializer<T> {

	@Override
	public byte[] serialize(T graph) {
		if (graph == null) return SerializationTool.EMPTY_ARRAY;

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = null; 
		try { 
			oos = new ObjectOutputStream(os);;
			oos.writeObject(graph);
			oos.flush();

			return os.toByteArray();
		} catch (Exception ex) {
			
		} finally {
			try {
				oos.close();
			} catch (Exception ex) {
			}
			try {
				os.close();
			} catch (Exception ex) {
			}
		}
		return SerializationTool.EMPTY_ARRAY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(byte[] bytes) {
		if (SerializationTool.isEmpty(bytes))
			return null;

		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try {
			 ois = new ObjectInputStream(is);
			 return (T) ois.readObject();
		} catch (Exception ex) {
			return (T) null;
		} finally {
			try {
				ois.close();
			} catch (Exception ex) {
			}
			try {
				is.close();
			} catch (Exception ex) {
			}			
		}
	}
}