package jp.co.nri.openapi.sample.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author nwh
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(
			name = Service.FIND_BY_CLIENT_ID, 
			query = "select s from Service s where s.client.id = :client_id"),
	@NamedQuery(
			name = Service.FIND_BY_NAME,
			query = "select s from Service s where s.name = :name"
			)
})
@Table(name = "service_master")
public class Service {
	public static final String FIND_BY_CLIENT_ID = "92223f85-4481-4550-93d3-2d25a3d36418";
	public static final String FIND_BY_NAME = "c21c2a1c-46a9-44d2-9079-b0a3006d7049";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name="service_master_id")
	private long id;
	
	private String name;

	private String url;

	private String scope;

	@ManyToOne
	@JoinColumn(name = "api_master_id")
	private Client client;
	
	/**
	 * @return クライアント識別子
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id クライアント識別子
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return サービス呼び出すURL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url サービス呼び出すURL
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return 	サービスが必要なスコープ
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @param scope 	サービスが必要なスコープ
	 */
	public void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return 紐づくクライアント
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @param client 紐づくクライアント
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	/**
	 * @return サービス名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name サービス名称
	 */
	public void setName(String name) {
		this.name = name;
	}
}
