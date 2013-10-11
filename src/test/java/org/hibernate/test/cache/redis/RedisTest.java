package org.hibernate.test.cache.redis;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.transaction.internal.jdbc.JdbcTransactionFactory;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * org.hibernate.test.cache.redis.RedisTest
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public abstract class RedisTest extends BaseCoreFunctionalTestCase {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class<?>[] {
                Item.class,
                VersionedItem.class
        };
    }

    @Override
    public String getCacheConcurrencyStrategy() {
        return "read-write";
    }

    @Override
    protected void configure(Configuration cfg) {
        super.configure(cfg);
        cfg.setProperty(Environment.CACHE_REGION_PREFIX, "");
        cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "true");
        cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
        cfg.setProperty(Environment.USE_STRUCTURED_CACHE, "true");
        cfg.setProperty(Environment.TRANSACTION_STRATEGY, JdbcTransactionFactory.class.getName());

        configCache(cfg);
    }

    protected abstract void configCache(final Configuration cfg);

    protected abstract Map<?, ?> getMapFromCacheEntry(final Object entry);

    @Test
    public void queryCacheInvalidation() {
        Session session = openSession();
        Transaction transaction = session.beginTransaction();
        Item item = new Item();
        item.setName("widget");
        item.setDescription("A really top-quality, full-featured widget");
        
        session.persist(item);
        transaction.commit();
        session.clear();

        SecondLevelCacheStatistics slcs = 
        	session.getSessionFactory().getStatistics().getSecondLevelCacheStatistics(Item.class.getName());

        assertThat(slcs.getElementCountInMemory()).isGreaterThan(0);

        session = openSession();
        transaction = session.beginTransaction();
        item = (Item) session.get(Item.class, item.getId());

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("widget");

        item.setDescription("A blog standard item");

        transaction.commit();
        session.close();

        session = openSession();
        transaction = session.beginTransaction();
        session.delete(item);
        transaction.commit();
        session.close();
    }

    @Test
    public void emptySecondLevelCacheEntry() throws Exception {
        sessionFactory().getCache().evictEntityRegion(Item.class.getName());
        Statistics stats = sessionFactory().getStatistics();
        stats.clear();
        SecondLevelCacheStatistics statistics = stats.getSecondLevelCacheStatistics(Item.class.getName());
        Map<?, ?> cacheEntries = statistics.getEntries();

        assertThat(cacheEntries).isNotNull();
        assertThat(cacheEntries.size()).isEqualTo(0);
    }

    @Test
    public void staleWritesLeaveCacheConsistent() {
        Session s = openSession();
        Transaction txn = s.beginTransaction();
        VersionedItem item = new VersionedItem();
        item.setName("steve");
        item.setDescription("steve's item");
        s.save(item);
        txn.commit();
        s.close();

        Long initialVersion = item.getVersion();

        // manually revert the version property
        item.setVersion(Long.valueOf(item.getVersion().longValue() - 1));

        try {
            s = openSession();
            txn = s.beginTransaction();
            s.update(item);
            txn.commit();
            s.close();
            Assert.fail("expected stale write to fail");
        } catch (Throwable expected) {
            if (txn != null) {
                try {
                    txn.rollback();
                } catch (Throwable ignore) {}
            }
        } finally {
            if (s != null && s.isOpen()) {
                try {
                    s.close();
                } catch (Throwable ignore) {}
            }
        }

        // check the version value in the cache...
        SecondLevelCacheStatistics slcs = sessionFactory().getStatistics()
                .getSecondLevelCacheStatistics(VersionedItem.class.getName());

        @SuppressWarnings("rawtypes")
		Map cacheEntries = slcs.getEntries();
        
        Object entry = cacheEntries.get(item.getId());
        logger.debug("entry=[{}]", entry);

        Long cachedVersionValue;

        boolean isLock = entry.getClass().getName().equals("org.hibernate.cache.redis.strategy.AbstractReadWriteRedisAccessStrategy$Lock");
        if (isLock) {
            //
        } else {
            cachedVersionValue = (Long) getMapFromCacheEntry(entry).get("_version");
            assertThat(cachedVersionValue.longValue()).isEqualTo(initialVersion.longValue());
        }

        // cleanup
        s = openSession();
        txn = s.beginTransaction();
        item = (VersionedItem) s.load(VersionedItem.class, item.getId());
        s.delete(item);
        txn.commit();
        s.close();
    }
}
