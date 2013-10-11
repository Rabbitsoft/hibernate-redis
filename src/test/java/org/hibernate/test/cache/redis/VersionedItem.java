package org.hibernate.test.cache.redis;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

/**
 * org.hibernate.test.cache.redis.VersionedItem
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
@Entity
@Cache(region = "redis:test", usage = CacheConcurrencyStrategy.READ_WRITE)
public class VersionedItem {

    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private String name;

    private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
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
