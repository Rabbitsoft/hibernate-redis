package org.hibernate.cache.redis.strategy;

import org.hibernate.cache.redis.regions.RedisTransactionalDataRegion;
import org.hibernate.cfg.Settings;

/**
 * org.hibernate.cache.redis.strategy.ItemValueExtractor
 * 
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class ItemValueExtractor<T extends RedisTransactionalDataRegion> extends AbstractReadWriteRedisAccessStrategy<T> {
    /** Creates a read/write cache access strategy around the given cache region. */
    public ItemValueExtractor(T region, Settings settings) {
        super(region, settings);
    }

    @SuppressWarnings("unchecked")
	public static <T> T getValue(final Object entry) {
        if (!(entry instanceof Item)) {
            throw new IllegalArgumentException("Entry needs to be of type " + Item.class.getName());
        }
        return ((T) ((Item) entry).getValue());
    }
}
