package org.hibernate.test.domain;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * org.hibernate.test.domain.Person
 *
 * @author 배성혁 (sunghyouk.bae@gmail.com)
 **/
@Entity
@org.hibernate.annotations.Cache(region = "common", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Person implements Serializable {
    private static final long serialVersionUID = -8245742950718661800L;

    @Id
    @GeneratedValue
    private Long id;

    private int age;
    private String firstname;
    private String lastname;
    
    
    @ManyToMany(mappedBy = "participants")
    private List<Event> events = new ArrayList<Event>();

    @CollectionTable(name = "EmailAddressSet", joinColumns = @JoinColumn(name = "PersonId"))
    @ElementCollection(targetClass = String.class)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<String> emailAddresses = new HashSet<String>();

    @CollectionTable(name = "PhoneNumberSet", joinColumns = @JoinColumn(name = "ProductItemId"))
    @ElementCollection(targetClass = PhoneNumber.class)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<PhoneNumber> phoneNumbers = new HashSet<PhoneNumber>();

    @CollectionTable(name = "TailsManList", joinColumns = @JoinColumn(name = "PersonId"))
    @ElementCollection(targetClass = String.class)
    @org.hibernate.annotations.Cascade(org.hibernate.annotations.CascadeType.ALL)
    private List<String> tailsmans = new ArrayList<String>();
    
    public Long getId() {
		return id;
	}
    
	public void setId(Long id) {
		this.id = id;
	}
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public Set<String> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Set<String> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	public Set<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
	}
	
	public void setPhoneNumbers(Set<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public List<String> getTailsmans() {
		return tailsmans;
	}
	
	public void setTailsmans(List<String> tailsmans) {
		this.tailsmans = tailsmans;
	}
	
	public String toString() {
        return getFirstname() + " " + getLastname();
    }
}
