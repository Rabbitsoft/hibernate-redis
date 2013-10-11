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

package org.hibernate.cache.redis.jedis;

import org.hibernate.cache.CacheException;
import org.hibernate.cache.redis.serializer.BinaryRedisSerializer;
import org.hibernate.cache.redis.serializer.RedisSerializer;
import org.hibernate.cache.redis.serializer.SerializationTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisClient implements using Jedis library
 * <p/> 
 * * Reference: https://github.com/xetorthio/jedis/wiki/AdvancedUsage
 *
 * @author 배성혁 ( sunghyouk.bae@gmail.com)
 */
public class JedisClient {
    public static final int DEFAULT_EXPIRY_IN_SECONDS = 120;
    public static final String DEFAULT_REGION_NAME = "hibernate";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JedisPool jedisPool;

    private int database;
    private int expiryInSeconds;
    
    private RedisSerializer<Object> keySerializer = new BinaryRedisSerializer<Object>();
    private RedisSerializer<Object> valueSerializer = new BinaryRedisSerializer<Object>();

    public JedisClient() {
        this(new JedisPool("localhost"));
    }
    
    public int getDatabase() {
		return database;
	}
    
	public void setDatabase(int database) {
		this.database = database;
	}

	public int getExpiryInSeconds() {
		return expiryInSeconds;
	}

