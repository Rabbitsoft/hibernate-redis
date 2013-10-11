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

package org.hibernate.cache.redis;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.jedis.JedisClient;
import org.hibernate.cache.redis.util.JedisTool;
import org.hibernate.cfg.Settings;

/**
 * A singleton RedisRegionFactory implementation.
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class SingletonRedisRegionFactory extends AbstractRedisRegionFactory {
	private static final long serialVersionUID = -6751426029598427653L;

	private static final AtomicInteger ReferenceCount = new AtomicInteger();

	private JedisClient jedisClient;

	public SingletonRedisRegionFactory(Properties props) {
		super(props);
		
		logger.info("SingletonRedisRegionFactory initialized.");
		this.jedisClient = JedisTool.createJedisClient(props);
	}

	@Override
	public void start(Settings settings, Properties properties) throws CacheException {
        logger.info("Starting serialVersionUID 2nd Level Cache");
        
		this.settings = settings;
		try {
			if (jedisClient == null) {
				this.jedisClient = JedisTool.createJedisClient(props);
				ReferenceCount.incrementAndGet();
			}
			logger.info("RedisRegionFactory started");
		} catch (Exception e) {
			throw new CacheException(e);
		}
	}

	@Override
	public void stop() {
		logger.debug("RedisRegionFactory is stopping");
		try {
			if (jedisClient != null) {
				if (ReferenceCount.decrementAndGet() == 0) {
					jedisClient.flushDb();
					logger.debug("flush db");
				}
			}
		} catch (Exception e) {
			logger.error("jedisClient failed to stop.", e);
		} finally {
			logger.info("RedisRegionFactory stopped.");
			jedisClient = null;
		}
	}
}
