package jp.co.nri.openapi.sample.persistence;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

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
public class Service {
	public static final String FIND_BY_CLIENT_ID = "92223f85-4481-4550-93d3-2d25a3d36418";
	public static final String FIND_BY_NAME = "c21c2a1c-46a9-44d2-9079-b0a3006d7049";
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private String name;

	private String url;

	private String scope;

	@ManyToOne
	private Client client;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
