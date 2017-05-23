package jp.co.nri.openapi.sample.persistence;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@NamedQueries({ 
	@NamedQuery(
			name = User.Q_LIST_ALL_USERS, 
			query = "select u from User u"),
	@NamedQuery(
			name = User.Q_LOGIN_WITH_USERNAME_AND_PASSWORD,
			query = "select u from User u where u.name = :username and u.password = :password")
	})
@Entity
public class User {
	public static final String Q_LIST_ALL_USERS = "d2a43b45-5097-4151-8d64-cff09a143b17";
	public static final String Q_LOGIN_WITH_USERNAME_AND_PASSWORD = "7a6b6be3-d9dc-495d-abb4-b22672b787ec";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;

	private String password;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
	private Set<Token> tokens;

	public Set<Token> getTokens() {
		return tokens;
	}

	public void setTokens(Set<Token> tokens) {
		this.tokens = tokens;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
