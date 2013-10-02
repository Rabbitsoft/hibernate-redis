package org.hibernate.cache.redis.jedis;

import redis.clients.jedis.Pipeline;

/**
 * Pipeline 으로 작업
 *
 * @author 배성혁 sunghyouk.bae@gmail.com
 * @since 13. 10. 2. 오후 9:35
 */
public interface JedisPipelineCallback {

    /**
     * Pipeline 하에서 작업을 수행합니다.
     *
     * @param pipeline Pipeline
     */
    public void execute(Pipeline pipeline);
}
