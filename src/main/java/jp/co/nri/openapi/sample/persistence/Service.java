package jp.co.nri.openapi.sample.persistence;

import javax.persistence.*;

@Entity
public class Service {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
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
}
