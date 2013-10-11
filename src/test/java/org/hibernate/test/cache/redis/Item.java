package org.hibernate.test.cache.redis;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * org.hibernate.test.cache.redis.Item
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
@Entity
@Cache(region = "redis:test", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Item implements Serializable {
    private static final long serialVersionUID = -281066218676472922L;
    
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
