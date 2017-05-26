package jp.co.nri.openapi.sample.persistence;

import java.util.Set;
import javax.persistence.*;

/**
 * OAuthクライアントのマッピングを定義する。
 */
@Entity
public class Client {
	/**
	 * Object ID
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	/**
	 * クライアント識別子
	 */
	private String ident;

	/**
	 * クライアントシークレット
	 */
	private String secret;
	
	/**
	 * スコップ
	 */
	private String scope;
	
	/**
	 * このクライアントを認証するためのエンドポイント
	 */
	private String authorizeUrl;
	
	/**
	 * トークンを取得するためのエンドポイント
	 */
	private String tokenUrl;
	
	/**
	 * 認証コードを渡すURL 
	 */
	private String requestUrl;
	
	/**
	 * このクライアントに紐づくサービス
	 */
	@OneToMany(cascade=CascadeType.ALL, mappedBy="client")
	private Set<Service> services;

	/**
	 * @return	クライアント識別子
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id	クライアント識別子
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return クライアント識別子
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * @param ident クライアント識別子
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	/**
	 * @return クライアントシークレット
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @param secret クライアントシークレット
	 */
	public void setSecret(String secret) {
		this.secret = secret;
	}

	/**
	 * @return スコップ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope スコップ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return トークンを取得するためのエンドポイント
	 */
	public String getAuthorizeUrl() {
		return authorizeUrl;
	}

	/**
	 * @param authorizeUrl トークンを取得するためのエンドポイント
	 */
	public void setAuthorizeUrl(String authorizeUrl) {
		this.authorizeUrl = authorizeUrl;
	}

	/**
	 * @return トークンを取得するためのエンドポイント
	 */
	public String getTokenUrl() {
		return tokenUrl;
	}

	/**
	 * @param tokenUrl トークンを取得するためのエンドポイント
	 */
	public void setTokenUrl(String tokenUrl) {
		this.tokenUrl = tokenUrl;
	}

	/**
	 * @return このクライアントに紐づくサービス
	 */
	public Set<Service> getServices() {
		return services;
	}

	/**
	 * @param services このクライアントに紐づくサービス
	 */
	public void setServices(Set<Service> services) {
		this.services = services;
	}

	/**
	 * @return 認証コードを渡すURL 
	 */
	public String getRequestUrl() {
		return requestUrl;
	}

	/**
	 * @param requestUrl 認証コードを渡すURL 
	 */
	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
	
}
