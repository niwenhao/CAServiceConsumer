package jp.co.nri.openapi.sample.persistence;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@NamedQueries({ 
	@NamedQuery(
			name = User.LIST_ALL_USERS, 
			query = "select u from User u"),
	@NamedQuery(
			name = User.FIND_BY_USERNAME_AND_PASSWORD,
			query = "select u from User u where u.name = :username and u.password = :password")
	})
@Entity
@Table(name = "user_master")
public class User {
	public static final String LIST_ALL_USERS = "d2a43b45-5097-4151-8d64-cff09a143b17";
	public static final String FIND_BY_USERNAME_AND_PASSWORD = "7a6b6be3-d9dc-495d-abb4-b22672b787ec";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_master_id")
	private long id;

	private String name;

	private String password;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval=true)
	private Set<Token> tokens;

	/**
	 * @return	ユーザのトークン一覧
	 */
	public Set<Token> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens	ユーザのトークン一覧
	 */
	public void setTokens(Set<Token> tokens) {
		this.tokens = tokens;
	}

	/**
	 * @return	オブジェクトID
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id	オブジェクトID
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return	ユーザ名
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name	ユーザ名
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return	パスワード
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password	パスワード
	 */
	public void setPassword(String password) {
		this.password = password;
	}
}
