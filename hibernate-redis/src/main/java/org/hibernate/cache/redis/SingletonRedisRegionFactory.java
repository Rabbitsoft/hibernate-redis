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

import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.jedis.JedisClient;
import org.hibernate.cache.redis.util.JedisTool;
import org.hibernate.cfg.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A singleton RedisRegionFactory implementation.
 *
 * @author sunghyouk.bae@gmail.com
 * @since 13. 4. 6. 오전 12:31
 */
public class SingletonRedisRegionFactory extends AbstractRedisRegionFactory {

    private static final Logger log = LoggerFactory.getLogger(SingletonRedisRegionFactory.class);
    private static final boolean isTraceEnabled = log.isTraceEnabled();
    private static final boolean isDebugEnabled = log.isDebugEnabled();

    private static final AtomicInteger ReferenceCount = new AtomicInteger();

    private JedisClient jedisClient;

    public SingletonRedisRegionFactory(Properties props) {
        super(props);
        log.info("SingletonRedisRegionFactory를 생성했습니다.");
        this.jedisClient = JedisTool.createJedisClient(props);
    }

    @Override
    public void start(Settings settings, Properties properties) throws CacheException {
        log.info("Redis를 2차 캐시 저장소로 사용하는 RedisRegionFactory를 시작합니다...");

        this.settings = settings;
        try {
            if (jedisClient == null) {
                this.jedisClient = JedisTool.createJedisClient(props);
                ReferenceCount.incrementAndGet();
            }
            log.info("RedisRegionFactory를 시작했습니다!!!");
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void stop() {
        log.info("RedisRegionFactory 사용을 중지합니다...");
        // NOTE: Redis Database 를 다른 Application이 같이 사용할 경우 flushDb는 안된다. Region만 삭제해야 한다.
        if (ReferenceCount.decrementAndGet() == 0) {
            jedisClient = null;
        }
    }

    private static final long serialVersionUID = 5856244690750752287L;
}
