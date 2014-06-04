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

package org.hibernate.cache.redis.util;

import org.hibernate.cache.redis.jedis.JedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * org.hibernate.cache.redis.util.JedisTool
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public final class JedisTool {
    private static final Logger logger = LoggerFactory.getLogger(JedisTool.class);
    
    private JedisTool() { }

    /**
     * Creates {@link redis.clients.jedis.JedisPool}.
     */
    public static JedisPool createJedisPool(Properties props) {
        String host = props.getProperty("redis.host", "localhost");
        Integer port = Integer.decode(props.getProperty("redis.port", "6379"));
        Integer timeout = Integer.decode(props.getProperty("redis.timeout", "2000")); // msec
        String password = props.getProperty("redis.password", null);
        Integer database = Integer.decode(props.getProperty("redis.database", "0"));

        logger.info("Creating JedisPool host=[{}], port=[{}], timeout=[{}], password=[{}], database=[{}]", 
       		host, port, timeout, password, database);
        
        return new JedisPool(createJedisPoolConfig(), host, port, timeout, password, database);
    }

    /**
     * Creates {@link org.hibernate.cache.redis.jedis.JedisClient}.
     */
    public static JedisClient createJedisClient(Properties props) {
        Integer expiryInSeconds = Integer.decode(props.getProperty("redis.expiryInSeconds", "120"));  // 120 seconds
        Integer database = Integer.decode(props.getProperty("redis.database", "0"));
        
        JedisClient client = new JedisClient(createJedisPool(props), expiryInSeconds);
        if (database != 0) {
        	client.setDatabase(database);
        }
        return client;
    }

    private static JedisPoolConfig createJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxActive(32);
        poolConfig.setMinIdle(2);
        return poolConfig;
    }
}
