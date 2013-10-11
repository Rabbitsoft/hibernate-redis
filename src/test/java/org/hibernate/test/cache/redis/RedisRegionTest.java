package org.hibernate.test.cache.redis;

import org.hibernate.cache.redis.RedisRegionFactory;
import org.hibernate.cache.redis.strategy.ItemValueExtractor;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import java.util.Map;

/**
 * org.hibernate.test.cache.redis.RedisRegionTest
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class RedisRegionTest extends RedisTest {

    @Override
    protected void configCache(Configuration cfg) {
        cfg.setProperty(Environment.CACHE_REGION_FACTORY, RedisRegionFactory.class.getName());
        cfg.setProperty(Environment.CACHE_PROVIDER_CONFIG, "redis.properties");
    }

    @Override
    protected Map<?, ?> getMapFromCacheEntry(final Object entry) {
        final Map<?, ?> map;
        
        String className = "org.hibernate.cache.redis.strategy.AbstractReadWriteRedisAccessStrategy$Item";
        if (entry.getClass().getName().equals(className)) {
            map = ItemValueExtractor.getValue(entry);
        } else {
            map = (Map<?, ?>) entry;
        }
        return map;
    }
}
