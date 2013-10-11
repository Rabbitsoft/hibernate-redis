package org.hibernate.test.domain;

import java.io.Serializable;

/**
 * org.hibernate.test.domain.PhoneNumber
 *
 * @author sunghyouk.bae@gmail.com
 * @since 13. 4. 6. 오전 12:54
 */
public class PhoneNumber implements Serializable {

	private static final long serialVersionUID = 8568232753916897060L;

	private long personId = 0;
	private String numberType = "home";
	private long phone = 0;
	
	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
		this.personId = personId;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public boolean equals(Object obj) {
		if ((obj != null) && (obj instanceof PhoneNumber))
			return hashCode() == obj.hashCode();
		return false;
	}

	@Override
	public String toString() {
		return numberType + ":" + phone;
	}
}
