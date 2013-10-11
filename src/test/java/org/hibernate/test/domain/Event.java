package org.hibernate.test.domain;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * org.hibernate.test.domain.Event
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 */
@Entity
@org.hibernate.annotations.Cache(region = "account", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Event implements Serializable {
    private static final long serialVersionUID = 4714760453860670689L;

    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToMany(cascade = { CascadeType.ALL })
    private Set<Person> participants = new HashSet<Person>();

    @ManyToOne
    @JoinColumn(name = "Organizer_Id")
    private Person organizer;
    
    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Set<Person> getParticipants() {
		return participants;
	}

	public void setParticipants(Set<Person> participants) {
		this.participants = participants;
	}

	public Person getOrganizer() {
		return organizer;
	}

	public void setOrganizer(Person organizer) {
		this.organizer = organizer;
	}

	public void addParticipant(Person person) {
        participants.add(person);
        person.getEvents().add(this);
    }

    public void removeParticipant(Person person) {
        participants.remove(person);
        person.getEvents().remove(this);
    }

    public String toString() {
        return getTitle() + ": " + getDate();
    }
}
