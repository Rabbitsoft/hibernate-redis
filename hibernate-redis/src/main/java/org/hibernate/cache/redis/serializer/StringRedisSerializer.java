package org.hibernate.cache.redis.serializer;

import java.nio.charset.Charset;

/**
 * org.hibernate.cache.redis.serializer.StringRedisSerializer
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 10. 2. 오후 9:39
 */
public class StringRedisSerializer implements RedisSerializer<String> {

    private static final byte[] EMTPY_BYTES = new byte[0];
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Override
    public byte[] serialize(String graph) {
        if (graph == null || graph.length() == 0)
            return EMTPY_BYTES;
        return graph.getBytes(UTF_8);
    }

    @Override
    public String deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return "";
        return new String(bytes, UTF_8);
    }
}
