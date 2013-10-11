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

import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.jedis.JedisClient;
import org.hibernate.cache.redis.util.JedisTool;
import org.hibernate.cfg.Settings;

/**
 * A non-singleton RedisRegionFactory implementation.
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class RedisRegionFactory extends AbstractRedisRegionFactory {
	private static final long serialVersionUID = -4001799548536818404L;

    private JedisClient jedisClient;
    
    public RedisRegionFactory(Properties props) {
        super(props);
    }

    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        logger.info("Starting RedisRegionFactory 2nd Level Cache");
        this.settings = settings;
        try {
            if (jedisClient == null) {
                this.jedisClient = JedisTool.createJedisClient(props);
            }
            logger.info("RedisRegionFactory connected to server");
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void stop() {
        if (jedisClient == null) { 
        	return;
        }
        logger.debug("RedisRegionFactory stopping");
        try {
            jedisClient.flushDb();
            jedisClient = null;
            
            logger.info("RedisRegionFactory stopped");
        } catch (Exception e) {
            logger.error("Jedis client failed to stop.", e);
            throw new CacheException(e);
        }
    }
}
