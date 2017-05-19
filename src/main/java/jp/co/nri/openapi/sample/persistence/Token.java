package jp.co.nri.openapi.sample.persistence;

import java.util.Date;
import javax.persistence.*;

@Entity
public class Token {
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
}

