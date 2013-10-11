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

package org.hibernate.cache.redis.regions;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.jedis.JedisClient;
import org.hibernate.cache.redis.strategy.RedisAccessStrategyFactory;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.TransactionalDataRegion;
import org.hibernate.cfg.Settings;

import java.util.Properties;

/**
 * RedisTransactionalDataRegion
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class RedisTransactionalDataRegion extends RedisDataRegion implements TransactionalDataRegion {
    /**
     * Hibernate settings associated with the persistence unit.
     */
    protected final Settings settings;

    /**
     * Metadata associated with the objects sorted in the region
     */
    protected final CacheDataDescription metadata;

    
    public RedisTransactionalDataRegion(RedisAccessStrategyFactory accessStrategyFactory, 
    		JedisClient jedisClient, String regionName, Settings settings, CacheDataDescription metadata, Properties props) {
        super(accessStrategyFactory, jedisClient, regionName, props);

        this.settings = settings;
        this.metadata = metadata;
    }

    public Settings getSettings() {
        return settings;
    }

    @Override
    public boolean isTransactionAware() {
        return false;
    }

    @Override
    public CacheDataDescription getCacheDataDescription() {
        return metadata;
    }

    public Object get(Object key) throws CacheException {
    	logger.trace("Returning key=[{}]", key);
        try {
            return jedisClient.get(getName(), key);
        } catch (Exception e) {
            return new CacheException(e);
        }
    }


    public void put(Object key, Object value) throws CacheException {
        logger.trace("Setting key=[{}], value=[{}]", key, value);
        try {
            jedisClient.set(getName(), key, value);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void remove(Object key) throws CacheException {
        logger.trace("Removing key=[{}]", key);
        try {
            jedisClient.del(getName(), key);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }


    public void clear() throws CacheException {
        logger.trace("Clearing regionName=[{}]", getName());
        try {
            jedisClient.deleteRegion(getName());
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void writeLock(Object key) {
        // nothing to do.
    }

    public void writeUnlock(Object key) {
        // nothing to do.
    }

    public void readLock(Object key) {
        // nothing to do.
    }

    public void readUnlock(Object key) {
        // nothing to do.
    }

    public void evict(Object key) throws CacheException {
    	logger.trace("Evicting key=[{}]", key);
        try {
            jedisClient.del(getName(), key);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void evictAll() throws CacheException {
        logger.trace("Evicting all regionName=[{}]", getName());
        try {
            jedisClient.deleteRegion(getName());
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    /**
     * Returns <code>true</code> if the locks used by the locking methods of this region are the independent of the cache.
     * <p/>
     * Independent locks are not locked by the cache when the cache is accessed directly.  This means that for an independent lock
     * lock holds taken through a region method will not block direct access to the cache via other means.
     */
    public final boolean locksAreIndependentOfCache() {
        return false;
    }
}
