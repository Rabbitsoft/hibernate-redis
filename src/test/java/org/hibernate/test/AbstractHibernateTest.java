package org.hibernate.test;

import org.hibernate.SessionFactory;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * org.hibernate.test.AbstractHibernateTest
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { HibernateRedisConfiguration.class })
public abstract class AbstractHibernateTest {

    @Autowired
    protected SessionFactory sessionFactory;
}
