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

package org.hibernate.cache.redis.strategy;

import org.hibernate.cache.redis.regions.RedisCollectionRegion;
import org.hibernate.cache.redis.regions.RedisEntityRegion;
import org.hibernate.cache.redis.regions.RedisNaturalIdRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * org.hibernate.cache.redis.strategy.RedisAccessStrategyFactoryImpl
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class RedisAccessStrategyFactoryImpl implements RedisAccessStrategyFactory {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public EntityRegionAccessStrategy createEntityRegionAccessStrategy(RedisEntityRegion entityRegion, AccessType accessType) {
    	logger.debug("create EntityRegionAccessStrategy. regionName=[{}], accessType=[{}]", entityRegion.getName(), accessType.getExternalName());

        switch (accessType) {
            case READ_ONLY:
                if (entityRegion.getCacheDataDescription().isMutable()) {
                    logger.warn("read-only cache configured for mutable entity regionName=[{}]", entityRegion.getName());
                }
                return new ReadOnlyRedisEntityRegionAccessStrategy(entityRegion, entityRegion.getSettings());
            case READ_WRITE:
                return new ReadWriteRedisEntityRegionAccessStrategy(entityRegion, entityRegion.getSettings());
            case NONSTRICT_READ_WRITE:
                return new NonStrictReadWriteRedisEntityRegionAccessStrategy(entityRegion, entityRegion.getSettings());
            case TRANSACTIONAL:
                return new TransactionalRedisEntityRegionAccessStrategy(entityRegion, entityRegion.getSettings());
            default:
                throw new IllegalArgumentException("unrecognized access strategy type [" + accessType + "]");
        }
    }

    @Override
    public CollectionRegionAccessStrategy createCollectionRegionAccessStrategy(RedisCollectionRegion collectionRegion, AccessType accessType) {
    	logger.debug("create CollectionRegionAccessStrategy. regionName=[{}], accessType=[{}]", collectionRegion.getName(), accessType.getExternalName());
        switch (accessType) {
            case READ_ONLY:
                if (collectionRegion.getCacheDataDescription().isMutable()) {
                    logger.warn("read-only cache configured for mutable entity collectionRegionName=[{}]", collectionRegion.getName());
                }
                return new ReadOnlyRedisCollectionRegionAccessStrategy(collectionRegion, collectionRegion.getSettings());
            case READ_WRITE:
                return new ReadWriteRedisCollectionRegionAccessStrategy(collectionRegion, collectionRegion.getSettings());
            case NONSTRICT_READ_WRITE:
                return new NonStrictReadWriteRedisCollectionRegionAccessStrategy(collectionRegion, collectionRegion.getSettings());
            case TRANSACTIONAL:
                return new TransactionalRedisCollectionRegionAccessStrategy(collectionRegion, collectionRegion.getSettings());
            default:
                throw new IllegalArgumentException("unrecognized access strategy type [" + accessType + "]");
        }
    }

    @Override
    public NaturalIdRegionAccessStrategy createNaturalIdRegionAccessStrategy(RedisNaturalIdRegion naturalIdRegion, AccessType accessType) {
    	logger.debug("create NaturalIdRegionAccessStrategy. regionName=[{}], accessType=[{}]", naturalIdRegion.getName(), accessType.getExternalName());
        switch (accessType) {
            case READ_ONLY:
                if (naturalIdRegion.getCacheDataDescription().isMutable()) {
                    logger.warn("read-only cache configured for mutable entity naturalIdRegion=[{}]", naturalIdRegion.getName());
                }
                return new ReadOnlyRedisNaturalIdRegionAccessStrategy(naturalIdRegion, naturalIdRegion.getSettings());
            case READ_WRITE:
                return new ReadWriteRedisNaturalIdRegionAccessStrategy(naturalIdRegion, naturalIdRegion.getSettings());
            case NONSTRICT_READ_WRITE:
                return new NonStrictReadWriteRedisNaturalIdRegionAccessStrategy(naturalIdRegion, naturalIdRegion.getSettings());
            case TRANSACTIONAL:
                return new TransactionalRedisNaturalIdRegionAccessStrategy(
                	naturalIdRegion, naturalIdRegion.getJedisClient(), naturalIdRegion.getSettings());
            default:
                throw new IllegalArgumentException("unrecognized access strategy type [" + accessType + "]");
        }
    }
}