	public void setExpiryInSeconds(int expiryInSeconds) {
		this.expiryInSeconds = expiryInSeconds;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public JedisClient(JedisPool jedisPool) {
        this(jedisPool, DEFAULT_EXPIRY_IN_SECONDS);
    }

    public JedisClient(JedisPool jedisPool, int expiryInSeconds) {
        logger.debug("JedisClient created. jedisPool=[{}], expiryInSeconds=[{}]", jedisPool, expiryInSeconds);

        this.jedisPool = jedisPool;
        this.expiryInSeconds = expiryInSeconds;
    }
    
    public RedisSerializer<Object> getKeySerializer() {
		return keySerializer;
	}

	public void setKeySerializer(RedisSerializer<Object> keySerializer) {
		this.keySerializer = keySerializer;
	}

	public RedisSerializer<Object> getValueSerializer() {
		return valueSerializer;
	}

	public void setValueSerializer(RedisSerializer<Object> valueSerializer) {
		this.valueSerializer = valueSerializer;
	}

	/**
     * Tests communication with server.
     * 
     * @return "PONG"
     */
    public String ping() {
        return run(new JedisCallback<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.ping();
            }
        });
    }

    /**
     * db size를 구합니다.
     */
    public Long dbSize() {
        return run(new JedisCallback<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                return jedis.dbSize();
            }
        });
    }

    /**
     * Check if given value for key exists.
     * 
     * @param region area
     * @param key key
     */
    public Boolean exists(String region, Object key) {
        logger.trace("Checking if exists region=[{}], key=[{}]", region, key);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawKey = rawKey(key);
        
        return run(new JedisCallback<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) {
                return jedis.hexists(rawRegion, rawKey);
            }
        });
    }

    /**
     * Returns value for given key.
     * 
     * @param region region
     * @param key key
     * @return value or null
     */
    public Object get(String region, Object key) {
        logger.trace("Retrieving object region=[{}], key=[{}]", region, key);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawKey = rawKey(key);
        byte[] rawValue = run(new JedisCallback<byte[]>() {
            @Override
            public byte[] execute(Jedis jedis) {
                return jedis.hget(rawRegion, rawKey);
            }
        });

        Object value = deserializeValue(rawValue);
        logger.trace("Response region=[{}], key=[{}], value=[{}]", region, key, value);
        
        return value;
    }

    /**
     * Returns keys in region.
     *
     * @param region region
     * @return keys
     */
    public Set<Object> keysInRegion(String region) {
        logger.trace("Retrieving keys region=[{}]", region);

        final byte[] rawRegion = rawRegion(region);
        Set<byte[]> rawKeys = run(new JedisCallback<Set<byte[]>>() {
            @Override
            public Set<byte[]> execute(Jedis jedis) {
                return jedis.hkeys(rawRegion);
            }
        });
        return deserializeKeys(rawKeys);
    }

    /**
     * Executes hgetAll command on region. 
     * 
     * @param region region
     * @return all cache entries
     */
    public Map<Object, Object> hgetAll(String region) {
        logger.trace("Executing hgetAll region=[{}]", region);

        final byte[] rawRegion = rawRegion(region);
        Map<byte[], byte[]> rawMap = run(new JedisCallback<Map<byte[], byte[]>>() {
            @Override
            public Map<byte[], byte[]> execute(Jedis jedis) {
                return jedis.hgetAll(rawRegion);
            }
        });

        Map<Object, Object> map = new HashMap<Object, Object>();
        for (Map.Entry<byte[], byte[]> entry : rawMap.entrySet()) {
            Object key = deserializeKey(entry.getKey());
            Object value = deserializeValue(entry.getValue());
            map.put(key, value);
        }
        return map;
    }

    /**
     * Executes mget command. 
     *
     * @param region region
     * @param keys keys to retrieve
     * @return all values
     */
    public List<Object> mget(String region, Collection<?> keys) {
        logger.trace("Executing mget  region=[{}], keys=[{}]", region, keys);

        final byte[] rawRegion = rawRegion(region);
        final byte[][] rawKeys = rawKeys(keys);

        List<byte[]> rawValues = run(new JedisCallback<List<byte[]>>() {
            @Override
            public List<byte[]> execute(Jedis jedis) {
                return jedis.hmget(rawRegion, rawKeys);
            }
        });
        return deserializeValues(rawValues);
    }

    /**
     * Sets value in region.
     *
     * @param region region
     * @param key key
     * @param value value
     */
    public void set(String region, Object key, Object value) {
        set(region, key, value, expiryInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Sets value in region.
     *
     * @param region region
     * @param key key
     * @param value value
     * @param timeoutInSeconds timeout in seconds
     */
    public void set(String region, Object key, Object value, long timeoutInSeconds) {
        set(region, key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    /**
     * Sets value in region.
     *
     * @param region region
     * @param key key
     * @param value value
     * @param timeout timeout
     * @param unit timeout unit
     */
    public void set(final String region, final Object key, final Object value, long timeout, TimeUnit unit) {
        logger.trace("Setting value region=[{}], key=[{}], value=[{}], timeout=[{}], unit=[{}]", region, key, value, timeout, unit);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawKey = rawKey(key);
        final byte[] rawValue = rawValue(value);
        final int seconds = (int) unit.toSeconds(timeout);

        runWithTx(new JedisTransactionalCallback() {
            @Override
            public void execute(Transaction tx) {
                tx.hset(rawRegion, rawKey, rawValue);
                if (seconds > 0) {
                    final byte[] rawZkey = rawZkey(region);
                    final long score = new Date().getTime() + seconds * 1000L;
                    tx.zadd(rawZkey, score, rawKey);
                }
            }
        });
    }

    /**
     * Expires all entries in region. 
     * 
     * @param region region
     */
    public void expire(final String region) {
    	// will expire all entries that are before score
    	final byte[] rawZkey = rawZkey(region);
        final long score = new Date().getTime();
        final byte[] rawRegion = rawRegion(region);

        logger.debug("Running expire on region[{}] where score before time=[{}]", region, score);
        
        run(new JedisCallback<Object>() {
            @Override
            public Object execute(Jedis jedis) {
                Set<byte[]> rawKeys = jedis.zrangeByScore(rawZkey, 0, score);
                if (rawKeys != null) {
                    for (byte[] rawKey : rawKeys) {
                        jedis.hdel(rawRegion, rawKey);
                    }
                    jedis.zremrangeByScore(rawZkey, 0, score);
                }
                return null;
            }
        });
    }

    /**
     * Deletes key in region.
     *
     * @param region region
     * @param key key
     */
    public Long del(String region, Object key) {
        logger.trace("Deleting key region=[{}], key=[{}]", region, key);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawKey = rawKey(key);
        final byte[] rawZkey = rawZkey(region);
        runWithTx(new JedisTransactionalCallback() {
            @Override
            public void execute(Transaction tx) {
                tx.hdel(rawRegion, rawKey);
                tx.zrem(rawZkey, rawKey);
            }
        });
        return 1L;
    }

    /**
     * Deletes multiple keys.
     *
     * @param keys keys
     */
    public void mdel(String region, Collection<?> keys) {
        logger.trace("Deleting keys region=[{}], keys=[{}]", region, keys);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawZkey = rawZkey(region);
        final byte[][] rawKeys = rawKeys(keys);
        
        runWithTx(new JedisTransactionalCallback() {
            @Override
            public void execute(Transaction tx) {
                for (byte[] rawKey : rawKeys) {
                    tx.hdel(rawRegion, rawKey);
                    tx.zrem(rawZkey, rawKey);
                }
            }
        });
    }

    /**
     * Deletes region.
     */
    public void deleteRegion(final String region) throws CacheException {
        logger.debug("Deleting region region=[{}]", region);

        final byte[] rawRegion = rawRegion(region);
        final byte[] rawZkey = rawZkey(region);
        runWithTx(new JedisTransactionalCallback() {
            @Override
            public void execute(Transaction tx) {
                tx.del(rawRegion);
                tx.del(rawZkey);
            }
        });
    }

    /**
     * Flushes database.
     *  
     * NOTE: (Google Translate) Used in conjunction with other data, if the DB is at risk of being deleted.
     */
    public String flushDb() {
        logger.info("Flusing Redis DB");
        return run(new JedisCallback<String>() {
            @Override
            public String execute(Jedis jedis) {
                return jedis.flushDB();
            }
        });
    }

    private byte[] rawKey(Object key) {
        return getKeySerializer().serialize(key);
    }

    private byte[][] rawKeys(Collection<?> keys) {
        byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;
        for (Object key : keys) {
            rawKeys[i++] = getKeySerializer().serialize(key);
        }
        return rawKeys;
    }

    private byte[] rawZkey(String region) {
        return rawRegion("z:" + region);
    }

    /**
     * Serialize region name to byte[] array.
     */
    private byte[] rawRegion(String region) {
        return region.getBytes();
    }

    /**
     * Deserialize byte[] key to object.
     */
    private Object deserializeKey(byte[] rawKey) {
        return getKeySerializer().deserialize(rawKey);
    }

    /**
     * Serialize value to byte[] array. 
     */
    private byte[] rawValue(Object value) {
        return getValueSerializer().serialize(value);
    }

    /**
     * Deserialize byte[] array to object value.
     */
    private Object deserializeValue(byte[] rawValue) {
        return getValueSerializer().deserialize(rawValue);
    }
    
    /**
     * Deserialize byte[] array set to object set.
     */
    private Set<Object> deserializeKeys(Set<byte[]> rawKeys) {
        return SerializationTool.deserialize(rawKeys, getKeySerializer());
    }

    /**
     * Deserialize byte array list to object list.
     */
    private List<Object> deserializeValues(List<byte[]> rawValues) {
        return SerializationTool.deserialize(rawValues, getValueSerializer());
    }    

    /**
     * Execute command in jedis.
     */
    private <T> T run(final JedisCallback<T> callback) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.select(database);
            return callback.execute(jedis);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * The same as {@link #run(JedisCallback)} except runs in transaction.
     */
    private List<Object> runWithTx(final JedisTransactionalCallback callback) {

        Jedis jedis = jedisPool.getResource();
        try {
            Transaction tx = jedis.multi();
            tx.select(database);
            callback.execute(tx);
            return tx.exec();
        } finally {
            jedisPool.returnResource(jedis);
        }
    }
}
