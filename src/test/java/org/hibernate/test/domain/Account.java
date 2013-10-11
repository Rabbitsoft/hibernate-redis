package org.hibernate.test.domain;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * org.hibernate.test.domain.Account
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
@Entity
@org.hibernate.annotations.Cache(region = "account", usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Account implements Serializable {
    private static final long serialVersionUID = 6662300674854084326L;

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PersonId")
    private Person person;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}
}
