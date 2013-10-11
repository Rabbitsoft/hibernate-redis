package org.hibernate.test.cache;

import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cache.redis.util.HibernateCacheUtil;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.hibernate.test.AbstractHibernateTest;
import org.hibernate.test.domain.Account;
import org.hibernate.test.domain.Item;
import org.hibernate.test.domain.Person;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Hibernate tests.
 * <p/>
 * NOTE: (Google translate) Entity should not be specified for Cache Region! This has to be the Class Name Region.
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
public class HibernateCacheTest extends AbstractHibernateTest {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
    @Rule
    public TestRule benchmarkRun = new BenchmarkRule();


    @Before
    public void before() {
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();
    }

    public SecondLevelCacheStatistics getSecondLevelCacheStatistics(Class<?> clazz) {
        final String regionName = HibernateCacheUtil.getRegionName(sessionFactory, clazz);
        return sessionFactory.getStatistics().getSecondLevelCacheStatistics(regionName);
    }

    @Test
    public void emptySecondLevelCacheEntry() throws Exception {
        sessionFactory.getCache().evictEntityRegion(Item.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();

        final String regionName = HibernateCacheUtil.getRegionName(sessionFactory, Item.class);
        SecondLevelCacheStatistics statistics = getSecondLevelCacheStatistics(Item.class);

        logger.info("SecondLevel Cache Region=[{}], ElementInMemory=[{}], HitCount=[{}]",
        	regionName, statistics.getElementCountInMemory(), statistics.getHitCount());
    }

    @Test
    public void queryCacheInvalidation() throws Exception {

        sessionFactory.getCache().evictEntityRegion(Item.class);

        Session s = sessionFactory.openSession();
        Transaction t = s.beginTransaction();
        Item i = new Item();
        i.setName("Widget");
        i.setDescription("A realy top-quality, full-featured widget.");
        s.persist(i);
        t.commit();
        s.close();


        SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(Item.class);
        logger.info(slcs.toString());

        Session session = sessionFactory.openSession();
        Item loaded = (Item) session.get(Item.class, i.getId());
        assertThat(loaded).isNotNull();
        session.close();

        logger.info(slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(1);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(1);
    }

    @Test
    public void simpleEntityCaching() {

        sessionFactory.getCache().evictEntityRegion(Item.class);
        SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(Item.class);
        Session session = sessionFactory.openSession();

        Item item = new Item();
        item.setName("redis");
        item.setDescription("redis cache item");

        session = sessionFactory.openSession();
        session.save(item);
        session.flush();
        session.close();

        session = sessionFactory.openSession();
        Item loaded = (Item) session.get(Item.class, item.getId());
        assertThat(loaded).isNotNull();
        session.close();

        logger.info(slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(1);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(1);

        session = sessionFactory.openSession();
        loaded.setDescription("Update description...");
        session.save(loaded);
        session.flush();
        session.close();

        session = sessionFactory.openSession();
        loaded = (Item) session.get(Item.class, item.getId());
        assertThat(loaded).isNotNull();
        session.close();

        logger.info(slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(1);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(1);
    }

    @Test
    public void nonrestrictCaching() {
        sessionFactory.getCache().evictEntityRegion(Account.class);
        sessionFactory.getCache().evictEntityRegion(Person.class);

        SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(Account.class);
        SecondLevelCacheStatistics slcs2 = getSecondLevelCacheStatistics(Person.class);

        Session session = sessionFactory.openSession();

        Person person = new Person();
        person.setAge(40);
        person.setFirstname("Bae");
        person.setLastname("Sunghyouk");
        session.save(person);

        Account account = new Account();
        account.setPerson(person);
        session.save(account);

        session.flush();
        session.close();

        session = sessionFactory.openSession();
        Account acc = (Account) session.get(Account.class, account.getId());
        assertThat(acc).isNotNull();
        assertThat(acc.getPerson()).isNotNull();
        session.close();

        logger.info("Account:[{}]", slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(1);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(1);

        logger.info("Person:[{}]", slcs2.toString());
        assertThat(slcs2.getPutCount()).isEqualTo(1);
        assertThat(slcs2.getElementCountInMemory()).isEqualTo(1);

        session = sessionFactory.openSession();
        acc = (Account) session.get(Account.class, account.getId());
        assertThat(acc).isNotNull();
        assertThat(acc.getPerson()).isNotNull();
        session.close();

        logger.info("Account:[{}]", slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(1);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(1);

        logger.info("Person:[{}]", slcs2.toString());
        assertThat(slcs2.getPutCount()).isEqualTo(1);
        assertThat(slcs2.getElementCountInMemory()).isEqualTo(1);
    }

    
	@SuppressWarnings("unchecked") @Test
    public void massiveCaching() {
        sessionFactory.getCache().evictEntityRegion(Item.class);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        int count = 100;

        for (int i = 0; i < count; i++) {
            Item item = new Item();
            item.setName("redis - " + i);
            item.setDescription("redis cache item - " + i);
            session.save(item);
        }
        tx.commit();
        session.close();

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        List<Item> items = (List<Item>) session.createCriteria(Item.class).list();

        assertThat(items.size()).isEqualTo(count);

        items = (List<Item>) session.createCriteria(Item.class).list();

        for (Item item : items) {
            session.update(item);
        }
        tx.commit();
        session.close();

        SecondLevelCacheStatistics slcs = getSecondLevelCacheStatistics(Item.class);
        logger.info(slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(count);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(count);

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        items = (List<Item>) session.createCriteria(Item.class).list();
        for (Item item : items) {
            item.getName();
        }
        tx.commit();
        session.close();

        logger.info(slcs.toString());
        assertThat(slcs.getPutCount()).isEqualTo(count);
        assertThat(slcs.getElementCountInMemory()).isEqualTo(count);

        session = sessionFactory.openSession();
        tx = session.beginTransaction();
        items = (List<Item>) session.createCriteria(Item.class).list();
        for (Item item : items) {
            session.delete(item);
        }
        tx.commit();
        session.close();
    }
}
