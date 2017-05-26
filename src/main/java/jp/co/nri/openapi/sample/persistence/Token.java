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
	 * @return	アクセストークン
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken	アクセストークン
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return	リフレシュトークン
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken	リフレシュトークン
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return	有効期間
	 */
	public Date getTimeLimit() {
		return timeLimit;
	}

	/**
	 * @param timeLimit	有効期間
	 */
	public void setTimeLimit(Date timeLimit) {
		this.timeLimit = timeLimit;
	}

	/**
	 * @return	紐づくユーザ
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user	紐づくユーザ
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return	紐づくクライアント
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @param client	紐づくクライアント
	 */
	public void setClient(Client client) {
		this.client = client;
	}
}

