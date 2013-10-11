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
import org.hibernate.cache.redis.util.Timestamper;
import org.hibernate.cache.spi.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Base redis region implementation.
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 * @since 13. 4. 5. 8:48PM
 */
public abstract class RedisDataRegion implements Region {

    private static final String CACHE_LOCK_TIMEOUT_PROPERTY = "io.redis.hibernate.cache_lock_timeout";
    private static final String EXPIRE_IN_SECONDS = "redis.expireInSeconds";
    
    private static final int DEFAULT_CACHE_LOCK_TIMEOUT = 60 * 1000; // 60 seconds

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final RedisAccessStrategyFactory accessStrategyFactory;
    
    /**
     * Region name.
     */
    private final String name;

    /**
     * Redis client instance deal hibernate data region.
     */
    protected final JedisClient jedisClient;

    private final int cacheLockTimeout; // milliseconds
    private final int expireInSeconds; // seconds
    protected boolean regionDeleted = false;

    public boolean isRegionDeleted() {
		return regionDeleted;
	}

	public void setRegionDeleted(boolean regionDeleted) {
		this.regionDeleted = regionDeleted;
	}

	public RedisAccessStrategyFactory getAccessStrategyFactory() {
		return accessStrategyFactory;
	}

	public JedisClient getJedisClient() {
		return jedisClient;
	}

	public int getCacheLockTimeout() {
		return cacheLockTimeout;
	}

	public int getExpireInSeconds() {
		return expireInSeconds;
	}

    @Override
    public long nextTimestamp() {
        return Timestamper.next();
    }

    @Override
    public int getTimeout() {
        return cacheLockTimeout;
    }	

	protected RedisDataRegion(RedisAccessStrategyFactory accessStrategyFactory, JedisClient jedisClient, String regionName, Properties props) {
        logger.trace("RedisDataRegion ctor. region name=[{}]", regionName);
        
        this.accessStrategyFactory = accessStrategyFactory;
        this.jedisClient = jedisClient;
        this.name = regionName;

        this.cacheLockTimeout = Integer.decode(props.getProperty(
        	CACHE_LOCK_TIMEOUT_PROPERTY, String.valueOf(DEFAULT_CACHE_LOCK_TIMEOUT)));
        
        this.expireInSeconds = Integer.decode(props.getProperty(EXPIRE_IN_SECONDS, "120"));
    }

    public String getName() {
        return name;
    }

    @Override
    public void destroy() throws CacheException {
        try {
            logger.debug("Destroying region=[{}]", getName());
            if (!regionDeleted) {
                jedisClient.deleteRegion(name);
                regionDeleted = true;
            }
        } catch (Exception ignored) {
            logger.info("Failed to destroy region");
        }
    }

    @Override
    public boolean contains(Object key) {
        try {
            return jedisClient.exists(name, key);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public long getSizeInMemory() {
        try {
            return jedisClient.dbSize();
        } catch (Throwable t) {
            logger.warn("Failed to call jedis command:", t);
            return -1;
        }
    }

    @Override
    public long getElementCountInMemory() {
        try {
            return jedisClient.keysInRegion(name).size();
        } catch (Exception e) {
            logger.warn("Failed to call jedis command:", e);
            return -1;
        }
    }

    @Override
    public long getElementCountOnDisk() {
        return -1;
    }

    @Override
    public Map<Object, Object> toMap() {
        try {
            return jedisClient.hgetAll(name);
        } catch (Exception e) {
            logger.warn("Failed to create. Returning empty map", e);
            return Collections.emptyMap();
        }
    }
}
