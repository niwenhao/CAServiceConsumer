package jp.co.nri.openapi.sample.persistence;

import java.util.Date;
import javax.persistence.*;

@Entity
@NamedQueries({
	@NamedQuery(
			name = Token.FIND_ALL_BY_USERID_AND_SERVICE_NAME,
			query = "select t from "
					+ "Token t join t.client c join c.services s "
					+ "where t.user.id = :user_id and s.name = :name")
})
public class Token {
	
	public static final String FIND_ALL_BY_USERID_AND_SERVICE_NAME = "90bbdc87-21f4-44c4-82a0-6936b30b77bc";
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	private String accessToken;
	private String refreshToken;
	private Date timeLimit;
	
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Client client;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Date getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(Date timeLimit) {
		this.timeLimit = timeLimit;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}

